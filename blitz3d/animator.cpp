
#include "std.h"
#include "animator.h"
#include "object.h"

Animator::Animator( Animator *t ):_seqs( t->_seqs ){

	_objs.resize( t->_objs.size() );
	_anims.resize( t->_anims.size() );

	for( int k=0;k<t->_objs.size();++k ){
		_objs[k]=t->_objs[k]->getLastCopy();
		_anims[k].keys=t->_anims[k].keys;
	}

	reset();
}

Animator::Animator( Object *obj,int frames ){
	addObjs( obj );
	_anims.resize( _objs.size() );
	addSeq( frames );
	reset();
}

Animator::Animator( const vector<Object*> &objs,int frames ):_objs(objs){
	_anims.resize( _objs.size() );
	addSeq( frames );
	reset();
}

void Animator::reset(){
	_seq=_mode=_seq_len=_time=_speed=_trans_time=_trans_speed=0;
}

void Animator::addObjs( Object *obj ){
	_objs.push_back( obj );
	for( Entity *e=obj->children();e;e=e->successor() ){
		addObjs( e->getObject() );
	}
}

void Animator::addSeq( int frames ){
	Seq seq;
	seq.frames=frames;
	_seqs.push_back( seq );
	for( int k=0;k<_objs.size();++k ){
		Object *obj=_objs[k];
		_anims[k].keys.push_back( obj->getAnimation() );
		obj->setAnimation( Animation() );
	}
}

void Animator::addSeqs( Animator *t ){
	for( int n=0;n<t->_seqs.size();++n ){
		_seqs.push_back( t->_seqs[n] );
		for( int k=0;k<_objs.size();++k ){
			int j;
			for( j=0;j<t->_objs.size();++j ){
				if( _objs[k]->getName()==t->_objs[j]->getName() ) break;
			}
			if( j==t->_objs.size() ){
				_anims[k].keys.push_back( Animation() );
				continue;
			}
			_anims[k].keys.push_back( t->_anims[j].keys[n] );
		}
	}
}

void Animator::extractSeq( int first,int last,int seq ){
	Seq sq;
	sq.frames=last-first;
	_seqs.push_back( sq );

	for( int k=0;k<_objs.size();++k ){
		Animation &keys=_anims[k].keys[seq];
		_anims[k].keys.push_back( Animation( keys,first,last ) );
	}
}

void Animator::updateAnim(){

	for( int k=0;k<_objs.size();++k ){

		Object *obj=_objs[k];
		const Animation &keys=_anims[k].keys[_seq];

		if( keys.numPositionKeys() ){
			obj->setLocalPosition( keys.getPosition( _time ) );
		}
		if( keys.numScaleKeys() ){
			obj->setLocalScale( keys.getScale( _time ) );
		}
		if( keys.numRotationKeys() ){
			obj->setLocalRotation( keys.getRotation( _time ) );
		}
	}
}

void Animator::updateTrans(){

	for( int k=0;k<_objs.size();++k ){

		Object *obj=_objs[k];
		const Anim &anim=_anims[k];

		if( anim.pos ) obj->setLocalPosition( (anim.dest_pos-anim.src_pos)*_trans_time+anim.src_pos );
		if( anim.scl ) obj->setLocalScale( (anim.dest_scl-anim.src_scl)*_trans_time+anim.src_scl );
		if( anim.rot ) obj->setLocalRotation( anim.src_rot.slerpTo( anim.dest_rot,_trans_time ) );
	}
}

void Animator::beginTrans(){

	for( int k=0;k<_objs.size();++k ){

		Object *obj=_objs[k];
		Anim &anim=_anims[k];
		const Animation &keys=_anims[k].keys[_seq];

		if( anim.pos=!!keys.numPositionKeys() ){
			anim.src_pos=obj->getLocalPosition();
			anim.dest_pos=keys.getPosition( _time );
		}
		if( anim.scl=!!keys.numScaleKeys() ){
			anim.src_scl=obj->getLocalScale();
			anim.dest_scl=keys.getScale( _time );
		}
		if( anim.rot=!!keys.numRotationKeys() ){
			anim.src_rot=obj->getLocalRotation();
			anim.dest_rot=keys.getRotation( _time );
		}
	}
}

void Animator::setAnimTime( float time,int seq ){
	if( seq<0 || seq>_seqs.size() ) return;

	_mode=0;
	_speed=0;
	_seq=seq;
	_seq_len=_seqs[_seq].frames;

	//Ok, mod the anim time!
	_time=fmod( time,_seq_len );

	//if( time<0 || time>_seq_len ) time=fmod( time,_seq_len );
	//_time=time;
		
	if( _time<0 ) _time+=+_seq_len;

	updateAnim();
}

void Animator::animate( int mode,float speed,int seq,float trans ){
	if( !mode && !speed ){ _mode=0;return; }

	if( seq<0 || seq>=_seqs.size() ) return;

	_seq=seq;
	_mode=mode;
	_seq_len=_seqs[_seq].frames;
	_speed=speed;
	_time=_speed>=0 ? 0 : _seq_len;

	if( trans<=0 ){
		updateAnim();
		if( !_speed ) _mode=0;
		return;
	}

	_mode|=0x8000;
	_trans_time=0;
	_trans_speed=1/trans;
	beginTrans();
}

void Animator::update( float elapsed ){

	if( !_mode ) return;

	if( _mode&0x8000 ){
		_trans_time+=_trans_speed*elapsed;
		if( _trans_time<1 ){
			updateTrans();
			return;
		}
		_mode&=0x7fff;
		if( !_mode || !_speed ){
			updateAnim();
			_mode=0;
			return;
		}
	}

	//do anim...
	_time+=_speed*elapsed;

	switch( _mode ){
	case ANIM_MODE_LOOP:
		_time=fmod( _time,_seq_len );
		if( _time<0 ) _time+=_seq_len;
		break;
	case ANIM_MODE_PINGPONG:
		_time=fmod( _time,_seq_len*2 );
		if( _time<0 ) _time+=_seq_len*2;
		if( _time>=_seq_len ){ _time=_seq_len-(_time-_seq_len);_speed=-_speed; }
		break;
	case ANIM_MODE_ONESHOT:
		if( _time<0 ){ _time=0;_mode=0; }
		else if( _time>=_seq_len ){ _time=_seq_len;_mode=0; }
		break;
	}

	updateAnim();
}

