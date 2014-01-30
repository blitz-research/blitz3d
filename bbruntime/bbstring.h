
#ifndef BBSTRING_H
#define BBSTRING_H

#include "basic.h"

BBStr *  bbString( BBStr *s,int n );
BBStr *	 bbLeft( BBStr *s,int n );
BBStr *	 bbRight( BBStr *s,int n );
BBStr *  bbReplace( BBStr *s,BBStr *from,BBStr *to );
int		 bbInstr( BBStr *s,BBStr *t,int from );
BBStr *	 bbMid( BBStr *s,int o,int n );
BBStr *  bbUpper( BBStr *s );
BBStr *	 bbLower( BBStr *s );
BBStr *  bbTrim( BBStr *s );
BBStr *  bbLSet( BBStr *s,int n );
BBStr *  bbRSet( BBStr *s,int n );
BBStr *  bbChr( int n );
int		 bbAsc( BBStr *s );
int		 bbLen( BBStr *s );
BBStr *	 bbHex( int n );
BBStr *  bbBin( int n );
BBStr *  bbCurrentDate();
BBStr *  bbCurrentTime();

#endif