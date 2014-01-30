
#ifndef BBGRAPHICS_H
#define BBGRAPHICS_H

#include "bbsys.h"
#include "../gxruntime/gxgraphics.h"

extern gxGraphics *gx_graphics;
extern gxCanvas *gx_canvas;
extern gxScene *gx_scene;

class bbImage;

//general graphics functions
int		 bbCountGfxDrivers();
BBStr *	 bbGfxDriverName( int n );
BBStr *	 bbGfxDriverDesc( int n );
void	 bbSetGfxDriver( int n );
int		 bbGfxModeExists( int w,int h,int d );
int		 bbCountGfxModes();
int		 bbGfxModeWidth( int n );
int		 bbGfxModeHeight( int n );
int		 bbGfxModeDepth( int n );
int		 bbGraphicsWidth();
int		 bbGraphicsHeight();
int		 bbGraphicsDepth();
int		 bbAvailVidMem();
int		 bbTotalVidMem();

//mode functions
void	 bbGraphics( int w,int h,int d,int mode );
gxCanvas * bbFrontBuffer();
gxCanvas * bbBackBuffer();
void	 bbEndGraphics();
int		 bbGraphicsLost();
int		 bbScanLine();
void	 bbVWait( int n );
void	 bbFlip( int vwait );

//graphics buffer functions
void	 bbSetBuffer( gxCanvas *buff );
gxCanvas * bbGraphicsBuffer();
int		 bbLoadBuffer( gxCanvas *surf,BBStr *str );
int		 bbSaveBuffer( gxCanvas *surf,BBStr *str );

//fast read/write operations...
void	 bbLockBuffer( gxCanvas *buff );
void	 bbUnlockBuffer( gxCanvas *buff );
int		 bbReadPixel( int x,int y,gxCanvas *buff );
void	 bbWritePixel( int x,int y,int argb,gxCanvas *buff );
int		 bbReadPixelFast( int x,int y,gxCanvas *buff );
void	 bbWritePixelFast( int x,int y,int argb,gxCanvas *buff );


//2d rendering functions
void	 bbOrigin( int x,int y );
void	 bbViewport( int x,int y,int w,int h );
void	 bbColor( int r,int g,int b );
void	 bbClsColor( int r,int g,int b );
void	 bbCls();
void	 bbPlot( int x,int y );
void	 bbLine( int x1,int y1,int x2,int y2 );
void	 bbRect( int x,int y,int w,int h,int solid );
void	 bbOval( int x,int y,int w,int h,int solid );
void	 bbText( int x,int y,BBStr *str,int centre_x,int centre_y );
void	 bbGetColor( int x,int y );
int		 bbColorRed();
int		 bbColorGreen();
int		 bbColorBlue();

//font functions
gxFont * bbLoadFont( BBStr *name,int height,int bold,int italic,int underline );
void	 bbFreeFont( gxFont *f );
void	 bbSetFont( gxFont *f );
int		 bbFontWidth();
int		 bbFontHeight();
int		 bbStringWidth( BBStr *str );
int		 bbStringHeight( BBStr *str );

//image functions
bbImage* bbLoadImage( BBStr *s );
bbImage* bbCopyImage( bbImage *i );
bbImage* bbCreateImage( int w,int h,int n );
bbImage* bbLoadAnimImage( BBStr *s,int w,int h,int first,int cnt );
void	 bbFreeImage( bbImage *i );
int		 bbSaveImage( bbImage *i,BBStr *filename,int frame );
void	 bbGrabImage( bbImage *i,int x,int y,int n );
gxCanvas * bbImageBuffer( bbImage *i,int n );
void	 bbDrawImage( bbImage *i,int x,int y,int frame );
void	 bbDrawBlock( bbImage *i,int x,int y,int frame );
void	 bbTileImage( bbImage *i,int x,int y,int frame );
void	 bbTileBlock( bbImage *i,int x,int y,int frame );
void	 bbDrawImageRect( bbImage *i,int x,int y,int r_x,int r_y,int r_w,int r_h,int frame );
void	 bbDrawBlockRect( bbImage *i,int x,int y,int r_x,int r_y,int r_w,int r_h,int frame );
void	 bbMaskImage( bbImage *i,int r,int g,int b );
void	 bbHandleImage( bbImage *i,int x,int y );
void	 bbScaleImage( bbImage *i,float w,float h );
void	 bbResizeImage( bbImage *i,float w,float h );
void	 bbRotateImage( bbImage *i,float angle );
void	 bbTFormImage( bbImage *i,float a,float b,float c,float d );
void	 bbTFormFilter( int enable );
void	 bbAutoMidHandle( int enable );
void	 bbMidHandle( bbImage *i );
int		 bbImageWidth( bbImage *i );
int		 bbImageHeight( bbImage *i );
int		 bbImageXHandle( bbImage *i );
int		 bbImageYHandle( bbImage *i );
int		 bbImagesOverlap( bbImage *i1,int x1,int y1,bbImage *i2,int x2,int y2 );
int		 bbImagesCollide( bbImage *i1,int x1,int y1,int f1,bbImage *i2,int x2,int y2,int f2 );
int		 bbRectsOverlap( int x1,int y1,int w1,int h1,int x2,int y2,int w2,int h2 );
int		 bbImageRectOverlap( bbImage *i,int x,int y,int r_x,int r_y,int r_w,int r_h );
int		 bbImageRectCollide( bbImage *i,int x,int y,int f,int r_x,int r_y,int r_w,int r_h );

//simple print functions
void	 bbWrite( BBStr *str );
void	 bbPrint( BBStr *str );
BBStr *	 bbInput( BBStr *prompt );
void	 bbLocate( int x,int y );

#endif