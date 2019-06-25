
#include "std.h"
#include "brush.h"

#include "../gxruntime/gxgraphics.h"

struct Brush::Rep{
	union{ int ref_cnt;Rep *next; };
	int blend,max_tex;
	bool blend_valid;
	gxScene::RenderState rs;
	Texture texs[gxScene::MAX_TEXTURES];

	static Rep *pool;

	Rep():
	ref_cnt(1),blend(0),max_tex(0),blend_valid(true){
		memset( &rs,0,sizeof(rs) );
		rs.blend=gxScene::BLEND_REPLACE;
		rs.color[0]=rs.color[1]=rs.color[2]=rs.alpha=1;
	}

	Rep( const Rep &t ):
	ref_cnt(1),blend(t.blend),max_tex(t.max_tex),rs(t.rs),blend_valid(t.blend_valid){
		for( int k=0;k<max_tex;++k ) texs[k]=t.texs[k];
	}

	void *operator new( size_t sz ){
		static const int GROW=64;
		if( !pool ){
			pool=new Rep[GROW];
			for( int k=0;k<GROW-1;++k ) pool[k].next=&pool[k+1];
			pool[GROW-1].next=0;
		}
		Rep *p=pool;
		pool=p->next;
		return p;
	}

	void operator delete( void *q ){
		Rep *p=(Rep*)q;
		p->next=pool;
		pool=p;
	}
};

Brush::Rep *Brush::Rep::pool;

Brush::Brush():
rep( new Rep() ){
}

Brush::Brush( const Brush &t ):
rep( t.rep ){
	++rep->ref_cnt;
}

Brush::Brush( const Brush &a,const Brush &b ):
rep( new Rep( *a.rep ) ){

	*(Vector*)rep->rs.color*=*(Vector*)b.rep->rs.color;

	rep->rs.alpha*=b.rep->rs.alpha;
	rep->rs.shininess+=b.rep->rs.shininess;

	if( b.rep->blend ) rep->blend=b.rep->blend;

	rep->rs.fx|=b.rep->rs.fx;

	if( b.rep->max_tex>rep->max_tex ) rep->max_tex=b.rep->max_tex;

	for( int k=0;k<rep->max_tex;++k ){
		if( b.rep->rs.tex_states[k].canvas ){
			rep->rs.tex_states[k].canvas=b.rep->rs.tex_states[k].canvas;
			rep->texs[k]=b.rep->texs[k];
		}
	}

	rep->blend_valid=false;
}

Brush::~Brush(){
	if( !--rep->ref_cnt ) delete rep;
}

Brush &Brush::operator=( const Brush &t ){
	++t.rep->ref_cnt;
	if( !--rep->ref_cnt ) delete rep;
	rep=t.rep;return *this;
}

Brush::Rep *Brush::write()const{
	if( rep->ref_cnt>1 ){
		--rep->ref_cnt;
		rep=new Rep( *rep );
	}
	return rep;
}

void Brush::setColor( const Vector &color ){
	*(Vector*)write()->rs.color=color;
}

void Brush::setAlpha( float alpha ){
	float a=rep->rs.alpha;
	write()->rs.alpha=alpha;
	if( (a<1)!=(alpha<1) ) rep->blend_valid=false;
}

void Brush::setShininess( float n ){
	write()->rs.shininess=n;
}

void Brush::setBlend( int blend ){
	write()->blend=blend;
	rep->blend_valid=false;
}

void Brush::setFX( int fx ){
	write()->rs.fx=fx;
	rep->blend_valid=false;
}

void Brush::setTexture( int index,const Texture &t,int n ){
	write();
	gxScene::RenderState &rs=rep->rs;

	rep->texs[index]=t;
	rs.tex_states[index].canvas=t.getCanvas( n );

	rep->max_tex=0;
	for( int k=0;k<gxScene::MAX_TEXTURES;++k ){
		if( rs.tex_states[k].canvas ) rep->max_tex=k+1;
	}
	rep->blend_valid=false;
}

const Vector &Brush::getColor()const{
	return *(Vector*)rep->rs.color;
}

float Brush::getAlpha()const{
	return rep->rs.alpha;
}

float Brush::getShininess()const{
	return rep->rs.shininess;
}

int Brush::getBlend()const{
	if( rep->blend_valid ) return rep->rs.blend;

	rep->blend_valid=true;	//well, it will be...

	gxScene::RenderState &rs=rep->rs;

	//alphatest
	if( rep->texs[0].getCanvasFlags() & gxCanvas::CANVAS_TEX_MASK ){
		rs.fx|=gxScene::FX_ALPHATEST;
	}else{
		rs.fx&=~gxScene::FX_ALPHATEST;
	}

	//0 = default/replace
	//1 = alpha
	//2 = multiply
	//3 = add
	if( rep->blend ){
		if( rep->blend!=gxScene::BLEND_ALPHA ){
			return rs.blend=rep->blend;
		}
		for( int k=0;k<rep->max_tex;++k ){
			if( rep->texs[k].isTransparent() ){
				return rs.blend=gxScene::BLEND_ALPHA;
			}
		}
	}else if( rep->max_tex==1 && rep->texs[0].isTransparent() ){
		//single transparent texture?
		return rs.blend=gxScene::BLEND_ALPHA;
	}

	//vertex alpha or entityalpha?
	if( (rs.fx&gxScene::FX_VERTEXALPHA) || rs.alpha<1 ){
		return rs.blend=gxScene::BLEND_ALPHA;
	}

	return rs.blend=gxScene::BLEND_REPLACE;
}

int Brush::getFX()const{
	return rep->rs.fx;
}

Texture Brush::getTexture( int index )const{
	return rep->texs[index];
}

const gxScene::RenderState &Brush::getRenderState()const{
	getBlend();
	for( int k=0;k<rep->max_tex;++k ){
		gxScene::RenderState::TexState *ts=&rep->rs.tex_states[k];
		ts->matrix=rep->texs[k].getMatrix();
		ts->blend=rep->texs[k].getBlend();
		ts->flags=rep->texs[k].getFlags();
		ts->bumpEnvMat[0][0] = rep->texs[k].getBumpEnvMat(0,0);
		ts->bumpEnvMat[1][0] = rep->texs[k].getBumpEnvMat(1,0);
		ts->bumpEnvMat[0][1] = rep->texs[k].getBumpEnvMat(0,1);
		ts->bumpEnvMat[1][1] = rep->texs[k].getBumpEnvMat(1,1);
		ts->bumpEnvScale = rep->texs[k].getBumpEnvScale();
		ts->bumpEnvOffset = rep->texs[k].getBumpEnvOffset();
	}
	return rep->rs;
}

bool Brush::operator<( const Brush &t )const{
	return memcmp( &getRenderState(),&t.getRenderState(),sizeof(gxScene::RenderState) )<0;
}
