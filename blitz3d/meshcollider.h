
#ifndef MESHCOLLIDER_H
#define MESHCOLLIDER_H

#include "collision.h"

class MeshCollider{
public:
	struct Vertex{
		Vector coords;
	};
	struct Triangle{
		void *surface;
		int verts[3],index;
	};
	MeshCollider( const vector<Vertex> &verts,const vector<Triangle> &tris );
	~MeshCollider();

	//sphere collision
	bool collide( const Line &line,float radius,Collision *curr_coll,const Transform &tform );

	bool intersects( const MeshCollider &c,const Transform &t )const;

private:
	vector<Vertex> vertices;
	vector<Triangle> triangles;

	struct Node{
		Box box;
		Node *left,*right;
		vector<int> triangles;
		Node():left(0),right(0){}
		~Node(){ delete left;delete right; }
	};

	Node *tree;
	vector<Node*> leaves;

	Box nodeBox( const vector<int> &tris );
	Node *createLeaf( const vector<int> &tris );
	Node *createNode( const vector<int> &tris );
	bool collide( const Box &box,const Line &line,float radius,const Transform &tform,Collision *curr_coll,Node *node );
};

#endif