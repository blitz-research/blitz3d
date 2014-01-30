
#include "std.h"
#include "camera.h"

extern gxScene *gx_scene;

Camera::Camera(){
	setZoom( 1 );
	setRange( 1,1000 );
	setViewport( 0,0,0,0 );
	setClsColor( Vector() );
	setClsMode( true,true );
	setProjMode( PROJ_PERSP );
	setFogRange( 1,1000 );
	setFogColor( Vector() );
	setFogMode( gxScene::FOG_NONE );
}

void Camera::setZoom( float z ){
	zoom=z;
	local_valid=false;
}

void Camera::setRange( float n,float f ){
	frustum_nr=n;frustum_fr=f;
	local_valid=false;
}

void Camera::setViewport( int x,int y,int w,int h ){
	vp_x=x;vp_y=y;vp_w=w;vp_h=h;
	local_valid=false;
}

void Camera::setClsColor( const Vector &v ){
	cls_color=v;
}

void Camera::setClsMode( bool c,bool z ){
	cls_argb=c;cls_z=z;
}

void Camera::setProjMode( int mode ){
	proj_mode=mode;
}

void Camera::setFogColor( const Vector &v ){
	fog_color=v;
}

void Camera::setFogRange( float nr,float fr ){
	fog_nr=nr;fog_fr=fr;
}

void Camera::setFogMode( int mode ){
	fog_mode=mode;
}

const Frustum &Camera::getFrustum()const{
	if( !local_valid ){
		float ar=(float)vp_h/vp_w;
		frustum_w=frustum_nr*2/zoom;
		frustum_h=frustum_nr*2/zoom*ar;
		new( &local_frustum ) Frustum( frustum_nr,frustum_fr,frustum_w,frustum_h );
		local_valid=true;
	}
	return local_frustum;
}

float Camera::getFrustumNear()const{
	return frustum_nr;
}

float Camera::getFrustumFar()const{
	return frustum_fr;
}

float Camera::getFrustumWidth()const{
	getFrustum();return frustum_w;
}

float Camera::getFrustumHeight()const{
	getFrustum();return frustum_h;
}

void Camera::getViewport( int *x,int *y,int *w,int *h )const{
	*x=vp_x;*y=vp_y;*w=vp_w;*h=vp_h;
}

bool Camera::beginRenderFrame(){
	if( !proj_mode ) return false;
	getFrustum();
	gx_scene->setViewport( vp_x,vp_y,vp_w,vp_h );
	gx_scene->clear( &(cls_color.x),1,1,cls_argb,cls_z );
	if( proj_mode==PROJ_ORTHO ){
		gx_scene->setOrthoProj( frustum_nr,frustum_fr,frustum_w,frustum_h );
	}else{
		gx_scene->setPerspProj( frustum_nr,frustum_fr,frustum_w,frustum_h );
	}
	gx_scene->setFogRange( fog_nr,fog_fr );
	gx_scene->setFogColor( (float*)&fog_color.x );
	gx_scene->setFogMode( fog_mode );
	return true;
}
