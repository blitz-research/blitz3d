
#ifndef ENTITY_H
#define ENTITY_H

#include <list>

#include "geom.h"

class Entity;
class Object;
class Camera;
class Light;
class Model;
class Mirror;
class Listener;
class MeshModel;
class MD2Model;

class Entity{
public:

	Entity();
	Entity( const Entity &e );
	virtual ~Entity();

	virtual Entity *clone()=0;

	//ugly casts!
	virtual Object *getObject(){ return 0; }
	virtual Camera *getCamera(){ return 0; }
	virtual Light *getLight(){ return 0; }
	virtual Model *getModel(){ return 0; }
	virtual Mirror *getMirror(){ return 0; }
	virtual Listener *getListener(){ return 0; }

	void setName( const std::string &t );
	void setParent( Entity *parent );

	void setVisible( bool vis );
	void setEnabled( bool ena );

	bool visible()const{ return _visible; }
	bool enabled()const{ return _enabled; }

	void enumVisible( vector<Object*> &out );
	void enumEnabled( vector<Object*> &out );

	Entity *children()const{ return _children; }
	Entity *successor()const{ return _succ; }

	std::string getName()const{ return _name; }
	Entity *getParent()const{ return _parent; }

	void setLocalPosition( const Vector &v );
	void setLocalScale( const Vector & v );
	void setLocalRotation( const Quat &q );
	void setLocalTform( const Transform &t );

	void setWorldPosition( const Vector &v );
	void setWorldScale( const Vector &v );
	void setWorldRotation( const Quat &q );
	void setWorldTform( const Transform &t );

	const Vector &getLocalPosition()const;
	const Vector &getLocalScale()const;
	const Quat &getLocalRotation()const;
	const Transform &getLocalTform()const;

	const Vector &getWorldPosition()const;
	const Vector &getWorldScale()const;
	const Quat &getWorldRotation()const;
	const Transform &getWorldTform()const;

	static Entity *orphans(){ return _orphans; }

private:
	Entity *_succ,*_pred,*_parent,*_children,*_last_child;

	static Entity *_orphans,*_last_orphan;

	bool _visible,_enabled;

	std::string _name;

	mutable int invalid;

	Quat local_rot;
	Vector local_pos,local_scl;
	mutable Transform local_tform;

	mutable Quat world_rot;
	mutable Vector world_pos,world_scl;
	mutable Transform world_tform;

	void insert();
	void remove();
	void invalidateLocal();
	void invalidateWorld();
};

#endif
