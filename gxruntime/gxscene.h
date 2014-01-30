
#ifndef GXSCENE_H
#define GXSCENE_H

#include <map>
#include <d3d.h>

#include "gxlight.h"

class gxCanvas;

class gxMesh;
class gxLight;
class gxGraphics;
class gxTexture;

class gxScene{
public:
	gxGraphics *graphics;
	IDirect3DDevice7 *dir3dDev;

	gxScene( gxGraphics *graphics,gxCanvas *target );
	~gxScene();

	void setEnabled( gxLight *light,bool enabled );

	/***** GX INTERFACE *****/
public:
	enum{
		MAX_TEXTURES=	8
	};
	enum{
		FX_FULLBRIGHT=	0x0001,
		FX_VERTEXCOLOR=	0x0002,
		FX_FLATSHADED=	0x0004,
		FX_NOFOG=		0x0008,
		FX_DOUBLESIDED=	0x0010,
		FX_VERTEXALPHA=	0x0020,

		FX_ALPHATEST=	0x2000,
		FX_CONDLIGHT=	0x4000,
		FX_EMISSIVE=	0x8000
	};
	enum{
		BLEND_REPLACE=	0,
		BLEND_ALPHA=	1,
		BLEND_MULTIPLY=	2,
		BLEND_ADD=		3,
		BLEND_DOT3=		4,
		BLEND_MULTIPLY2=5,
	};
	enum{
		ZMODE_NORMAL=	0,
		ZMODE_DISABLE=	1,
		ZMODE_CMPONLY=	2
	};
	enum{
		FOG_NONE=		0,
		FOG_LINEAR=		1
	};
	enum{
		TEX_COORDS2=	0x0001
	};
	struct Matrix{
		float elements[4][3];
	};
	struct RenderState{
		float color[3];
		float shininess,alpha;
		int blend,fx;
		struct TexState{
			gxCanvas *canvas;
			const Matrix *matrix;
			int blend,flags;
		}tex_states[MAX_TEXTURES];
	};
	
	//state
	int  hwTexUnits();
	int  gfxDriverCaps3D();

	void setWBuffer( bool enable );
	void setHWMultiTex( bool enable );
	void setDither( bool enable );
	void setAntialias( bool enable );
	void setWireframe( bool enable );
	void setFlippedTris( bool enable );
	void setAmbient( const float rgb[3] );
	void setAmbient2( const float rgb[3] );
	void setFogColor( const float rgb[3] );
	void setFogRange( float nr,float fr );
	void setFogMode( int mode );
	void setZMode( int mode );
	void setViewport( int x,int y,int w,int h );
	void setOrthoProj( float nr,float fr,float nr_w,float nr_h );
	void setPerspProj( float nr,float fr,float nr_w,float nr_h );
	void setViewMatrix( const Matrix *matrix );
	void setWorldMatrix( const Matrix *matrix );
	void setRenderState( const RenderState &state );

	//rendering
	bool begin( const std::vector<gxLight*> &lights );
	void clear( const float rgb[3],float alpha,float z,bool clear_argb,bool clear_z );
	void render( gxMesh *mesh,int first_vert,int vert_cnt,int first_tri,int tri_cnt );
	void end();

	//lighting
	gxLight *createLight( int flags );
	void freeLight( gxLight *l );

	//info
	int getTrianglesDrawn()const;

private:
	gxCanvas *target;
	bool wbuffer,dither,antialias,wireframe,flipped;
	unsigned ambient,ambient2,fogcolor;
	int caps_level,fogmode,zmode;
	float fogrange_nr,fogrange_fr;
	D3DVIEWPORT7 viewport;
	bool ortho_proj;
	float frustum_nr,frustum_fr,frustum_w,frustum_h;
	D3DMATRIX projmatrix,viewmatrix,worldmatrix;
	D3DMATRIX inv_viewmatrix;
	D3DMATERIAL7 material;
	float shininess;
	int blend,fx;
	struct TexState{
		gxCanvas *canvas;
		int blend,flags;
		D3DMATRIX matrix;
		bool mat_valid;
	};
	TexState texstate[MAX_TEXTURES];
	int n_texs,tris_drawn;

	std::set<gxLight*> _allLights;
	std::vector<gxLight*> _curLights;

	int d3d_rs[160];
	int d3d_tss[8][32];

	void setRS( int n,int t );
	void setTSS( int n,int s,int t );

	void setLights();
	void setZMode();
	void setAmbient();
	void setFogMode();
	void setTriCull();
	void setTexState( int index,const TexState &state,bool set_blend );
};

#endif