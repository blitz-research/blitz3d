
#include "std.h"
#include "gxdevice.h"
#include "gxruntime.h"

gxDevice::gxDevice(){
	reset();
}

gxDevice::~gxDevice(){
}

void gxDevice::reset(){
	memset( down_state,0,sizeof(down_state) );
	memset( axis_states,0,sizeof(axis_states) );
	memset( hit_count,0,sizeof(hit_count) );
	put=get=0;
}

void gxDevice::downEvent( int key ){
	down_state[key]=true;
	++hit_count[key];
	if( put-get<QUE_SIZE ) que[put++&QUE_MASK]=key;
}

void gxDevice::upEvent( int key ){
	down_state[key]=false;
}

void gxDevice::setDownState( int key,bool down ){
	if( down==down_state[key] ) return;
	if( down ) downEvent( key );
	else upEvent( key );
}

void gxDevice::flush(){
	update();
	memset( hit_count,0,sizeof(hit_count) );
	put=get=0;
}

bool gxDevice::keyDown( int key ){
	update();
	return down_state[key];
}

int gxDevice::keyHit( int key ){
	update();
	int n=hit_count[key];
	hit_count[key]-=n;
	return n;
}

int gxDevice::getKey(){
	update();
	return get<put ? que[get++ & QUE_MASK] : 0;
}

float gxDevice::getAxisState( int axis ){
	update();
	return axis_states[axis];
}
