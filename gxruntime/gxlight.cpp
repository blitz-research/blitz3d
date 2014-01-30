
#include "std.h"
#include "gxlight.h"
#include "gxscene.h"
#include "gxgraphics.h"

const float PI=3.14159265359f;	//180 degrees
const float TWOPI=PI*2.0f;		//360 degrees
const float HALFPI=PI*.5f;		//90  degrees
const float EPSILON=.000001f;

gxLight::gxLight( gxScene *s,int type ):
scene(s){

	memset(&d3d_light,0,sizeof(d3d_light));

	switch( type ){
	case LIGHT_POINT:
		d3d_light.dltType=D3DLIGHT_POINT;
		break;
	case LIGHT_SPOT:
		d3d_light.dltType=D3DLIGHT_SPOT;
		break;
	default:
		d3d_light.dltType=D3DLIGHT_DIRECTIONAL;
	}

	d3d_light.dcvDiffuse.a=1;
	d3d_light.dcvDiffuse.r=d3d_light.dcvDiffuse.g=d3d_light.dcvDiffuse.b=1;
	d3d_light.dcvSpecular.r=d3d_light.dcvSpecular.g=d3d_light.dcvSpecular.b=1;
	d3d_light.dvRange=D3DLIGHT_RANGE_MAX;
	d3d_light.dvTheta=0;
	d3d_light.dvPhi=HALFPI;
	d3d_light.dvFalloff=1;
	d3d_light.dvDirection.z=1;
	setRange( 1000 );
}

gxLight::~gxLight(){
}

void gxLight::setRange( float r ){
	d3d_light.dvAttenuation1=1.0f/r;
}

void gxLight::setPosition( const float pos[3] ){
	d3d_light.dvPosition.x=pos[0];
	d3d_light.dvPosition.y=pos[1];
	d3d_light.dvPosition.z=pos[2];
}

void gxLight::setDirection( const float dir[3] ){
	d3d_light.dvDirection.x=dir[0];
	d3d_light.dvDirection.y=dir[1];
	d3d_light.dvDirection.z=dir[2];
}

void gxLight::setConeAngles( float inner,float outer ){
	d3d_light.dvTheta=inner;
	d3d_light.dvPhi=outer;
}
