
#ifndef OBJECT_H
#define OBJECT_H

#include <vector>

#include "entity.h"
#include "animator.h"
#include "collision.h"

class gxSound;

struct ObjCollision{
	Object *with;
	Vector coords;
	Collision collision;
};

class Object : public Entity{
public:
	typedef std::vector<const ObjCollision*> Collisions;

	Object();
	Object( const Object &object );
	~Object();

	//Entity interface
	Object *getObject(){ return this; }
	Entity *clone(){ return d_new Object( *this ); }

	//deep object copy!
	Object *copy();

	//called by user
	void reset();
	void setCollisionType( int type );
	void setCollisionRadii( const Vector &radii );
	void setCollisionBox( const Box &box );
	void setOrder( int n ){ order=n; }
	void setPickGeometry( int n ){ pick_geom=n; }
	void setObscurer( bool t ){ obscurer=t; }
	void setAnimation( const Animation &t ){ anim=t; }
	void setAnimator( Animator *t );

	gxChannel *emitSound( gxSound *sound );

	//overridables!
	virtual bool collide( const Line &line,float radius,::Collision *curr_coll,const Transform &t ){ return false; }
	virtual void capture();
	virtual void animate( float e );
	virtual bool beginRender( float tween );
	virtual void endRender();

	//for use by world
	void beginUpdate( float elapsed );
	void addCollision( const ObjCollision *c );
	void endUpdate();

	//accessors
	int getCollisionType()const;
	const Vector &getCollisionRadii()const;
	const Box &getCollisionBox()const;
	int getOrder()const{ return order; }
	const Vector &getVelocity()const;
	const Collisions &getCollisions()const;
	const Transform &getRenderTform()const;
	const Transform &getPrevWorldTform()const;
	int getPickGeometry()const{ return pick_geom; }
	int getObscurer()const{ return obscurer; }
	Animation getAnimation()const{ return anim; }
	Animator *getAnimator()const{ return animator; }
	Object *getLastCopy()const{ return last_copy; }

private:
	int coll_type;
	int order;
	Vector coll_radii;
	Collisions colls;
	bool captured;
	Box coll_box;
	int pick_geom;
	bool obscurer;
	float elapsed;
	Vector velocity;
	vector<gxChannel*> channels;
	Vector capt_pos,capt_scl;
	Quat capt_rot;
	mutable Object *last_copy;

	Transform prev_tform;
	Transform captured_tform,tween_tform;
	mutable Transform render_tform;
	mutable bool render_tform_valid;

	Animation anim;
	Animator *animator;

	void updateSounds();
};

#endif
