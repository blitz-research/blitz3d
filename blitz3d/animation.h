
#ifndef ANIMATION_H
#define ANIMATION_H

#include <list>

#include "geom.h"

class Animation{
public:
	Animation();
	Animation( const Animation &t );
	Animation( const Animation &t,int first,int last );
	~Animation();

	Animation &operator=( const Animation &t );

	void setScaleKey( int frame,const Vector &q );
	void setPositionKey( int frame,const Vector &p );
	void setRotationKey( int frame,const Quat &q );

	int numScaleKeys()const;
	int numRotationKeys()const;
	int numPositionKeys()const;

	Vector getScale( float time )const;
	Vector getPosition( float time )const;
	Quat getRotation( float time )const;

private:
	struct Rep;
	Rep *rep;

	Rep *write();
};

#endif