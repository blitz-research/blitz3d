
#include "std.h"
#include "gxcanvas.h"
#include "gxgraphics.h"
#include "gxruntime.h"
#include "asmcoder.h"

#define DEBUG_BITMASK

static int canvas_cnt;
static DDBLTFX bltfx={sizeof(DDBLTFX)};

extern gxRuntime *gx_runtime;

static unsigned FWMS[]={
	0xffffffff,0x7fffffff,0x3fffffff,0x1fffffff,
	0x0fffffff,0x07ffffff,0x03ffffff,0x01ffffff,
	0x00ffffff,0x007fffff,0x003fffff,0x001fffff,
	0x000fffff,0x0007ffff,0x0003ffff,0x0001ffff,
	0x0000ffff,0x00007fff,0x00003fff,0x00001fff,
	0x00000fff,0x000007ff,0x000003ff,0x000001ff,
	0x000000ff,0x0000007f,0x0000003f,0x0000001f,
	0x0000000f,0x00000007,0x00000003,0x00000001};
static unsigned LWMS[]={
	0x80000000,0xc0000000,0xe0000000,0xf0000000,
	0xf8000000,0xfc000000,0xfe000000,0xff000000,
	0xff800000,0xffc00000,0xffe00000,0xfff00000,
	0xfff80000,0xfffc0000,0xfffe0000,0xffff0000,
	0xffff8000,0xffffc000,0xffffe000,0xfffff000,
	0xfffff800,0xfffffc00,0xfffffe00,0xffffff00,
	0xffffff80,0xffffffc0,0xffffffe0,0xfffffff0,
	0xfffffff8,0xfffffffc,0xfffffffe,0xffffffff};

static void calcShifts( unsigned mask,unsigned char *shr,unsigned char *shl ){
	if( mask ){
		for( *shl=0;!(mask&1);++*shl,mask>>=1 ){}
		for( *shr=8;mask&1;--*shr,mask>>=1 ){}
	}else *shr=*shl=0;
}

struct Rect : public RECT{
	Rect(){
	}
	Rect( int x,int y,int w,int h ){
		left=x;top=y;right=x+w;bottom=y+h;
	}
};

static bool clip( const RECT &viewport,RECT *d ){
	if( d->right<=d->left ||
		d->bottom<=d->top ||
		d->left>=viewport.right ||
		d->right<=viewport.left ||
		d->top>=viewport.bottom ||
		d->bottom<=viewport.top ) return false;
	if( d->left<viewport.left ) d->left=viewport.left;
	if( d->right>viewport.right ) d->right=viewport.right;
	if( d->top<viewport.top ) d->top=viewport.top;
	if( d->bottom>viewport.bottom ) d->bottom=viewport.bottom;
	return true;
}

static bool clip( const RECT &viewport,RECT *d,RECT *s ){
	if( d->right<=d->left ||
		d->bottom<=d->top ||
		d->left>=viewport.right ||
		d->right<=viewport.left ||
		d->top>=viewport.bottom ||
		d->bottom<=viewport.top ) return false;
	int dx,dy;
	if( (dx=viewport.left-d->left)>0 ){ d->left+=dx;s->left+=dx; }
	if( (dx=viewport.right-d->right)<0 ){ d->right+=dx;s->right+=dx; }
	if( (dy=viewport.top-d->top)>0 ){ d->top+=dy;s->top+=dy; }
	if( (dy=viewport.bottom-d->bottom)<0 ){ d->bottom+=dy;s->bottom+=dy; }
	return true;
}

gxCanvas::gxCanvas( gxGraphics *g,IDirectDrawSurface7 *s,int f ):
graphics(g),main_surf(s),surf(0),z_surf(0),flags(f),cube_mode(CUBEMODE_REFLECTION|CUBESPACE_WORLD),
t_surf(0),cm_mask(0),locked_cnt(0),mod_cnt(0),remip_cnt(0){

	if( flags & CANVAS_TEX_CUBE ){
		cube_surfs[2]=main_surf;
		for( int k=0;k<6;++k ){
			if( k==2 ) continue;
			DWORD n;
			switch( k ){
			case 0:n=DDSCAPS2_CUBEMAP_NEGATIVEX;break;
			case 1:n=DDSCAPS2_CUBEMAP_POSITIVEZ;break;
			case 2:n=DDSCAPS2_CUBEMAP_POSITIVEX;break;
			case 3:n=DDSCAPS2_CUBEMAP_NEGATIVEZ;break;
			case 4:n=DDSCAPS2_CUBEMAP_POSITIVEY;break;
			case 5:n=DDSCAPS2_CUBEMAP_NEGATIVEY;break;
			default:return;
			}
			DDSCAPS2 caps={0};
			caps.dwCaps2=DDSCAPS2_CUBEMAP|n;
			main_surf->GetAttachedSurface( &caps,&cube_surfs[k] );
		}
		surf=cube_surfs[1];
	}else{
		surf=main_surf;
		memset( cube_surfs,0,sizeof(cube_surfs) );
	}

	DDSURFACEDESC2 desc={sizeof(desc)};
	surf->GetSurfaceDesc( &desc );
	format.setFormat( desc.ddpfPixelFormat );

	clip_rect.left=clip_rect.top=0;
	clip_rect.right=desc.dwWidth;
	clip_rect.bottom=desc.dwHeight;
	cm_pitch=(clip_rect.right+31)/32+1;
	setMask( 0 );
	setColor( ~0 );
	setClsColor( 0 );
	setOrigin( 0,0 );
	setHandle( 0,0 );
	setFont( graphics->getDefaultFont() );
	setViewport( 0,0,getWidth(),getHeight() );
	if( flags & gxCanvas::CANVAS_TEXTURE ) ddUtil::buildMipMaps( surf );
}

gxCanvas::~gxCanvas(){
	delete[] cm_mask;
	if( locked_cnt ) surf->Unlock( 0 );
	if( t_surf ) t_surf->Release();
	releaseZBuffer();
	main_surf->Release();
}

void gxCanvas::backup()const{
	if( flags & CANVAS_TEX_CUBE ) return;

	if( !t_surf ){
		DDSURFACEDESC2 desc={sizeof(desc)};
		if( surf->GetSurfaceDesc(&desc)<0 ) return;
		if( desc.ddsCaps.dwCaps & DDSCAPS_SYSTEMMEMORY ) return;

		DDSURFACEDESC2 t_desc={sizeof(t_desc)};
		t_desc.dwFlags=DDSD_CAPS|DDSD_PIXELFORMAT|DDSD_WIDTH|DDSD_HEIGHT;
		t_desc.ddsCaps.dwCaps=DDSCAPS_OFFSCREENPLAIN|DDSCAPS_SYSTEMMEMORY;
		t_desc.dwWidth=desc.dwWidth;t_desc.dwHeight=desc.dwHeight;
		t_desc.ddpfPixelFormat=desc.ddpfPixelFormat;

		if( graphics->dirDraw->CreateSurface( &t_desc,&t_surf,0 )<0 ){
			t_surf=0;
			return;
		}
	}

	if( t_surf->Blt( 0,surf,0,DDBLT_WAIT,0 )<0 ) return;
}

void gxCanvas::restore()const{
	if( !t_surf ) return;

	if( surf->Blt( 0,t_surf,0,DDBLT_WAIT,0 )<0 ) return;
}

ddSurf *gxCanvas::getSurface()const{
	return surf;
}

ddSurf *gxCanvas::getTexSurface()const{
	if( mod_cnt==remip_cnt ) return main_surf;
	ddUtil::buildMipMaps( surf );
	remip_cnt=mod_cnt;
	return main_surf;
}

bool gxCanvas::clip( RECT *d )const{
	return ::clip( viewport,d );
}

bool gxCanvas::clip( RECT *d,RECT *s )const{
	return ::clip( viewport,d,s );
}

void gxCanvas::updateBitMask( const RECT &r )const{

	int w=r.right-r.left;if( w<=0 ) return;
	int h=r.bottom-r.top;if( h<=0 ) return;

	lock();
	RECT t=r;
	t.left&=~31;
	t.right=(t.right+31)&~31;
	w=(t.right-t.left)/32;
	unsigned char *src_row=locked_surf+t.top*locked_pitch+t.left*format.getPitch();
	unsigned *dest_row=cm_mask+t.top*cm_pitch+t.left/32;
	unsigned mask_argb=format.toARGB( mask_surf ) & 0xffffff;

#ifdef DEBUG_BITMASK
	unsigned *cm_mask_end=cm_mask+cm_pitch*clip_rect.bottom;
#endif

	while( h-- ){
		unsigned *dest=dest_row;
		unsigned char *src=src_row;
		for( int c=0;c<w;++c ){
			unsigned mask=0;
			for( int x=0;x<32;++x ){
				unsigned pix=format.getPixel(src) & 0xffffff;
				mask=(mask<<1)|(pix!=mask_argb);
				src+=format.getPitch();
			}
#ifdef DEBUG_BITMASK
			if( dest<cm_mask || dest>=cm_mask_end ){
				gx_runtime->debugError( "gxCanvas::updateBitMask dest out of range" );
			}
#endif
			*dest++=mask;
		}
		dest_row+=cm_pitch;
		src_row+=locked_pitch;
	}
	unlock();
}

void gxCanvas::setModify( int n ){
	mod_cnt=n;
}

int gxCanvas::getModify()const{
	return mod_cnt;
}

bool gxCanvas::attachZBuffer(){
	if( z_surf ) return true;
	DDSURFACEDESC2 desc={sizeof(desc)};
	desc.dwFlags=DDSD_WIDTH|DDSD_HEIGHT|DDSD_CAPS|DDSD_PIXELFORMAT;
	desc.dwWidth=getWidth();
	desc.dwHeight=getHeight();
	desc.ddsCaps.dwCaps=DDSCAPS_ZBUFFER|DDSCAPS_VIDEOMEMORY;
	desc.ddpfPixelFormat=graphics->zbuffFmt;
	if( graphics->dirDraw->CreateSurface( &desc,&z_surf,0 )<0 ) return false;
	surf->AddAttachedSurface( z_surf );
	return true;
}

void gxCanvas::releaseZBuffer(){
	if( !z_surf ) return;
	surf->DeleteAttachedSurface( 0,z_surf );
	z_surf->Release();
	z_surf=0;
}

void gxCanvas::damage( const RECT &r )const{
	++mod_cnt;if( cm_mask ) updateBitMask( r );
}

void gxCanvas::setFont( gxFont *f ){
	font=f;
}

void gxCanvas::setMask( unsigned argb ){
	mask_surf=format.fromARGB( argb );
}

void gxCanvas::setColor( unsigned argb ){
	argb|=0xff000000;
	color_argb=argb;
	color_surf=format.fromARGB( argb );
}

void gxCanvas::setClsColor( unsigned argb ){
	argb|=0xff000000;
	clsColor_surf=format.fromARGB( argb );
}

void gxCanvas::setOrigin( int x,int y ){
	origin_x=x;origin_y=y;
}

void gxCanvas::setHandle( int x,int y ){
	handle_x=x;handle_y=y;
}

void gxCanvas::setViewport( int x,int y,int w,int h ){
	Rect r( x,y,w,h );
	if( !::clip( clip_rect,&r ) ) r=Rect(0,0,0,0);
	viewport=r;
}

//renderering primitives
void gxCanvas::cls(){
	bltfx.dwFillColor=clsColor_surf;
	surf->Blt( &viewport,0,0,DDBLT_WAIT|DDBLT_COLORFILL,&bltfx );
	damage( viewport );
}

void gxCanvas::plot( int x,int y ){
	x+=origin_x;if( x<viewport.left || x>=viewport.right ) return;
	y+=origin_y;if( y<viewport.top || y>=viewport.bottom ) return;
	bltfx.dwFillColor=color_surf;
	Rect dest( x,y,1,1 );
	surf->Blt( &dest,0,0,DDBLT_WAIT|DDBLT_COLORFILL,&bltfx );
	damage( dest );
}

void gxCanvas::line( int x0,int y0,int x1,int y1 ){
	int ddf,padj,sadj;
	int dx,dy,sx,sy,ax,ay;

	x0+=origin_x;y0+=origin_y;
	x1+=origin_x;y1+=origin_y;

	int cx0,cx1,cy0,cy1,clip0,clip1;

	cx0=viewport.left;
	cx1=viewport.right-1;
	cy0=viewport.top;
	cy1=viewport.bottom-1;

	while( true ){
		clip0=0;clip1=0;
		
		if(y0>cy1)clip0|=1;else if(y0<cy0)clip0|=2;
		if(x0>cx1)clip0|=4;else if(x0<cx0)clip0|=8;
		if(y1>cy1)clip1|=1;else if(y1<cy0)clip1|=2;
		if(x1>cx1)clip1|=4;else if(x1<cx0)clip1|=8;

		if((clip0|clip1)==0) break;		//draw line
		if((clip0&clip1)!=0) return;	//outside

		if((clip0&1)==1) {x0=x0+((x1-x0)*(cy1-y0))/(y1-y0);y0=cy1;continue;}
		if((clip0&2)==2) {x0=x0+((x1-x0)*(cy0-y0))/(y1-y0);y0=cy0;continue;}
		if((clip0&4)==4) {y0=y0+((y1-y0)*(cx1-x0))/(x1-x0);x0=cx1;continue;}
		if((clip0&8)==8) {y0=y0+((y1-y0)*(cx0-x0))/(x1-x0);x0=cx0;continue;}

		if((clip1&1)==1) {x1=x0+((x1-x0)*(cy1-y0))/(y1-y0);y1=cy1;continue;}
		if((clip1&2)==2) {x1=x0+((x1-x0)*(cy0-y0))/(y1-y0);y1=cy0;continue;}
		if((clip1&4)==4) {y1=y0+((y1-y0)*(cx1-x0))/(x1-x0);x1=cx1;continue;}
		if((clip1&8)==8) {y1=y0+((y1-y0)*(cx0-x0))/(x1-x0);x1=cx0;continue;}
	}

	dx=x1-x0;dy=y1-y0;
	if( (dx|dy)==0 ){
		setPixel( x0,y0,color_argb );
		return;
	}

	if (dx>=0) {sx=1;ax=dx;} else {sx=-1;ax=-dx;}
	if (dy>=0) {sy=1;ay=dy;} else {sy=-1;ay=-dy;}

	lock();
	if( ax>ay ){
		ddf=-ax;sadj=ax+ax;padj=ay+ay;
		while( ax-->=0 ){
			setPixelFast( x0,y0,color_argb );
			x0+=sx;ddf+=padj;if( ddf>=0 ){ y0+=sy;ddf-=sadj; }
		}
	}else{
		ddf=-ay;sadj=ay+ay;padj=ax+ax;
		while( ay-->=0 ){
			setPixelFast( x0,y0,color_argb );
			y0+=sy;ddf+=padj;if( ddf>=0 ){ x0+=sx;ddf-=sadj; }
		}
	
	}
	unlock();
}

void gxCanvas::rect( int x,int y,int w,int h,bool solid ){
	x+=origin_x;y+=origin_y;
	Rect dest( x,y,w,h );
	if( !clip( &dest ) ) return;

	bltfx.dwFillColor=color_surf;

	if( solid ){
		surf->Blt( &dest,0,0,DDBLT_WAIT|DDBLT_COLORFILL,&bltfx );
		damage( dest );
		return;
	}
	Rect r1( x,y,w,1 );if( clip( &r1 ) ){
		surf->Blt( &r1,0,0,DDBLT_WAIT|DDBLT_COLORFILL,&bltfx );
	}
	Rect r2( x,y,1,h );if( clip( &r2 ) ){
		surf->Blt( &r2,0,0,DDBLT_WAIT|DDBLT_COLORFILL,&bltfx );
	}
	Rect r3( x+w-1,y,1,h );if( clip( &r3 ) ){
		surf->Blt( &r3,0,0,DDBLT_WAIT|DDBLT_COLORFILL,&bltfx );
	}
	Rect r4( x,y+h-1,w,1 );if( clip( &r4 ) ){
		surf->Blt( &r4,0,0,DDBLT_WAIT|DDBLT_COLORFILL,&bltfx );
	}
	damage( dest );
}

void gxCanvas::oval( int x1,int y1,int w,int h,bool solid ){
	x1+=origin_x;y1+=origin_y;
	Rect dest( x1,y1,w,h );
	if( !clip( &dest ) ) return;

	bltfx.dwFillColor=color_surf;

	float xr=w*.5f,yr=h*.5f,ar=(float)w/(float)h;
	float cx=x1+xr+.5f,cy=y1+yr-.5f,rsq=yr*yr,y;

	if( solid ){
		y=dest.top-cy;
		for( int t=dest.top;t<dest.bottom;++y,++t ){
			float x=sqrt( rsq-y*y )*ar;
			int xa=floor( cx-x ),xb=floor( cx+x );
			if( xb<=xa || xa>=viewport.right || xb<=viewport.left ) continue;
			Rect dr;dr.top=t;dr.bottom=t+1;
			dr.left=xa<viewport.left ? viewport.left : xa;
			dr.right=xb>viewport.right ? viewport.right : xb;
			surf->Blt( &dr,0,0,DDBLT_WAIT|DDBLT_COLORFILL,&bltfx );
		}
		damage( dest );
		return;
	}

	int p_xa,p_xb,t,hh=floor(cy);

	p_xa=p_xb=cx;
	t=dest.top;y=t-cy;
	if( dest.top>y1 ){ --t;--y; }
	for( ;t<=hh;++y,++t ){
		float x=sqrt( rsq-y*y )*ar;
		int xa=floor( cx-x ),xb=floor( cx+x );
		Rect r1( xa,t,p_xa-xa,1 );if( r1.right<=r1.left ) r1.right=r1.left+1;
		if( clip( &r1 ) ) surf->Blt( &r1,0,0,DDBLT_WAIT|DDBLT_COLORFILL,&bltfx );
		Rect r2( p_xb,t,xb-p_xb,1 );if( r2.left>=r2.right ) r2.left=r2.right-1;
		if( clip( &r2 ) ) surf->Blt( &r2,0,0,DDBLT_WAIT|DDBLT_COLORFILL,&bltfx );
		p_xa=xa;p_xb=xb;
	}

	p_xa=p_xb=cx;
	t=dest.bottom-1;y=t-cy;
	if( dest.bottom<y1+h ){ ++t;++y; }
	for( ;t>hh;--y,--t ){
		float x=sqrt( rsq-y*y )*ar;
		int xa=floor( cx-x ),xb=floor( cx+x );
		Rect r1( xa,t,p_xa-xa,1 );if( r1.right<=r1.left ) r1.right=r1.left+1;
		if( clip( &r1 ) ) surf->Blt( &r1,0,0,DDBLT_WAIT|DDBLT_COLORFILL,&bltfx );
		Rect r2( p_xb,t,xb-p_xb,1 );if( r2.left>=r2.right ) r2.left=r2.right-1;
		if( clip( &r2 ) ) surf->Blt( &r2,0,0,DDBLT_WAIT|DDBLT_COLORFILL,&bltfx );
		p_xa=xa;p_xb=xb;
	}
	damage( dest );
}

void gxCanvas::blit( int x,int y,gxCanvas *src,int src_x,int src_y,int src_w,int src_h,bool solid ){
	x+=origin_x-src->handle_x;
	y+=origin_y-src->handle_y;
	Rect dest_r( x,y,src_w,src_h ),src_r( src_x,src_y,src_w,src_h );
	if( !clip( &dest_r,&src_r ) ) return;
	if( !::clip( src->clip_rect,&src_r,&dest_r ) ) return;

	if( solid ){
		surf->Blt( &dest_r,src->surf,&src_r,DDBLT_WAIT,0 );
	}else{
		bltfx.ddckSrcColorkey.dwColorSpaceLowValue=
		bltfx.ddckSrcColorkey.dwColorSpaceHighValue=src->mask_surf;
		surf->Blt( &dest_r,src->surf,&src_r,DDBLT_WAIT|DDBLT_KEYSRCOVERRIDE,&bltfx );
	}
	damage( dest_r );
}

void gxCanvas::text( int x,int y,const string &t ){

	int ty=y+origin_y;
	if( ty>=viewport.bottom ) return;
	if( ty+font->getHeight()<=viewport.top ) return;

	int tx=x+origin_x;
	if( tx>=viewport.right ) return;

	int b=0,w;
	while( b<t.size() && tx+(w=font->charWidth( t[b] ))<=viewport.left ){
		tx+=w;x+=w;++b;
	}
	int e=b;
	while( e<t.size() && tx<viewport.right ){
		tx+=font->charWidth( t[e] );++e;
	}

	if( e>b ) font->render( this,format.toARGB( color_surf ),x,y,t.substr( b,e-b ) );
}

int gxCanvas::getWidth()const{
	return clip_rect.right;
}

int gxCanvas::getHeight()const{
	return clip_rect.bottom;
}

int gxCanvas::getDepth()const{
	return format.getDepth();
}

void gxCanvas::getOrigin( int *x,int *y )const{
	*x=origin_x;*y=origin_y;
}

void gxCanvas::getHandle( int *x,int *y )const{
	*x=handle_x;*y=handle_y;
}

void gxCanvas::getViewport( int *x,int *y,int *w,int *h )const{
	*x=viewport.left;*y=viewport.top;
	*w=viewport.right-viewport.left;*h=viewport.bottom-viewport.top;
}

unsigned gxCanvas::getMask()const{
	return format.toARGB( mask_surf );
}

unsigned gxCanvas::getColor()const{
	return format.toARGB( color_surf );
}

unsigned gxCanvas::getClsColor()const{
	return format.toARGB( clsColor_surf );
}

bool gxCanvas::collide( int x1,int y1,const gxCanvas *i2,int x2,int y2,bool solid )const{

	x1-=handle_x;x2-=i2->handle_x;
	if( x1+clip_rect.right<=x2 || x1>=x2+i2->clip_rect.right ) return false;
	y1-=handle_y;y2-=i2->handle_y;
	if( y1+clip_rect.bottom<=y2 || y1>=y2+i2->clip_rect.bottom ) return false;

	if( solid ) return true;

	if( !cm_mask ){
		cm_mask=d_new unsigned[cm_pitch*clip_rect.bottom];
		updateBitMask( clip_rect );
	}
	if( !i2->cm_mask ){
		i2->cm_mask=d_new unsigned[i2->cm_pitch*i2->clip_rect.bottom];
		i2->updateBitMask( i2->clip_rect );
	}

	const gxCanvas *i1=this;

	//to keep me sane!
	if( x1>x2 ){
		std::swap( x1,x2 );
		std::swap( y1,y2 );
		std::swap( i1,i2 );
	}

	Rect r1,r2,ir;
	r1.left=x1;r1.top=y1;r1.right=x1+i1->clip_rect.right;r1.bottom=y1+i1->clip_rect.bottom;
	r2.left=x2;r2.top=y2;r2.right=x2+i2->clip_rect.right;r2.bottom=y2+i2->clip_rect.bottom;
	ir.left=r1.left>r2.left ? r1.left : r2.left;
	ir.right=r1.right<r2.right ? r1.right : r2.right;
	ir.top=r1.top>r2.top ? r1.top : r2.top;
	ir.bottom=r1.bottom<r2.bottom ? r1.bottom : r2.bottom;

	unsigned *s1=i1->cm_mask,*s2=i2->cm_mask;
	int i1_pitch=i1->cm_pitch,i2_pitch=i2->cm_pitch;
	s1+=(ir.top-r1.top)*i1_pitch;
	s2+=(ir.top-r2.top)*i2_pitch;

	int startx=ir.left-r1.left;
	int stopx=ir.right-r1.left-1;
	int shr=startx&31;
	int shl=32-shr;
	int cnt=stopx/32-startx/32;
	unsigned lwm=LWMS[stopx&31];

#ifdef DEBUG_BITMASK
	unsigned *cm_mask_end1=i1->cm_mask + i1_pitch*i1->clip_rect.bottom;
	unsigned *cm_mask_end2=i2->cm_mask + i2_pitch*i2->clip_rect.bottom;
#endif

	s1+=startx/32;
	for( int y=ir.top;y<ir.bottom;++y ){
		unsigned p=0;
		unsigned *row1=s1,*row2=s2;
		for( int x=0;x<cnt;++x ){
#ifdef DEBUG_BITMASK
			if( row1<i1->cm_mask || row2<i2->cm_mask ){
				gx_runtime->debugError( "gxCanvas::collide row underflow" );
			}
			if( row1>=cm_mask_end1 || row2>=cm_mask_end2 ){
				gx_runtime->debugError( "gxCanvas::collide row overflow" );
			}
#endif
			unsigned n=*row2++;
			if( ((n>>shr)|p) & *row1++ ) return true;
			p=shl<32 ? n<<shl : 0;
		}
#ifdef DEBUG_BITMASK
		if( row1<i1->cm_mask || row2<i2->cm_mask ){
			gx_runtime->debugError( "gxCanvas::collide row underflow" );
		}
		if( row1>=cm_mask_end1 || row2>=cm_mask_end2 ){
			gx_runtime->debugError( "gxCanvas::collide row overflow" );
		}
#endif
		if( ((*row2>>shr)|p) & *row1 & lwm ) return true;
		s1+=i1_pitch;s2+=i2_pitch;
	}
	return false;
}

bool gxCanvas::rect_collide( int x1,int y1,int x2,int y2,int w2,int h2,bool solid )const{

	x1-=handle_x;if( x1+clip_rect.right<=x2 || x1>=x2+w2 ) return false;
	y1-=handle_y;if( y1+clip_rect.bottom<=y2 || y1>=y2+h2 ) return false;

	if( solid ) return true;

	Rect r1( x1,y1,clip_rect.right,clip_rect.bottom ),r2( x2,y2,w2,h2 ),ir;

	ir.left=r1.left>r2.left ? r1.left : r2.left;
	ir.right=r1.right<r2.right ? r1.right : r2.right;
	ir.top=r1.top>r2.top ? r1.top : r2.top;
	ir.bottom=r1.bottom<r2.bottom ? r1.bottom : r2.bottom;

	if( !cm_mask ){
		cm_mask=d_new unsigned[cm_pitch*clip_rect.bottom];
		updateBitMask( clip_rect );
	}

	unsigned *s1=cm_mask+(ir.top-r1.top)*cm_pitch;

	int startx=ir.left-r1.left;
	int stopx=ir.right-r1.left-1;
	int cnt=stopx/32-startx/32;
	unsigned fwm=FWMS[startx&31];
	unsigned lwm=LWMS[stopx&31];

	if( !cnt ) {fwm&=lwm;lwm=0;}

	s1+=startx/32;
	for( int h=ir.top;h<ir.bottom;++h ){
		unsigned *row=s1;
		if( *row & fwm ) return true;
		for( int x=1;x<cnt;++x ){
			if( *++row ) return true;
		}
		if( lwm && (*++row & lwm) ) return true;
		s1+=cm_pitch;
	}
	return false;
}

bool gxCanvas::lock()const{
	if( !locked_cnt++ ){
		DDSURFACEDESC2 desc={sizeof(desc)};
		if( surf->Lock( 0,&desc,DDLOCK_WAIT|DDLOCK_NOSYSLOCK,0 )<0 ){
			--locked_cnt;
			return false;
		}
		locked_pitch=desc.lPitch;
		locked_surf=(unsigned char*)desc.lpSurface;
		lock_mod_cnt=mod_cnt;
	}
	return true;
}

void gxCanvas::unlock()const{
	if( locked_cnt==1 ){
		if( lock_mod_cnt!=mod_cnt && cm_mask ) updateBitMask( clip_rect );
		surf->Unlock( 0 );
	}
	--locked_cnt;
}

void gxCanvas::setPixel( int x,int y,unsigned argb ){
	x+=origin_x;if( x<viewport.left || x>=viewport.right ) return;
	y+=origin_y;if( y<viewport.top || y>=viewport.bottom ) return;
	lock();
	setPixelFast( x,y,argb );
	unlock();
}

unsigned gxCanvas::getPixel( int x,int y )const{
	x+=origin_x;if( x<viewport.left || x>=viewport.right ) return format.toARGB( mask_surf );
	y+=origin_y;if( y<viewport.top || y>=viewport.bottom ) return format.toARGB( mask_surf );
	lock();
	unsigned p=getPixelFast( x,y );
	unlock();
	return p;
}

void gxCanvas::copyPixelFast( int x,int y,gxCanvas *src,int src_x,int src_y ){
	switch( format.getDepth() ){
	case 16:
		*(short*)(locked_surf+y*locked_pitch+x*2)=
		*(short*)(src->locked_surf+src_y*src->locked_pitch+src_x*2);
		break;
	case 24:{
		unsigned char *p=locked_surf+y*locked_pitch+x*3;
		unsigned char *t=src->locked_surf+src_y*src->locked_pitch+src_x*3;
		*(short*)p=*(short*)t;*(char*)(p+2)=*(char*)(t+2);}
		break;
	case 32:
		*(int*)(locked_surf+y*locked_pitch+x*4)=
		*(int*)(src->locked_surf+src_y*src->locked_pitch+src_x*4);
		break;
	}
}

void gxCanvas::copyPixel( int x,int y,gxCanvas *src,int src_x,int src_y ){
	x+=origin_x;if( x<viewport.left || x>=viewport.right ) return;
	y+=origin_y;if( y<viewport.top || y>=viewport.bottom ) return;
	src_x+=src->origin_x;if( src_x<src->viewport.left || src_x>=src->viewport.right ) return;
	src_y+=src->origin_y;if( src_y<src->viewport.top || src_y>=src->viewport.bottom ) return;
	lock();
	src->lock();
	copyPixelFast( x,y,src,src_x,src_y );
	src->unlock();
	unlock();
}

void gxCanvas::setCubeMode( int mode ){
	cube_mode=mode;
}

void gxCanvas::setCubeFace( int face ){

	getTexSurface();
	surf=cube_surfs[face];
}