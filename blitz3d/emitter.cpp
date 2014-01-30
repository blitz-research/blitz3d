
#include "std.h"
#include "emitter.h"

Emitter::Emitter(){
}

Emitter::Emitter( const Emitter &t ){
}

Emitter::~Emitter(){
}

void Emitter::beginRender( float tween ){
	Object::beginRender( tween );

	vel=getRenderTform().v-pos;
	pos=getRenderTform().v;

	for( int k=0;k<channels.size();++k ){
		gxChannel *chan=channels[k];
		if( !chan->isPlaying() ){
			channels[k]=0;
			continue;
		}
		chan->set3d( &pos.x,&vel.x );
	}
}

gxChannel *Emitter::emitSound( gxSound *sound ){

	gxChannel *chan=sound->play3d( &pos.x,&vel.x );

	for( int k=0;k<channels.size();++k ){
		if( chan=channels[k] ) return chan;
		if( channels[k] ) continue;
		channels[k]=chan;
		return chan;
	}
	channels.push_back( chan );
	return chan;
}