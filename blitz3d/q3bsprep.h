
#ifndef Q3BSPREP_H
#define Q3BSPREP_H

#include "model.h"
#include "meshcollider.h"

struct Q3BSPSurf;
struct Q3BSPFace;
struct Q3BSPLeaf;
struct Q3BSPNode;

class Q3BSPRep{
public:
	//constructor
	Q3BSPRep( const string &f,float gamma_adj );
	~Q3BSPRep();

	void render( Model *model,const RenderContext &rc );
	bool collide( const Line &line,float radius,Collision *curr_coll,const Transform &t );

	void setAmbient( const Vector &t );
	void setLighting( bool use_lmap );

	bool isValid()const{ return root_node!=0; }

private:
	Q3BSPNode *root_node;

	Vector ambient;

	vector<Q3BSPFace*> faces;
	vector<Q3BSPSurf*> surfs,r_surfs;
	vector<Texture> textures,light_maps;

	int vis_sz;
	char *vis_data;
	bool use_lmap;

	MeshCollider *collider;

	void createVis();
	void createSurfs();
	void createCollider();

	void createTextures();
	void createLightMaps();
	Q3BSPLeaf *createLeaf( int n );
	Q3BSPNode *createNode( int n );

	void vis( Q3BSPNode *node );
	void render( Q3BSPLeaf *l,int clip );
	void render( Q3BSPNode *n,int clip );
};

#endif