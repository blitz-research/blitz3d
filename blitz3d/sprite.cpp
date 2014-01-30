
#include "std.h"
#include "sprite.h"

extern float stats3d[];

static float null[]={0,0,0};

static float tex_coords0[2][2]={ {0,0},{0,0} };
static float tex_coords1[2][2]={ {1,0},{1,0} };
static float tex_coords2[2][2]={ {1,1},{1,1} };
static float tex_coords3[2][2]={ {0,1},{0,1} };

extern gxRuntime *gx_runtime;
extern gxGraphics *gx_graphics;

static gxMesh *mesh;
static int mesh_size;
static vector<int> mesh_indices;

static int allocIndex(){
	if( !mesh_indices.size() ){
		if( mesh_size ) gx_graphics->freeMesh( mesh );
		for( int k=0;k<256;++k ){
			mesh_indices.push_back( mesh_size++ );
		}
		mesh=gx_graphics->createMesh( mesh_size*4,mesh_size*2,0 );
	}
	int n=mesh_indices.back();
	mesh_indices.pop_back();
	return n;
}

static void freeIndex( int n ){
	mesh_indices.push_back( n );
	if( mesh_indices.size()!=mesh_size ) return;
	gx_graphics->freeMesh( mesh );
	mesh_indices.clear();
	mesh_size=0;
}

Sprite::Sprite():
view_mode(VIEW_MODE_FREE),
xhandle(0),yhandle(0),
rot(0),xscale(1),yscale(1),captured(false){
	setRenderSpace( RENDER_SPACE_WORLD );
	mesh_index=allocIndex();
}

Sprite::Sprite( const Sprite &t ):
Model(t),
view_mode(t.view_mode),
xhandle(t.xhandle),yhandle(t.yhandle),
rot(t.rot),xscale(t.xscale),yscale(t.yscale),captured(false){
	mesh_index=allocIndex();
}

Sprite::~Sprite(){
	freeIndex( mesh_index );
}

void Sprite::setRotation( float angle ){
	rot=angle;
}

void Sprite::setScale( float x,float y ){
	xscale=x;yscale=y;
}

void Sprite::setHandle( float x,float y ){
	xhandle=x;yhandle=y;
}

void Sprite::setViewmode( int mode ){
	view_mode=mode;
}

void Sprite::capture(){
	Model::capture();
	r_rot=rot;
	r_xscale=xscale;
	r_yscale=yscale;
	captured=true;
}

bool Sprite::beginRender( float tween ){
	Model::beginRender( tween );
	if( tween==1 || !captured ){
		r_rot=rot;
		r_xscale=xscale;
		r_yscale=yscale;
	}else{
		r_rot=(rot-r_rot)*tween+r_rot;
		r_xscale=(xscale-r_xscale)*tween+r_xscale;
		r_yscale=(yscale-r_yscale)*tween+r_yscale;
	}
	return true;
}

bool Sprite::render( const RenderContext &rc ){

	Transform t=getRenderTform();

	if( view_mode==VIEW_MODE_FREE ){
		t.m=rc.getCameraTform().m;
	}else if( view_mode==VIEW_MODE_UPRIGHT ){
		t.m.k=rc.getCameraTform().m.k;t.m.orthogonalize();
	}else if( view_mode==VIEW_MODE_UPRIGHT2 ){
		t.m=yawMatrix( matrixYaw( rc.getCameraTform().m ) ) * t.m;
	}

	t.m=t.m * rollMatrix( r_rot ) * scaleMatrix( r_xscale,r_yscale,1 );

	static Vector verts[4];
	verts[0]=t * Vector( -1-xhandle, 1-yhandle,0 );
	verts[1]=t * Vector(  1-xhandle, 1-yhandle,0 );
	verts[2]=t * Vector(  1-xhandle,-1-yhandle,0 );
	verts[3]=t * Vector( -1-xhandle,-1-yhandle,0 );

	if( !rc.getWorldFrustum().cull( verts,4 ) ) return false;

	mesh->lock( false );
	int fv=mesh_index*4,ft=mesh_index*2;
	mesh->setVertex( fv+0,&verts[0].x,null,tex_coords0 );
	mesh->setVertex( fv+1,&verts[1].x,null,tex_coords1 );
	mesh->setVertex( fv+2,&verts[2].x,null,tex_coords2 );
	mesh->setVertex( fv+3,&verts[3].x,null,tex_coords3 );
	if( rc.isReflected() ){
		mesh->setTriangle( ft+0,0,2,1 );
		mesh->setTriangle( ft+1,0,3,2 );
	}else{
		mesh->setTriangle( ft+0,0,1,2 );
		mesh->setTriangle( ft+1,0,2,3 );
	}
	mesh->unlock();

	enqueue( mesh,fv,4,ft,2 );
	return false;
}
