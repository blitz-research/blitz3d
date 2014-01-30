
#include "std.h"
#include "gxmesh.h"
#include "gxgraphics.h"

#include "gxruntime.h"

extern gxRuntime *gx_runtime;

gxMesh::gxMesh( gxGraphics *g,IDirect3DVertexBuffer7 *vs,WORD *is,int max_vs,int max_ts ):
graphics(g),locked_verts(0),vertex_buff(vs),tri_indices(is),max_verts(max_vs),max_tris(max_ts),mesh_dirty(false){
}

gxMesh::~gxMesh(){
	unlock();

	vertex_buff->Release();

	delete[] tri_indices;
}

bool gxMesh::lock( bool all ){
	if( locked_verts ) return true;

	//V1.104
	//int flags=all ? DDLOCK_DISCARDCONTENTS : 0;

	//V1.105
	//int flags=all ? DDLOCK_DISCARDCONTENTS : DDLOCK_NOOVERWRITE;

	//V1.104/1.105
	//if( vertex_buff->Lock( DDLOCK_WRITEONLY|DDLOCK_WAIT|flags,(void**)&locked_verts,0 )>=0 ){

	//V1.1.06...
	int flags=DDLOCK_WAIT|DDLOCK_WRITEONLY;

	//XP or less?
	if( graphics->runtime->osinfo.dwMajorVersion<6 ){
		flags|=(all ? DDLOCK_DISCARDCONTENTS : DDLOCK_NOOVERWRITE);
	}

	if( vertex_buff->Lock( flags,(void**)&locked_verts,0 )>=0 ){
		mesh_dirty=false;
		return true;
	}

	static dxVertex *err_verts=new dxVertex[32768];

	locked_verts=err_verts;
	return true;
}

void gxMesh::unlock(){
	if( locked_verts ){
		vertex_buff->Unlock();
		locked_verts=0;
	}
}

void gxMesh::backup(){
	unlock();
}

void gxMesh::restore(){
	mesh_dirty=true;
}

/*
void gxMesh::backup(){

	unlock();

	if( backup_verts ){
		delete[] backup_verts;
		backup_verts=0;
	}

	dxVertex *verts;
	if( vertex_buff->Lock( DDLOCK_READONLY|DDLOCK_WAIT,(void**)&verts,0 )>=0 ){
		backup_verts=d_new dxVertex[ max_verts ];
		memcpy( backup_verts,verts,sizeof(dxVertex)*max_verts );
		vertex_buff->Unlock();
	}
}

void gxMesh::restore(){

	if( !backup_verts ) return;

	dxVertex *verts;
	if( vertex_buff->Lock( DDLOCK_WRITEONLY|DDLOCK_WAIT,(void**)&verts,0 )>=0 ){
		memcpy( verts,backup_verts,sizeof(dxVertex)*max_verts );
		vertex_buff->Unlock();
	}

	delete[] backup_verts;
	backup_verts=0;
}
*/

void gxMesh::render( int first_vert,int vert_cnt,int first_tri,int tri_cnt ){
	unlock();
	graphics->dir3dDev->DrawIndexedPrimitiveVB(
		D3DPT_TRIANGLELIST,
		vertex_buff,first_vert,vert_cnt,
		tri_indices+first_tri*3,tri_cnt*3,0 );
}
