
#ifndef VAR_H
#define VAR_H

struct Type;

struct Var{

	int index;
	Type *type;

	Var( int i,Type *t ):index(i),type(t){}
};

#endif