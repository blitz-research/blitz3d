
#include "std.h"
#include "q3bsprep.h"

/* Quake3 File format types */

#pragma pack(push,1)

struct q3_plane{
	Vector normal;
	float distance;
};

struct q3_tex{
	char name[64];
	int flags,contents;
};

struct q3_vertex{
	Vector coords;
	float tex_coords[4];
	Vector normal;
	unsigned char color[4];
};

struct q3_node{
	int plane;
	int children[2];
	int mins[3];
	int maxs[3];
};

struct q3_face{
	int texture;
	int effect;
	int type;
	int vertex;
	int n_verts;
	int meshvert;
	int n_meshverts;
	int lm_index;
	int lm_start[2];
	int lm_size[2];
	float lm_origin[3];
	float lm_vecs[2][3];
	float normal[3];
	int patch_size[2];
};

struct q3_leaf{
	int cluster;
	int area;
	int mins[3];
	int maxs[3];
	int leafface;
	int n_leaffaces;
	int leafbrush;
	int n_leafbrushes;
};

struct q3_brush{
	int brushside;
	int n_brushsides;
	int texture;
};

struct q3_brushside{
	int plane;
	int texture;
};

struct q3_direntry{
	union{
		int offset;
		void *lump;
	};
	int length;
};

struct q3_header{
	unsigned magic;
	int version;
	q3_direntry dir[17];
};

#pragma pack(pop)

/* Loading reps */
struct Surf{
	Q3BSPSurf *surf;
	int texture,lm_index;
	vector<int> verts,tris;
};

struct FaceCmp{
	bool operator()( const q3_face *a,const q3_face *b )const{
		if( a->texture<b->texture ) return true;
		if( b->texture<a->texture ) return false;
		if( a->lm_index<b->lm_index ) return true;
		return false;
	}
};

typedef map<q3_face*,Surf*,FaceCmp> FaceMap;

/* render reps */

struct Q3BSPFace;

struct Q3BSPSurf{
	Brush brush;
	gxMesh *mesh;
	vector<Q3BSPFace*> r_faces;
	int texture,lm_index;
};

struct Q3BSPFace{
	union{
		Surf *t_surf;
		Q3BSPSurf *surf;
	};
	int vert,n_verts,tri,n_tris;
};

struct Q3BSPBrush{
	vector<Plane> planes;
};

struct Q3BSPLeaf{
	int cluster;
	Box box;
	vector<Q3BSPFace*> faces;
};

struct Q3BSPNode{
	Box box;
	Plane plane;
	Q3BSPNode *nodes[2];
	Q3BSPLeaf *leafs[2];

	~Q3BSPNode(){ delete nodes[0];delete nodes[1];delete leafs[0];delete leafs[1]; }
};

static q3_header header;
static FaceMap face_map;
static vector<Surf*> t_surfs;
static vector<q3_vertex> p_verts;	//patch vertices
static vector<Vector> p_coll_verts;
static vector<MeshCollider::Triangle> coll_tris;

static float gamma_adj;

static Vector r_eye;
static int r_cluster;
static Frustum r_frustum;
static Vector r_frustedges[12];
static map<int,Q3BSPFace*> q3face_map;

extern gxScene *gx_scene;
extern gxRuntime *gx_runtime;
extern gxGraphics *gx_graphics;

//#define SWAPTRIS
Vector static tf( const Vector &v ){
	return Vector( -v.y,v.z,v.x );
}

#ifdef BETA
static log( const string &t ){
	gx_runtime->debugLog( t.c_str() );
}
#else
static log( const string &t ){}
#endif

static Surf *findSurf( q3_face *f ){
	FaceMap::const_iterator it=face_map.find( f );
	if( it!=face_map.end() ) return it->second;
	Surf *s=d_new Surf;
	s->texture=f->texture;
	s->lm_index=f->lm_index;
	face_map.insert( make_pair( f,s ) );
	t_surfs.push_back( s );
	return s;
}

void Q3BSPRep::createTextures(){
	int n_texs=header.dir[1].length/sizeof(q3_tex);
	q3_tex *q3tex=(q3_tex*)header.dir[1].lump;
	for( int k=0;k<n_texs;++k ){
		string t=string(q3tex->name);
		char fl[32],co[32];
		itoa( q3tex->flags,fl,16 );
		itoa( q3tex->contents,co,16 );
		log( t+", flags=0x"+fl+", contents=0x"+co );
		Texture tex( t+".tga",1 );
		if( !tex.getCanvas(0) ){
			tex=Texture( t+".jpg",1 );
			if( !tex.getCanvas(0) ){
				tex=Texture( t+".png",1 );
				if( !tex.getCanvas(0) ){
					tex=Texture( t+".dds",1 );
					if( !tex.getCanvas(0) ) log( "Failed!" );
				}
			}
		}
		tex.setFlags( 1 );
		textures.push_back( tex );
		++q3tex;
	}
}

void Q3BSPRep::createLightMaps(){
	int n_lmaps=header.dir[14].length/(128*128*3);
	unsigned char *rgb=(unsigned char*)header.dir[14].lump;
	unsigned char adj[256];
	int k;
	for( k=0;k<256;++k ) adj[k]=pow( k/255.0f,gamma_adj )*255.0f;

	for( k=0;k<n_lmaps;++k ){
		Texture tex( 128,128,1+8+16+32,1 );
		tex.setBlend( gxScene::BLEND_ADD );
		gxCanvas *c=tex.getCanvas(0);
		c->lock();
		for( int y=0;y<128;++y ){
			for( int x=0;x<128;++x ){
				unsigned argb=0xff000000|(adj[rgb[0]]<<16)|(adj[rgb[1]]<<8)|adj[rgb[2]];
				c->setPixelFast( x,y,argb );
				rgb+=3;
			}
		}
		c->unlock();
		light_maps.push_back( tex );
	}
}

void Q3BSPRep::createVis(){
	int *vis=(int*)header.dir[16].lump;
	int n_vecs=*vis++;
	vis_sz=*vis++;
	log( "vis: "+itoa(n_vecs)+","+itoa(vis_sz) );
	vis_data=new char[n_vecs*vis_sz];
	memcpy( vis_data,vis,n_vecs*vis_sz );
}

void Q3BSPRep::createCollider(){
	vector<MeshCollider::Vertex> coll_verts;
	int n_verts=header.dir[10].length/sizeof(q3_vertex);
	q3_vertex *t=(q3_vertex*)header.dir[10].lump;
	MeshCollider::Vertex cv;
	int k;
	for( k=0;k<n_verts;++k ){
		cv.coords=tf( t->coords );
		coll_verts.push_back( cv );
		++t;
	}
	for( k=0;k<p_coll_verts.size();++k ){
		cv.coords=p_coll_verts[k];
		coll_verts.push_back( cv );
	}
#ifdef SWAPTRIS
	for( k=0;k<coll_tris.size();++k ){
		std::swap( coll_tris[k].verts[1],coll_tris[k].verts[2] );
	}
#endif
	collider=d_new MeshCollider( coll_verts,coll_tris );
	p_coll_verts.clear();
	coll_verts.clear();
	coll_tris.clear();
}

void Q3BSPRep::createSurfs(){
	int k;
	for( k=0;k<t_surfs.size();++k ){
		Surf *s=t_surfs[k];
		gxMesh *mesh=gx_graphics->createMesh( s->verts.size(),s->tris.size()/3,0 );

		mesh->lock( true );
		int j;
		for( j=0;j<s->verts.size();++j ){
			q3_vertex *t;
			int n=s->verts[j];
			if( n>=0 ){
				t=(q3_vertex*)header.dir[10].lump+n;
			}else{
				t=&p_verts[-n-1];
			}
			float tex_coords[2][2]={ {t->tex_coords[2],t->tex_coords[3]},{t->tex_coords[0],t->tex_coords[1]}};
			unsigned argb=0xff000000|(t->color[0]<<16)|(t->color[1]<<8)|t->color[2];
			mesh->setVertex( j,tf(t->coords),tf(t->normal),argb,tex_coords );
		}
		for( j=0;j<s->tris.size();j+=3 ){
#ifdef SWAPTRIS
			mesh->setTriangle( j/3,s->tris[j],s->tris[j+2],s->tris[j+1] );
#else
			mesh->setTriangle( j/3,s->tris[j],s->tris[j+1],s->tris[j+2] );
#endif
		}
		mesh->unlock();

		Q3BSPSurf *surf=d_new Q3BSPSurf;
		surf->texture=s->texture;
		surf->lm_index=s->lm_index;
		surf->mesh=mesh;
		surfs.push_back( surf );
		s->surf=surf;
	}
	for( k=0;k<faces.size();++k ){
		Q3BSPFace *f=faces[k];
		f->surf=f->t_surf->surf;
		f->tri/=3;f->n_tris/=3;
	}
	for( k=0;k<t_surfs.size();++k ){
		delete t_surfs[k];
	}
	face_map.clear();
	t_surfs.clear();
	p_verts.clear();
}

static void average( const q3_vertex &a,const q3_vertex &b,q3_vertex *c ){
	c->coords=(a.coords+b.coords)*.5f;
	c->normal=(a.normal+b.normal)*.5f;
	for( int k=0;k<4;++k ){
		c->color[k]=(a.color[k]+b.color[k]+1)/2;
		c->tex_coords[k]=(a.tex_coords[k]+b.tex_coords[k])*.5f;
	}
}

static void subdivide( vector<q3_vertex> &verts,int level,int index,int step ){
	if( !level ){
		q3_vertex t1,t2;
		average( verts[index],verts[index+step],&t1 );
		average( verts[index+step],verts[index+step*2],&t2 );
		average( t1,t2,&verts[index+step] );
		return;
	}
	average( verts[index],verts[index+step],&verts[index+step/2] );
	average( verts[index+step],verts[index+step*2],&verts[index+step+step/2] );
	average( verts[index+step/2],verts[index+step+step/2],&verts[index+step] );
	subdivide( verts,level-1,index,step/2 );
	subdivide( verts,level-1,index+step,step/2 );
}

static void patchFace( Q3BSPFace *face,q3_face *q3face,bool draw,bool solid,int level ){

	int k,x,y;
	vector<q3_vertex> verts;

	if( draw ){
		int step=1<<level;
		int size_x=(q3face->patch_size[0]-1)*step+1;
		int size_y=(q3face->patch_size[1]-1)*step+1;
		verts.resize( size_x*size_y );

		//seed initial verts
		q3_vertex *t=(q3_vertex*)header.dir[10].lump+q3face->vertex;
		for( y=0;y<size_y;y+=step ){
			for( x=0;x<size_x;x+=step ){
				verts[y*size_x+x]=*t++;
			}
		}
		//subdivide!
		for( y=0;y<size_y;y+=step ){
			for( x=0;x<size_x-1;x+=step*2 ){
				subdivide( verts,level,y*size_x+x,step );
			}
		}
		for( x=0;x<size_x;++x ){
			for( y=0;y<size_y-1;y+=step*2 ){
				subdivide( verts,level,y*size_x+x,size_x*step );
			}
		}

		Surf *surf=face->t_surf;
		int vert=surf->verts.size()-face->vert;

		//generate patch verts
		for( k=0;k<size_x*size_y;++k ){
			p_verts.push_back( verts[k] );
			surf->verts.push_back( -p_verts.size() );
		}
		face->n_verts+=size_x*size_y;

		//generate tris...
		for( y=0;y<size_y-1;++y ){
			int n=y*size_x+vert;
			for( x=0;x<size_x-1;++n,++x ){
				surf->tris.push_back( n );
				surf->tris.push_back( n+size_x );
				surf->tris.push_back( n+1 );
				surf->tris.push_back( n+size_x+1 );
				surf->tris.push_back( n+1 );
				surf->tris.push_back( n+size_x );
			}
		}
		face->n_tris+=(size_x-1)*(size_y-1)*6;
	}

	if( solid ){
		vector<q3_vertex> verts;
		int step=1;
		int size_x=q3face->patch_size[0];
		int size_y=q3face->patch_size[1];
		verts.resize( size_x*size_y );

		//seed initial verts
		q3_vertex *t=(q3_vertex*)header.dir[10].lump+q3face->vertex;
		for( k=0;k<size_x*size_y;++k ) verts[k]=*t++;
		//subdivide!
		for( y=0;y<size_y;y+=step ){
			for( x=0;x<size_x-1;x+=step*2 ){
				subdivide( verts,0,y*size_x+x,step );
			}
		}
		for( x=0;x<size_x;++x ){
			for( y=0;y<size_y-1;y+=step*2 ){
				subdivide( verts,0,y*size_x+x,size_x*step );
			}
		}

		int vert=header.dir[10].length/sizeof(q3_vertex)+p_coll_verts.size();

		//generate patch verts
		for( k=0;k<size_x*size_y;++k ) p_coll_verts.push_back( tf(verts[k].coords) );

		MeshCollider::Triangle ct;
		ct.surface=0;ct.index=0;
		//generate tris...
		for( y=0;y<size_y-1;++y ){
			int n=y*size_x+vert;
			for( x=0;x<size_x-1;++n,++x ){
				ct.verts[0]=n;
				ct.verts[1]=n+size_x;
				ct.verts[2]=n+1;
				coll_tris.push_back( ct );
				ct.verts[0]=n+size_x+1;
				ct.verts[1]=n+1;
				ct.verts[2]=n+size_x;
				coll_tris.push_back( ct );
			}
		}
	}
}

static void meshFace( Q3BSPFace *face,q3_face *q3face,bool draw,bool solid ){
	static map<int,int> vert_map;
	vert_map.clear();
	int *meshverts=(int*)header.dir[11].lump+q3face->meshvert;
	MeshCollider::Triangle ct;
	ct.surface=0;ct.index=0;
	for( int j=0;j<q3face->n_meshverts;j+=3 ){
		for( int q=0;q<3;++q ){
			int n=meshverts[j+q]+q3face->vertex;
			if( draw ){
				if( !vert_map.count( n ) ){
					vert_map[n]=face->t_surf->verts.size()-face->vert;
					face->t_surf->verts.push_back(n);
					++face->n_verts;
				}
				face->t_surf->tris.push_back( vert_map[n] );
				++face->n_tris;
			}
			ct.verts[q]=n;
		}
		if( solid ) coll_tris.push_back( ct );
	}
}

static Q3BSPBrush *createBrush( int n ){
	Q3BSPBrush *brush=d_new Q3BSPBrush;
	q3_brush *q3brush=(q3_brush*)header.dir[8].lump+n;
	q3_brushside *q3brushside=(q3_brushside*)header.dir[9].lump+q3brush->brushside;
	Plane p;
	for( int j=0;j<q3brush->n_brushsides;++j ){
		q3_plane *q3plane=(q3_plane*)header.dir[2].lump+q3brushside[j].plane;
		p.n=tf( q3plane->normal );
		p.d=-q3plane->distance;
		brush->planes.push_back( p );
	}
	return brush;
}

Q3BSPLeaf *Q3BSPRep::createLeaf( int n ){
	q3_leaf *q3leaf=(q3_leaf*)header.dir[4].lump+n;

	Q3BSPLeaf *leaf=d_new Q3BSPLeaf;

	leaf->cluster=q3leaf->cluster;

	Vector mins( q3leaf->mins[0],q3leaf->mins[1],q3leaf->mins[2] );
	Vector maxs( q3leaf->maxs[0],q3leaf->maxs[1],q3leaf->maxs[2] );
	leaf->box=Box( tf(mins) );
	leaf->box.update( tf(maxs) );
	int *leaffaces=(int*)header.dir[5].lump+q3leaf->leafface;

	for( int k=0;k<q3leaf->n_leaffaces;++k ){

		int face_n=leaffaces[k];

		map<int,Q3BSPFace*>::const_iterator it=q3face_map.find(face_n);
		if( it!=q3face_map.end() ){
			if( it->second ) leaf->faces.push_back( it->second );
			continue;
		}

		q3_face *q3face=(q3_face*)header.dir[13].lump+leaffaces[k];

		if( q3face->type==1 || q3face->type==3 ){
			if( !q3face->n_meshverts || (q3face->n_meshverts%3) ) continue;
		}else if( q3face->type!=2 ) continue;

		bool draw=true,solid=true;

		if( q3face->texture>=0 ){
			q3_tex *q3tex=(q3_tex*)header.dir[1].lump+q3face->texture;
			if( !(q3tex->contents & 1) ) continue;
			if( q3tex->flags & 0x84 ) draw=false;
		}

		if( !draw && !solid ) continue;

		Q3BSPFace *face=0;
		if( draw ){
			Surf *surf=findSurf( q3face );
			face=d_new Q3BSPFace;
			face->t_surf=surf;
			face->vert=surf->verts.size();
			face->tri=surf->tris.size();
			face->n_verts=face->n_tris=0;
			leaf->faces.push_back( face );
			faces.push_back( face );
			q3face_map.insert( make_pair( face_n,face ) );
		}

		if( q3face->type==2 ){
			patchFace( face,q3face,draw,solid,1 );
		}else{
			meshFace( face,q3face,draw,solid );
		}
	}

	return leaf;
}

Q3BSPNode *Q3BSPRep::createNode( int n ){
	q3_node *q3node=(q3_node*)header.dir[3].lump+n;
	q3_plane *q3plane=(q3_plane*)header.dir[2].lump+q3node->plane;

	Q3BSPNode *node=new Q3BSPNode;

	Vector mins( q3node->mins[0],q3node->mins[1],q3node->mins[2] );
	Vector maxs( q3node->maxs[0],q3node->maxs[1],q3node->maxs[2] );

	node->box=Box( tf(mins) );
	node->box.update( tf(maxs) );
	node->plane.n=tf(q3plane->normal);
	node->plane.d=-q3plane->distance;

	for( int k=0;k<2;++k ){
		if( q3node->children[k]>=0 ){
			node->nodes[k]=createNode( q3node->children[k] );
			node->leafs[k]=0;
		}else{
			node->leafs[k]=createLeaf( -q3node->children[k]-1 );
			node->nodes[k]=0;
		}
	}

	return node;
}

Q3BSPRep::Q3BSPRep( const string &f,float gam ):root_node(0),vis_sz(0),vis_data(0),use_lmap(true){

	gamma_adj=1-gam;

	FILE *buf=fopen( f.c_str(),"rb" );if( !buf ) return;

	fread( &header,sizeof(header),1,buf );
	if( header.magic!='PSBI' || header.version!=0x2e ){
		fclose( buf );return;
	}

	log( "Header OK" );

	int k;
	//load all lumps...
	for( k=0;k<17;++k ){
		if( header.dir[k].offset && header.dir[k].length ){
			fseek( buf,header.dir[k].offset,SEEK_SET );
			header.dir[k].lump=d_new char[header.dir[k].length];
			fread( header.dir[k].lump,header.dir[k].length,1,buf );
		}else{
			header.dir[k].lump=0;
		}
	}

	//create root of BSP tree
	root_node=createNode( 0 );

	createCollider();

	createTextures();

	createLightMaps();

	createSurfs();

	createVis();

	//unload all lumps...
	for( k=0;k<17;++k ){
		delete[] header.dir[k].lump;
	}

	fclose( buf );

	use_lmap=false;
	setLighting( true );

	q3face_map.clear();
}

Q3BSPRep::~Q3BSPRep(){
	delete root_node;
	delete[] vis_data;
	int k;
	for( k=0;k<surfs.size();++k ){
		gx_graphics->freeMesh( surfs[k]->mesh );
		delete surfs[k];
	}
	for( k=0;k<faces.size();++k ){
		delete faces[k];
	}
}

void Q3BSPRep::vis( Q3BSPNode *n ){
	int i=n->plane.distance( r_eye )<0;
	if( n->nodes[i] ) vis( n->nodes[i] );
	else r_cluster=n->leafs[i]->cluster;
}

static bool cull( const Box &b,int *clip ){
	for( int n=0;n<6;++n ){
		int mask=1<<n;
		if( !(*clip & mask) ) continue;
		const Plane &p=r_frustum.getPlane( n );
		int q=
			(p.distance(b.corner(0))>=0)+(p.distance(b.corner(1))>=0)+
			(p.distance(b.corner(2))>=0)+(p.distance(b.corner(3))>=0)+
			(p.distance(b.corner(4))>=0)+(p.distance(b.corner(5))>=0)+
			(p.distance(b.corner(6))>=0)+(p.distance(b.corner(7))>=0);
		if( !q ) return false;
		if( q==8 ) *clip&=~mask;
	}
	return true;
}

void Q3BSPRep::render( Q3BSPLeaf *l,int clip ){
	int cluster=l->cluster;
	if( cluster<0 ) return;

	if( r_cluster>=0 ){
		if( !( vis_data[cluster*vis_sz+r_cluster/8] & (1<<(r_cluster&7))) ) return;
	}

	if( clip && !cull( l->box,&clip ) ) return;

	for( int k=0;k<l->faces.size();++k ){
		Q3BSPFace *f=l->faces[k];
		if( Q3BSPSurf *s=f->surf ){
			if( !s->r_faces.size() ) r_surfs.push_back( s );
			s->r_faces.push_back( f );
			f->surf=0;
		}
	}
}

void Q3BSPRep::render( Q3BSPNode *n,int clip ){
	if( clip && !cull( n->box,&clip ) ) return;

	//draw front to back...
	int i=n->plane.distance( r_eye )<0;
	if( n->nodes[i] ) render( n->nodes[i],clip );
	else render( n->leafs[i],clip );
	i^=1;
	if( n->nodes[i] ) render( n->nodes[i],clip );
	else render( n->leafs[i],clip );
}

void Q3BSPRep::render( Model *model,const RenderContext &rc ){
	r_eye=-model->getRenderTform() * rc.getCameraTform().v;
	new( &r_frustum ) Frustum( rc.getWorldFrustum(),-model->getRenderTform() );

	vis( root_node );
	if( r_cluster==-1 ) log( "No cluster!" );
	render( root_node,0x3f );

	if( !r_surfs.size() ) return;

	gx_scene->setAmbient2( &ambient.x );
	gx_scene->setWorldMatrix( (gxScene::Matrix*)&model->getRenderTform() );

	int k;
	for( k=0;k<r_surfs.size();++k ){
		Q3BSPSurf *s=r_surfs[k];
		gx_scene->setRenderState( s->brush.getRenderState() );
		int j;
		for( j=0;j<s->r_faces.size();++j ){
			Q3BSPFace *f=s->r_faces[j];
			gx_scene->render( s->mesh,f->vert,f->n_verts,f->tri,f->n_tris );
			f->surf=s;
		}
		s->r_faces.clear();
	}
	r_surfs.clear();
}

bool Q3BSPRep::collide( const Line &line,float radius,Collision *curr_coll,const Transform &t ){
	return collider->collide( line,radius,curr_coll,t );
}

void Q3BSPRep::setAmbient( const Vector &t ){
	ambient=t;
}

void Q3BSPRep::setLighting( bool lmap ){
	if( lmap==use_lmap ) return;
	int fx=gxScene::FX_CONDLIGHT;
	if( use_lmap=lmap ){
		int k;
		for( k=0;k<surfs.size();++k ){
			Q3BSPSurf *s=surfs[k];
			if( s->lm_index>=0 ){
				//has a lightmap...
				s->brush.setFX( fx );
				s->brush.setTexture( 0,light_maps[s->lm_index],0 );
				if( s->texture>=0 ){
					s->brush.setTexture( 1,textures[s->texture],0 );
				}
			}else{
				s->brush.setFX( fx|gxScene::FX_EMISSIVE );
				if( s->texture>=0 ){
					s->brush.setTexture( 0,textures[s->texture],0 );
				}
			}
		}
	}else{
		int k;
		Texture tex;
		for( k=0;k<surfs.size();++k ){
			Q3BSPSurf *s=surfs[k];
			s->brush.setFX( fx|gxScene::FX_EMISSIVE );
			if( s->texture>=0 ){
				s->brush.setTexture( 0,textures[s->texture],0 );
			}else{
				s->brush.setTexture( 0,tex,0 );
			}
			s->brush.setTexture( 1,tex,0 );
		}
	}
}
