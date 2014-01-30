
#include "std.h"
#include "entity.h"

#include "stats.h"

Entity *Entity::_orphans,*Entity::_last_orphan;

enum{
	INVALID_LOCALTFORM=1,
	INVALID_WORLDTFORM=2
};

void Entity::remove(){
	if( _parent ){
		if( _parent->_children==this ) _parent->_children=_succ;
		if( _parent->_last_child==this ) _parent->_last_child=_pred;
	}else{
		if( _orphans==this ) _orphans=_succ;
		if( _last_orphan==this ) _last_orphan=_pred;
	}
	if( _succ ) _succ->_pred=_pred;
	if( _pred ) _pred->_succ=_succ;
}

void Entity::insert(){
	_succ=0;
	if( _parent ){
		if( _pred=_parent->_last_child ) _pred->_succ=this;
		else _parent->_children=this;
		_parent->_last_child=this;
	}else{
		if( _pred=_last_orphan ) _pred->_succ=this;
		else _orphans=this;
		_last_orphan=this;
	}
}

Entity::Entity():
_succ(0),_pred(0),_parent(0),_children(0),_last_child(0),
_visible(true),_enabled(true),
local_scl(1,1,1),
invalid(0){
	insert();
}

Entity::Entity( const Entity &e ):
_succ(0),_pred(0),_parent(0),_children(0),_last_child(0),
_name(e._name),_visible(e._visible),_enabled(e._enabled),
local_pos(e.local_pos),
local_scl(e.local_scl),
local_rot(e.local_rot),
invalid( INVALID_LOCALTFORM|INVALID_WORLDTFORM ){
	insert();
}

Entity::~Entity(){
	while( children() ) delete children();
	remove();
}

void Entity::invalidateWorld(){
	if( invalid & INVALID_WORLDTFORM ) return;
	invalid|=INVALID_WORLDTFORM;
	for( Entity *e=_children;e;e=e->_succ ){
		e->invalidateWorld();
	}
}

void Entity::invalidateLocal(){
	invalid|=INVALID_LOCALTFORM;
	invalidateWorld();
}

const Transform &Entity::getLocalTform()const{
	if( invalid&INVALID_LOCALTFORM ){
		local_tform.m=Matrix( local_rot );
		local_tform.m.i*=local_scl.x;
		local_tform.m.j*=local_scl.y;
		local_tform.m.k*=local_scl.z;
		local_tform.v=local_pos;
		invalid&=~INVALID_LOCALTFORM;
	}
	return local_tform;
}

const Transform &Entity::getWorldTform()const{
	if( invalid&INVALID_WORLDTFORM ){
		world_tform=_parent ? _parent->getWorldTform() * getLocalTform() : getLocalTform();
		invalid&=~INVALID_WORLDTFORM;
	}
	return world_tform;
}

void Entity::setParent( Entity *p ){
	if( _parent==p ) return;

	remove();

	_parent=p;

	insert();

	invalidateWorld();
}

void Entity::setName( const string &t ){
	_name=t;
}

void Entity::setVisible( bool visible ){
	_visible=visible;
}

void Entity::setEnabled( bool enabled ){
	_enabled=enabled;
}

void Entity::enumVisible( vector<Object*> &out ){
	if( !_visible ) return;
	if( Object *o=getObject() ) out.push_back(o);
	for( Entity *e=_children;e;e=e->_succ ){
		e->enumVisible( out );
	}
}

void Entity::enumEnabled( vector<Object*> &out ){
	if( !_enabled ) return;
	if( Object *o=getObject() ) out.push_back(o);
	for( Entity *e=_children;e;e=e->_succ ){
		e->enumEnabled( out );
	}
}

void Entity::setLocalPosition( const Vector &v ){
	local_pos=v;
	invalidateLocal();
}

void Entity::setLocalScale( const Vector &v ){
	local_scl=v;
	invalidateLocal();
}

void Entity::setLocalRotation( const Quat &q ){
	local_rot=q.normalized();
	invalidateLocal();
}

void Entity::setLocalTform( const Transform &t ){
	local_pos=t.v;
	local_scl=Vector( t.m.i.length(),t.m.j.length(),t.m.k.length() );
	local_rot=matrixQuat( t.m );
	invalidateLocal();
}

void Entity::setWorldPosition( const Vector &v ){
	setLocalPosition( _parent ? -_parent->getWorldTform() * v : v );
}

void Entity::setWorldScale( const Vector &v ){
	setLocalScale( _parent ? v/_parent->getWorldScale() : v );
}

void Entity::setWorldRotation( const Quat &q ){
	setLocalRotation( _parent ? -_parent->getWorldRotation() * q : q );
}

void Entity::setWorldTform( const Transform &t ){
	setLocalTform( _parent ? -_parent->getWorldTform() * t : t );
}

const Vector &Entity::getLocalPosition()const{
	return local_pos;
}

const Vector &Entity::getLocalScale()const{
	return local_scl;
}

const Quat &Entity::getLocalRotation()const{
	return local_rot;
}

const Vector &Entity::getWorldPosition()const{
	return getWorldTform().v;
}

const Vector &Entity::getWorldScale()const{
	world_scl=_parent ? _parent->getWorldScale() * local_scl : local_scl;
	return world_scl;
}

const Quat &Entity::getWorldRotation()const{
	world_rot=_parent ? _parent->getWorldRotation() * local_rot : local_rot;
	return world_rot;
}
