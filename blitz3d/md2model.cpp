
#include "std.h"
#include "md2rep.h"
#include "md2model.h"

struct MD2Model::Rep : public MD2Rep{
	int ref_cnt;

	Rep( const string &f):MD2Rep( f ),
	ref_cnt(1){
	}
};

MD2Model::MD2Model( const string &f ):
rep( d_new Rep( f ) ),
anim_mode(0),anim_time(0),
render_a(0),render_b(0),render_t(0),trans_verts(0){
}

MD2Model::MD2Model( const MD2Model &t ):
Model(t),rep( t.rep ),
anim_mode(0),anim_time(0),
render_a(0),render_b(0),render_t(0),trans_verts(0){
	++rep->ref_cnt;
}

MD2Model::~MD2Model(){
	if( !--rep->ref_cnt ) delete rep;
	if( trans_verts ) delete[] trans_verts;
}

void MD2Model::startMD2Anim( int first,int last,int mode,float speed,float trans ){

	if( last<first ) std::swap( first,last );

	if( first<0 ) first=0;
	else if( first>=rep->numFrames() ) first=rep->numFrames()-1;

	if( last<0 ) last=0;
	else if( last>=rep->numFrames() ) last=rep->numFrames()-1;

	if( trans>0 ){
		if( !trans_verts ) trans_verts=d_new MD2Rep::Vert[rep->numVertices()];

		if( anim_mode & 0x8000 ) rep->render( trans_verts,anim_time,trans_time );
		else rep->render( trans_verts,render_a,render_b,render_t );
		trans_speed=1.0f/trans;
		trans_time=0;
		mode|=0x8000;
	}

	anim_first=first;
	anim_last=last;
	anim_len=last-first;
	anim_speed=speed;
	anim_time=((mode&0x7fff)==Animator::ANIM_MODE_LOOP || anim_speed>=0) ? anim_first : anim_last;
	anim_mode=mode;

	if( !anim_speed || !anim_len ){
		render_a=render_b=anim_time;
		render_t=0;
		anim_mode&=0x8000;
	}
}

void MD2Model::animate( float e ){
	Model::animate( e );
	if( !anim_mode ) return;
	if( anim_mode & 0x8000 ){
		trans_time+=trans_speed;
		if( trans_time<1 ) return;
		anim_mode&=~0x8000;
		if( !anim_mode ) return;
	}
	anim_time=anim_time+anim_speed * e;
	if( anim_time<anim_first ){
		switch( anim_mode ){
		case Animator::ANIM_MODE_LOOP:
			anim_time+=anim_len;
			break;
		case Animator::ANIM_MODE_PINGPONG:
			anim_time=anim_first+(anim_first-anim_time);
			anim_speed=-anim_speed;
			break;
		default:
			anim_time=anim_first;
			anim_mode=0;
			break;
		}
	}else if( anim_time>=anim_last ){
		switch( anim_mode ){
		case Animator::ANIM_MODE_LOOP:
			anim_time-=anim_len;
			break;
		case Animator::ANIM_MODE_PINGPONG:
			anim_time=anim_last-(anim_time-anim_last);
			anim_speed=-anim_speed;
			break;
		default:
			anim_time=anim_last;
			anim_mode=0;
			break;
		}
	}
	render_a=floor(anim_time);render_b=render_a+1;
	if( anim_mode==Animator::ANIM_MODE_LOOP && render_b==anim_last ) render_b=anim_first;
	render_t=anim_time-render_a;
}

bool MD2Model::render( const RenderContext &rc ){
	static Frustum f;
	new( &f ) Frustum( rc.getWorldFrustum(),-getRenderTform() );
	if( !f.cull( rep->getBox() ) ) return false;

	if( anim_mode & 0x8000 ){
		rep->render( this,trans_verts,anim_time,trans_time );
	}else{
		rep->render( this,render_a,render_b,render_t );
	}
	return false;
}

int MD2Model::getMD2AnimLength()const{
	return rep->numFrames();
}

bool MD2Model::getValid()const{
	return rep->numFrames()>0;
}
