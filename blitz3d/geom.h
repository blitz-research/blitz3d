
#ifndef GEOM_H
#define GEOM_H

#include <math.h>

class Vector;
class Line;
class Plane;
class Matrix;
class Transform;

const float PI=3.14159265359f;		//180 degrees
const float TWOPI=PI*2.0f;			//360 degrees
const float HALFPI=PI*.5f;			//90  degrees
const float QUARTERPI=PI*.25f;		//45  degrees
const float EPSILON=.000001f;		//small value
const float INFINITY=10000000.0f;	//big value

class Vector{
public:
	float x,y,z;

	Vector():x(0),y(0),z(0){
	}
	Vector( float x,float y,float z ):x(x),y(y),z(z){
	}
	operator float*(){
		return &x;
	}
	operator const float *(){
		return &x;
	}
	float &operator[]( int n ){
		return (&x)[n]; 
	}
	float operator[]( int n )const{
		return (&x)[n]; 
	}
	Vector operator-()const{
		return Vector( -x,-y,-z ); 
	}
	Vector operator*( float scale )const{
		return Vector( x*scale,y*scale,z*scale );
	}
	Vector operator*( const Vector &q )const{
		return Vector( x*q.x,y*q.y,z*q.z );
	}
	Vector operator/( float scale )const{
		return Vector( x/scale,y/scale,z/scale );
	}
	Vector operator/( const Vector &q )const{
		return Vector( x/q.x,y/q.y,z/q.z );
	}
	Vector operator+( const Vector &q )const{
		return Vector( x+q.x,y+q.y,z+q.z );
	}
	Vector operator-( const Vector &q )const{
		return Vector( x-q.x,y-q.y,z-q.z );
	}
	Vector &operator*=( float scale ){
		x*=scale;y*=scale;z*=scale;return *this;
	}
	Vector &operator*=( const Vector &q ){
		x*=q.x;y*=q.y;z*=q.z;return *this;
	}
	Vector &operator/=( float scale ){
		x/=scale;y/=scale;z/=scale;return *this;
	}
	Vector &operator/=( const Vector &q ){
		x/=q.x;y/=q.y;z/=q.z;return *this;
	}
	Vector &operator+=( const Vector &q ){
		x+=q.x;y+=q.y;z+=q.z;return *this;
	}
	Vector &operator-=( const Vector &q ){
		x-=q.x;y-=q.y;z-=q.z;return *this;
	}
	bool operator<( const Vector &q )const{
		if( fabs(x-q.x)>EPSILON ) return x<q.x ? true : false;
		if( fabs(y-q.y)>EPSILON ) return y<q.y ? true : false;
		return fabs(z-q.z)>EPSILON && z<q.z;
	}
	bool operator==( const Vector &q )const{
		return fabs(x-q.x)<=EPSILON && fabs(y-q.y)<=EPSILON && fabs(z-q.z)<=EPSILON;
	}
	bool operator!=( const Vector &q )const{
		return fabs(x-q.x)>EPSILON || fabs(y-q.y)>EPSILON || fabs(z-q.z)>EPSILON;
	}
	float dot( const Vector &q )const{
		return x*q.x+y*q.y+z*q.z;
	}
	Vector cross( const Vector &q )const{
		return Vector( y*q.z-z*q.y,z*q.x-x*q.z,x*q.y-y*q.x );
	}
	float length()const{
		return sqrtf(x*x+y*y+z*z);
	}
	float distance( const Vector &q )const{
		float dx=x-q.x,dy=y-q.y,dz=z-q.z;return sqrtf(dx*dx+dy*dy+dz*dz);
	}
	Vector normalized()const{
		float l=length();return Vector( x/l,y/l,z/l );
	}
	void normalize(){
		float l=length();x/=l;y/=l;z/=l;
	}
	float yaw()const{
		return -atan2f( x,z );
	}
	float pitch()const{
		return -atan2f( y,sqrtf( x*x+z*z ) );
	}
	void clear(){
		x=y=z=0;
	}
};

class Line{
public:
	Vector o,d;
	Line(){
	}
	Line( const Vector &o,const Vector &d ):o(o),d(d){
	}
	Line operator+( const Vector &q )const{
		return Line( o+q,d );
	}
	Line operator-( const Vector &q )const{
		return Line( o-q,d );
	}
	Vector operator*( float q )const{
		return o+d*q;
	}
	Vector nearest( const Vector &q )const{
		return o+d*(d.dot(q-o)/d.dot(d));
	}
};

class Plane{
public:
	Vector n;
	float d;

	Plane():d(0){
	}
	//normal/offset form
	Plane( const Vector &n,float d ):n(n),d(d){
	}
	//point/normal form
	Plane( const Vector &p,const Vector &n ):n(n),d(-n.dot(p)){
	}
	//create plane from tri
	Plane( const Vector &v0,const Vector &v1,const Vector &v2 ){
		n=(v1-v0).cross(v2-v0).normalized();d=-n.dot(v0);
	}
	Plane operator-()const{
		return Plane( -n,-d );
	}
	float t_intersect( const Line &q )const{
		return -distance(q.o)/n.dot(q.d);
	}
	Vector intersect( const Line &q )const{
		return q*t_intersect(q);
	}
	Line intersect( const Plane &q )const{
		Vector lv=n.cross( q.n ).normalized();
		return Line( q.intersect( Line( nearest( n*-d ),n.cross(lv) ) ),lv );
	}
	Vector nearest( const Vector &q )const{
		return q-n*distance(q);
	}
	void negate(){
		n=-n;d=-d;
	}
	float distance( const Vector &q )const{
		return n.dot(q)+d;
	}
};

struct Quat{
	float w;
	Vector v;
	Quat():w(1){
	}
	Quat( float w,const Vector &v ):w(w),v(v){
	}
	Quat operator-()const{
		return Quat( w,-v );
	}
	Quat operator+( const Quat &q )const{
		return Quat( w+q.w,v+q.v );
	}
	Quat operator-( const Quat &q )const{
		return Quat( w-q.w,v-q.v );
	}
	Quat operator*( const Quat &q )const{
		return Quat( w*q.w-v.dot(q.v),q.v.cross(v)+q.v*w+v*q.w );
	}
	Vector operator*( const Vector &q )const{
		return (*this * Quat(0,q) * -*this).v;
	}
	Quat operator*( float q )const{
		return Quat( w*q,v*q );
	}
	Quat operator/( float q )const{
		return Quat( w/q,v/q );
	}
	float dot( const Quat &q )const{
		return v.x*q.v.x+v.y*q.v.y+v.z*q.v.z+w*q.w;
	}
	float length()const{
		return sqrtf( w*w+v.x*v.x+v.y*v.y+v.z*v.z );
	}
	void normalize(){
		*this=*this/length();
	}
	Quat normalized()const{
		return *this/length();
	}
	Quat slerpTo( const Quat &q,float a )const{
		Quat t=q;
		float d=dot(q),b=1-a;
		if( d<0 ){ t.w=-t.w;t.v=-t.v;d=-d; }
		if( d<1-EPSILON ){
			float om=acosf( d );
			float si=sinf( om );
			a=sinf( a*om )/si;
			b=sinf( b*om )/si;
		}
		return *this*b + t*a;
	}
	Vector i()const{
		float xz=v.x*v.z,wy=w*v.y;
		float xy=v.x*v.y,wz=w*v.z;
		float yy=v.y*v.y,zz=v.z*v.z;
		return Vector( 1-2*(yy+zz),2*(xy-wz),2*(xz+wy) );
	}
	Vector j()const{
		float yz=v.y*v.z,wx=w*v.x;
		float xy=v.x*v.y,wz=w*v.z;
		float xx=v.x*v.x,zz=v.z*v.z;
		return Vector( 2*(xy+wz),1-2*(xx+zz),2*(yz-wx) );
	}
	Vector k()const{
		float xz=v.x*v.z,wy=w*v.y;
		float yz=v.y*v.z,wx=w*v.x;
		float xx=v.x*v.x,yy=v.y*v.y;
		return Vector( 2*(xz-wy),2*(yz+wx),1-2*(xx+yy) );
	}
};

class Matrix{
	static Matrix tmps[64];
	static Matrix &alloc_tmp(){ static int tmp=0;return tmps[tmp++&63];	}
	friend class Transform;
public:
	Vector i,j,k;

	Matrix():i(Vector(1,0,0)),j(Vector(0,1,0)),k(Vector(0,0,1)){
	}
	Matrix( const Vector &i,const Vector &j,const Vector &k ):i(i),j(j),k(k){
	}
	Matrix( const Quat &q ){
		float xx=q.v.x*q.v.x,yy=q.v.y*q.v.y,zz=q.v.z*q.v.z;
		float xy=q.v.x*q.v.y,xz=q.v.x*q.v.z,yz=q.v.y*q.v.z;
		float wx=q.w*q.v.x,wy=q.w*q.v.y,wz=q.w*q.v.z;
		i=Vector( 1-2*(yy+zz),2*(xy-wz),2*(xz+wy) ),
		j=Vector( 2*(xy+wz),1-2*(xx+zz),2*(yz-wx) ),
		k=Vector( 2*(xz-wy),2*(yz+wx),1-2*(xx+yy) );
	}
	Matrix( float angle,const Vector &axis ){
		const Vector &u=axis;
		float c=cosf(angle),s=sinf(angle);
		float x2=axis.x*axis.x,y2=axis.y*axis.y,z2=axis.z*axis.z;
		i=Vector( x2+c*(1-x2),u.x*u.y*(1-c)-u.z*s,u.z*u.x*(1-c)+u.y*s );
		j=Vector( u.x*u.y*(1-c)+u.z*s,y2+c*(1-y2),u.y*u.z*(1-c)-u.x*s );
		k=Vector( u.z*u.x*(1-c)-u.y*s,u.y*u.z*(1-c)+u.x*s,z2+c*(1-z2) );
	}
	Vector &operator[]( int n ){
		return (&i)[n]; 
	}
	const Vector &operator[]( int n )const{
		return (&i)[n];
	}
	Matrix &operator~()const{
		Matrix &m=alloc_tmp();
		m.i.x=i.x;m.i.y=j.x;m.i.z=k.x;
		m.j.x=i.y;m.j.y=j.y;m.j.z=k.y;
		m.k.x=i.z;m.k.y=j.z;m.k.z=k.z;
		return m;
	}
	float determinant()const{
		return i.x*(j.y*k.z-j.z*k.y )-i.y*(j.x*k.z-j.z*k.x )+i.z*(j.x*k.y-j.y*k.x );
	}
	Matrix &operator-()const{
		Matrix &m=alloc_tmp();
		float t=1.0f/determinant();
		m.i.x= t*(j.y*k.z-j.z*k.y);m.i.y=-t*(i.y*k.z-i.z*k.y);m.i.z= t*(i.y*j.z-i.z*j.y);
		m.j.x=-t*(j.x*k.z-j.z*k.x);m.j.y= t*(i.x*k.z-i.z*k.x);m.j.z=-t*(i.x*j.z-i.z*j.x);
		m.k.x= t*(j.x*k.y-j.y*k.x);m.k.y=-t*(i.x*k.y-i.y*k.x);m.k.z= t*(i.x*j.y-i.y*j.x);
		return m;
	}
	Matrix &cofactor()const{
		Matrix &m=alloc_tmp();
		m.i.x= (j.y*k.z-j.z*k.y);m.i.y=-(j.x*k.z-j.z*k.x);m.i.z= (j.x*k.y-j.y*k.x);
		m.j.x=-(i.y*k.z-i.z*k.y);m.j.y= (i.x*k.z-i.z*k.x);m.j.z=-(i.x*k.y-i.y*k.x);
		m.k.x= (i.y*j.z-i.z*j.y);m.k.y=-(i.x*j.z-i.z*j.x);m.k.z= (i.x*j.y-i.y*j.x);
		return m;
	}
	bool operator==( const Matrix &q )const{
		return i==q.i && j==q.j && k==q.k;
	}
	bool operator!=( const Matrix &q )const{
		return i!=q.i || j!=q.j || k!=q.k;
	}
	Vector operator*( const Vector &q )const{
		return Vector( i.x*q.x+j.x*q.y+k.x*q.z,i.y*q.x+j.y*q.y+k.y*q.z,i.z*q.x+j.z*q.y+k.z*q.z );
	}
	Matrix &operator*( const Matrix &q )const{
		Matrix &m=alloc_tmp();
		m.i.x=i.x*q.i.x+j.x*q.i.y+k.x*q.i.z;m.i.y=i.y*q.i.x+j.y*q.i.y+k.y*q.i.z;m.i.z=i.z*q.i.x+j.z*q.i.y+k.z*q.i.z;
		m.j.x=i.x*q.j.x+j.x*q.j.y+k.x*q.j.z;m.j.y=i.y*q.j.x+j.y*q.j.y+k.y*q.j.z;m.j.z=i.z*q.j.x+j.z*q.j.y+k.z*q.j.z;
		m.k.x=i.x*q.k.x+j.x*q.k.y+k.x*q.k.z;m.k.y=i.y*q.k.x+j.y*q.k.y+k.y*q.k.z;m.k.z=i.z*q.k.x+j.z*q.k.y+k.z*q.k.z;
		return m;
	}
	void orthogonalize(){
		k.normalize();
		i=j.cross( k ).normalized();
		j=k.cross( i );
	}
	Matrix &orthogonalized()const{
		Matrix &m=alloc_tmp();
		m=*this;m.orthogonalize();
		return m;
	}
};

class Box{
public:
	Vector a,b;
	Box():a( Vector(INFINITY,INFINITY,INFINITY) ),b( Vector(-INFINITY,-INFINITY,-INFINITY) ){
	}
	Box( const Vector &q ):a(q),b(q){
	}
	Box( const Vector &a,const Vector &b ):a(a),b(b){
	}
	Box( const Line &l ):a(l.o),b(l.o){
		update( l.o+l.d );
	}
	void clear(){
		a.x=a.y=a.z=INFINITY;
		b.x=b.y=b.z=-INFINITY;
	}
	bool empty()const{
		return b.x<a.x || b.y<a.y || b.z<a.z;
	}
	Vector centre()const{
		return Vector( (a.x+b.x)*.5f,(a.y+b.y)*.5f,(a.z+b.z)*.5f );
	}
	Vector corner( int n )const{
		return Vector( ((n&1)?b:a).x,((n&2)?b:a).y,((n&4)?b:a).z );
	}
	void update( const Vector &q ){
		if( q.x<a.x ) a.x=q.x;if( q.y<a.y ) a.y=q.y;if( q.z<a.z ) a.z=q.z;
		if( q.x>b.x ) b.x=q.x;if( q.y>b.y ) b.y=q.y;if( q.z>b.z ) b.z=q.z;
	}
	void update( const Box &q ){
		if( q.a.x<a.x ) a.x=q.a.x;if( q.a.y<a.y ) a.y=q.a.y;if( q.a.z<a.z ) a.z=q.a.z;
		if( q.b.x>b.x ) b.x=q.b.x;if( q.b.y>b.y ) b.y=q.b.y;if( q.b.z>b.z ) b.z=q.b.z;
	}
	bool overlaps( const Box &q )const{
		return
		(b.x<q.b.x?b.x:q.b.x)>=(a.x>q.a.x?a.x:q.a.x) &&
		(b.y<q.b.y?b.y:q.b.y)>=(a.y>q.a.y?a.y:q.a.y) &&
		(b.z<q.b.z?b.z:q.b.z)>=(a.z>q.a.z?a.z:q.a.z);
	}
	void expand( float n ){
		a.x-=n;a.y-=n;a.z-=n;b.x+=n;b.y+=n;b.z+=n;
	}
	float width()const{
		return b.x-a.x;
	}
	float height()const{
		return b.y-a.y;
	}
	float depth()const{
		return b.z-a.z;
	}
	bool contains( const Vector &q ){
		return q.x>=a.x && q.x<=b.x && q.y>=a.y && q.y<=b.y && q.z>=a.z && q.z<=b.z;
	}
};

class Transform{
	static Transform tmps[64];
	static Transform &alloc_tmp(){ static int tmp=0;return tmps[tmp++&63]; }
public:
	Matrix m;
	Vector v;

	Transform(){
	}
	Transform( const Matrix &m ):m(m){
	}
	Transform( const Vector &v ):v(v){
	}
	Transform( const Matrix &m,const Vector &v ):m(m),v(v){
	}
	Transform &operator-()const{
		Transform &t=alloc_tmp();
		t.m=-m;t.v=t.m*-v;
		return t;
	}
	Transform &operator~()const{
		Transform &t=alloc_tmp();
		t.m=~m;t.v=t.m*-v;
		return t;
	}
	Vector operator*( const Vector &q )const{
		return m*q+v;
	}
	Line operator*( const Line &q )const{
		Vector t=(*this)*q.o;
		return Line( t,(*this)*(q.o+q.d)-t );
	}
	Box operator*( const Box &q )const{
		Box t( (*this*q.corner(0) ) );
		for( int k=1;k<8;++k ) t.update( *this*q.corner(k) );
		return t;
	}
	Transform &operator*( const Transform &q )const{
		Transform &t=alloc_tmp();
		t.m=m*q.m;t.v=m*q.v+v;
		return t;
	}
	bool operator==( const Transform &q )const{
		return m==q.m && v==q.v;
	}
	bool operator!=( const Transform &q )const{
		return !operator==( q );
	}
};

inline float transformRadius( float r,const Matrix &t ){
	static const float sq_3=sqrtf(1.0f/3.0f);
	return (t * Vector( sq_3,sq_3,sq_3 )).length()*r;
}

inline Matrix pitchMatrix( float q ){
	return Matrix( Vector(1,0,0),Vector(0,cosf(q),sinf(q)),Vector(0,-sinf(q),cosf(q)) );
}

inline Matrix yawMatrix( float q ){
	return Matrix( Vector(cosf(q),0,sinf(q)),Vector(0,1,0),Vector(-sinf(q),0,cosf(q)) );
}

inline Matrix rollMatrix( float q ){
	return Matrix( Vector(cosf(q),sinf(q),0),Vector(-sinf(q),cosf(q),0),Vector(0,0,1) );
}

inline float matrixPitch( const Matrix &m ){
	return m.k.pitch();
//	return asinf( -m.k.y );
}

inline float matrixYaw( const Matrix &m ){
	return m.k.yaw();
	//return atan2f( -m.k.x,m.k.z );
}

inline float matrixRoll( const Matrix &m ){
	return atan2f( m.i.y,m.j.y );
	//Matrix t=pitchMatrix( -matrixPitch(m) )*yawMatrix( -matrixYaw(m) )*m;
	//return atan2f( t.i.y,t.i.x );
}

inline Matrix scaleMatrix( float x,float y,float z ){
	return Matrix( Vector( x,0,0 ),Vector( 0,y,0 ),Vector( 0,0,z ) );
}

inline Matrix scaleMatrix( const Vector &scale ){
	return Matrix( Vector( scale.x,0,0 ),Vector( 0,scale.y,0 ),Vector( 0,0,scale.z ) );
}

inline Quat pitchQuat( float p ){
	return Quat( cosf(p/-2),Vector( sinf(p/-2),0,0 ) );
}

inline Quat yawQuat( float y ){
	return Quat( cosf(y/2),Vector( 0,sinf(y/2),0 ) );
}

inline Quat rollQuat( float r ){
	return Quat( cosf(r/-2),Vector( 0,0,sinf(r/-2) ) );
}

//inline Quat rotationQuat( float p,float y,float r ){
//	return yawQuat(y)*pitchQuat(p)*rollQuat(r);
//}

Quat rotationQuat( float p,float y,float r );

inline Matrix rotationMatrix( float p,float y,float r ){
	return yawMatrix(y)*pitchMatrix(p)*rollMatrix(r);
}

inline Matrix rotationMatrix( const Vector &rot ){
	return yawMatrix(rot.y)*pitchMatrix(rot.x)*rollMatrix(rot.z);
}

inline float quatPitch( const Quat &q ){
	return q.k().pitch();
}

inline float quatYaw( const Quat &q ){
	return q.k().yaw();
}

inline float quatRoll( const Quat &q ){
//	Vector i=q.i(),j=q.j();
//	return atan2f( i.y,j.y );
	return matrixRoll( q );
}

inline Quat matrixQuat( const Matrix &p ){
	Matrix m=p;
	m.orthogonalize();
	float t=m.i.x+m.j.y+m.k.z,w,x,y,z;
	if( t>EPSILON ){
		t=sqrtf( t+1 )*2;
		x=(m.k.y-m.j.z)/t;
		y=(m.i.z-m.k.x)/t;
		z=(m.j.x-m.i.y)/t;
		w=t/4;
	}else if( m.i.x>m.j.y && m.i.x>m.k.z ){
		t=sqrtf( m.i.x-m.j.y-m.k.z+1 )*2;
		x=t/4;
		y=(m.j.x+m.i.y)/t;
		z=(m.i.z+m.k.x)/t;
		w=(m.k.y-m.j.z)/t;
	}else if( m.j.y>m.k.z ){
		t=sqrtf( m.j.y-m.k.z-m.i.x+1 )*2;
		x=(m.j.x+m.i.y)/t;
		y=t/4;
		z=(m.k.y+m.j.z)/t;
		w=(m.i.z-m.k.x)/t;
	}else{
		t=sqrtf( m.k.z-m.j.y-m.i.x+1 )*2;
		x=(m.i.z+m.k.x)/t;
		y=(m.k.y+m.j.z)/t;
		z=t/4;
		w=(m.j.x-m.i.y)/t;
	}
	return Quat( w,Vector( x,y,z ) );
}

#endif