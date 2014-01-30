
#ifndef SPRITE_H
#define SPRITE_H

#include "model.h"
#include "brush.h"
#include "../gxruntime/gxmesh.h"

class Sprite : public Model{
public:
	enum{
		VIEW_MODE_FREE=1,		//visible from any angle
		VIEW_MODE_FIXED=2,		//visible only from front
		VIEW_MODE_UPRIGHT=3,	//upright tree-style
		VIEW_MODE_UPRIGHT2=4	//better upright tree-style
	};

	Sprite();
	Sprite( const Sprite &t );
	~Sprite();

	Sprite *getSprite(){ return this; }

	Entity *clone(){ return d_new Sprite( *this ); }

	void capture();
	bool beginRender( float tween );

	void setRotation( float angle );
	void setScale( float x_scale,float y_scale );
	void setHandle( float x,float y );
	void setViewmode( int mode );

	bool render( const RenderContext &rc );

private:
	float xhandle,yhandle;
	float rot,xscale,yscale;
	float r_rot,r_xscale,r_yscale;
	int view_mode,mesh_index;
	bool captured;
};

#endif