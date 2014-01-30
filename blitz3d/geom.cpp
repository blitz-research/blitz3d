
#include "std.h"
#include "geom.h"

Matrix Matrix::tmps[64];
Transform Transform::tmps[64];

Quat rotationQuat( float p,float y,float r ){
	return yawQuat(y)*pitchQuat(p)*rollQuat(r);
}

/*
Quat rotationQuat( float p,float y,float r ){
	float sp=sin(p/-2),cp=cos(p/-2);
	float sy=sin(y/ 2),cy=cos(y/ 2);
	float sr=sin(r/-2),cr=cos(r/-2);
	float qw=cr*cp*cy + sr*sp*sy;
	float qx=cr*sp*cy + sr*cp*sy;
	float qy=cr*cp*sy - sr*sp*cy;
	float qz=sr*cp*cy - cr*sp*sy;
	return Quat( qw,Vector(-qx,-qy,qz) );
}
*/