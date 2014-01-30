
#ifndef MESHLOADER_H
#define MESHLOADER_H

#include "model.h"
#include "surface.h"

class MeshLoader{
public:
	enum{
		HINT_COLLAPSE=1,
		HINT_ANIMONLY=2
	};

	virtual MeshModel *load( const string &f,const Transform &conv,int hint )=0;

	//clear
	static void beginMesh();

	//add a vertex
	static void addVertex( const Surface::Vertex &v );

	//add a triangle
	static void addTriangle( const int verts[3],const Brush &b );

	//also add a triangle
	static void addTriangle( int v0,int v1,int v2,const Brush &b );

	//add a bone
	static void addBone( int vert,float weight,int bone );

	//reference a vertex
	static Surface::Vertex &refVertex( int vert );

	//number of verts
	static int numVertices();

	//finally, update the mesh...
	static void endMesh( MeshModel *mesh );
};

#endif