
#include "std.h"
#include "skinmodel.h"

SkinModel::SkinModel(){
	setRenderSpace( RENDER_SPACE_WORLD );
}

void SkinModel::setBones( const vector<Object*> &bones ){
	_bones=bones;
	_surf_bones.resize( _bones.size() );
}

void SkinModel::render( const RenderContext &rc ){

	int k;
	for( k=0;k<_bones.size();++k ){
		Object *obj=_bones[k];
		_surf_bones[k].coord_tform=obj->getRenderTform();
		_surf_bones[k].normal_tform=_surf_bones[k].coord_tform.m.cofactor();
	}

	const MeshModel::SurfaceList &_surfs=getSurfaces();

	for( k=0;k<_surfs.size();++k ){
		Surface *surf=_surfs[k];
		if( gxMesh *mesh=surf->getMesh( _surf_bones ) ){
			enqueue( mesh,0,surf->numVertices(),0,surf->numTriangles(),surf->getBrush() );
		}
	}
}
