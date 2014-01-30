
#ifndef DDUTIL_H
#define DDUTIL_H

#include <ddraw.h>

class gxGraphics;
typedef IDirectDrawSurface7 ddSurf;

struct ddUtil{

	static void buildMipMaps( ddSurf *surf );
	static void copy( ddSurf *dest,int dx,int dy,int dw,int dh,ddSurf *src,int sx,int sy,int sw,int sh );
	static ddSurf *loadSurface( const std::string &f,int flags,gxGraphics *gfx );
	static ddSurf *createSurface( int width,int height,int flags,gxGraphics *gfx );
};

class PixelFormat{
	int depth,pitch;
	unsigned amask,rmask,gmask,bmask,argbfill;
	unsigned char ashr,ashl,rshr,rshl,gshr,gshl,bshr,bshl;
	typedef void (_fastcall *Plot)(void *pix,unsigned argb);
	typedef unsigned (_fastcall *Point)(void *pix);
	Plot plot;
	Point point;

	char *plot_code,*point_code;

public:
	PixelFormat():plot_code(0){
	}

	PixelFormat( const DDPIXELFORMAT &pf ):plot_code(0){
		setFormat( pf );
	}

	~PixelFormat();

	void setFormat( const DDPIXELFORMAT &pf );

	int getDepth()const{
		return depth; 
	}
	int getPitch()const{ 
		return pitch; 
	}
	unsigned fromARGB( unsigned n )const{
		return ( (n>>ashr<<ashl)&amask ) | ( (n>>rshr<<rshl)&rmask ) | ( (n>>gshr<<gshl)&gmask ) | ( (n>>bshr<<bshl)&bmask );
	}
	unsigned toARGB( unsigned n )const{
		return ( (n&amask)>>ashl<<ashr ) | ( (n&rmask)>>rshl<<rshr ) | ( (n&gmask)>>gshl<<gshr ) | ( (n&bmask)>>bshl<<bshr ) | argbfill;
	}
	void setPixel( void *p,unsigned n )const{ plot(p,n); }
	unsigned getPixel( void *p )const{ return point(p); }
};

#endif