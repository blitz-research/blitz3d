
#include "std.h"
#include "md2rep.h"
#include "md2norms.h"

extern gxRuntime *gx_runtime;
extern gxGraphics *gx_graphics;

static Vector *normals=0;
static float tex_coords[2][2]={{0,0},{0,0}};

#pragma pack( push,1 )

struct md2_header{
	int magic; 
	int version; 
	int skinWidth; 
	int skinHeight; 
	int frameSize; 
	int numSkins; 
	int numVertices; 
	int numTexCoords; 
	int numTriangles; 
	int numGlCommands; 
	int numFrames; 
	int offsetSkins; 
	int offsetTexCoords; 
	int offsetTriangles; 
	int offsetFrames; 
	int offsetGlCommands; 
	int offsetEnd;
};

struct md2_uv{
	short u,v;
};

struct md2_vert{
	unsigned char x,y,z,n;
};

struct md2_tri{
	unsigned short verts[3],uvs[3];
};

#pragma pack( pop )

struct t_vert{
	unsigned short i,uv;
	bool operator<( const t_vert &t )const{
		return memcmp( &i,&t.i,4 )<0;
	}
};

struct t_tri{
	unsigned short verts[3];
};

MD2Rep::MD2Rep( const string &f ):
mesh(0),n_verts(0),n_tris(0),n_frames(0){

	filebuf in;
	md2_header header;

	if( !in.open( f.c_str(),ios_base::in|ios_base::binary ) ) return;
	if( in.sgetn( (char*)&header,sizeof(header) )!=sizeof(header) ) return;
	if( header.magic!='2PDI' || header.version!=8 ) return;

	n_frames=header.numFrames;
	n_tris=header.numTriangles;

	//read in tex coords
	vector<md2_uv> md2_uvs;
	md2_uvs.resize( header.numTexCoords );
	in.pubseekpos( header.offsetTexCoords );
	in.sgetn( (char*)md2_uvs.begin(),header.numTexCoords*sizeof(md2_uv) );

	//read in triangles
	vector<md2_tri> md2_tris;
	md2_tris.resize( n_tris );
	in.pubseekpos( header.offsetTriangles );
	in.sgetn( (char*)md2_tris.begin(),n_tris*sizeof(md2_tri) );

	vector<t_tri> t_tris;
	vector<t_vert> t_verts;
	map<t_vert,int> t_map;

	int k;
	for( k=0;k<n_tris;++k ){
		t_tri tr;
		for( int j=0;j<3;++j ){
			t_vert t;
			t.i=md2_tris[k].verts[j];
			t.uv=md2_tris[k].uvs[j];
			map<t_vert,int>::iterator it=t_map.find( t );
			if( it==t_map.end() ){
				//create new vert
				tr.verts[j]=t_map[t]=t_verts.size();
				t_verts.push_back( t );
				//add UVs
				VertexUV uv;
				uv.u=md2_uvs[t.uv].u/(float)(header.skinWidth);
				uv.v=md2_uvs[t.uv].v/(float)(header.skinHeight);
				uvs.push_back( uv );
			}else{
				//reuse vert
				tr.verts[j]=it->second;
			}
		}
		t_tris.push_back( tr );
	}
	n_verts=t_verts.size();

	frames.resize( n_frames );
	in.pubseekpos( header.offsetFrames );

	vector<md2_vert> md2_verts;
	md2_verts.resize( header.numVertices );

	//read in frames
	for( k=0;k<n_frames;++k ){
		char t_buff[16];
		Frame *fr=&frames[k];
		in.sgetn( (char*)&fr->scale,12 );
		in.sgetn( (char*)&fr->trans,12 );
		in.sgetn( t_buff,16 );

		fr->scale=Vector( fr->scale.y,fr->scale.z,fr->scale.x );
		fr->trans=Vector( fr->trans.y,fr->trans.z,fr->trans.x );

		//read vertices
		in.sgetn( (char*)md2_verts.begin(),header.numVertices*sizeof(md2_vert) );

		fr->verts.resize( n_verts );
		for( int j=0;j<n_verts;++j ){
			Vertex *v=&fr->verts[j];
			const t_vert &tv=t_verts[j];
			const md2_vert &mv=md2_verts[tv.i];
			v->x=mv.y;
			v->y=mv.z;
			v->z=mv.x;
			v->n=mv.n;
			box.update( Vector( v->x,v->y,v->z ) * fr->scale + fr->trans );
		}
	}

	//create mesh and setup tris
	mesh=gx_graphics->createMesh( n_verts,n_tris,0 );
	mesh->lock( true );
	for( k=0;k<n_tris;++k ){
		const t_tri &t=t_tris[k];
		mesh->setTriangle( k,t.verts[0],t.verts[2],t.verts[1] );
	}
	mesh->unlock();

	//build normals
	if( !normals ){
		normals=(Vector*)md2norms;
		for( int k=0;k<sizeof(md2norms)/12;++k ){
			normals[k]=Vector(normals[k].y,normals[k].z,normals[k].x);
		}
	}
}

MD2Rep::~MD2Rep(){
	if( mesh ) gx_graphics->freeMesh( mesh );
}

/*
void MD2Rep::render( Vert *v,int frame ){

	const Frame &frame_a=frames[frame];
	const Vertex *v_a=frame_a.verts.begin();
	const Vector scale_a=frame_a.scale,trans_a=frame_a.trans;

	for( int k=0;k<n_verts;++v,++v_a,++k ){
		v->coords=Vector( v_a->x*scale_a.x+trans_a.x,v_a->y*scale_a.y+trans_a.y,v_a->z*scale_a.z+trans_a.z );
		v->normal=normals[ v_a->n ];
	}
}
*/

void MD2Rep::render( Vert *v,int frame,float time ){

	const Frame &frame_b=frames[frame];
	const Vertex *v_b=frame_b.verts.begin();
	const Vector scale_b=frame_b.scale,trans_b=frame_b.trans;

	for( int k=0;k<n_verts;++v,++v_b,++k ){

		const Vector t_b( v_b->x*scale_b.x+trans_b.x,v_b->y*scale_b.y+trans_b.y,v_b->z*scale_b.z+trans_b.z );
		const Vector &n_b=normals[ v_b->n ];

		v->coords+=(t_b-v->coords)*time;
		v->normal+=(n_b-v->normal)*time;
	}
}

void MD2Rep::render( Vert *v,int render_a,int render_b,float render_t ){
	const Frame &frame_a=frames[render_a];
	const Vector scale_a=frame_a.scale,trans_a=frame_a.trans;

	const Frame &frame_b=frames[render_b];
	const Vector scale_b=frame_b.scale,trans_b=frame_b.trans;

	const Vertex *v_a=frame_a.verts.begin();
	const Vertex *v_b=frame_b.verts.begin();

	for( int k=0;k<n_verts;++v,++v_a,++v_b,++k ){

		const Vector t_a( v_a->x*scale_a.x+trans_a.x,v_a->y*scale_a.y+trans_a.y,v_a->z*scale_a.z+trans_a.z );
		const Vector t_b( v_b->x*scale_b.x+trans_b.x,v_b->y*scale_b.y+trans_b.y,v_b->z*scale_b.z+trans_b.z );
		v->coords=(t_b-t_a)*render_t+t_a;

		const Vector &n_a=normals[v_a->n];
		const Vector &n_b=normals[v_b->n];
		v->normal=(n_b-n_a)*render_t+n_a;
	}
}

/*
void MD2Rep::render( Vert *v,const Vert *v_a,const Vert *v_b,float render_t ){
	for( int k=0;k<n_verts;++v,++v_a,++v_b,++k ){
		v->coords=(v_b->coords-v_a->coords)*render_t+v_a->coords;
		v->normal=(v_b->normal-v_a->normal)*render_t+v_a->normal;
	}
}
*/

void MD2Rep::render( Model *model,int render_a,int render_b,float render_t ){
	const Frame &frame_a=frames[render_a];
	const Vector scale_a=frame_a.scale,trans_a=frame_a.trans;

	const Frame &frame_b=frames[render_b];
	const Vector scale_b=frame_b.scale,trans_b=frame_b.trans;

	const VertexUV *uv=uvs.begin();
	const Vertex *v_a=frame_a.verts.begin();
	const Vertex *v_b=frame_b.verts.begin();

	mesh->lock( true );
	for( int k=0;k<n_verts;++uv,++v_a,++v_b,++k ){

		const Vector t_a( v_a->x*scale_a.x+trans_a.x,v_a->y*scale_a.y+trans_a.y,v_a->z*scale_a.z+trans_a.z );
		const Vector t_b( v_b->x*scale_b.x+trans_b.x,v_b->y*scale_b.y+trans_b.y,v_b->z*scale_b.z+trans_b.z );
		const Vector t( (t_b-t_a)*render_t+t_a );

		const Vector &n_a=normals[v_a->n];
		const Vector &n_b=normals[v_b->n];
		const Vector n( (n_b-n_a)*render_t+n_a );

		tex_coords[0][0]=uv->u;
		tex_coords[0][1]=uv->v;

		mesh->setVertex( k,&t.x,&n.x,tex_coords );
	}
	mesh->unlock();

	model->enqueue( mesh,0,n_verts,0,n_tris );
}
/*
void MD2Rep::render( Model *model,const Vert *v_a,const Vert *v_b,float render_t ){

	const VertexUV *uv=uvs.begin();

	mesh->lock();
	for( int k=0;k<n_verts;++uv,++v_a,++v_b,++k ){

		const Vector t( ( v_b->coords-v_a->coords)*render_t+v_a->coords );
		const Vector n( ( v_b->normal-v_a->normal)*render_t+v_a->normal );

		tex_coords[0]=uv->u;
		tex_coords[1]=uv->v;

		mesh->setVertex( k,&t.x,&n.x,tex_coords );
	}
	mesh->unlock();

	model->enqueue( mesh,0,n_verts,0,n_tris );
}
*/

void MD2Rep::render( Model *model,const Vert *v_a,int render_b,float render_t ){

	const Frame &frame_b=frames[render_b];
	const Vector scale_b=frame_b.scale,trans_b=frame_b.trans;

	const VertexUV *uv=uvs.begin();
	const Vertex *v_b=frame_b.verts.begin();

	mesh->lock( true );
	for( int k=0;k<n_verts;++uv,++v_a,++v_b,++k ){

		const Vector t_b( v_b->x*scale_b.x+trans_b.x,v_b->y*scale_b.y+trans_b.y,v_b->z*scale_b.z+trans_b.z );
		const Vector t( (t_b-v_a->coords)*render_t+v_a->coords );

		const Vector &n_b=normals[v_b->n];
		const Vector n( (n_b-v_a->normal)*render_t+v_a->normal );

		tex_coords[0][0]=uv->u;
		tex_coords[0][1]=uv->v;

		mesh->setVertex( k,&t.x,&n.x,tex_coords );
	}
	mesh->unlock();

	model->enqueue( mesh,0,n_verts,0,n_tris );
}

