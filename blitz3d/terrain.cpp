
#include "std.h"
#include "terrain.h"
#include "terrainrep.h"

Terrain::Terrain( int size_shift ):
rep( d_new TerrainRep( size_shift ) ){
}

Terrain::~Terrain(){
	delete rep;
}

void Terrain::setDetail( int n,bool m ){
	rep->setDetail( n,m );
}

void Terrain::setShading( bool t ){
	rep->setShading( t );
}

void Terrain::setHeight( int x,int z,float h,bool realtime ){
	if( x>=0 && z>=0 && x<=rep->getSize() && z<=rep->getSize() ) rep->setHeight( x,z,h,realtime );
}

int Terrain::getSize()const{
	return rep->getSize();
}

float Terrain::getHeight( int x,int z )const{
	return (x>=0 && z>=0 && x<=rep->getSize() && z<=rep->getSize() ) ? rep->getHeight( x,z ) : 0;
}

bool Terrain::render( const RenderContext &rc ){
	rep->render( this,rc );
	return false;
}

bool Terrain::collide( const Line &line,float radius,Collision *curr_coll,const Transform &tf ){
	return rep->collide( line,radius,curr_coll,tf );
}
