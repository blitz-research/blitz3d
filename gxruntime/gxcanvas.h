
#ifndef GXCANVAS_H
#define GXCANVAS_H

#include "ddutil.h"

class gxFont;
class gxGraphics;

typedef IDirectDrawSurface7 ddSurf;

class gxCanvas{
public:
	gxCanvas( gxGraphics *graphics,ddSurf *surface,int flags );
	~gxCanvas();

	void backup()const;
	void restore()const;
	ddSurf *getSurface()const;
	ddSurf *getTexSurface()const;
	void setModify( int n );
	int getModify()const;

	bool attachZBuffer();
	void releaseZBuffer();

	bool clip( RECT *d )const;
	bool clip( RECT *d,RECT *s )const;
	void damage( const RECT &r )const;

private:
	int flags,cube_mode;
	gxGraphics *graphics;

	ddSurf *main_surf,*surf,*z_surf,*cube_surfs[6];

	mutable int mod_cnt;
	mutable ddSurf *t_surf;

	mutable int locked_pitch,locked_cnt,lock_mod_cnt,remip_cnt;
	mutable unsigned char *locked_surf;

	mutable int cm_pitch;
	mutable unsigned *cm_mask;

	RECT clip_rect;

	PixelFormat format;

	gxFont *font;
	RECT viewport;
	int origin_x,origin_y,handle_x,handle_y;
	unsigned mask_surf,color_surf,color_argb,clsColor_surf;

	void updateBitMask( const RECT &r )const;

	/***** GX INTERFACE *****/
public:
	enum{
		CANVAS_TEX_RGB=			0x0001,
		CANVAS_TEX_ALPHA=		0x0002,
		CANVAS_TEX_MASK=		0x0004,
		CANVAS_TEX_MIPMAP=		0x0008,
		CANVAS_TEX_CLAMPU=		0x0010,
		CANVAS_TEX_CLAMPV=		0x0020,
		CANVAS_TEX_SPHERE=		0x0040,
		CANVAS_TEX_CUBE=		0x0080,
		CANVAS_TEX_VIDMEM=		0x0100,
		CANVAS_TEX_HICOLOR=		0x0200,

		CANVAS_TEXTURE=			0x10000,
		CANVAS_NONDISPLAY=		0x20000,
		CANVAS_HIGHCOLOR=		0x40000
	};

	enum{
		CUBEMODE_REFLECTION=1,
		CUBEMODE_NORMAL=2,
		CUBEMODE_POSITION=3,

		CUBESPACE_WORLD=0,
		CUBESPACE_CAMERA=4
	};

	//MANIPULATORS
	void setFont( gxFont *font );
	void setMask( unsigned argb );
	void setColor( unsigned argb );
	void setClsColor( unsigned argb );
	void setOrigin( int x,int y );
	void setHandle( int x,int y );
	void setViewport( int x,int y,int w,int h );

	void cls();
	void plot( int x,int y );
	void line( int x,int y,int x2,int y2 );
	void rect( int x,int y,int w,int h,bool solid );
	void oval( int x,int y,int w,int h,bool solid );
	void text( int x,int y,const std::string &t );
	void blit( int x,int y,gxCanvas *src,int src_x,int src_y,int src_w,int src_h,bool solid );

	bool collide( int x,int y,const gxCanvas *src,int src_x,int src_y,bool solid )const;
	bool rect_collide( int x,int y,int rect_x,int rect_y,int rect_w,int rect_h,bool solid )const;

	bool lock()const;
	void setPixel( int x,int y,unsigned argb );
	void setPixelFast( int x,int y,unsigned argb ){
		format.setPixel( locked_surf+y*locked_pitch+x*format.getPitch(),argb );
		++mod_cnt;
	}
	void copyPixel( int x,int y,gxCanvas *src,int src_x,int src_y );
	void copyPixelFast( int x,int y,gxCanvas *src,int src_x,int src_y );
	unsigned getPixel( int x,int y )const;
	unsigned getPixelFast( int x,int y )const{
		return format.getPixel( locked_surf+y*locked_pitch+x*format.getPitch() );
	};
	void unlock()const;

	void setCubeMode( int mode );
	void setCubeFace( int face );

	//ACCESSORS
	int getWidth()const;
	int getHeight()const;
	int getDepth()const;
	int getFlags()const{ return flags; }
	int cubeMode()const{ return cube_mode; }
	void getOrigin( int *x,int *y )const;
	void getHandle( int *x,int *y )const;
	void getViewport( int *x,int *y,int *w,int *h )const;
	unsigned getMask()const;
	unsigned getColor()const;
	unsigned getClsColor()const;
};

#endif