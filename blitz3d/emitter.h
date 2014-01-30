
#ifndef EMITTER_H
#define EMITTER_H

#include "object.h"

class gxSound;
class gxChannel;

class Emitter : public Object{
public:
	Emitter();
	Emitter( const Emitter &t );
	~Emitter();

	//Entity interface
	Entity *clone(){ return d_new Emitter( *this ); }
	Emitter *getEmitter(){ return this; }

	//Object interface
	void beginRender( float tween );

	//Public interface
	gxChannel *emitSound( gxSound *sound );

private:
	Vector pos,vel;

	vector<gxChannel*> channels;
};

#endif