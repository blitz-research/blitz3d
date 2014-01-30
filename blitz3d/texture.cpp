
#include "std.h"
#include "geom.h"
#include "texture.h"
#include "cachedtexture.h"

#include "../gxruntime/gxgraphics.h"

extern gxScene *gx_scene;
extern gxGraphics *gx_graphics;

struct Filter{
	string t;
	int flags;
	Filter( const string &t,int flags ):t(t),flags(flags){
	}
};

static vector<Filter> filters;

static int filterFile( const string &t,int flags ){
	//check filters...
	string l=tolower(t);
	for( int k=0;k<filters.size();++k ){
		if( l.find( filters[k].t )!=string::npos ){
			flags|=filters[k].flags;
		}
	}
	return flags;
}

struct Texture::Rep{

	int ref_cnt;
	CachedTexture cached_tex;
	vector<gxCanvas*> tex_frames;

	int tex_blend,tex_flags;
	bool transparent;

	float sx,sy,tx,ty,rot;
	bool mat_used,mat_valid;
	gxScene::Matrix matrix;

	Rep( int w,int h,int flags,int cnt ):
	ref_cnt(1),cached_tex( w,h,flags,cnt ),
	tex_blend(gxScene::BLEND_MULTIPLY),tex_flags(0),
	sx(1),sy(1),tx(0),ty(0),rot(0),mat_used(false){
		tex_frames=cached_tex.getFrames();
		transparent=
			(flags & gxCanvas::CANVAS_TEX_ALPHA) &&
			!(flags & gxCanvas::CANVAS_TEX_MASK);
		memset( &matrix,0,sizeof( matrix ) );
	}

	Rep( const string &f,int flags,int w,int h,int first,int cnt ):
	ref_cnt(1),cached_tex( f,flags,w,h,first,cnt ),
	tex_blend(gxScene::BLEND_MULTIPLY),tex_flags(0),
	sx(1),sy(1),tx(0),ty(0),rot(0),mat_used(false){
		tex_frames=cached_tex.getFrames();
		transparent=
			(flags & gxCanvas::CANVAS_TEX_ALPHA) &&
			!(flags & gxCanvas::CANVAS_TEX_MASK);
		memset( &matrix,0,sizeof( matrix ) );
	}

	Rep( const Rep &t ):
	ref_cnt(1),cached_tex(t.cached_tex),tex_frames(t.tex_frames),
	tex_blend(t.tex_blend),tex_flags(t.tex_flags),
	sx(t.sx),sy(t.sy),tx(t.tx),ty(t.ty),rot(t.rot),
	mat_used(t.mat_used),mat_valid(t.mat_valid),matrix(t.matrix),
	transparent(t.transparent){
	}
};

Texture::Texture():rep(0){
}

Texture::Texture( const string &f,int flags ){
	flags=filterFile( f,flags )|gxCanvas::CANVAS_TEXTURE;
	if( flags & gxCanvas::CANVAS_TEX_MASK ) flags|=gxCanvas::CANVAS_TEX_RGB|gxCanvas::CANVAS_TEX_ALPHA;
	rep=d_new Rep( f,flags,0,0,0,1 );
}

Texture::Texture( const string &f,int flags,int w,int h,int first,int cnt ){
	flags=filterFile( f,flags )|gxCanvas::CANVAS_TEXTURE;
	if( flags & gxCanvas::CANVAS_TEX_MASK ) flags|=gxCanvas::CANVAS_TEX_RGB|gxCanvas::CANVAS_TEX_ALPHA;
	rep=d_new Rep( f,flags,w,h,first,cnt );
}

Texture::Texture( int w,int h,int flags,int cnt ){
	flags|=gxCanvas::CANVAS_TEXTURE;
	if( flags & gxCanvas::CANVAS_TEX_MASK ) flags|=gxCanvas::CANVAS_TEX_RGB|gxCanvas::CANVAS_TEX_ALPHA;
	rep=d_new Rep( w,h,flags,cnt );
}

Texture::Texture( const Texture &t ):
rep(t.rep){
	if( rep ) ++rep->ref_cnt;
}

Texture::~Texture(){
	if( rep && !--rep->ref_cnt ) delete rep;
}

Texture &Texture::operator=( const Texture &t ){
	if( t.rep ) ++t.rep->ref_cnt;
	if( rep && !--rep->ref_cnt ) delete rep;
	rep=t.rep;
	return *this;
}

void Texture::setScale( float u_scale,float v_scale ){
	if( !rep ) return;
	rep->sx=u_scale;rep->sy=v_scale;
	rep->mat_valid=false;
	rep->mat_used=true;
}

void Texture::setRotation( float angle ){
	if( !rep ) return;
	rep->rot=angle;
	rep->mat_valid=false;
	rep->mat_used=true;
}

void Texture::setPosition( float u_pos,float v_pos ){
	if( !rep ) return;
	rep->tx=u_pos;
	rep->ty=v_pos;
	rep->mat_valid=false;
	rep->mat_used=true;
}

void Texture::setBlend( int blend ){
	if( !rep ) return;
	rep->tex_blend=blend;
}

void Texture::setFlags( int flags ){
	if( !rep ) return;
	rep->tex_flags=flags;
}

bool Texture::isTransparent()const{
	return rep ? rep->transparent : false;
}

gxCanvas *Texture::getCanvas( int n )const{
	return rep && n>=0 && n<rep->tex_frames.size() ? rep->tex_frames[n] : 0;
}

int Texture::getCanvasFlags()const{
	return rep && rep->tex_frames.size() ? rep->tex_frames[0]->getFlags() : 0;
}

CachedTexture *Texture::getCachedTexture()const{
	return rep ? &rep->cached_tex : 0;
}

int Texture::getBlend()const{
	return rep ? rep->tex_blend : 0;
}

int Texture::getFlags()const{
	return rep ? rep->tex_flags : 0;
}

const gxScene::Matrix *Texture::getMatrix()const{
	if( !rep || !rep->mat_used ) return 0;
	if( !rep->mat_valid ){
		float c=cos(rep->rot),s=sin(rep->rot);
		rep->matrix.elements[0][0]=c*rep->sx;
		rep->matrix.elements[1][0]=s*rep->sx;
		rep->matrix.elements[0][1]=-s*rep->sy;
		rep->matrix.elements[1][1]=c*rep->sy;
		rep->matrix.elements[2][0]=rep->tx;
		rep->matrix.elements[2][1]=rep->ty;
		rep->mat_valid=true;
	}
	return &rep->matrix;
}

bool Texture::operator<( const Texture &t )const{
	if( rep && t.rep ) return rep->cached_tex<t.rep->cached_tex;
	return rep<t.rep;
}

void Texture::clearFilters(){
	filters.clear();
}

void Texture::addFilter( const string &t,int flags ){
	filters.push_back( Filter( tolower(t),flags ) );
}
