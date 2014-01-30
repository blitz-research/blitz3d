
#ifndef MD2MODEL_H
#define MD2MODEL_H

#include "model.h"
#include "md2rep.h"

class MD2Model : public Model{
public:
	MD2Model( const string &filename );
	MD2Model( const MD2Model &t );
	~MD2Model();

	//Entity interface
	Entity *clone(){ return d_new MD2Model( *this ); }
	MD2Model *getMD2Model(){ return this; }

	//Object interface
	void animate( float elapsed );

	//Model interface
	bool render( const RenderContext &rc );

	//MD2 interface
	void startMD2Anim( int first,int last,int mode,float speed,float trans );

	int getMD2AnimLength()const;
	bool getMD2Animating()const{ return !!anim_mode; }
	float getMD2AnimTime()const{ return anim_time; }

	bool getValid()const;

private:
	struct Rep;
	Rep *rep;

	int anim_mode;
	float anim_time,anim_speed;
	int anim_first,anim_last,anim_len;

	float render_t;
	int render_a,render_b;

	float trans_time,trans_speed;
	MD2Rep::Vert *trans_verts;

	//Unimplemented
	MD2Model &operator=( const MD2Model & );
};

#endif