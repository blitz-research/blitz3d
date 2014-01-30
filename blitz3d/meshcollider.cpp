
#include "std.h"
#include "meshcollider.h"

static const int MAX_COLL_TRIS=16;
static vector<Vector> tri_centres;

extern float stats3d[10];

extern gxRuntime *gx_runtime;

static bool triTest( const Vector a[3],const Vector b[3] ){
	bool pb0=false,pb1=false,pb2=false;
	Plane p( a[0],a[1],a[2] ),p0,p1,p2;
	for( int k=0;k<3;++k ){
		Line l( b[k],b[(k+1)%3]-b[k] );
		float t=p.t_intersect( l );
		if( t<0 || t>1 ) continue;
		Vector i=l*t;
		if( !pb0 ){ p0=Plane( a[0]+p.n,a[1],a[0] );pb0=true; }
		if( p0.distance( i )<0 ) continue;
		if( !pb1 ){ p1=Plane( a[1]+p.n,a[2],a[1] );pb1=true; }
		if( p1.distance( i )<0 ) continue;
		if( !pb2 ){ p2=Plane( a[2]+p.n,a[0],a[2] );pb2=true; }
		if( p2.distance( i )<0 ) continue;
		return true;
	}
	return false;
}

static bool trisIntersect( const Vector a[3],const Vector b[3] ){
	return triTest( a,b ) || triTest( b,a );
}

MeshCollider::MeshCollider( const vector<Vertex> &verts,const vector<Triangle> &tris ):
vertices(verts),triangles(tris){
	vector<int> ts;
	tri_centres.clear();
	for( int k=0;k<triangles.size();++k ){
		const MeshCollider::Triangle &t=triangles[k];
		const Vector &v0=vertices[t.verts[0]].coords;
		const Vector &v1=vertices[t.verts[1]].coords;
		const Vector &v2=vertices[t.verts[2]].coords;
		tri_centres.push_back( (v0+v1+v2)/3 );
		ts.push_back( k );
	}
	tree=createNode( ts );
}

MeshCollider::~MeshCollider(){
	delete tree;
}

bool MeshCollider::collide( const Line &line,float radius,Collision *curr_coll,const Transform &t ){

	if( !tree ) return false;

	//create local box
	Box box( line );
	box.expand( radius );
	Box local_box=-t * box;

	return collide( local_box,line,radius,t,curr_coll,tree );
}

bool MeshCollider::collide( const Box &line_box,const Line &line,float radius,const Transform &tform,Collision *curr_coll,MeshCollider::Node *node ){
	if( !line_box.overlaps( node->box ) ){
		return false;
	}

	bool hit=false;
	if( !node->triangles.size() ){
		if( node->left ) hit|=collide( line_box,line,radius,tform,curr_coll,node->left );
		if( node->right ) hit|=collide( line_box,line,radius,tform,curr_coll,node->right );
		return hit;
	}

	stats3d[0]+=node->triangles.size();

	for( int k=0;k<node->triangles.size();++k ){

		const Triangle &tri=triangles[node->triangles[k]];
		const Vector &t_v0=vertices[ tri.verts[0] ].coords;
		const Vector &t_v1=vertices[ tri.verts[1] ].coords;
		const Vector &t_v2=vertices[ tri.verts[2] ].coords;

		//tri box
		Box tri_box( t_v0 );
		tri_box.update( t_v1 );
		tri_box.update( t_v2 );
		if( !tri_box.overlaps( line_box ) ) continue;

		if( !curr_coll->triangleCollide( line,radius,tform*t_v0,tform*t_v1,tform*t_v2 ) ) continue;

		curr_coll->surface=tri.surface;
		curr_coll->index=tri.index;
		
		hit=true;
	}
	return hit;
}

Box MeshCollider::nodeBox( const vector<int> &tris ){
	Box box;
	for( int k=0;k<tris.size();++k ){
		const Triangle &t=triangles[ tris[k] ];
		for( int j=0;j<3;++j ) box.update( vertices[t.verts[j]].coords );
	}
	return box;
}

MeshCollider::Node *MeshCollider::createLeaf( const vector<int> &tris ){

	Node *c=d_new Node;
	c->box=nodeBox( tris );
	c->triangles=tris;
	leaves.push_back( c );
	return c;
}

MeshCollider::Node *MeshCollider::createNode( const vector<int> &tris ){

	if( tris.size()<=MAX_COLL_TRIS ) return createLeaf( tris );

	Node *c=d_new Node;
	c->box=nodeBox( tris );

	//find longest axis
	//
	float max=c->box.width();
	if( c->box.height()>max ) max=c->box.height();
	if( c->box.depth()>max ) max=c->box.depth();
	int axis=0;
	if( max==c->box.height() ) axis=1;
	else if( max==c->box.depth() ) axis=2;

	//sort by axis
	//
	int k;
	multimap<float,int> axis_map;
	for( k=0;k<tris.size();++k ){
		pair<float,int> p( tri_centres[tris[k]][axis],tris[k] );
		axis_map.insert( p );
	}

	//generate left node
	//
	vector<int> new_tris;
	multimap<float,int>::iterator it=axis_map.begin();
	for( k=axis_map.size()/2;k--;++it ){
		new_tris.push_back( it->second );
	}
	c->left=createNode( new_tris );

	//generate right node
	//
	new_tris.clear();
	for( ;it!=axis_map.end();++it ){
		new_tris.push_back( it->second );
	}
	c->right=createNode( new_tris );

	return c;
}

bool MeshCollider::intersects( const MeshCollider &c,const Transform &t )const{

	static Vector a[MAX_COLL_TRIS][3],b[3];
	
	if( !(t * tree->box).overlaps( c.tree->box ) ) return false;
	for( int k=0;k<leaves.size();++k ){
		Node *p=leaves[k];
		Box box=t*p->box;
		bool tformed=false;
		for( int j=0;j<c.leaves.size();++j ){
			Node *q=c.leaves[j];
			if( !box.overlaps( q->box ) ) continue;
			if( !tformed ){
				for( int n=0;n<p->triangles.size();++n ){
					const Triangle &tri=triangles[p->triangles[n]];
					a[n][0]=t * vertices[tri.verts[0]].coords;
					a[n][1]=t * vertices[tri.verts[1]].coords;
					a[n][2]=t * vertices[tri.verts[2]].coords;
				}
				tformed=true;
			}
			for( int n=0;n<q->triangles.size();++n ){
				const Triangle &tri=c.triangles[q->triangles[n]];
				b[0]=c.vertices[tri.verts[0]].coords;
				b[1]=c.vertices[tri.verts[1]].coords;
				b[2]=c.vertices[tri.verts[2]].coords;
				for( int t=0;t<p->triangles.size();++t ){
					if( trisIntersect( a[t],b ) ) return true;
				}
			}
		}
	}
	return false;
}
