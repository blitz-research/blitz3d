
#include "std.h"
#include "model.h"

extern gxScene *gx_scene;

class Model::MeshQueue{
	union{
		gxMesh *mesh;
		MeshQueue *next;
	};
	int fv,vc,ft,tc;
	Brush brush;
	int q_type;
//	bool opaque;

	static MeshQueue *pool;

public:
	MeshQueue(){}

	MeshQueue( gxMesh *m,int fv,int vc,int ft,int tc,const Brush &b ):
	mesh(m),fv(fv),vc(vc),ft(ft),tc(tc),brush(b){
		int n=brush.getBlend();
		q_type=(n==gxScene::BLEND_REPLACE) ? QUEUE_OPAQUE : QUEUE_TRANSPARENT;
	}

	int getQueueType()const{
		return q_type;
	}
	void render(){
		gx_scene->setRenderState( brush.getRenderState() );
		gx_scene->render( mesh,fv,vc,ft,tc );
	}
	void *operator new( size_t sz ){
		static const int GROW=256;
		if( !pool ){
			pool=new MeshQueue[GROW];
			for( int k=0;k<GROW-1;++k ) pool[k].next=&pool[k+1];
			pool[GROW-1].next=0;
		}
		MeshQueue *t=pool;
		pool=t->next;
		return t;
	}
	void operator delete( void *q ){
		MeshQueue *t=(MeshQueue*)q;
		t->next=pool;
		pool=t;
	}
};

Model::MeshQueue *Model::MeshQueue::pool;

Model::Model():
space( RENDER_SPACE_LOCAL ),
auto_fade(false),
captured_alpha(1),w_brush(true){
}

Model::Model( const Model &t ):Object(t),
space(t.space),brush(t.brush),
auto_fade(t.auto_fade),auto_fade_nr(t.auto_fade_nr),auto_fade_fr(t.auto_fade_fr),
captured_alpha(t.captured_alpha),w_brush(true){
}

void Model::capture(){
	Object::capture();
	captured_alpha=brush.getAlpha();
}

bool Model::beginRender( float t ){
	Object::beginRender( t );
	tweened_alpha=brush.getAlpha();
	if( t!=1 && tweened_alpha!=captured_alpha ){
		//
		//render tweening of alpha
		//
		tweened_alpha=(tweened_alpha-captured_alpha)*t+captured_alpha;
	}
	return tweened_alpha>0;
}

bool Model::doAutoFade( const Vector &eye ){
	float alpha=tweened_alpha;
	if( auto_fade ){
		//
		//autofading of alpha
		//
		float d=eye.distance( getRenderTform().v );
		if( d>=auto_fade_fr ) return false;
		if( d>=auto_fade_nr ){
			float t=1-(d-auto_fade_nr)/(auto_fade_fr-auto_fade_nr );
			alpha*=t;if( alpha<=0 ) return false;
		}
	}
	if( w_brush ) render_brush=brush;

	if( alpha!=render_brush.getAlpha() ){
		render_brush.setAlpha( alpha );
	}else if( !w_brush ){
		return true;
	}

	setRenderBrush( render_brush );
	w_brush=false;
	return true;
}

void Model::enqueue( MeshQueue *q ){
	queues[q->getQueueType()].push_back( q );
}

void Model::enqueue( gxMesh *mesh,int fv,int vc,int ft,int tc ){
	enqueue( new MeshQueue( mesh,fv,vc,ft,tc,render_brush ) );
}

void Model::enqueue( gxMesh *mesh,int fv,int vc,int ft,int tc,const Brush &brush ){
	enqueue( new MeshQueue( mesh,fv,vc,ft,tc,brush ) );
}

void Model::renderQueue( int type ){
	vector<MeshQueue*> *que=&queues[type];
	for( ;que->size();que->pop_back() ){
		MeshQueue *q=que->back();
		q->render();
		delete q;
	}
}
