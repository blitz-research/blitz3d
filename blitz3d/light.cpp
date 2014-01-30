
#include "std.h"
#include "light.h"
#include "../gxruntime/gxscene.h"

extern gxScene *gx_scene;

Light::Light( int type ){
	light=gx_scene->createLight( type );
}

Light::~Light(){
	gx_scene->freeLight( light );
}

void Light::setRange( float r ){
	light->setRange( r );
}

void Light::setColor( const Vector &v ){
	light->setColor( (float*)&v.x );
}

void Light::setConeAngles( float inner,float outer ){
	light->setConeAngles( inner,outer );
}

bool Light::beginRender( float tween ){
	Object::beginRender( tween );
	light->setPosition( &getRenderTform().v.x );
	light->setDirection( &getRenderTform().m.k.x );
	return true;
}
