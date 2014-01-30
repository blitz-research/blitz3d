
#include "std.h"
#include "world.h"

static World *w;

struct Face{
	Vector verts[4];

	Face( const Vector &v0,const Vector &v1,const Vector &v2,const Vector &v3 ){
		verts[0]=v0;
		verts[1]=v1;
		verts[2]=v2;
		verts[3]=v3;
	}
};

static int face_verts[][4]={
	2,3,1,0,
	3,7,5,1,
	7,6,4,5,
	6,2,0,4,
	6,7,3,2,
	0,1,5,4
};

struct Coll{
	int obj,surf,tri;
	Coll( const ObjCollision &t ):obj((int)t.with),surf((int)t.collision.surface),tri(t.collision.index){
	}
};

struct CollCmp{
	bool operator()( const Coll &a,const Coll &b )const{
		if( a.obj<b.obj ) return true;
		if( b.obj<a.obj ) return false;
		if( a.surf<b.surf ) return true;
		if( b.surf<a.surf ) return false;
		if( a.tri<b.tri ) return true;
		return false;
	}
};

typedef set<Coll,CollCmp> CollSet;

//returns: 1 for visible, 0 for hidden, -1 for don't know
static int faceVis( const Face &src,const Face &dest ){

	static CollSet all;
	static CollSet colls[16];

	all.clear();

	for( int k=0;k<4;++k ){
		for( int j=0;j<4;++j ){
			int n=k*4+j;
			colls[n].clear();

			Vector sv=src.verts[k];
			Vector dv=dest.verts[j];
			Vector adj=(dv-sv).normalized()*.01f;
			dv-=adj;

			for(;;){
				sv+=adj;
				Line line( sv,dv-sv );

				ObjCollision c;
				if( !w->traceRay( line,EPSILON,&c ) ) break;

				Coll t( c );
				all.insert( t );
				colls[n].insert( t );

				sv=c.coords;
			}
			if( !colls[n].size() ) return 1;
		}
	}
	CollSet::const_iterator it;
	for( it=all.begin();it!=all.end();++it ){
		int k=0;
		for( ;k<16;++k ){
			if( !colls[k].count( *it ) ) break;
		}
		if( k==16 ) return 0;	//definitely hidden!
	}
	return -1;
}

static void subdivide( list<Face> &lst ){
	int n=lst.size();
	while( n-- ){
		const Face &f=lst.front();
		Vector a( (f.verts[0]+f.verts[1])/2 );
		Vector b( (f.verts[1]+f.verts[2])/2 );
		Vector c( (f.verts[2]+f.verts[3])/2 );
		Vector d( (f.verts[3]+f.verts[0])/2 );
		Vector e( (f.verts[0]+f.verts[1]+f.verts[2]+f.verts[3])/4 );
		lst.push_back( Face( f.verts[0],a,e,d ) );
		lst.push_back( Face( a,f.verts[1],b,e ) );
		lst.push_back( Face( e,b,f.verts[2],c ) );
		lst.push_back( Face( d,e,c,f.verts[3] ) );
		lst.erase( lst.begin() );
	}
}

static int faceVis( const Face &src,const Face &dest,int recurs_limit ){

	static list<Face> src_faces,dest_faces;

	src_faces.clear();
	dest_faces.clear();

	src_faces.push_back( src );
	dest_faces.push_back( dest );

	while( recurs_limit-- ){
		list<Face>::iterator src_it,dest_it;
		for( src_it=src_faces.begin();src_it!=src_faces.end();++src_it ){
			int cnt=0;
			for( dest_it=dest_faces.begin();dest_it!=dest_faces.end();++dest_it ){
				int n=faceVis( *src_it,*dest_it );
				if( n==1 ) return 1;
				if( !n ) ++cnt;
			}
			if( cnt==dest_faces.size() ){
				//source can't see ANY dest faces
				src_it=src_faces.erase( src_it );
				--src_it;
			}
		}
		if( !src_faces.size() ) return 0;
		//ok, subdivide!
		subdivide( src_faces );
		subdivide( dest_faces );
	}
	return -1;
}

bool World::boxVis( const Box &src,const Box &dest,int recurs_limit ){

	w=this;

	Box big;

	big.update( src );
	big.update( dest );

	Plane planes[6];

	for( int n=0;n<6;++n ){
		planes[n]=Plane( 
			big.corner( face_verts[n][0] ),
			big.corner( face_verts[n][1] ),
			big.corner( face_verts[n][2] ));
	}

	for( int k=0;k<6;++k ){
		Vector v0=src.corner( face_verts[k][0] );
		Vector v1=src.corner( face_verts[k][1] );
		Vector v2=src.corner( face_verts[k][2] );
		Vector v3=src.corner( face_verts[k][3] );

		int n;
		for( n=0;n<6;++n ){
			const Plane &p=planes[n];
			if( fabs(p.distance(v0))<=EPSILON &&
				fabs(p.distance(v1))<=EPSILON &&
				fabs(p.distance(v2))<=EPSILON &&
				fabs(p.distance(v3))<=EPSILON ) break;
		}
		if( n<6 ) continue;

		Face src_face( v0,v1,v2,v3 );

		for( int j=0;j<6;++j ){
			Vector v0=dest.corner( face_verts[j][0] );
			Vector v1=dest.corner( face_verts[j][1] );
			Vector v2=dest.corner( face_verts[j][2] );
			Vector v3=dest.corner( face_verts[j][3] );

			int n;
			for( n=0;n<6;++n ){
				const Plane &p=planes[n];
				if( fabs(p.distance(v0))<=EPSILON &&
					fabs(p.distance(v1))<=EPSILON &&
					fabs(p.distance(v2))<=EPSILON &&
					fabs(p.distance(v3))<=EPSILON ) break;
			}
			if( n<6 ) continue;

			Face dest_face( v0,v1,v2,v3 );

			int t=faceVis( src_face,dest_face,recurs_limit );

			if( t ) return true;
		}
	}
	return false;
}
