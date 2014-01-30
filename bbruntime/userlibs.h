
#ifndef USERLIBS_H
#define USERLIBS_H

#include "basic.h"

void		 _bbLoadLibs( char *p );

const char*	 _bbStrToCStr( BBStr *str );
BBStr*		 _bbCStrToStr( const char *str );

#endif
