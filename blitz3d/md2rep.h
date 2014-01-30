
#ifndef MD2REP_H
#define MD2REP_H

#include "model.h"

class MD2Rep{
public:
	struct Vert{
		Vector coords,normal;
	};

	MD2Rep( const string &f );
	virtual ~MD2Rep();

	void render( Vert *verts,int frame );
	void render( Vert *verts,int frame,float time );
	void render( Vert *verts,int frame_a,int frame_b,float time );
	void render( Vert *verts,const Vert *verts_a,const Vert *verts_b,float time );

	void render( Model *model,int frame_a,int frame_b,float time );
	void render( Model *model,const Vert *verts_a,const Vert *verts_b,float time );
	void render( Model *model,const Vert *verts_a,int frame_b,float time );

	const Box &getBox()const{ return box; }
	const int numFrames()const{ return n_frames; }
	const int numVertices()const{ return n_verts; }

private:
	struct Vertex{
		unsigned char x,y,z,n;
	};

	struct VertexUV{
		float u,v;
	};

	struct Frame{
		Vector scale,trans;
		vector<Vertex> verts;
	};

	Box box;
	gxMesh *mesh;
	int n_frames;
	int n_verts,n_tris;
	vector<Frame> frames;
	vector<VertexUV> uvs;
};

#endif