
#include "std.h"
#include "collision.h"

const float COLLISION_EPSILON=.001f;

/*
//
// OLD VERSION
//
bool Collision::sphereCollide( const Line &line,float radius,const Vector &dest,float dest_radius ){

	radius+=dest_radius;
	Line l( line.o-dest,line.d );

	float a=l.d.dot(l.d);
	if( !a ) return false;
	float b=l.o.dot(l.d)*2;
	float c=l.o.dot(l.o)-radius*radius;
	float d=b*b-4*a*c;
	if( d<0 ) return false;
	float t1=(-b+sqrt(d))/(2*a);
	float t2=(-b-sqrt(d))/(2*a);

	float t=t1<t2 ? t1 : t2;
	if( t<0 || t>=time ) return false;

	time=t;
	normal=(l*t).normalized();
	return true;
}

bool Collision::sphereCollide( const Line &line,float radius,const Vector &dest,const Vector &radii ){

	radius+=radii.x;

	Line l( line.o-dest,line.d );

//	float y_scale=1;
//	if( radii.x!=radii.y ){
//		y_scale=radii.x/radii.y;
//		l.o.y*=y_scale;
//		l.d.y*=y_scale;
//	}

	float a=l.d.dot(l.d);
	if( !a ) return false;
	float b=l.o.dot(l.d)*2;
	float c=l.o.dot(l.o)-radius*radius;
	float d=b*b-4*a*c;
	if( d<0 ) return false;
	float t1=(-b+sqrt(d))/(2*a);
	float t2=(-b-sqrt(d))/(2*a);

	float t=t1<t2 ? t1 : t2;
	if( t<0 || t>=time ) return false;

	time=t;
	normal=(l*t).normalized();

	return true;
}


//v0,v1 = edge verts
//pn = poly normal
//en = edge normal
static bool edgeTest( const Vector &v0,const Vector &v1,const Vector &pn,const Vector &en,const Line &line,float radius,Collision *curr_coll ){

	Matrix tm=~Matrix( en,(v1-v0).normalized(),pn );
	Vector sv=tm*(line.o-v0),dv=tm*(line.o+line.d-v0);
	Line l( sv,dv-sv );
	//do cylinder test...
	float a,b,c,d,t1,t2,t;
	a=(l.d.x*l.d.x+l.d.z*l.d.z);
	if( !a ) return false;					//ray parallel to cylinder
	b=(l.o.x*l.d.x+l.o.z*l.d.z)*2;
	c=(l.o.x*l.o.x+l.o.z*l.o.z)-radius*radius;
	d=b*b-4*a*c;
	if( d<0 ) return false;					//ray misses cylinder
	t1=(-b+sqrt(d))/(2*a);
	t2=(-b-sqrt(d))/(2*a);
	t=t1<t2 ? t1 : t2;
	if( t>curr_coll->time ) return false;	//intersects too far away
	Vector i=l*t,p;
	if( i.y>v0.distance(v1) ) return false;	//intersection above cylinder
	if( i.y>=0 ){
		if( t<0 ) return false;
		p.y=i.y;
	}else{
		//below bottom of cylinder...do sphere test...
		a=l.d.dot(l.d);
		if( !a ) return false;				//ray parallel to sphere
		b=l.o.dot(l.d)*2;
		c=l.o.dot(l.o)-radius*radius;
		d=b*b-4*a*c;
		if( d<0 ) return false;				//ray misses sphere
		t1=(-b+sqrt(d))/(2*a);
		t2=(-b-sqrt(d))/(2*a);
		t=t1<t2 ? t1 : t2;
		if( t<0 || t>curr_coll->time ) return false;	//intersects behind or too far away
		i=l*t;
	}
	curr_coll->time=t;
	curr_coll->normal=~tm*(i-p);
	curr_coll->normal.normalize();
	return true;
}

bool Collision::triangleCollide( const Line &line,float radius,const Vector &v0,const Vector &v1,const Vector &v2 ){

	//triangle plane
	Plane p( v0,v1,v2 );
	if( p.n.dot( line.d )>=0 ) return false;

	//intersection time
	Plane tp=p;tp.d-=radius;
	float t=tp.t_intersect( line );
	if( t>time ) return false;

	//intersection point
	Plane p0( v0+p.n,v1,v0 ),p1( v1+p.n,v2,v1 ),p2( v2+p.n,v0,v2 );

	if( t>=0 ){
		Vector i=line*t;
		if( p0.distance(i)>=0 && p1.distance(i)>=0 && p2.distance(i)>=0 ){
			time=t;
			normal=p.n;
			return true;
		}
	}

	if( radius<=0 ) return false;

	return
	edgeTest( v0,v1,p.n,p0.n,line,radius,this )|
	edgeTest( v1,v2,p.n,p1.n,line,radius,this )|
	edgeTest( v2,v0,p.n,p2.n,line,radius,this );
}

bool Collision::boxCollide( const Line &line,float radius,const Box &box ){

	static int quads[]={
		2,3,1,0,
		3,7,5,1,
		7,6,4,5,
		6,2,0,4,
		6,7,3,2,
		0,1,5,4
	};

	bool hit=false;

	for( int n=0;n<24;n+=4 ){
		Vector
		v0( box.corner( quads[n] ) ),
		v1( box.corner( quads[n+1] ) ),
		v2( box.corner( quads[n+2] ) ),
		v3( box.corner( quads[n+3] ) );

		//quad plane
		Plane p( v0,v1,v2 );
		if( p.n.dot( line.d )>=0 ) continue;

		p.d-=radius;
		float t=p.t_intersect( line );
		if( t>time ) continue;

		//intersection point
		Plane
			p0( v0+p.n,v1,v0 ),
			p1( v1+p.n,v2,v1 ),
			p2( v2+p.n,v3,v2 ),
			p3( v3+p.n,v0,v3 );

		if( t>=0 ){
			Vector i=line*t;
			if( p0.distance(i)>=0 && p1.distance(i)>=0 && p2.distance(i)>=0 && p3.distance(i)>=0 ){
				time=t;
				normal=p.n;
				hit=true;
				continue;
			}
		}

		if( radius<=0 ) continue;

		hit|=
		edgeTest( v0,v1,p.n,p0.n,line,radius,this )|
		edgeTest( v1,v2,p.n,p1.n,line,radius,this )|
		edgeTest( v2,v3,p.n,p2.n,line,radius,this )|
		edgeTest( v3,v0,p.n,p3.n,line,radius,this );
	}
	return hit;
}
*/

bool Collision::update( const Line &line,float t,const Vector &n ){

//	if( t<0 || t>time ) return false;

	if( t>time ) return false;
	Plane p(line*t,n);
	if( p.n.dot( line.d )>=0 ) return false;
	if( p.distance(line.o)<-COLLISION_EPSILON ) return false;

	time=t;
	normal=n;
	return true;
}

//
// NEW VERSION
//
extern gxRuntime *gx_runtime;

bool Collision::sphereCollide( const Line &line,float radius,const Vector &dest,float dest_radius ){

	radius+=dest_radius;
	Line l( line.o-dest,line.d );

	float a=l.d.dot(l.d);
	if( !a ) return false;
	float b=l.o.dot(l.d)*2;
	float c=l.o.dot(l.o)-radius*radius;
	float d=b*b-4*a*c;
	if( d<0 ) return false;

	float t1=(-b+sqrt(d))/(2*a);
	float t2=(-b-sqrt(d))/(2*a);

	float t=t1<t2 ? t1 : t2;

	if( t>time ) return false;

	return update( line,t,(l*t).normalized() );
}

//v0,v1 = edge verts
//pn = poly normal
//en = edge normal
static bool edgeTest( const Vector &v0,const Vector &v1,const Vector &pn,const Vector &en,const Line &line,float radius,Collision *curr_coll ){

	Matrix tm=~Matrix( en,(v1-v0).normalized(),pn );
	Vector sv=tm*(line.o-v0),dv=tm*(line.o+line.d-v0);
	Line l( sv,dv-sv );
	//do cylinder test...
	float a,b,c,d,t1,t2,t;
	a=(l.d.x*l.d.x+l.d.z*l.d.z);
	if( !a ) return false;					//ray parallel to cylinder
	b=(l.o.x*l.d.x+l.o.z*l.d.z)*2;
	c=(l.o.x*l.o.x+l.o.z*l.o.z)-radius*radius;
	d=b*b-4*a*c;
	if( d<0 ) return false;					//ray misses cylinder
	t1=(-b+sqrt(d))/(2*a);
	t2=(-b-sqrt(d))/(2*a);
	t=t1<t2 ? t1 : t2;
	if( t>curr_coll->time ) return false;	//intersects too far away
	Vector i=l*t,p;
	if( i.y>v0.distance(v1) ) return false;	//intersection above cylinder
	if( i.y>=0 ){
		p.y=i.y;
	}else{
		//below bottom of cylinder...do sphere test...
		a=l.d.dot(l.d);
		if( !a ) return false;				//ray parallel to sphere
		b=l.o.dot(l.d)*2;
		c=l.o.dot(l.o)-radius*radius;
		d=b*b-4*a*c;
		if( d<0 ) return false;				//ray misses sphere
		t1=(-b+sqrt(d))/(2*a);
		t2=(-b-sqrt(d))/(2*a);
		t=t1<t2 ? t1 : t2;
		if( t>curr_coll->time ) return false;
		i=l*t;
	}

	return curr_coll->update( line,t,(~tm*(i-p)).normalized() );
}

bool Collision::triangleCollide( const Line &line,float radius,const Vector &v0,const Vector &v1,const Vector &v2 ){

	//triangle plane
	Plane p( v0,v1,v2 );
	if( p.n.dot( line.d )>=0 ) return false;

	//move plane out
	p.d-=radius;
	float t=p.t_intersect( line );
	if( t>time ) return false;

	//edge planes
	Plane p0( v0+p.n,v1,v0 ),p1( v1+p.n,v2,v1 ),p2( v2+p.n,v0,v2 );

	//intersects triangle?
	Vector i=line*t;
	if( p0.distance(i)>=0 && p1.distance(i)>=0 && p2.distance(i)>=0 ){
		return update( line,t,p.n );
	}

	if( radius<=0 ) return false;

	return
	edgeTest( v0,v1,p.n,p0.n,line,radius,this )|
	edgeTest( v1,v2,p.n,p1.n,line,radius,this )|
	edgeTest( v2,v0,p.n,p2.n,line,radius,this );
}

bool Collision::boxCollide( const Line &line,float radius,const Box &box ){

	static int quads[]={
		2,3,1,0,
		3,7,5,1,
		7,6,4,5,
		6,2,0,4,
		6,7,3,2,
		0,1,5,4
	};

	bool hit=false;

	for( int n=0;n<24;n+=4 ){
		Vector
		v0( box.corner( quads[n] ) ),
		v1( box.corner( quads[n+1] ) ),
		v2( box.corner( quads[n+2] ) ),
		v3( box.corner( quads[n+3] ) );

		//quad plane
		Plane p( v0,v1,v2 );
		if( p.n.dot( line.d )>=0 ) continue;
		
		//move plane out
		p.d-=radius;
		float t=p.t_intersect( line );
		if( t>time ) return false;

		//edge planes
		Plane
			p0( v0+p.n,v1,v0 ),
			p1( v1+p.n,v2,v1 ),
			p2( v2+p.n,v3,v2 ),
			p3( v3+p.n,v0,v3 );

		//intersects triangle?
		Vector i=line*t;
		if( p0.distance(i)>=0 && p1.distance(i)>=0 && p2.distance(i)>=0 && p3.distance(i)>=0 ){
			hit|=update( line,t,p.n );
			continue;
		}

		if( radius<=0 ) continue;

		hit|=
		edgeTest( v0,v1,p.n,p0.n,line,radius,this )|
		edgeTest( v1,v2,p.n,p1.n,line,radius,this )|
		edgeTest( v2,v3,p.n,p2.n,line,radius,this )|
		edgeTest( v3,v0,p.n,p3.n,line,radius,this );
	}
	return hit;
}
