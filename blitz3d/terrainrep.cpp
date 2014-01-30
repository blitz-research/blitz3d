
#include "std.h"
#include "terrainrep.h"
#include <queue>

extern gxRuntime *gx_runtime;
extern gxGraphics *gx_graphics;
extern float stats3d[10];

static Vector eye_vec;
static Plane eye_plane;
static const Vector up_normal( 0,1,0 );
static TerrainRep::Tri *tri_pool;
static const TerrainRep *curr;
static Frustum frustum;
static int out_cnt,proc_cnt,clip_cnt;

static float proj_epsilon=EPSILON;	//.01f;

struct TerrainRep::Cell{
	unsigned char height;
};

struct TerrainRep::Error{
	unsigned char error,bound;
};

int TerrainRep::getSize()const{
	return cell_size;
}

float TerrainRep::getHeight( int x,int z )const{
	return cells[((z&cell_mask)<<cell_shift)|(x&cell_mask)].height/255.0f;
}

struct TerrainRep::Vert{
	short x,z;
	Vector v;
	float src_y;

	Vert(){
	}
	Vert( int x,int z ):x(x),z(z),v( x,curr->getHeight(x,z),z){
		src_y=v.y;
	}
	Vert( int x,int z,float sy ):x(x),z(z),v( x,curr->getHeight(x,z),z ),src_y(sy){
	}
};

static int vert_cnt,max_verts;
static TerrainRep::Vert *verts,*next_vert;

struct TerrainRep::Tri{
	int id;
	short clip,v0,v1,v2;
	Tri *e0,*e1,*e2;
	float proj_err;

	Tri(){
	}
	Tri( int id,int clip,int v0,int v1,int v2,Tri *e0=0,Tri *e1=0,Tri *e2=0 ):
	id(id),clip(clip),
	v0(v0),v1(v1),v2(v2),
	e0(e0),e1(e1),e2(e2),proj_err(0){
	}

	void *operator new( size_t sz ){
		static const int GROW=64;
		if( !tri_pool ){
			tri_pool=new Tri[GROW];
			for( int k=0;k<GROW-1;++k ) tri_pool[k].e0=&tri_pool[k+1];
			tri_pool[GROW-1].e0=0;
		}
		Tri *t=tri_pool;
		tri_pool=t->e0;
		return t;
	}
	void operator delete( void *q ){
		Tri *t=(Tri*)q;
		t->e0=tri_pool;
		tri_pool=t;
	}
	void unlink(){
		if( e0 ){
			if( e0->e0==this ) e0->e0=0;
			else if( e0->e1==this ) e0->e1=0;
			else e0->e2=0;
		}
		if( e1 ){
			if( e1->e0==this ) e1->e0=0;
			else if( e1->e1==this ) e1->e1=0;
			else e1->e2=0;
		}
		if( e2 ){
			if( e2->e0==this ) e2->e0=0;
			else if( e2->e1==this ) e2->e1=0;
			else e2->e2=0;
		}
	}
};

struct TriComp{
	bool operator()( TerrainRep::Tri *a,TerrainRep::Tri *b )const{ return a->proj_err<b->proj_err; }
};

struct TriQue : public priority_queue<TerrainRep::Tri*,vector<TerrainRep::Tri*>,TriComp>{
	vector<TerrainRep::Tri*> &getVector(){ return c; }
	const vector<TerrainRep::Tri*> &getVector()const{ return c; }
};

static TriQue tri_que;
static vector<TerrainRep::Tri*> tris;

static bool clip( const Line &l,const Box &box ){
	static const Vector normals[]={
		Vector( 1,0,0 ),
		Vector( 0,0,1 ),
		Vector( 0,-1,0 ),
		Vector( -1,0,0 ),
		Vector( 0,0,-1 ),
		Vector( 0,1,0 )
	};
	Vector v0=l.o,v1=l.o+l.d;
	for( int k=0;k<6;++k ){
		Vector t=box.corner(k);
		const Vector &n=normals[k];
		float d0=n.dot( v0-t ),d1=n.dot( v1-t );
		if( d0<0 ){
			if( d1<0 ) return false;
			v0+=(v1-v0)*( d0/(d0-d1) );
		}else if( d1<0 ){
			v1+=(v0-v1)*( d1/(d1-d0) );
		}
	}
	return true;
}

TerrainRep::TerrainRep( int n ):
cell_shift(n),cell_size(1<<n),cell_mask((1<<n)-1),
end_tri_id( (1<<n)*(1<<n)*2 ),
shading(false),mesh(0),detail(0),morph(true){
	cells=d_new Cell[cell_size*cell_size];
	errors=d_new Error[end_tri_id];
	setDetail( 2000,false );
	clear();
}

TerrainRep::~TerrainRep(){
	if( mesh ) gx_graphics->freeMesh( mesh );
	delete[] errors;
	delete[] cells;
}

void TerrainRep::clear(){
	memset( cells,0,cell_size*cell_size*sizeof(Cell) );
	memset( errors,0,end_tri_id*sizeof(Error) );
	errs_valid=true;
}

void TerrainRep::setDetail( int n,bool m ){
	morph=m;
	if( n==detail ) return;
	detail=n;

	n+=32;
	if( n>max_verts ){
		delete[] verts;
		max_verts=n;
		verts=new Vert[max_verts];
	}
	if( mesh ) gx_graphics->freeMesh( mesh );
	mesh_verts=mesh_tris=n;
	mesh=gx_graphics->createMesh( mesh_verts,mesh_tris,0 );
}

void TerrainRep::setShading( bool t ){
	shading=t;
}

void TerrainRep::setHeight( int x,int z,float h,bool realtime ){
	cells[((z&cell_mask)<<cell_shift)|(x&cell_mask)].height=h*255.0f;
	if( !errs_valid ) return;
	if( realtime ){
		Vert v0(0,0),v1(cell_size,0),v2(cell_size,cell_size),v3(0,cell_size);
		calcErr( 2,x,z,v1,v2,v0 );
		calcErr( 3,x,z,v3,v0,v2 );
		return;
	}
	errs_valid=false;
}

Vector TerrainRep::getNormal( int x,int z )const{
	Vector 
		vt( x,getHeight(x,z),z ),
		v0( x,getHeight(x,z-1),z-1 ),
		v1( x+1,getHeight(x+1,z),z ),
		v2( x,getHeight(x,z+1),z+1 ),
		v3( x-1,getHeight(x-1,z),z );
	return (
		Plane( vt,v1,v0 ).n+
		Plane( vt,v2,v1 ).n+
		Plane( vt,v3,v2 ).n+
		Plane( vt,v0,v3 ).n ).normalized();
}

void TerrainRep::insert( Tri *t ){
	++proc_cnt;

	//quicker clip check for 'thin' triangles...
	if( t->id>=end_tri_id || !errors[t->id].error ){
		if( t->clip & 63 ){
			++clip_cnt;
			Vector e0( verts[t->v0].v ),e1( verts[t->v1].v ),e2( verts[t->v2].v );
			for( int n=0;n<6;++n ){
				if( !( t->clip & (1<<n) ) ) continue;
				const Plane &p=frustum.getPlane( n );
				if( p.distance(e0)<0 && p.distance(e1)<0 && p.distance(e2)<0 ){
					t->unlink();
					delete t;
					++out_cnt;
					return;
				}
			}
		}
		t->clip|=128;
		tris.push_back( t );
		++out_cnt;
		return;
	}

	//clip?
	if( t->id<end_tri_id/2 && (t->clip & 63) ){
		++clip_cnt;
		Vector e0( verts[t->v0].v ),e1( verts[t->v1].v ),e2( verts[t->v2].v );
		Vector e3(e0),e4(e1),e5(e2);
		e0.y=e1.y=e2.y=0;
		e3.y=e4.y=e5.y=errors[t->id].bound/255.0f;
		for( int n=0;n<6;++n ){
			int mask=1<<n;
			if( !(t->clip & mask) ) continue;
			const Plane &p=frustum.getPlane( n );
			int q=
			(p.distance( e0 )>=0)+(p.distance( e1 )>=0)+(p.distance( e2 )>=0)+
			(p.distance( e3 )>=0)+(p.distance( e4 )>=0)+(p.distance( e5 )>=0);
			if( !q ){
				t->unlink();
				delete t;
				++out_cnt;
				return;
			}
			if( q==6 ) t->clip&=~mask;
		}
	}

	if( t->clip & 128 ){
		t->clip|=128;
		tris.push_back( t );
	}else{
		Vector v=Vector( verts[t->v1].v+verts[t->v2].v )/2;
//		float d=eye_plane.distance( v );
		float d=eye_vec.distance( v );
		if( d<EPSILON ) d=EPSILON;
		t->proj_err=errors[t->id].error/d;
		if( t->proj_err>proj_epsilon ){
			tri_que.push( t );
		}else{
			t->clip|=128;
			tris.push_back( t );
		}
	}
	++out_cnt;
}

void TerrainRep::split( Tri *t ){

	if( t->e2 && t->e2->e2!=t ) split( t->e2 );

	int tv=vert_cnt++;
	if( tv>=max_verts ){
		max_verts+=max_verts/2+32;
		Vert *t=verts;
		verts=d_new Vert[max_verts];
		memcpy( verts,t,sizeof(Vert)*tv );
		next_vert=verts+tv;
	}
	Vert *vert=next_vert++;
	vert->v.x=vert->x=(verts[t->v1].x+verts[t->v2].x)/2;
	vert->v.z=vert->z=(verts[t->v1].z+verts[t->v2].z)/2;
	vert->src_y=(verts[t->v1].v.y+verts[t->v2].v.y)/2;
	vert->v.y=getHeight( vert->x,vert->z );

	Tri *tl=new Tri( t->id*2,t->clip,tv,t->v2,t->v0,0,0,t->e0 );
	if( Tri *p=tl->e2 ){
		if( p->e0==t ) p->e0=tl;
		else if( p->e1==t ) p->e1=tl;
		else p->e2=tl;
	}
	Tri *tr=new Tri( t->id*2+1,t->clip,tv,t->v0,t->v1,0,tl,t->e1 );
	tl->e0=tr;
	if( Tri *p=tr->e2 ){
		if( p->e0==t ) p->e0=tr;
		else if( p->e1==t ) p->e1=tr;
		else p->e2=tr;
	}

	if( Tri *b=t->e2 ){
		Tri *br=new Tri( b->id*2,b->clip,tv,b->v2,b->v0,0,tr,b->e0 );
		tr->e0=br;
		if( Tri *p=br->e2 ){
			if( p->e0==b ) p->e0=br;
			else if( p->e1==b ) p->e1=br;
			else p->e2=br;
		}
		Tri *bl=new Tri( b->id*2+1,b->clip,tv,b->v0,b->v1,tl,br,b->e1 );
		tl->e1=br->e0=bl;
		if( Tri *p=bl->e2 ){
			if( p->e0==b ) p->e0=bl;
			else if( p->e1==b ) p->e1=bl;
			else p->e2=bl;
		}
		b->id=0;
		--out_cnt;
		insert( br );
		insert( bl );
	}
	t->id=0;
	--out_cnt;
	insert( tl );
	insert( tr );
}

TerrainRep::Error TerrainRep::calcErr( int id,const Vert &v0,const Vert &v1,const Vert &v2 )const{

	Error et;

	float y=v0.v.y;
	if( v1.v.y>y ) y=v1.v.y;
	if( v2.v.y>y ) y=v2.v.y;

	et.error = 0;
	et.bound = y>=1 ? 255 : ceil(y*255.0f);

	if( id>=end_tri_id ) return et;

	Vert tv( (v1.x+v2.x)/2,(v1.z+v2.z)/2 );
	float e=fabs(tv.v.y-(v1.v.y+v2.v.y)/2);
	et.error= e>=1 ? 255 : ceil( (e-EPSILON)*255.0f );

	Error el=calcErr( id*2,tv,v2,v0 );
	Error er=calcErr( id*2+1,tv,v0,v1 );

	if( el.error>et.error ) et.error=el.error;
	if( er.error>et.error ) et.error=er.error;

	if( el.bound>et.bound ) et.bound=el.bound;
	if( er.bound>et.bound ) et.bound=er.bound;

	return errors[id]=et;
}

TerrainRep::Error TerrainRep::calcErr( int id,int x,int z,const Vert &v0,const Vert &v1,const Vert &v2 )const{

	Error et;

	float y=v0.v.y;
	if( v1.v.y>y ) y=v1.v.y;
	if( v2.v.y>y ) y=v2.v.y;

	et.error = 0;
	et.bound = y>=1 ? 255 : ceil(y*255.0f);

	if( id>=end_tri_id ) return et;

	//is x/z inside this triangle?
	int dx,dz;
	dx=-(v1.z-v0.z);dz=(v1.x-v0.x);
	if( (x-v0.x)*dx+(z-v0.z)*dz<0 ) return errors[id];
	dx=-(v2.z-v1.z);dz=(v2.x-v1.x);
	if( (x-v1.x)*dx+(z-v1.z)*dz<0 ) return errors[id];
	dx=-(v0.z-v2.z);dz=(v0.x-v2.x);
	if( (x-v2.x)*dx+(z-v2.z)*dz<0 ) return errors[id];

	Vert tv( (v1.x+v2.x)/2,(v1.z+v2.z)/2 );
	float e=fabs(tv.v.y-(v1.v.y+v2.v.y)/2);
	et.error= e>=1 ? 255 : ceil( (e-EPSILON)*255.0f );

	Error el=calcErr( id*2,x,z,tv,v2,v0 );
	Error er=calcErr( id*2+1,x,z,tv,v0,v1 );

	if( el.error>et.error ) et.error=el.error;
	if( er.error>et.error ) et.error=er.error;

	if( el.bound>et.bound ) et.bound=el.bound;
	if( er.bound>et.bound ) et.bound=er.bound;

	return errors[id]=et;
}

void TerrainRep::validateErrs()const{
	if( errs_valid ) return;
	Vert v0(0,0),v1(cell_size,0),v2(cell_size,cell_size),v3(0,cell_size);
	calcErr( 2,v1,v2,v0 );
	calcErr( 3,v3,v0,v2 );
	errs_valid=true;
}

void TerrainRep::render( Model *model,const RenderContext &rc ){

	curr=this;
	validateErrs();

	new( &frustum ) Frustum( rc.getWorldFrustum(),-model->getRenderTform() );
	eye_plane=frustum.getPlane( Frustum::PLANE_NEAR );
	eye_vec=frustum.getVertex( Frustum::VERT_EYE );

	vert_cnt=4;next_vert=verts;
	out_cnt=proc_cnt=clip_cnt=0;
	tri_que.getVector().clear();
	tris.clear();

	new(next_vert++) Vert(0,0);
	new(next_vert++) Vert(cell_size,0);
	new(next_vert++) Vert(cell_size,cell_size);
	new(next_vert++) Vert(0,cell_size);

	Tri *t0=new Tri( 2,0x3f,1,2,0 );
	Tri *t1=new Tri( 3,0x3f,3,0,2 );
	t0->e2=t1;t1->e2=t0;

	insert( t0 );
	insert( t1 );

	while( tri_que.size() && out_cnt<detail ){

		Tri *t=tri_que.top();
		tri_que.pop();
		if( t->id ) split( t );
		delete t;
	}

	int k;
	const vector<Tri*> &q_tris=tri_que.getVector();

	if( !mesh ) out_cnt=0;

	if( !out_cnt ){
		for( k=0;k<tris.size();++k ) delete tris[k];
		for( k=0;k<q_tris.size();++k ) delete q_tris[k];
		return;
	}

	int err_cnt=0;
	for( k=0;k<q_tris.size();++k ){
		Tri *t=q_tris[k];
		if( t->id ){ tris.push_back( t );++err_cnt; }
		else delete t;
	}

	if( morph ){
		if( int morph_cnt=err_cnt/4 ){
			if( morph_cnt>vert_cnt ) morph_cnt=vert_cnt;
			float t=0,morph_step=1.0f/morph_cnt;
			for( int vn=vert_cnt-morph_cnt;vn<vert_cnt;++vn ){
				verts[vn].v.y+=(verts[vn].src_y-verts[vn].v.y)*t;
				t+=morph_step;
			}
		}
	}

	int tri_cnt=tris.size();

	if( vert_cnt>mesh_verts || tri_cnt>mesh_tris ){
		int vc=vert_cnt+32;if( vc>mesh_verts ) mesh_verts=vc;
		int tc=tri_cnt+32;if( tc>mesh_tris ) mesh_tris=tc;
		if( mesh ) gx_graphics->freeMesh( mesh );
		mesh=gx_graphics->createMesh( mesh_verts,mesh_tris,0 );
	}

	mesh->lock( true );
	int tc=0,vc=0;
	if( !shading ){
		for( k=0;k<vert_cnt;++k ){
			const Vert &t=verts[k];
			const Vector &v=t.v;
			float tex_coords[2][2]={ {v.x,cell_size-v.z},{v.x,cell_size-v.z} };
			mesh->setVertex( vc++,&v.x,&up_normal.x,tex_coords );
		}
	}else{
		for( k=0;k<vert_cnt;++k ){
			const Vert &t=verts[k];
			const Vector &v=t.v;
			float tex_coords[2][2]={ {v.x,cell_size-v.z},{v.x,cell_size-v.z} };
			Vector normal=getNormal( v.x,v.z );
			mesh->setVertex( vc++,&v.x,&normal.x,tex_coords );
		}
	}
	for( k=0;k<tri_cnt;++k ){
		Tri *t=tris[k];
		if( t->id ) mesh->setTriangle( tc++,t->v0,t->v2,t->v1 );
		delete t;
	}
	mesh->unlock();

	static int mvc,mtc;
	if( vc>mvc ) mvc=vc;
	if( tc>mtc ) mtc=tc;
	stats3d[1]=mvc;
	stats3d[2]=mtc;

	model->enqueue( mesh,0,vc,0,tc );
}

bool TerrainRep::collide( const Line &line,Collision *curr_coll,const Transform &tform,int id,const Vert &v0,const Vert &v1,const Vert &v2,const Line &l )const{
	Box b( v0.v );
	b.update( v1.v );
	b.update( v2.v );

	if( id>=end_tri_id || !errors[id].error ){
		return ::clip( l,b ) ? 
		curr_coll->triangleCollide( line,0,tform*v0.v,tform*v2.v,tform*v1.v )
		: false;
	}

	b.a.y=0;
	b.b.y=errors[id].bound/255.0f;
	if( !::clip( l,b ) ) return false;

	Vert tv( (v1.x+v2.x)/2,(v1.z+v2.z)/2 );

	return
	collide( line,curr_coll,tform,id*2,tv,v2,v0,l )|
	collide( line,curr_coll,tform,id*2+1,tv,v0,v1,l );
}

bool TerrainRep::collide( const Line &line,float radius,Collision *curr_coll,const Transform &tform,int id,const Vert &v0,const Vert &v1,const Vert &v2,const Box &box )const{
	Box b( v0.v );
	b.update( v1.v );
	b.update( v2.v );

	if( id>=end_tri_id || !errors[id].error ){
		if( v0.v==v1.v || v0.v==v2.v || v1.v==v2.v ){
			gx_runtime->debugLog( "OUCH!" );
		}
		return b.overlaps(box) ?
		curr_coll->triangleCollide( line,radius,tform*v0.v,tform*v2.v,tform*v1.v )
		: false;
	}

	b.a.y=0;
	b.b.y=errors[id].bound/255.0f;
	if( !b.overlaps( box ) ) return false;

	Vert tv( (v1.x+v2.x)/2,(v1.z+v2.z)/2 );
	return
	collide( line,radius,curr_coll,tform,id*2,tv,v2,v0,box )|
	collide( line,radius,curr_coll,tform,id*2+1,tv,v0,v1,box );
}

bool TerrainRep::collide( const Line &line,float radius,Collision *curr_coll,const Transform &tform )const{

	curr=this;
	validateErrs();

	Vert v0(0,0),v1(cell_size,0),v2(cell_size,cell_size),v3(0,cell_size);

	if( !radius ){
		Line l=-tform * line;
		return 
		collide( line,curr_coll,tform,2,v1,v2,v0,l )|
		collide( line,curr_coll,tform,3,v3,v0,v2,l );
	}

	//create local box
	Box b( line );
	b.expand( radius );
	Box box=-tform * b;

	return 
	collide( line,radius,curr_coll,tform,2,v1,v2,v0,box )|
	collide( line,radius,curr_coll,tform,3,v3,v0,v2,box );
}

