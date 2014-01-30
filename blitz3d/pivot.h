
#ifndef PIVOT_H
#define PIVOT_H

#include "object.h"

class Pivot : public Object{
public:
	Pivot();
	Pivot( const Object &t );

	//Entity interface
	Entity *clone(){ return d_new Pivot( *this ); }
};

#endif