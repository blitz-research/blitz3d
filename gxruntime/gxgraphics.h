
#ifndef GXGRAPHICS_H
#define GXGRAPHICS_H

#include <set>
#include <string>
#include <d3d.h>

#include "ddutil.h"

#include "gxfont.h"
#include "gxcanvas.h"
#include "gxscene.h"
#include "gxmesh.h"
#include "gxmovie.h"

class gxRuntime;

class gxGraphics{
public:
	IDirectDraw7 *dirDraw;
	IDirectDraw *ds_dirDraw;

	IDirect3D7 *dir3d;
	IDirect3DDevice7 *dir3dDev;
	D3DDEVICEDESC7 dir3dDevDesc;
	DDPIXELFORMAT primFmt,zbuffFmt;

	DDPIXELFORMAT texRGBFmt[2],texAlphaFmt[2],texRGBAlphaFmt[2],texRGBMaskFmt[2];

	gxGraphics( gxRuntime *runtime,IDirectDraw7 *dirDraw,IDirectDrawSurface7 *front,IDirectDrawSurface7 *back,bool d3d );
	~gxGraphics();

	void backup();
	bool restore();

	gxRuntime *runtime;

private:

	gxCanvas *front_canvas,*back_canvas;
	gxFont *def_font;
	bool gfx_lost;
	gxMesh *dummy_mesh;

	DDSURFACEDESC2 initDesc( int w,int h,int flags );
	ddSurf *createSurface( int width,int height,int flags );
	ddSurf *loadSurface( const std::string &f,int flags );

	std::set<gxFont*> font_set;
	std::set<gxCanvas*> canvas_set;
	std::set<gxMesh*> mesh_set;
	std::set<gxScene*> scene_set;
	std::set<gxMovie*> movie_set;
	std::set<std::string> font_res;

	DDGAMMARAMP _gammaRamp;
	IDirectDrawGammaControl *_gamma;

	/***** GX INTERFACE *****/
public:
	enum{
		GRAPHICS_WINDOWED=1,	//windowed mode
		GRAPHICS_SCALED=2,		//scaled window
		GRAPHICS_3D=4,			//3d mode! Hurrah!
		GRAPHICS_AUTOSUSPEND=8	//suspend graphics when app suspended
	};

	//MANIPULATORS
	void vwait();
	void flip( bool vwait );

	//SPECIAL!
	void copy( gxCanvas *dest,int dx,int dy,int dw,int dh,gxCanvas *src,int sx,int sy,int sw,int sh );

	//NEW! Gamma control!
	void setGamma( int r,int g,int b,float dr,float dg,float db );
	void getGamma( int r,int g,int b,float *dr,float *dg,float *db );
	void updateGamma( bool calibrate );

	//ACCESSORS
	int getWidth()const;
	int getHeight()const;
	int getDepth()const;
	int getScanLine()const;
	int getAvailVidmem()const;
	int getTotalVidmem()const;

	gxCanvas *getFrontCanvas()const;
	gxCanvas *getBackCanvas()const;
	gxFont *getDefaultFont()const;

	//OBJECTS
	gxCanvas *createCanvas( int width,int height,int flags );
	gxCanvas *loadCanvas( const std::string &file,int flags );
	gxCanvas *verifyCanvas( gxCanvas *canvas );
	void freeCanvas( gxCanvas *canvas );

	gxMovie *openMovie( const std::string &file,int flags );
	gxMovie *verifyMovie( gxMovie *movie );
	void closeMovie( gxMovie *movie );

	gxFont *loadFont( const std::string &font,int height,int flags );
	gxFont *verifyFont( gxFont *font );
	void freeFont( gxFont *font );

	gxScene *createScene( int flags );
	gxScene *verifyScene( gxScene *scene );
	void freeScene( gxScene *scene );

	gxMesh *createMesh( int max_verts,int max_tris,int flags );
	gxMesh *verifyMesh( gxMesh *mesh );
	void freeMesh( gxMesh *mesh );
};

#endif