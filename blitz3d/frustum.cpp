
#include "std.h"
#include "frustum.h"

Frustum::Frustum(){
}

Frustum::Frustum( float nr,float fr,float w,float h ){
	verts[VERT_TLNEAR]=Vector( w*-.5f,h*+.5f,nr );
	verts[VERT_TRNEAR]=Vector( w*+.5f,h*+.5f,nr );
	verts[VERT_BRNEAR]=Vector( w*+.5f,h*-.5f,nr );
	verts[VERT_BLNEAR]=Vector( w*-.5f,h*-.5f,nr );
	float t=fr/nr;
	verts[VERT_TLFAR]=verts[VERT_TLNEAR] * t;
	verts[VERT_TRFAR]=verts[VERT_TRNEAR] * t;
	verts[VERT_BRFAR]=verts[VERT_BRNEAR] * t;
	verts[VERT_BLFAR]=verts[VERT_BLNEAR] * t;
	verts[VERT_EYE]=Vector();
	makePlanes();
}

Frustum::Frustum( const Frustum &f,const Transform &t ){
	for( int k=0;k<9;++k ){
		verts[k]=t*f.verts[k];
	}
	makePlanes();
}

bool Frustum::cull( const Vector v[],int cnt )const{
	for( int n=0;n<6;++n ){
		int k;
		for( k=0;k<cnt && planes[n].distance( v[k] )<0;++k ){}
		if( k==cnt ) return false;
	}
	return true;
}

bool Frustum::cull( const Box &b )const{
	Vector v[8];
	for( int k=0;k<8;++k ) v[k]=b.corner(k);
	return cull( v,8 );
}

void Frustum::makePlanes(){
	planes[PLANE_TOP]=Plane( verts[VERT_EYE],verts[VERT_TRFAR],verts[VERT_TLFAR] );
	planes[PLANE_LEFT]=Plane( verts[VERT_EYE],verts[VERT_TLFAR],verts[VERT_BLFAR] );
	planes[PLANE_BOTTOM]=Plane( verts[VERT_EYE],verts[VERT_BLFAR],verts[VERT_BRFAR] );
	planes[PLANE_RIGHT]=Plane( verts[VERT_EYE],verts[VERT_BRFAR],verts[VERT_TRFAR] );
	planes[PLANE_NEAR]=Plane( verts[VERT_TRNEAR],verts[VERT_TLNEAR],verts[VERT_BLNEAR] );
	planes[PLANE_FAR]=Plane( verts[VERT_TLFAR],verts[VERT_TRFAR],verts[VERT_BRFAR] );
	if( planes[PLANE_NEAR].distance( verts[VERT_EYE] )>0 ){
		for( int k=0;k<6;++k ) planes[k]=-planes[k];
	}
}
