
#ifndef TERRAINREP_H
#define TERRAINREP_H

#include <queue>

#include "model.h"

struct TerrainRep{
public:
	TerrainRep( int cell_shift );
	~TerrainRep();
	
	void clear();
	void setShading( bool shading );
	void setDetail( int n,bool morph );
	void setHeight( int x,int z,float h,bool realtime );
	void setTile( int x,int z,const Brush &brush );
	void render( Model *model,const RenderContext &rc );

	int getSize()const;
	float getHeight( int x,int z )const;
	bool collide( const Line &line,float radius,Collision *curr_coll,const Transform &tform )const;

	struct Tri;
	struct Vert;

private:
	struct Cell;
	struct Error;

	friend struct Tri;
	friend struct Vert;

	Cell *cells;
	Error *errors;
	gxMesh *mesh;

	int cell_size,cell_shift,cell_mask;
	int end_tri_id,detail,mesh_verts,mesh_tris;
	bool morph,shading;
	mutable bool errs_valid;

	void insert( Tri *t );
	void split( Tri *t );

	void validateErrs()const;
	Vector getNormal( int x,int z )const;
	Error calcErr( int id,const Vert &v0,const Vert &v1,const Vert &v2 )const;
	Error calcErr( int id,int x,int z,const Vert &v0,const Vert &v1,const Vert &v2 )const;
	bool collide( const Line &line,Collision *curr_coll,const Transform &tform,int id,const Vert &v0,const Vert &v1,const Vert &v2,const Line &l )const;
	bool collide( const Line &line,float radius,Collision *curr_coll,const Transform &tform,int id,const Vert &v0,const Vert &v1,const Vert &v2,const Box &box )const;
};

#endif