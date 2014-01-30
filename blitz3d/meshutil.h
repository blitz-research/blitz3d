
#ifndef MESHUTIL_H
#define MESHUTIL_H

#include "meshmodel.h"

struct MeshUtil{

	static MeshModel *createCube( const Brush &b );
	static MeshModel *createSphere( const Brush &b,int segs );
	static MeshModel *createCylinder( const Brush &b,int segs,bool solid );
	static MeshModel *createCone( const Brush &b,int segs,bool solid );

	static void lightMesh( MeshModel *m,const Vector &pos,const Vector &rgb,float range );

	/*
	static void flipMesh( Mesh *m );
	static void fitMesh( Mesh *m,const Box &b );
	static void paintMesh( Mesh *m,const Brush &b );
	static void transformMesh( Mesh *m,const Transform &t );
	static void lightMesh( Mesh *m,const Vector &pos,const Vector &rgb,float range );
	static void lightMapMesh( Mesh *m,const Mesh &l );

	static Mesh createCube( const Brush &b,int segs );
	static Mesh createSphere( const Brush &b,int segs );
	static Mesh createCylinder( const Brush &b,int segs );
	*/
};

#endif