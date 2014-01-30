
#include "std.h"
#include "animation.h"

struct Animation::Rep{

	int ref_cnt;

	typedef map<int,Quat> KeyList;

	KeyList scale_anim,rot_anim,pos_anim;

	Rep():
	ref_cnt(1){
	}

	Rep( const Rep &t ):
	ref_cnt(1),
	scale_anim(t.scale_anim),rot_anim(t.rot_anim),pos_anim(t.pos_anim){
	}

	Vector getLinearValue( const KeyList &keys,float time )const{
		KeyList::const_iterator next,curr;

		//for( next=keys.begin();next!=keys.end() && time>=next->first;++next ){}
		next=keys.upper_bound( (int)time );

		if( next==keys.begin() ) return next->second.v;
		curr=next;--curr;
		if( next==keys.end() ) return curr->second.v;

		float delta=( time-curr->first )/( next->first-curr->first );
		return ( next->second.v-curr->second.v )*delta+curr->second.v;
	}
	
	Quat getSlerpValue( const KeyList &keys,float time )const{
		KeyList::const_iterator next,curr;

		//for( next=keys.begin();next!=keys.end() && time>=next->first;++next ){}
		next=keys.upper_bound( (int)time );

		if( next==keys.begin() ) return next->second;
		curr=next;--curr;
		if( next==keys.end() ) return curr->second;

		float delta=( time-curr->first )/( next->first-curr->first );
		return curr->second.slerpTo( next->second,delta );
	}

	void setKey( KeyList &keys,int time,const Quat &value ){
		keys[time]=value;
	}
};

Animation::Animation():
rep( new Rep() ){
}

Animation::Animation( const Animation &t ):
rep( t.rep ){
	++rep->ref_cnt;
}

Animation::Animation( const Animation &t,int first,int last ):
rep( new Rep() ){
	Rep::KeyList::const_iterator it;
	for( it=t.rep->pos_anim.begin();it!=t.rep->pos_anim.end();++it ){
		if( it->first<first || it->first>last ) continue;
		rep->setKey( rep->pos_anim,it->first-first,it->second );
	}
	for( it=t.rep->scale_anim.begin();it!=t.rep->scale_anim.end();++it ){
		if( it->first<first || it->first>last ) continue;
		rep->setKey( rep->scale_anim,it->first-first,it->second );
	}
	for( it=t.rep->rot_anim.begin();it!=t.rep->rot_anim.end();++it ){
		if( it->first<first || it->first>last ) continue;
		rep->setKey( rep->rot_anim,it->first-first,it->second );
	}
}

Animation::~Animation(){
	if( !--rep->ref_cnt ) delete rep;
}

Animation &Animation::operator=( const Animation &t ){
	++t.rep->ref_cnt;
	if( !--rep->ref_cnt ) delete rep;
	rep=t.rep;
	return *this;
}

Animation::Rep *Animation::write(){
	if( rep->ref_cnt>1 ){
		--rep->ref_cnt;
		rep=new Rep( *rep );
	}
	return rep;
}

void Animation::setScaleKey( int time,const Vector &q ){
	write();
	rep->setKey( rep->scale_anim,time,Quat( 0,q ) );
}

void Animation::setPositionKey( int time,const Vector &q ){
	write();
	rep->setKey( rep->pos_anim,time,Quat( 0,q ) );
}

void Animation::setRotationKey( int time,const Quat &q ){
	write();
	rep->setKey( rep->rot_anim,time,q );
}

int Animation::numScaleKeys()const{
	return rep->scale_anim.size();
}

int Animation::numRotationKeys()const{
	return rep->rot_anim.size();
}

int Animation::numPositionKeys()const{
	return rep->pos_anim.size();
}

Vector Animation::getScale( float time )const{
	if( !rep->scale_anim.size() ) return Vector(1,1,1);
	return rep->getLinearValue( rep->scale_anim,time );
}

Vector Animation::getPosition( float time )const{
	if( !rep->pos_anim.size() ) return Vector(0,0,0);
	return rep->getLinearValue( rep->pos_anim,time );
}

Quat Animation::getRotation( float time )const{
	if( !rep->rot_anim.size() ) return Quat();
	return rep->getSlerpValue( rep->rot_anim,time );
}

/*
struct Animation::Rep{

	int ref_cnt;

	struct Key{
		int time;
		Quat value;
	};
	typedef list<Key> KeyList;

	KeyList scale_anim,rot_anim,pos_anim;

	Rep():
	ref_cnt(1){
	}

	Rep( const Rep &t ):
	ref_cnt(1),
	scale_anim(t.scale_anim),rot_anim(t.rot_anim),pos_anim(t.pos_anim){
	}

	Vector getLinearValue( const KeyList &keys,float time )const{
		if( keys.size()==1 ) return keys.front().value.v;
		if( time>=keys.back().time ) return keys.back().value.v;
		if( time<=keys.front().time ) return keys.front().value.v;
		KeyList::const_iterator it;
		for( it=keys.begin();time>=it->time;++it ){}
		const Key *next=&*it;
		const Key *curr=&*--it;
		float delta=( time-curr->time )/( next->time-curr->time );
		return (next->value.v-curr->value.v)*delta+curr->value.v;
	}

	Quat getSlerpValue( const KeyList &keys,float time )const{
		if( keys.size()==1 ) return keys.front().value;
		if( time>=keys.back().time ) return keys.back().value;
		if( time<=keys.front().time ) return keys.front().value;
		KeyList::const_iterator it;
		for( it=keys.begin();time>=it->time;++it ){}
		const Key *next=&*it;
		const Key *curr=&*--it;
		float delta=(time-curr->time)/(next->time-curr->time);
		return curr->value.slerpTo( next->value,delta );
	}

	void setKey( KeyList &keys,int time,const Quat &value ){
		KeyList::iterator it;
		for( it=keys.begin();it!=keys.end() && time>it->time;++it ){}
		if( it==keys.end() || time<it->time ){
			it=keys.insert( it );
			it->time=time;
		}
		it->value=value;
	}
};

Animation::Animation():
rep( d_new Rep() ){
}

Animation::Animation( const Animation &t ):
rep( t.rep ){
	++rep->ref_cnt;
}

Animation::Animation( const Animation &t,int first,int last ):
rep( d_new Rep() ){
	Rep::KeyList::const_iterator it;
	for( it=t.rep->pos_anim.begin();it!=t.rep->pos_anim.end();++it ){
		const Rep::Key &key=*it;
		if( key.time<first || key.time>last ) continue;
		rep->setKey( rep->pos_anim,key.time-first,key.value );
	}
	for( it=t.rep->scale_anim.begin();it!=t.rep->scale_anim.end();++it ){
		const Rep::Key &key=*it;
		if( key.time<first || key.time>last ) continue;
		rep->setKey( rep->scale_anim,key.time-first,key.value );
	}
	for( it=t.rep->rot_anim.begin();it!=t.rep->rot_anim.end();++it ){
		const Rep::Key &key=*it;
		if( key.time<first || key.time>last ) continue;
		rep->setKey( rep->rot_anim,key.time-first,key.value );
	}
}

Animation::~Animation(){
	if( !--rep->ref_cnt ) delete rep;
}

Animation &Animation::operator=( const Animation &t ){
	++t.rep->ref_cnt;
	if( !--rep->ref_cnt ) delete rep;
	rep=t.rep;
	return *this;
}

Animation::Rep *Animation::write(){
	if( rep->ref_cnt>1 ){
		--rep->ref_cnt;
		rep=d_new Rep( *rep );
	}
	return rep;
}

void Animation::setScaleKey( int time,const Vector &q ){
	write();
	rep->setKey( rep->scale_anim,time,Quat( 0,q ) );
}

void Animation::setPositionKey( int time,const Vector &q ){
	write();
	rep->setKey( rep->pos_anim,time,Quat( 0,q ) );
}

void Animation::setRotationKey( int time,const Quat &q ){
	write();
	rep->setKey( rep->rot_anim,time,q );
}

int Animation::numScaleKeys()const{
	return rep->scale_anim.size();
}

int Animation::numRotationKeys()const{
	return rep->rot_anim.size();
}

int Animation::numPositionKeys()const{
	return rep->pos_anim.size();
}

Vector Animation::getScale( float time )const{
	if( !rep->scale_anim.size() ) return Vector(1,1,1);
	return rep->getLinearValue( rep->scale_anim,time );
}

Vector Animation::getPosition( float time )const{
	if( !rep->pos_anim.size() ) return Vector(0,0,0);
	return rep->getLinearValue( rep->pos_anim,time );
}

Quat Animation::getRotation( float time )const{
	if( !rep->rot_anim.size() ) return Quat();
	return rep->getSlerpValue( rep->rot_anim,time );
}
*/
