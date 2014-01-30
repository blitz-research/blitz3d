
#include "std.h"
#include "object.h"

extern gxRuntime *gx_runtime;

Object::Object():
order(0),animator(0),last_copy(0),
coll_type(0),coll_radii(Vector(1,1,1)),coll_box(Box(Vector(-1,-1,-1),Vector(1,1,1))),
pick_geom(0),obscurer(false),captured(false){
	reset();
}

Object::Object( const Object &o ):
Entity(o),
order(o.order),animator(0),last_copy(0),
coll_type(o.coll_type),coll_radii(o.coll_radii),coll_box(o.coll_box),
pick_geom(o.pick_geom),obscurer(o.obscurer),captured(false){
	reset();
}

Object::~Object(){
	delete animator;
	velocity=Vector();
	updateSounds();
}

Object *Object::copy(){
	last_copy=clone()->getObject();
	for( Entity *e=children();e;e=e->successor() ){
		Object *cpy=e->getObject()->copy();
		cpy->setParent( last_copy );
	}
	if( animator ) last_copy->setAnimator( d_new Animator( animator ) );
	return last_copy;
}

void Object::reset(){
	colls.clear();
	velocity=Vector();
	prev_tform=getWorldTform();
}

void Object::setCollisionType( int type ){
	coll_type=type;
}

void Object::setCollisionRadii( const Vector &radii ){
	coll_radii=radii;
}

void Object::setCollisionBox( const Box &box ){
	coll_box=box;
}

void Object::setAnimator( Animator *t ){
	if( animator ) delete animator;
	animator=t;
}

void Object::beginUpdate( float e ){
	elapsed=e;
	colls.clear();
	animate( e );
}

void Object::animate( float e ){
	if( animator ) animator->update( e );
}

void Object::addCollision( const ObjCollision *c ){
	colls.push_back( c );
}

void Object::endUpdate(){
	velocity=(getWorldTform().v-prev_tform.v)/elapsed;
	prev_tform=getWorldTform();
}

void Object::capture(){
	capt_pos=getLocalPosition();
	capt_scl=getLocalScale();
	capt_rot=getLocalRotation();
	captured=true;
}

bool Object::beginRender( float tween ){
	updateSounds();
	if( tween==1 || !captured ){
		render_tform=getWorldTform();
		render_tform_valid=true;
	}else{
		Vector pos=(getLocalPosition()-capt_pos)*tween+capt_pos;
		Vector scl=(getLocalScale()-capt_scl)*tween+capt_scl;
		Quat rot=capt_rot.slerpTo( getLocalRotation(),tween );
		tween_tform.m=Matrix( rot );
		tween_tform.m.i*=scl.x;
		tween_tform.m.j*=scl.y;
		tween_tform.m.k*=scl.z;
		tween_tform.v=pos;
		render_tform_valid=false;
	}
	return true;
}

void Object::endRender(){
}

int Object::getCollisionType()const{
	return coll_type;
}

const Vector &Object::getCollisionRadii()const{
	return coll_radii;
}

const Box &Object::getCollisionBox()const{
	return coll_box;
}

const Vector &Object::getVelocity()const{
	return velocity;
}

const Object::Collisions &Object::getCollisions()const{
	return colls;
}

const Transform &Object::getRenderTform()const{
	if( render_tform_valid ) return render_tform;

	Object *parent=(Object*)getParent();
	render_tform=parent ? parent->getRenderTform() * tween_tform : tween_tform;
	render_tform_valid=true;

	return render_tform;
}

const Transform &Object::getPrevWorldTform()const{
	return prev_tform;
}

gxChannel *Object::emitSound( gxSound *sound ){
	if( !sound ) return 0;

	gxChannel *chan=sound->play3d( &getWorldTform().v.x,&velocity.x );
	for( int k=0;k<channels.size();++k ){
		if( chan==channels[k] ) return chan;
		if( !channels[k] ) return channels[k]=chan;
	}
	channels.push_back( chan );
	return chan;
}

void Object::updateSounds(){
	for( int k=0;k<channels.size();++k ){
		if( gxChannel *chan=channels[k] ){
			if( chan->isPlaying() )	chan->set3d( &getWorldTform().v.x,&velocity.x );
			else channels[k]=0;
		}
	}
}
