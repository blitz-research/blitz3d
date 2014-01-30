
#ifndef ASSEM_H
#define ASSEM_H

#include "..\linker\linker.h"

class Assem{
public:
	istream &in;
	Module *mod;
	Assem( istream &in,Module *mod ):in( in ),mod( mod ){}
	virtual void assemble()=0;
};

#endif