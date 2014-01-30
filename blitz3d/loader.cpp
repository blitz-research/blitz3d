
#include "std.h"
#include "meshloader.h"
#include "meshmodel.h"

struct Surf{
	vector<Surface::Triangle> tris;
};

static map<Brush,Surf*> brush_map;
static vector<Surface::Vertex> verts;

void MeshLoader::clear(){
	map<Brush,Surf*>::const_iterator it;
	for( it=brush_map.begin();it!=brush_map.end();++it ){
		delete it->second;
	}
	brush_map.clear();
	verts.clear();
}

int MeshLoader::numVertices(){
	return verts.size();
}

void MeshLoader::addVertex( const Surface::Vertex &v ){
	verts.push_back( v );
}

Surface::Vertex &refVertex( int n ){
	return verts[n];
}

void MeshLoader::addTriangle( const int verts[3],const Brush &b ){
	addTriangle( verts[0],verts[1],verts[2],b );
}

void MeshLoader::addTriangle( int v0,int v1,int v2,const Brush &b ){
	//find surface
	Surf *surf;
	map<Brush,Surf*>::const_iterator it=brush_map.find( b );
	if( it!=brush_map.end() ) surf=it->second;
	else{
		surf=d_new Surf;
		brush_map.insert( make_pair( b,surf ) );
	}

	Surface::Triangle tri;
	tri.verts[0]=v0;tri.verts[1]=v1;tri.verts[2]=v2;
	surf->tris.push_back( tri );
}

void MeshLoader::updateMesh( MeshModel *mesh ){
	map<int,int> vert_map;
	map<Brush,Surf*>::iterator it;
	for( it=brush_map.begin();it!=brush_map.end();++it ){
		vert_map.clear();
		Brush b=it->first;
		Surf *t=it->second;
		Surface *surf=mesh->findSurface( b );
		if( !surf ) surf=mesh->createSurface( b );
		for( int k=0;k<t->tris.size();++k ){
			for( int j=0;j<3;++j ){
				int n=t->tris[k].verts[j],id;
				map<int,int>::const_iterator it=vert_map.find( n );
				if( it!=vert_map.end() ) id=it->second;
				else{
					id=surf->numVertices();
					surf->addVertex( verts[n] );
					vert_map.insert( make_pair( n,id ) );
				}
				t->tris[k].verts[j]=id;
			}
			surf->addTriangle( t->tris[k] );
		}
	}
	clear();
}
