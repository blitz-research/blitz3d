
#include "std.h"

#ifdef PRO

#include "bbblitz3d.h"
#include "bbgraphics.h"
#include "../blitz3d/blitz3d.h"
#include "../blitz3d/world.h"
#include "../blitz3d/texture.h"
#include "../blitz3d/brush.h"
#include "../blitz3d/camera.h"
#include "../blitz3d/sprite.h"
#include "../blitz3d/meshmodel.h"
#include "../blitz3d/loader_x.h"
#include "../blitz3d/loader_3ds.h"
#include "../blitz3d/loader_b3d.h"
#include "../blitz3d/md2model.h"
#include "../blitz3d/q3bspmodel.h"
#include "../blitz3d/meshutil.h"
#include "../blitz3d/pivot.h"
#include "../blitz3d/planemodel.h"
#include "../blitz3d/terrain.h"
#include "../blitz3d/listener.h"
#include "../blitz3d/cachedtexture.h"

gxScene *gx_scene;
extern gxFileSystem *gx_filesys;

static int tri_count;
static World *world;

static set<Brush*> brush_set;
static set<Texture*> texture_set;
static set<Entity*> entity_set;

static Listener *listener;

static bool stats_mode;

//converts 0...255 color to 0...1
static const float ctof=1.0f/255.0f;

//degrees to radians and back
static const float dtor=0.0174532925199432957692369076848861f;
static const float rtod=1/dtor;

static Vector projected,tformed;

static ObjCollision picked;

extern float stats3d[10];

static Loader_X loader_x;
static Loader_3DS loader_3ds;
static Loader_B3D loader_b3d;

static map<string,Transform> loader_mat_map;

static inline void debug3d(){
	if( debug && !gx_scene ) RTEX( "3D Graphics mode not set" );
}
static inline void debugTexture( Texture *t ){
	if( debug && !texture_set.count( t ) ) RTEX( "Texture does not exist" );
}
static inline void debugBrush( Brush *b ){
	if( debug && !brush_set.count( b ) ) RTEX( "Brush does not exist" );
}
static inline void debugEntity( Entity *e ){
	if( debug && !entity_set.count(e) ) RTEX( "Entity does not exist" );
}
static inline void debugParent( Entity *e ){
	if( debug ){
		debug3d();
		if( e && !entity_set.count(e) ) RTEX( "Parent entity does not exist" );
	}
}
static inline void debugMesh( MeshModel *m ){
	if( debug ){
		debugEntity(m);if( !m->getMeshModel() ) RTEX( "Entity is not a mesh" );
	}
}
static inline void debugObject( Object *o ){
	if( debug ){
		debugEntity(o);if( !o->getObject() ) RTEX( "Entity is not an object" );
	}
}
static inline void debugColl( Object *o,int index ){
	if( debug ){
		debugObject(o);
		if( index<1 || index>o->getCollisions().size() ) RTEX( "Collision index out of range" );
	}
}
static inline void debugCamera( Camera *c ){
	if( debug ){
		debugEntity(c);if( !c->getCamera() ) RTEX( "Entity is not a camera" );
	}
}
static inline void debugLight( Light *l ){
	if( debug ){
		debugEntity(l);if( !l->getLight() ) RTEX( "Entity is not a light" );
	}
}
static inline void debugModel( Model *m ){
	if( debug ){
		debugEntity(m);if( !m->getModel() ) RTEX( "Entity is not a model" );
	}
}
static inline void debugSprite( Sprite *s ){
	if( debug ){
		debugModel(s);if( !s->getSprite() ) RTEX( "Entity is not a sprite" );
	}
}
static inline void debugMD2( MD2Model *m ){
	if( debug ){
		debugModel(m);if( !m->getMD2Model() ) RTEX( "Entity is not an MD2 Model" );
	}
}
static inline void debugBSP( Q3BSPModel *m ){
	if( debug ){
		debugModel(m);if( !m->getBSPModel() ) RTEX( "Entity is not a BSP Model" );
	}
}
static inline void debugTerrain( Terrain *t ){
	if( debug ){
		debugModel(t);if( !t->getTerrain() ) RTEX( "Entity is not a terrain" );
	}
}
static inline void debugSegs( int n ){
	if( debug ){
		debug3d();
		if( n<3 || n>50 ) RTEX( "Illegal number of segments" );
	}
}
static inline void debugVertex( Surface *s,int n ){
	if( debug ){
		debug3d();
		if( n<0 || n>=s->numVertices() ) RTEX( "Vertex index out of range" );
	}
}
static inline void debugVertex( Surface *s,int n,int t ){
	if( debug ){
		debug3d();
		if( n<0 || n>=s->numVertices() ) RTEX( "Vertex index out of range" );
		if( t<0 || t>1 ) RTEX( "Texture coordinate set out of range" );
	}
}

static Entity *loadEntity( string t,int hint ){
	t=tolower(t);
	int n=t.rfind( "." );if( n==string::npos ) return 0;
	string ext=t.substr( n+1 );
	MeshLoader *l;

	if( ext=="x" ) l=&loader_x;
	else if( ext=="3ds" ) l=&loader_3ds;
	else if( ext=="b3d" ) l=&loader_b3d;
	else return 0;

	const Transform &conv=loader_mat_map[ext];

	CachedTexture::setPath( filenamepath( t ) );
	Entity *e=l->load( t,conv,hint );
	CachedTexture::setPath( "" );
	return e;
}

static void collapseMesh( MeshModel *mesh,Entity *e ){
	while( e->children() ){
		collapseMesh( mesh,e->children() );
	}
	if( Model *p=e->getModel() ){
		if( MeshModel *t=p->getMeshModel() ){
			t->transform( e->getWorldTform() );
			mesh->add( *t );
		}
	}
	delete e;
}

static void insert( Entity *e ){
	if( debug ) entity_set.insert( e );
	e->setVisible(true);
	e->setEnabled(true);
	e->getObject()->reset();
	for( Entity *p=e->children();p;p=p->successor() ){
		insert( p );
	}
}

static Entity *insertEntity( Entity *e,Entity *p ){
	e->setParent( p );
	insert( e );
	return e;
}

static void erase( Entity *e ){
	for( Entity *p=e->children();p;p=p->successor() ){
		erase( p );
	}
	if( e->getListener() ) listener=0;
	if( debug ) entity_set.erase( e );
}

static Entity *findChild( Entity *e,const string &t ){
	if( e->getName()==t ) return e;
	for( Entity *p=e->children();p;p=p->successor() ){
		if( Entity *q=findChild(p,t) ) return q;
	}
	return 0;
}

///////////////////////////
// GLOBAL WORLD COMMANDS //
///////////////////////////
void  bbLoaderMatrix( BBStr *ext,float xx,float xy,float xz,float yx,float yy,float yz,float zx,float zy,float zz ){
	loader_mat_map.erase( *ext );
	loader_mat_map[*ext]=Transform(Matrix(Vector(xx,xy,xz),Vector(yx,yy,yz),Vector(zx,zy,zz)));
	delete ext;
}

int   bbHWTexUnits(){
	debug3d();
	return gx_scene->hwTexUnits();
}

int	  bbGfxDriverCaps3D(){
	debug3d();
	return gx_scene->gfxDriverCaps3D();
}

void  bbHWMultiTex( int enable ){
	debug3d();
	gx_scene->setHWMultiTex( !!enable );
}

void  bbWBuffer( int enable ){
	debug3d();
	gx_scene->setWBuffer( !!enable );
}

void  bbDither( int enable ){
	debug3d();
	gx_scene->setDither( !!enable );
}

void  bbAntiAlias( int enable ){
	debug3d();
	gx_scene->setAntialias( !!enable );
}

void  bbWireFrame( int enable ){
	debug3d();
	gx_scene->setWireframe( !!enable );
}

void  bbAmbientLight( float r,float g,float b ){
	debug3d();
	Vector t( r*ctof,g*ctof,b*ctof );
	gx_scene->setAmbient( &(t.x) );
}

void  bbClearCollisions(){
	debug3d();
	world->clearCollisions();
}

void  bbCollisions( int src_type,int dest_type,int method,int response ){
	debug3d();
	world->addCollision( src_type,dest_type,method,response );
}

static int update_ms;

void  bbUpdateWorld( float elapsed ){
	debug3d();

#ifndef BETA
	world->update( elapsed );
	return;
#endif

	update_ms=gx_runtime->getMilliSecs();
	world->update( elapsed );
	update_ms=gx_runtime->getMilliSecs()-update_ms;
}

void  bbCaptureWorld(){
	debug3d();
	world->capture();
}

void  bbRenderWorld( float tween ){
	debug3d();

#ifndef BETA
	tri_count=gx_scene->getTrianglesDrawn();
	world->render( tween );
	tri_count=gx_scene->getTrianglesDrawn()-tri_count;
	return;
#endif

	int tris=gx_scene->getTrianglesDrawn();
	int render_ms=gx_runtime->getMilliSecs();
	world->render( tween );
	render_ms=gx_runtime->getMilliSecs()-render_ms;

	extern int bbKeyHit(int);
	extern void bbDelay(int);
	bbDelay(0);
	if( bbKeyHit( 0x57 ) ){
		stats_mode=!stats_mode;
	}
	if( bbKeyHit( 0x58 ) ){
		static int n;
		string t="screenshot"+itoa(++n)+".bmp";
		bbSaveBuffer( bbBackBuffer(),d_new BBStr(t) );
	}

	if( !stats_mode ) return;

	tris=gx_scene->getTrianglesDrawn()-tris;

	static int time;
	int frame_ms=gx_runtime->getMilliSecs()-time;
	time+=frame_ms;

	int fps=frame_ms ? 1000/frame_ms : 1000;
	int ups=update_ms ? 1000/update_ms : 1000;
	int rps=render_ms ? 1000/render_ms : 1000;

	string t_fps="000"+itoa(fps);t_fps=t_fps.substr( t_fps.size()-4 );
	string t_ups="000"+itoa(ups);t_ups=t_ups.substr( t_ups.size()-4 );
	string t_rps="000"+itoa(rps);t_rps=t_rps.substr( t_rps.size()-4 );
	string t_tris="00000"+itoa(tris);t_tris=t_tris.substr( t_tris.size()-6 );

	string t="FPS:"+t_fps+" UPS:"+t_ups+" RPS:"+t_rps+" TRIS:"+t_tris;

	bbText( 0,bbGraphicsHeight()-bbFontHeight(),d_new BBStr(t),0,0 );
}

int  bbTrisRendered(){
	return tri_count;
}

float  bbStats3D( int n ){
	return stats3d[n];
}

//////////////////////
// TEXTURE COMMANDS //
//////////////////////

//Note: modify canvas->backup() to NOT release backup image!
//
Texture *  bbLoadTexture( BBStr *file,int flags ){
	debug3d();
	Texture *t=d_new Texture( *file,flags );delete file;
	if( !t->getCanvas(0) ){ delete t;return 0; }
	texture_set.insert( t );
	return t;
}

Texture *  bbLoadAnimTexture( BBStr *file,int flags,int w,int h,int first,int cnt ){
	debug3d();
	Texture *t=d_new Texture( *file,flags,w,h,first,cnt );
	delete file;
	if( !t->getCanvas(0) ){
		delete t;
		return 0; 
	}
	texture_set.insert( t );
	return t;
}

Texture *  bbCreateTexture( int w,int h,int flags,int frames ){
	if( debug ){
		debug3d();
		if( frames<=0 ){
			RTEX( "Illegal number of texture frames" );
		}
	}
	Texture *t=d_new Texture( w,h,flags,frames );
	texture_set.insert( t );
	return t;
}

void  bbFreeTexture( Texture *t ){
	if( !t ) return;
	debugTexture(t);
	if( texture_set.erase( t ) ) delete t;
}

void  bbTextureBlend( Texture *t,int blend ){
	debugTexture(t);
	t->setBlend( blend );
}

void  bbTextureCoords( Texture *t,int flags ){
	debugTexture(t);
	t->setFlags( flags );
}

void  bbScaleTexture( Texture *t,float u_scale,float v_scale ){
	debugTexture(t);
	t->setScale( 1/u_scale,1/v_scale );
}

void  bbRotateTexture( Texture *t,float angle ){
	debugTexture(t);
	t->setRotation( -angle*dtor );
}

void  bbPositionTexture( Texture *t,float u_pos,float v_pos ){
	debugTexture(t);
	t->setPosition( -u_pos,-v_pos );
}

int  bbTextureWidth( Texture *t ){
	debugTexture(t);
	return t->getCanvas(0)->getWidth();
}

int  bbTextureHeight( Texture *t ){
	debugTexture(t);
	return t->getCanvas(0)->getHeight();
}

BBStr *bbTextureName( Texture *t ){
	debugTexture(t);
	CachedTexture *c=t->getCachedTexture();
	return c ? d_new BBStr( c->getName().c_str() ) : d_new BBStr("");
}

void bbSetCubeFace( Texture *t,int face ){
	debugTexture(t);
	if( gxCanvas *c=t->getCanvas( 0 ) ){
		c->setCubeFace(face);
	}
}

void bbSetCubeMode( Texture *t,int mode ){
	debugTexture(t);
	if( gxCanvas *c=t->getCanvas( 0 ) ){
		c->setCubeMode( mode );
	}
}

gxCanvas *bbTextureBuffer( Texture *t,int frame ){
	//v1.04
	debugTexture(t);
	if( gxCanvas *c=t->getCanvas( frame ) ){
		if( c->getDepth() ) return c;
	}
	return 0;
	/*
	//v1.03  crashes if t->getCanvas returns null!
	debugTexture(t);
	gxCanvas *c=t->getCanvas( frame );
	if( c->getDepth() ) return c;
	return 0;
	*/
}

void  bbClearTextureFilters(){
	debug3d();
	Texture::clearFilters();
}

void  bbTextureFilter( BBStr *t,int flags ){
	debug3d();
	Texture::addFilter( *t,flags );
	delete t;
}

////////////////////
// BRUSH COMMANDS //
////////////////////
Brush *  bbCreateBrush( float r,float g,float b ){
	debug3d();
	Brush *br=d_new Brush();
	br->setColor( Vector( r*ctof,g*ctof,b*ctof ) );
	brush_set.insert( br );
	return br;
}

Brush *  bbLoadBrush( BBStr *file,int flags,float u_scale,float v_scale ){
	debug3d();
	Texture t( *file,flags );
	delete file;if( !t.getCanvas(0) ) return 0;
	if( u_scale!=1 || v_scale!=1 ) t.setScale( 1/u_scale,1/v_scale );
	Brush *br=bbCreateBrush( 255,255,255 );
	br->setTexture( 0,t,0 );
	delete file;
	return br;
}

void  bbFreeBrush( Brush *b ){
	if( !b ) return;
	debugBrush(b);
	if( brush_set.erase( b ) ) delete b;
}

void  bbBrushColor( Brush *br,float r,float g,float b ){
	debugBrush(br);
	br->setColor( Vector( r*ctof,g*ctof,b*ctof ) );
}

void  bbBrushAlpha( Brush *b,float alpha ){
	debugBrush(b);
	b->setAlpha( alpha );
}

void  bbBrushShininess( Brush *b,float n ){
	debugBrush(b);
	b->setShininess( n );
}

void  bbBrushTexture( Brush *b,Texture *t,int frame,int index ){
	debugBrush(b);
	debugTexture(t);
	b->setTexture( index,*t,frame );
}

Texture *bbGetBrushTexture( Brush *b,int index ){
	debugBrush(b);
	Texture *tex=d_new Texture(b->getTexture(index));
	texture_set.insert( tex );
	return tex;
}

void  bbBrushBlend( Brush *b,int blend ){
	debugBrush(b);
	b->setBlend( blend );
}

void  bbBrushFX( Brush *b,int fx ){
	debugBrush(b);
	b->setFX( fx );
}

///////////////////
// MESH COMMANDS //
///////////////////
Entity *  bbCreateMesh( Entity *p ){
	debugParent(p);
	MeshModel *m=d_new MeshModel();
	return insertEntity( m,p );
}

Entity *  bbLoadMesh( BBStr *f,Entity *p ){
	debugParent(p);
	Entity *e=loadEntity( f->c_str(),MeshLoader::HINT_COLLAPSE );
	delete f;

	if( !e ) return 0;
	MeshModel *m=d_new MeshModel();
	collapseMesh( m,e );
	return insertEntity( m,p );
}

Entity *  bbLoadAnimMesh( BBStr *f,Entity *p ){
	debugParent(p);
	Entity *e=loadEntity( f->c_str(),0 );
	delete f;

	if( !e ) return 0;
	if( Animator *anim=e->getObject()->getAnimator() ){
		anim->animate( 1,0,0,0 );
	}
	return insertEntity( e,p );
}

Entity *  bbCreateCube( Entity *p ){
	debugParent(p);
	Entity *e=MeshUtil::createCube( Brush() );
	return insertEntity( e,p );
}

Entity *  bbCreateSphere( int segs,Entity *p ){
	if( debug ){ debugParent(p);if( segs<2 || segs>100 ) RTEX( "Illegal number of segments" ); }
	Entity *e=MeshUtil::createSphere( Brush(),segs );
	return insertEntity( e,p );
}

Entity *  bbCreateCylinder( int segs,int solid,Entity *p ){
	if( debug ){ debugParent(p);if( segs<3 || segs>100 ) RTEX( "Illegal number of segments" ); }
	Entity *e=MeshUtil::createCylinder( Brush(),segs,!!solid );
	return insertEntity( e,p );
}

Entity *  bbCreateCone( int segs,int solid,Entity *p ){
	if( debug ){ debugParent(p);if( segs<3 || segs>100 ) RTEX( "Illegal number of segments" ); }
	Entity *e=MeshUtil::createCone( Brush(),segs,!!solid );
	return insertEntity( e,p );
}

Entity *  bbCopyMesh( MeshModel *m,Entity *p ){
	debugMesh(m);
	debugParent(p);

	MeshModel *t=d_new MeshModel();
	t->add( *m );
	return insertEntity( t,p );
}

void  bbScaleMesh( MeshModel *m,float x,float y,float z ){
	debugMesh(m);
	m->transform( scaleMatrix(x,y,z) );
}

void  bbRotateMesh( MeshModel *m,float x,float y,float z ){
	debugMesh(m);
	m->transform( rotationMatrix(x*dtor,y*dtor,z*dtor) );
}

void  bbPositionMesh( MeshModel *m,float x,float y,float z ){
	debugMesh(m);
	m->transform( Vector(x,y,z) );
}

void  bbFitMesh( MeshModel *m,float x,float y,float z,float w,float h,float d,int uniform ){
	debugMesh(m);
	Box box( Vector(x,y,z) );
	box.update( Vector(x+w,y+h,z+d) );
	const Box &curr_box=m->getBox();
	float x_scale=box.width()/curr_box.width();
	float y_scale=box.height()/curr_box.height();
	float z_scale=box.depth()/curr_box.depth();
	Transform t;
	if( uniform ){
		if( x_scale<y_scale && x_scale<z_scale ){
			y_scale=z_scale=x_scale;
		}else if( y_scale<x_scale && y_scale<z_scale ){
			x_scale=z_scale=y_scale;
		}else{
			x_scale=y_scale=z_scale;
		}
	}
	t.m.i.x=x_scale;
	t.m.j.y=y_scale;
	t.m.k.z=z_scale;
	t.v=box.centre() - t.m * curr_box.centre();
	m->transform( t );
}

void  bbFlipMesh( MeshModel *m ){
	debugMesh(m);
	m->flipTriangles();
}

void  bbPaintMesh( MeshModel *m,Brush *b ){
	if( debug ){ debugMesh(m);debugBrush(b); }
	m->paint( *b );
}

void  bbAddMesh( MeshModel *src,MeshModel *dest ){
	if( debug ){
		debugMesh(src);debugMesh(dest); 
		if( src==dest ) RTEX( "A mesh cannot be added to itself" );
	}

	dest->add( *src );
}

void  bbUpdateNormals( MeshModel *m ){
	debugMesh(m);
	m->updateNormals();
}

void  bbLightMesh( MeshModel *m,float r,float g,float b,float range,float x,float y,float z ){
	debugMesh(m);
	MeshUtil::lightMesh( m,Vector(x,y,z),Vector(r*ctof,g*ctof,b*ctof),range );
}

float  bbMeshWidth( MeshModel *m ){
	debugMesh(m);
	return m->getBox().width();
}

float  bbMeshHeight( MeshModel *m ){
	debugMesh(m);
	return m->getBox().height();
}

float  bbMeshDepth( MeshModel *m ){
	debugMesh(m);
	return m->getBox().depth();
}

int  bbMeshesIntersect( MeshModel *a,MeshModel *b ){
	if( debug ){ debugMesh(a);debugMesh(b); }
	return a->intersects( *b );
}

int  bbCountSurfaces( MeshModel *m ){
	debugMesh(m);
	return m->getSurfaces().size();
}

Surface *  bbGetSurface( MeshModel *m,int index ){
	if( debug ){
		debugMesh(m);
		if( index<1 || index>m->getSurfaces().size() ){
			RTEX( "Surface Index out of range" );
		}
	}
	return m->getSurfaces()[index-1];
}

void bbMeshCullBox( MeshModel *m,float x,float y,float z,float width,float height,float depth ){
	if( debug ){
		debugMesh( m );
	}
	m->setCullBox( Box( Vector(x,y,z),Vector(x+width,y+height,z+depth) ) );
}


//////////////////////
// SURFACE COMMANDS //
//////////////////////
Surface *  bbFindSurface( MeshModel *m,Brush *b ){
	if( debug ){ debugMesh(m);debugBrush(b); }
	return m->findSurface(*b);
}

Surface *  bbCreateSurface( MeshModel *m,Brush *b ){
	if( debug ){ debugMesh(m);if( b ) debugBrush(b); }
	Surface *s=b ? m->createSurface( *b ) : m->createSurface( Brush() );
	return s;
}

Brush *bbGetSurfaceBrush( Surface *s ){
	Brush *br=d_new Brush( s->getBrush() );
	brush_set.insert( br );
	return br;
}

Brush *bbGetEntityBrush( Model *m ){
	debugModel(m);
	Brush *br=d_new Brush( m->getBrush() );
	brush_set.insert( br );
	return br;
}

void  bbClearSurface( Surface *s,int verts,int tris ){
	s->clear( !!verts,!!tris );
}

void  bbPaintSurface( Surface *s,Brush *b ){
	debugBrush(b);
	s->setBrush(*b);
}

int  bbAddVertex( Surface *s,float x,float y,float z,float tu,float tv,float tw ){
	Surface::Vertex v;
	v.coords=Vector(x,y,z);
	v.color=0xffffffff;
	v.tex_coords[0][0]=v.tex_coords[1][0]=tu;
	v.tex_coords[0][1]=v.tex_coords[1][1]=tv;
	s->addVertex( v );
	return s->numVertices()-1;
}

int  bbAddTriangle( Surface *s,int v0,int v1,int v2 ){
	Surface::Triangle t;
	t.verts[0]=v0;t.verts[1]=v1;t.verts[2]=v2;
	s->addTriangle( t );
	return s->numTriangles()-1;
}

void  bbVertexCoords( Surface *s,int n,float x,float y,float z ){
	s->setCoords( n,Vector(x,y,z) );
}

void  bbVertexNormal( Surface *s,int n,float x,float y,float z ){
	s->setNormal( n,Vector(x,y,z) );
}

void  bbVertexColor( Surface *s,int n,float r,float g,float b,float a ){
	if(r<0)r=0;else if(r>255)r=255;
	if(g<0)g=0;else if(g>255)g=255;
	if(b<0)b=0;else if(b>255)b=255;
	a*=255;if(a<0)a=0;else if(a>255)a=255;
	s->setColor( n,(int(a)<<24)|(int(r)<<16)|(int(g)<<8)|int(b) );
}

void  bbVertexTexCoords( Surface *s,int n,float u,float v,float w,int set ){
	s->setTexCoords( n,Vector(u,v,w),set );
}

int  bbCountVertices( Surface *s ){
	return s->numVertices();
}

int  bbCountTriangles( Surface *s ){
	return s->numTriangles();
}

float  bbVertexX( Surface *s,int n ){
	debugVertex(s,n);
	return s->getVertex(n).coords.x;
}
float  bbVertexY( Surface *s,int n ){
	debugVertex(s,n);
	return s->getVertex(n).coords.y;
}
float  bbVertexZ( Surface *s,int n ){
	debugVertex(s,n);
	return s->getVertex(n).coords.z;
}
float  bbVertexNX( Surface *s,int n ){
	debugVertex(s,n);
	return s->getVertex(n).normal.x;
}
float  bbVertexNY( Surface *s,int n ){
	debugVertex(s,n);
	return s->getVertex(n).normal.y;
}
float  bbVertexNZ( Surface *s,int n ){
	debugVertex(s,n);
	return s->getVertex(n).normal.z;
}
float  bbVertexRed( Surface *s,int n ){
	debugVertex(s,n);
	return (s->getVertex(n).color&0xff0000)>>16;
}
float  bbVertexGreen( Surface *s,int n ){
	debugVertex(s,n);
	return (s->getVertex(n).color&0xff00)>>8;
}
float  bbVertexBlue( Surface *s,int n ){
	debugVertex(s,n);
	return s->getVertex(n).color&0xff;
}
float  bbVertexAlpha( Surface *s,int n ){
	debugVertex(s,n);
	return ((s->getVertex(n).color&0xff000000)>>24)/255.0f;
}
float  bbVertexU( Surface *s,int n,int t ){
	debugVertex(s,n,t);
	return s->getVertex(n).tex_coords[t][0];
}
float  bbVertexV( Surface *s,int n,int t ){
	debugVertex(s,n,t);
	return s->getVertex(n).tex_coords[t][1];
}
float  bbVertexW( Surface *s,int n,int t ){
	debugVertex(s,n,t);
	return 1;
}
int  bbTriangleVertex( Surface *s,int n,int v ){
	return s->getTriangle(n).verts[v];
}

/////////////////////
// CAMERA COMMANDS //
/////////////////////
Entity *  bbCreateCamera( Entity *p ){
	debugParent(p);
	int x,y,w,h;
	gx_canvas->getViewport( &x,&y,&w,&h );
	Camera *c=d_new Camera();
	c->setViewport( x,y,w,h );
	return insertEntity( c,p );
}

void  bbCameraZoom( Camera *c,float zoom ){
	debugCamera(c);
	c->setZoom( zoom );
}

void  bbCameraRange( Camera *c,float nr,float fr ){
	debugCamera(c);
	c->setRange( nr,fr );
}

void  bbCameraClsColor( Camera *c,float r,float g,float b ){
	debugCamera(c);
	c->setClsColor( Vector( r*ctof,g*ctof,b*ctof ) );
}

void  bbCameraClsMode( Camera *c,int cls_color,int cls_zbuffer ){
	debugCamera(c);
	c->setClsMode( cls_color ? true : false,cls_zbuffer ? true : false );
}

void  bbCameraProjMode( Camera *c,int mode ){
	debugCamera(c);
	c->setProjMode( mode );
}

void  bbCameraViewport( Camera *c,int x,int y,int w,int h ){
	debugCamera(c);
	c->setViewport( x,y,w,h );
}

void  bbCameraFogRange( Camera *c,float nr,float fr ){
	debugCamera(c);
	c->setFogRange( nr,fr );
}

void  bbCameraFogColor( Camera *c,float r,float g,float b ){
	debugCamera(c);
	c->setFogColor( Vector( r*ctof,g*ctof,b*ctof ) );
}

void  bbCameraFogMode( Camera *c,int mode ){
	debugCamera(c);
	c->setFogMode( mode );
}

int  bbCameraProject( Camera *c,float x,float y,float z ){
	debugCamera(c);
	Vector v=-c->getWorldTform()*Vector(x,y,z);
	const Frustum &f=c->getFrustum();
	if( c->getProjMode()==Camera::PROJ_ORTHO ){
		int vp_x,vp_y,vp_w,vp_h;
		c->getViewport( &vp_x,&vp_y,&vp_w,&vp_h );
		float nr=c->getFrustumNear();
		float fr=c->getFrustumFar();
		float nr_w=c->getFrustumWidth();
		float nr_h=c->getFrustumHeight();
		projected=Vector( (v.x/nr_w+.5f)*vp_w,(.5f-v.y/nr_h)*vp_h,nr );
		return 1;
	}
	if( v.z>0 ){
		float fr=+f.getPlane( Frustum::PLANE_FAR ).d;
		if( v.z<=fr ){
			int vp_x,vp_y,vp_w,vp_h;
			c->getViewport( &vp_x,&vp_y,&vp_w,&vp_h );
			float nr=c->getFrustumNear();
			float fr=c->getFrustumFar();
			float nr_w=c->getFrustumWidth();
			float nr_h=c->getFrustumHeight();
			projected=Vector( 
				(v.x*nr/v.z/nr_w+.5f)*vp_w,
				(.5f-v.y*nr/v.z/nr_h)*vp_h,nr );
			return 1;
		}
	}
	projected=Vector();
	return 0;
}

float  bbProjectedX(){
	return projected.x;
}

float  bbProjectedY(){
	return projected.y;
}

float  bbProjectedZ(){
	return projected.z;
}

static Object *doPick( const Line &l,float radius ){
	picked.collision.time=1;
	return world->traceRay( l,radius,&picked );
}

Entity *  bbCameraPick( Camera *c,float x,float y ){
	debugCamera(c);

	int vp_x,vp_y,vp_w,vp_h;
	c->getViewport( &vp_x,&vp_y,&vp_w,&vp_h );
	float nr=c->getFrustumNear();
	float fr=c->getFrustumFar();
	float nr_w=c->getFrustumWidth();
	float nr_h=c->getFrustumHeight();

	x=((x/vp_w)-.5f)*nr_w;
	y=(.5f-(y/vp_h))*nr_h;

	Line l;
	if( c->getProjMode()==Camera::PROJ_ORTHO ){
		l=c->getWorldTform() * Line( Vector(x,y,0),Vector(0,0,fr) );	//x,y,fr) );
	}else{
		x/=nr;y/=nr;
		l=c->getWorldTform() * Line( Vector(),Vector( x*fr,y*fr,fr ) );
	}

	return doPick( l,0 );
}

Entity *  bbLinePick( float x,float y,float z,float dx,float dy,float dz,float radius ){
	debug3d();

	Line l( Vector( x,y,z ),Vector( dx,dy,dz ) );

	return doPick( l,radius );
}

Entity *  bbEntityPick( Object *src,float range ){
	debugEntity(src);

	Line l( src->getWorldPosition(),src->getWorldTform().m.k * range );

	return doPick( l,0 );
}

int  bbEntityVisible( Object *src,Object *dest ){
	if( debug ){ debugObject(src);debugObject(dest); }

	return world->checkLOS( src,dest ) ? 1 : 0;
}

int  bbEntityInView( Entity *e,Camera *c ){
	if( debug ){ debugEntity(e);debugCamera(c); }
	if( Model *p=e->getModel() ){
		if( MeshModel *m=p->getMeshModel() ){
			const Box &b=m->getBox();
			Transform t=-c->getWorldTform() * e->getWorldTform();
			Vector p[]={ 
				t*b.corner(0),t*b.corner(1),t*b.corner(2),t*b.corner(3),
				t*b.corner(4),t*b.corner(5),t*b.corner(6),t*b.corner(7)
			};
			return c->getFrustum().cull( p,8 );
		}
	}
	Vector p[]={ -c->getWorldTform() * e->getWorldPosition() };
	return c->getFrustum().cull( p,1 );
}

float  bbPickedX(){
	return picked.coords.x;
}

float  bbPickedY(){
	return picked.coords.y;
}

float  bbPickedZ(){
	return picked.coords.z;
}

float  bbPickedNX(){
	return picked.collision.normal.x;
}

float  bbPickedNY(){
	return picked.collision.normal.y;
}

float  bbPickedNZ(){
	return picked.collision.normal.z;
}

float  bbPickedTime(){
	return picked.collision.time;
}

Object * bbPickedEntity(){
	return picked.with;
}

void * bbPickedSurface(){
	return picked.collision.surface;
}

int  bbPickedTriangle(){
	return picked.collision.index;
}

////////////////////
// LIGHT COMMANDS //
////////////////////
Entity *  bbCreateLight( int type,Entity *p ){
	debugParent(p);
	Light *t=d_new Light( type );
	return insertEntity( t,p );
}

void  bbLightColor( Light *light,float r,float g,float b ){
	debugLight(light);
	light->setColor( Vector(r*ctof,g*ctof,b*ctof) );
}

void  bbLightRange( Light *light,float range ){
	debugLight(light);
	light->setRange( range );
}

void  bbLightConeAngles( Light *light,float inner,float outer ){
	debugLight(light);
	inner*=dtor;
	outer*=dtor;
	if( inner<0 ) inner=0;
	else if( inner>PI ) inner=PI;
	if( outer<inner ) outer=inner;
	else if( outer>PI ) outer=PI;
	light->setConeAngles( inner,outer );
}

////////////////////
// PIVOT COMMANDS //
////////////////////
Entity *  bbCreatePivot( Entity *p ){
	debugParent(p);
	Pivot *t=d_new Pivot();
	return insertEntity( t,p );
}

/////////////////////
// SPRITE COMMANDS //
/////////////////////
Entity *  bbCreateSprite( Entity *p ){
	debugParent(p);
	Sprite *s=d_new Sprite();
	s->setFX( gxScene::FX_FULLBRIGHT );
	return insertEntity( s,p );
}

Entity *  bbLoadSprite( BBStr *file,int flags,Entity *p ){
	debugParent(p);
	Texture t( *file,flags );
	delete file;if( !t.getCanvas(0) ) return 0;
	Sprite *s=d_new Sprite();
	s->setTexture( 0,t,0 );
	s->setFX( gxScene::FX_FULLBRIGHT );

	if( flags & gxCanvas::CANVAS_TEX_MASK ) s->setBlend( gxScene::BLEND_REPLACE );
	else if( flags & gxCanvas::CANVAS_TEX_ALPHA ) s->setBlend( gxScene::BLEND_ALPHA );
	else s->setBlend( gxScene::BLEND_ADD );

	return insertEntity( s,p );
}

void  bbRotateSprite( Sprite *s,float angle ){
	debugSprite(s);
	s->setRotation( angle*dtor );
}

void  bbScaleSprite( Sprite *s,float x,float y ){
	debugSprite(s);
	s->setScale( x,y );
}

void  bbHandleSprite( Sprite *s,float x,float y ){
	debugSprite(s);
	s->setHandle( x,y );
}

void  bbSpriteViewMode( Sprite *s,int mode ){
	debugSprite(s);
	s->setViewmode( mode );
}

/////////////////////
// MIRROR COMMANDS //
/////////////////////
Entity *  bbCreateMirror( Entity *p ){
	debugParent(p);
	Mirror *t=d_new Mirror();
	return insertEntity( t,p );
}

////////////////////
// PLANE COMMANDS //
////////////////////
Entity *  bbCreatePlane( int segs,Entity *p ){
	if( debug ){
		debugParent(p);
		if( segs<1 || segs>20 ) RTEX( "Illegal number of segments" );
	}
	PlaneModel *t=d_new PlaneModel( segs );
	return insertEntity( t,p );
}

//////////////////
// MD2 COMMANDS //
//////////////////
Entity *  bbLoadMD2( BBStr *file,Entity *p ){
	debugParent(p);
	MD2Model *t=d_new MD2Model( *file );delete file;
	if( !t->getValid() ){ delete t;return 0; }
	return insertEntity( t,p );
}

void  bbAnimateMD2( MD2Model *m,int mode,float speed,int first,int last,float trans ){
	debugMD2(m);
	m->startMD2Anim( first,last,mode,speed,trans );
}

float  bbMD2AnimTime( MD2Model *m ){
	debugMD2(m);
	return m->getMD2AnimTime();
}

int  bbMD2AnimLength( MD2Model *m ){
	debugMD2(m);
	return m->getMD2AnimLength();
}

int  bbMD2Animating( MD2Model *m ){
	debugMD2(m);
	return m->getMD2Animating();
}

//////////////////
// BSP Commands //
//////////////////
Entity *  bbLoadBSP( BBStr *file,float gam,Entity *p ){
	debugParent(p);
	CachedTexture::setPath( filenamepath( *file ) );
	Q3BSPModel *t=d_new Q3BSPModel( *file,gam );delete file;
	CachedTexture::setPath( "" );

	if( !t->isValid() ){ delete t;return 0; }

	return insertEntity( t,p );
}

void  bbBSPAmbientLight( Q3BSPModel *t,float r,float g,float b ){
	debugBSP(t);
	t->setAmbient( Vector( r*ctof,g*ctof,b*ctof ) );
}

void  bbBSPLighting( Q3BSPModel *t,int lmap ){
	debugBSP(t);
	t->setLighting( !!lmap );
}

//////////////////////
// TERRAIN COMMANDS //
//////////////////////
static float terrainHeight( Terrain *t,float x,float z ){
	int ix=floor(x);
	int iz=floor(z);
	float tx=x-ix,tz=z-iz;
	float h0=t->getHeight(ix,iz);
	float h1=t->getHeight(ix+1,iz);
	float h2=t->getHeight(ix,iz+1);
	float h3=t->getHeight(ix+1,iz+1);
	float ha=(h1-h0)*tx+h0,hb=(h3-h2)*tx+h2;
	float h=(hb-ha)*tz+ha;
	return h;
}

static Vector terrainVector( Terrain *t,float x,float y,float z ){
	Vector v=-t->getWorldTform() * Vector( x,y,z );
	return t->getWorldTform() * Vector( v.x,terrainHeight( t,v.x,v.z ),v.z );
}

Entity *  bbCreateTerrain( int n,Entity *p ){
	debugParent(p);
	int shift=0;
	while( (1<<shift)<n ) ++shift;
	if( (1<<shift)!=n ) RTEX( "Illegal terrain size" );
	Terrain *t=d_new Terrain( shift );
	return insertEntity( t,p );
}

Entity *  bbLoadTerrain( BBStr *file,Entity *p ){
	debugParent(p);
	gxCanvas *c=gx_graphics->loadCanvas( *file,gxCanvas::CANVAS_HIGHCOLOR );
	if( !c ) RTEX( "Unable to load heightmap image" );
	int w=c->getWidth(),h=c->getHeight();
	if( w!=h ) RTEX( "Terrain must be square" );
	int shift=0;
	while( (1<<shift)<w ) ++shift;
	if( (1<<shift)!=w ) RTEX( "Illegal terrain size" );
	Terrain *t=d_new Terrain( shift );
	c->lock();
	for( int y=0;y<h;++y ){
		for( int x=0;x<w;++x ){
			int rgb=c->getPixelFast( x,y );
			int r=(rgb>>16)&0xff,g=(rgb>>8)&0xff,b=rgb&0xff;
			float p=(r>g?(r>b?r:b):(g>b?g:b))/255.0f;
			t->setHeight( x,h-1-y,p,false );
		}
	}
	c->unlock();
	gx_graphics->freeCanvas( c );
	return insertEntity( t,p );
}

void  bbTerrainDetail( Terrain *t,int n,int m ){
	debugTerrain(t);
	t->setDetail( n,!!m );
}

void  bbTerrainShading( Terrain *t,int enable ){
	debugTerrain(t);
	t->setShading( !!enable );
}

float  bbTerrainX( Terrain *t,float x,float y,float z ){
	debugTerrain(t);
	return terrainVector( t,x,y,z ).x;
}

float  bbTerrainY( Terrain *t,float x,float y,float z ){
	debugTerrain(t);
	return terrainVector( t,x,y,z ).y;
}

float  bbTerrainZ( Terrain *t,float x,float y,float z ){
	debugTerrain(t);
	return terrainVector( t,x,y,z ).z;
}

int  bbTerrainSize( Terrain *t ){
	debugTerrain(t);
	return t->getSize();
}

float  bbTerrainHeight( Terrain *t,int x,int z ){
	debugTerrain(t);
	return t->getHeight( x,z );
}

void  bbModifyTerrain( Terrain *t,int x,int z,float h,int realtime ){
	debugTerrain(t);
	t->setHeight( x,z,h,!!realtime );
}

////////////////////
// AUDIO COMMANDS //
////////////////////
Entity *  bbCreateListener( Entity *p,float roll,float dopp,float dist ){
	if( debug ){
		debugParent(p);
		if( listener ) RTEX( "Listener already created" );
	}
	listener=d_new Listener( roll,dopp,dist );
	return insertEntity( listener,p );
}

gxChannel *  bbEmitSound( gxSound *sound,Object *o ){
	if( debug ){
		debugObject(o);
		if( !listener ) RTEX( "No Listener created" );
	}
	return o->emitSound( sound );
}

/////////////////////
// ENTITY COMMANDS //
/////////////////////
Entity *  bbCopyEntity( Entity *e,Entity *p ){
	if( debug ){
		debugEntity(e);
		debugParent(p); 
	}
	Entity *t=e->getObject()->copy();
	if( !t ) return 0;
	return insertEntity( t,p );
}

void  bbFreeEntity( Entity *e ){
	if( !e ) return;
	if( debug ){
		debugEntity(e);
		erase(e);
	}
	delete e;
}

void  bbHideEntity( Entity *e ){
	debugEntity(e);
	e->setEnabled(false);
	e->setVisible(false);
}

void  bbShowEntity( Entity *e ){
	debugEntity(e);
	e->setVisible(true);
	e->setEnabled(true);
	e->getObject()->reset();
}

void  bbEntityParent( Entity *e,Entity *p,int global ){
	if( debug ){
		debugEntity(e);
		debugParent(p);
		Entity *t=p;
		while( t ){
			if( t==e ){
				RTEX( "Entity cannot be parented to itself!" );
			}
			t=t->getParent();
		}
	}

	if( e->getParent()==p ) return;

	if( global ){
		Transform t=e->getWorldTform();
		e->setParent( p );
		e->setWorldTform( t );
	}else{
		e->setParent( p );
		e->getObject()->reset();
	}
}

int  bbCountChildren( Entity *e ){
	debugEntity(e);
	int n=0;
	for( Entity *p=e->children();p;p=p->successor() ) ++n;
	return n;
}

Entity *  bbGetChild( Entity *e,int index ){
	debugEntity(e);
	Entity *p=e->children();
	while( --index && p ) p=p->successor();
	return p;
}

Entity *  bbFindChild( Entity *e,BBStr *t ){
	debugEntity(e);
	e=findChild( e,*t );
	delete t;
	return e;
}

////////////////////////
// ANIMATION COMMANDS //
////////////////////////
int  bbLoadAnimSeq( Object *o,BBStr *f ){
	debugObject( o );
	if( Animator *anim=o->getAnimator() ){
		Entity *t=loadEntity( f->c_str(),MeshLoader::HINT_ANIMONLY );
		delete f;
		if( t ){
			if( Animator *p=t->getObject()->getAnimator() ){
				anim->addSeqs( p );
			}
			delete t;
		}
		return anim->numSeqs()-1;
	}else{
		delete f;
	}
	return -1;
}

void  bbSetAnimTime( Object *o,float time,int seq ){
	debugObject( o );
	if( Animator *anim=o->getAnimator() ){
		anim->setAnimTime( time,seq );
	}else{
		RTEX( "Entity has not animation" );
	}
}

void  bbAnimate( Object *o,int mode,float speed,int seq,float trans ){
	debugObject( o );
	if( Animator *anim=o->getAnimator() ){
		anim->animate( mode,speed,seq,trans );
	}else{
		RTEX( "Entity has no animation" );
	}
}

void  bbSetAnimKey( Object *o,int frame,int pos_key,int rot_key,int scl_key ){
	debugObject( o );
	Animation anim=o->getAnimation();
	if( pos_key ) anim.setPositionKey( frame,o->getLocalPosition() );
	if( rot_key ) anim.setRotationKey( frame,o->getLocalRotation() );
	if( scl_key ) anim.setScaleKey( frame,o->getLocalScale() );
	o->setAnimation( anim );
}

int  bbExtractAnimSeq( Object *o,int first,int last,int seq ){
	debugObject( o );
	if( Animator *anim=o->getAnimator() ){
		anim->extractSeq( first,last,seq );
		return anim->numSeqs()-1;
	}
	return -1;
}

int  bbAddAnimSeq( Object *o,int length ){
	debugObject( o );
	Animator *anim=o->getAnimator();
	if( anim ){
		anim->addSeq( length );
	}else{
		anim=d_new Animator( o,length );
		o->setAnimator( anim );
	}
	return anim->numSeqs()-1;
}

int  bbAnimSeq( Object *o ){
	debugObject(o);
	if( Animator *anim=o->getAnimator() ) return anim->animSeq();
	return -1;
}

float  bbAnimTime( Object *o ){
	debugObject(o);
	if( Animator *anim=o->getAnimator() ) return anim->animTime();
	return -1;
}

int  bbAnimLength( Object *o ){
	debugObject(o);
	if( Animator *anim=o->getAnimator() ) return anim->animLen();
	return -1;
}

int  bbAnimating( Object *o ){
	debugObject(o);
	if( Animator *anim=o->getAnimator() ) return anim->animating();
	return 0;
}

////////////////////////////////
// ENTITY SPECIAL FX COMMANDS //
////////////////////////////////
void  bbPaintEntity( Model *m,Brush *b ){
	if( debug ){
		debugModel(m);
		debugBrush(b);
	}
	m->setBrush( *b );
}

void  bbEntityColor( Model *m,float r,float g,float b ){
	debugModel(m);
	m->setColor( Vector( r*ctof,g*ctof,b*ctof ) );
}

void  bbEntityAlpha( Model *m,float alpha ){
	debugModel(m);
	m->setAlpha( alpha );
}

void  bbEntityShininess( Model *m,float shininess ){
	debugModel(m);
	m->setShininess( shininess );
}

void  bbEntityTexture( Model *m,Texture *t,int frame,int index ){
	debugModel(m);
	debugTexture(t);
	m->setTexture( index,*t,frame );
}

void  bbEntityBlend( Model *m,int blend ){
	debugModel(m);
	m->setBlend( blend );
}

void  bbEntityFX( Model *m,int fx ){
	debugModel(m);
	m->setFX( fx );
}

void  bbEntityAutoFade( Model *m,float nr,float fr ){
	debugModel(m);
	m->setAutoFade( nr,fr );
}

void  bbEntityOrder( Object *o,int n ){
	if( debug ){
		debugEntity(o);
		if( !o->getModel() && !o->getCamera() ){
			RTEX( "Entity is not a model or camera" );
		}
	}
	o->setOrder( n );
}

//////////////////////////////
// ENTITY PROPERTY COMMANDS //
//////////////////////////////
float  bbEntityX( Entity *e,int global ){
	debugEntity(e);
	return global ? e->getWorldPosition().x : e->getLocalPosition().x;
}

float  bbEntityY( Entity *e,int global ){
	debugEntity(e);
	return global ? e->getWorldPosition().y : e->getLocalPosition().y;
}

float  bbEntityZ( Entity *e,int global ){
	debugEntity(e);
	return global ? e->getWorldPosition().z : e->getLocalPosition().z;
}

float  bbEntityPitch( Entity *e,int global ){
	debugEntity(e);
	return quatPitch( global ? e->getWorldRotation() : e->getLocalRotation() ) * rtod;
}

float  bbEntityYaw( Entity *e,int global ){
	debugEntity(e);
	return quatYaw( global ? e->getWorldRotation() : e->getLocalRotation() ) * rtod;
}

float  bbEntityRoll( Entity *e,int global ){
	debugEntity(e);
	return quatRoll( global ? e->getWorldRotation() : e->getLocalRotation() ) * rtod;
}

float  bbGetMatElement( Entity *e,int row,int col ){
	debugEntity(e);
	return row<3 ? e->getWorldTform().m[row][col] : e->getWorldTform().v[col];
}

void  bbTFormPoint( float x,float y,float z,Entity *src,Entity *dest ){
	if( debug ){
		if( src ) debugEntity(src);
		if( dest ) debugEntity(dest);
	}
	tformed=Vector( x,y,z );
	if( src ) tformed=src->getWorldTform() * tformed;
	if( dest ) tformed=-dest->getWorldTform() * tformed;
}

void  bbTFormVector( float x,float y,float z,Entity *src,Entity *dest ){
	if( debug ){
		if( src ) debugEntity(src);
		if( dest ) debugEntity(dest);
	}
	tformed=Vector( x,y,z );
	if( src ) tformed=src->getWorldTform().m * tformed;
	if( dest ) tformed=-dest->getWorldTform().m * tformed;
}

void  bbTFormNormal( float x,float y,float z,Entity *src,Entity *dest ){
	if( debug ){
		if( src ) debugEntity(src);
		if( dest ) debugEntity(dest);
	}
	tformed=Vector( x,y,z );
	if( src ) tformed=(src->getWorldTform().m).cofactor() * tformed;
	if( dest ) tformed=(-dest->getWorldTform().m).cofactor() * tformed;
	tformed.normalize();
}

float  bbTFormedX(){
	return tformed.x;
}

float  bbTFormedY(){
	return tformed.y;
}

float  bbTFormedZ(){
	return tformed.z;
}

float  bbVectorYaw( float x,float y,float z ){
	return Vector(x,y,z).yaw() * rtod;
}

float  bbVectorPitch( float x,float y,float z ){
	return Vector(x,y,z).pitch() * rtod;
}

float  bbDeltaYaw( Entity *src,Entity *dest ){
	float x=src->getWorldTform().m.k.yaw();
	float y=(dest->getWorldTform().v-src->getWorldTform().v).yaw();
	float d=y-x;
	if( d<-PI ) d+=TWOPI;
	else if( d>=PI ) d-=TWOPI;
	return d*rtod;
}

float  bbDeltaPitch( Entity *src,Entity *dest ){
	float x=src->getWorldTform().m.k.pitch();
	float y=(dest->getWorldTform().v-src->getWorldTform().v).pitch();
	float d=y-x;
	if( d<-PI ) d+=TWOPI;
	else if( d>=PI ) d-=TWOPI;
	return d*rtod;
}

///////////////////////////////
// ENTITY COLLISION COMMANDS //
///////////////////////////////
void  bbResetEntity( Object *o ){
	debugObject(o);
	o->reset();
}

static void entityType( Entity *e,int type ){
	e->getObject()->setCollisionType(type);
	e->getObject()->reset();
	for( Entity *p=e->children();p;p=p->successor() ){
		entityType( p,type );
	}
}

void  bbEntityType( Object *o,int type,int recurs ){
	if( debug ){
		debugObject(o);
		if( type<0 || type>999 ) RTEX( "EntityType ID must be in the range 0...999" );
	}
	if( recurs ) entityType( o,type );
	else{
		o->setCollisionType(type);
		o->reset();
	}
}

void  bbEntityPickMode( Object *o,int mode,int obs ){
	debugObject(o);
	o->setPickGeometry( mode );
	o->setObscurer( !!obs );
}

Entity *  bbGetParent( Entity *e ){
	debugEntity(e);
	return e->getParent();
}

int  bbGetEntityType( Object *o ){
	debugObject(o);
	return o->getCollisionType();
}

void  bbEntityRadius( Object *o,float x_radius,float y_radius ){
	debugObject(o);
	Vector radii( x_radius,y_radius ? y_radius : x_radius,x_radius );
	o->setCollisionRadii( radii );
}

void  bbEntityBox( Object *o,float x,float y,float z,float w,float h,float d ){
	debugObject(o);
	Box b( Vector(x,y,z) );
	b.update( Vector( x+w,y+h,z+d ) );
	o->setCollisionBox( b );
}

Object *  bbEntityCollided( Object *o,int type ){
	debugObject(o);
	Object::Collisions::const_iterator it;
	const Object::Collisions &c=o->getCollisions();
	for( it=c.begin();it!=c.end();++it ){
		const ObjCollision *c=*it;
		if( c->with->getCollisionType()==type ) return c->with;
	}
	return 0;
}

int  bbCountCollisions( Object *o ){
	debugObject(o);
	return o->getCollisions().size();
}

float  bbCollisionX( Object *o,int index ){
	debugColl(o,index);
	return o->getCollisions()[index-1]->coords.x;
}

float  bbCollisionY( Object *o,int index ){
	debugColl(o,index);
	return o->getCollisions()[index-1]->coords.y;
}

float  bbCollisionZ( Object *o,int index ){
	debugColl(o,index);
	return o->getCollisions()[index-1]->coords.z;
}

float  bbCollisionNX( Object *o,int index ){
	debugColl(o,index);
	return o->getCollisions()[index-1]->collision.normal.x;
}

float  bbCollisionNY( Object *o,int index ){
	debugColl(o,index);
	return o->getCollisions()[index-1]->collision.normal.y;
}

float  bbCollisionNZ( Object *o,int index ){
	debugColl(o,index);
	return o->getCollisions()[index-1]->collision.normal.z;
}

float  bbCollisionTime( Object *o,int index ){
	debugColl(o,index);
	return o->getCollisions()[index-1]->collision.time;
}

Object *  bbCollisionEntity( Object *o,int index ){
	debugColl(o,index);
	return o->getCollisions()[index-1]->with;
}

void *  bbCollisionSurface( Object *o,int index ){
	debugColl(o,index);
	return o->getCollisions()[index-1]->collision.surface;
}

int  bbCollisionTriangle( Object *o,int index ){
	debugColl(o,index);
	return o->getCollisions()[index-1]->collision.index;
}

float  bbEntityDistance( Entity *src,Entity *dest ){
	debugEntity(src);
	debugEntity(dest);
	return src->getWorldPosition().distance( dest->getWorldPosition() );
}

////////////////////////////////////
// ENTITY TRANSFORMATION COMMANDS //
////////////////////////////////////
void  bbMoveEntity( Entity *e,float x,float y,float z ){
	debugEntity(e);
	e->setLocalPosition( e->getLocalPosition()+e->getLocalRotation()*Vector(x,y,z) );
}

void  bbTurnEntity( Entity *e,float p,float y,float r,int global ){
	debugEntity(e);
	global?
	e->setWorldRotation( rotationQuat( p*dtor,y*dtor,r*dtor )*e->getWorldRotation() ):
	e->setLocalRotation( e->getLocalRotation()*rotationQuat( p*dtor,y*dtor,r*dtor ) );
}

void  bbTranslateEntity( Entity *e,float x,float y,float z,int global ){
	debugEntity(e);
	global?
	e->setWorldPosition( e->getWorldPosition()+Vector( x,y,z ) ):
	e->setLocalPosition( e->getLocalPosition()+Vector( x,y,z ) );
}

void  bbPositionEntity( Entity *e,float x,float y,float z,int global ){
	debugEntity(e);
	global?
	e->setWorldPosition(Vector(x,y,z)):
	e->setLocalPosition(Vector(x,y,z));
}

void  bbScaleEntity( Entity *e,float x,float y,float z,int global ){
	debugEntity(e);
	global?
	e->setWorldScale(Vector(x,y,z)):
	e->setLocalScale(Vector(x,y,z));
}

void  bbRotateEntity( Entity *e,float p,float y,float r,int global ){
	debugEntity(e);
	global?
	e->setWorldRotation( rotationQuat( p*dtor,y*dtor,r*dtor ) ):
	e->setLocalRotation( rotationQuat( p*dtor,y*dtor,r*dtor ) );
}

void  bbPointEntity( Entity *e,Entity *t,float roll ){
	if( debug ){ debugEntity(e);debugEntity(t); }
	Vector v=t->getWorldTform().v-e->getWorldTform().v;
	e->setWorldRotation( rotationQuat( v.pitch(),v.yaw(),roll*dtor ) );
}

void  bbAlignToVector( Entity *e,float nx,float ny,float nz,int axis,float rate ){
	Vector ax( nx,ny,nz );
	float l=ax.length();
	if( l<=EPSILON ) return;
	ax/=l;

	Quat q=e->getWorldRotation();
	Vector tv=(axis==1) ? q.i() : (axis==2 ? q.j() : q.k());

	float dp=ax.dot( tv );

	if( dp>=1-EPSILON ) return;

	if( dp<=-1+EPSILON ){
		float an=PI*rate/2;
		Vector cp=(axis==1) ? q.j() : (axis==2 ? q.k() : q.i());
		e->setWorldRotation( Quat( cosf(an),cp*sinf(an) ) * q );
		return;
	}

	float an=acosf( dp )*rate/2;
	Vector cp=ax.cross( tv ).normalized();
	e->setWorldRotation( Quat( cosf(an),cp*sinf(an) ) * q );
}

//////////////////////////
// ENTITY MISC COMMANDS //
//////////////////////////
void  bbNameEntity( Entity *e,BBStr *t ){
	debugEntity(e);
	e->setName( *t );
	delete t;
}

BBStr *  bbEntityName( Entity *e ){
	debugEntity(e);
	return d_new BBStr( e->getName() );
}

BBStr *bbEntityClass( Entity *e ){
	debugEntity(e);
	const char *p="Pivot";
	if( e->getLight() ) p="Light";
	else if( e->getCamera() ) p="Camera";
	else if( e->getMirror() ) p="Mirror";
	else if( e->getListener() ) p="Listener";
	else if( Model *t=e->getModel() ){
		if( t->getSprite() ) p="Sprite";
		else if( t->getTerrain() ) p="Terrain";
		else if( t->getPlaneModel() ) p="Plane";
		else if( t->getMeshModel() ) p="Mesh";
		else if( t->getMD2Model() ) p="MD2";
		else if( t->getBSPModel() ) p="BSP";
	}
	return new BBStr(p);
}

void  bbClearWorld( int e,int b,int t ){
	if( e ){
		while( Entity::orphans() ) bbFreeEntity( Entity::orphans() );
	}
	if( b ){
		while( brush_set.size() ) bbFreeBrush( *brush_set.begin() );
	}
	if( t ){
		while( texture_set.size() ) bbFreeTexture( *texture_set.begin() );
	}
}

extern int active_texs;

int  bbActiveTextures(){
	return active_texs;
}

void blitz3d_open(){
	gx_scene=gx_graphics->createScene( 0 );
	if( !gx_scene ) RTEX( "Unable to create 3D Scene" );
	world=d_new World();
	projected=Vector();
	picked.collision=Collision();
	picked.with=0;picked.coords=Vector();
	Texture::clearFilters();
	Texture::addFilter( "",gxCanvas::CANVAS_TEX_RGB|gxCanvas::CANVAS_TEX_MIPMAP );
	loader_mat_map.clear();
	loader_mat_map["x"]=Transform();
	loader_mat_map["3ds"]=Transform(Matrix(Vector(1,0,0),Vector(0,0,1),Vector(0,1,0)));
	listener=0;
	stats_mode=false;
}

void blitz3d_close(){
	if( !gx_scene ) return;
	bbClearWorld( 1,1,1 );
	Texture::clearFilters();
	loader_mat_map.clear();
	delete world;
	gx_graphics->freeScene( gx_scene );
	gx_scene=0;
}

bool blitz3d_create(){
	tri_count=0;
	gx_scene=0;world=0;
	return true;
}

bool blitz3d_destroy(){
	blitz3d_close();
	return true;
}

void blitz3d_link( void (*rtSym)( const char *sym,void *pc ) ){
	rtSym( "LoaderMatrix$file_ext#xx#xy#xz#yx#yy#yz#zx#zy#zz",bbLoaderMatrix );
	rtSym( "HWMultiTex%enable",bbHWMultiTex );
	rtSym( "%HWTexUnits",bbHWTexUnits );
	rtSym( "%GfxDriverCaps3D",bbGfxDriverCaps3D );
	rtSym( "WBuffer%enable",bbWBuffer );
	rtSym( "Dither%enable",bbDither );
	rtSym( "AntiAlias%enable",bbAntiAlias );
	rtSym( "WireFrame%enable",bbWireFrame );
	rtSym( "AmbientLight#red#green#blue",bbAmbientLight );
	rtSym( "ClearCollisions",bbClearCollisions );
	rtSym( "Collisions%source_type%destination_type%method%response",bbCollisions );
	rtSym( "UpdateWorld#elapsed_time=1",bbUpdateWorld );
	rtSym( "CaptureWorld",bbCaptureWorld );
	rtSym( "RenderWorld#tween=1",bbRenderWorld );
	rtSym( "ClearWorld%entities=1%brushes=1%textures=1",bbClearWorld );
	rtSym( "%ActiveTextures",bbActiveTextures );
	rtSym( "%TrisRendered",bbTrisRendered );
	rtSym( "#Stats3D%type",bbStats3D );

	rtSym( "%CreateTexture%width%height%flags=0%frames=1",bbCreateTexture );
	rtSym( "%LoadTexture$file%flags=1",bbLoadTexture );
	rtSym( "%LoadAnimTexture$file%flags%width%height%first%count",bbLoadAnimTexture );
	rtSym( "FreeTexture%texture",bbFreeTexture );
	rtSym( "TextureBlend%texture%blend",bbTextureBlend );
	rtSym( "TextureCoords%texture%coords",bbTextureCoords );
	rtSym( "ScaleTexture%texture#u_scale#v_scale",bbScaleTexture );
	rtSym( "RotateTexture%texture#angle",bbRotateTexture );
	rtSym( "PositionTexture%texture#u_offset#v_offset",bbPositionTexture );
	rtSym( "%TextureWidth%texture",bbTextureWidth );
	rtSym( "%TextureHeight%texture",bbTextureHeight );
	rtSym( "$TextureName%texture",bbTextureName );
	rtSym( "SetCubeFace%texture%face",bbSetCubeFace );
	rtSym( "SetCubeMode%texture%mode",bbSetCubeMode );
	rtSym( "%TextureBuffer%texture%frame=0",bbTextureBuffer );
	rtSym( "ClearTextureFilters",bbClearTextureFilters );
	rtSym( "TextureFilter$match_text%texture_flags=0",bbTextureFilter );

	rtSym( "%CreateBrush#red=255#green=255#blue=255",bbCreateBrush );
	rtSym( "%LoadBrush$file%texture_flags=1#u_scale=1#v_scale=1",bbLoadBrush );
	rtSym( "FreeBrush%brush",bbFreeBrush );
	rtSym( "BrushColor%brush#red#green#blue",bbBrushColor );
	rtSym( "BrushAlpha%brush#alpha",bbBrushAlpha );
	rtSym( "BrushShininess%brush#shininess",bbBrushShininess );
	rtSym( "BrushTexture%brush%texture%frame=0%index=0",bbBrushTexture );
	rtSym( "%GetBrushTexture%brush%index=0",bbGetBrushTexture );
	rtSym( "BrushBlend%brush%blend",bbBrushBlend );
	rtSym( "BrushFX%brush%fx",bbBrushFX );

	rtSym( "%LoadMesh$file%parent=0",bbLoadMesh );
	rtSym( "%LoadAnimMesh$file%parent=0",bbLoadAnimMesh );
	rtSym( "%LoadAnimSeq%entity$file",bbLoadAnimSeq );

	rtSym( "%CreateMesh%parent=0",bbCreateMesh );
	rtSym( "%CreateCube%parent=0",bbCreateCube );
	rtSym( "%CreateSphere%segments=8%parent=0",bbCreateSphere );
	rtSym( "%CreateCylinder%segments=8%solid=1%parent=0",bbCreateCylinder );
	rtSym( "%CreateCone%segments=8%solid=1%parent=0",bbCreateCone );
	rtSym( "%CopyMesh%mesh%parent=0",bbCopyMesh );
	rtSym( "ScaleMesh%mesh#x_scale#y_scale#z_scale",bbScaleMesh );
	rtSym( "RotateMesh%mesh#pitch#yaw#roll",bbRotateMesh );
	rtSym( "PositionMesh%mesh#x#y#z",bbPositionMesh );
	rtSym( "FitMesh%mesh#x#y#z#width#height#depth%uniform=0",bbFitMesh );
	rtSym( "FlipMesh%mesh",bbFlipMesh );
	rtSym( "PaintMesh%mesh%brush",bbPaintMesh );
	rtSym( "AddMesh%source_mesh%dest_mesh",bbAddMesh );
	rtSym( "UpdateNormals%mesh",bbUpdateNormals );
	rtSym( "LightMesh%mesh#red#green#blue#range=0#x=0#y=0#z=0",bbLightMesh );
	rtSym( "#MeshWidth%mesh",bbMeshWidth );
	rtSym( "#MeshHeight%mesh",bbMeshHeight );
	rtSym( "#MeshDepth%mesh",bbMeshDepth );
	rtSym( "%MeshesIntersect%mesh_a%mesh_b",bbMeshesIntersect );
	rtSym( "%CountSurfaces%mesh",bbCountSurfaces );
	rtSym( "%GetSurface%mesh%surface_index",bbGetSurface );
	rtSym( "MeshCullBox%mesh#x#y#z#width#height#depth",bbMeshCullBox );
	
	rtSym( "%CreateSurface%mesh%brush=0",bbCreateSurface );
	rtSym( "%GetSurfaceBrush%surface",bbGetSurfaceBrush );
	rtSym( "%GetEntityBrush%entity",bbGetEntityBrush );
	rtSym( "%FindSurface%mesh%brush",bbFindSurface );
	rtSym( "ClearSurface%surface%clear_vertices=1%clear_triangles=1",bbClearSurface );
	rtSym( "PaintSurface%surface%brush",bbPaintSurface );
	rtSym( "%AddVertex%surface#x#y#z#u=0#v=0#w=1",bbAddVertex );
	rtSym( "%AddTriangle%surface%v0%v1%v2",bbAddTriangle );
	rtSym( "VertexCoords%surface%index#x#y#z",bbVertexCoords );
	rtSym( "VertexNormal%surface%index#nx#ny#nz",bbVertexNormal );
	rtSym( "VertexColor%surface%index#red#green#blue#alpha=1",bbVertexColor );
	rtSym( "VertexTexCoords%surface%index#u#v#w=1%coord_set=0",bbVertexTexCoords );
	rtSym( "%CountVertices%surface",bbCountVertices );
	rtSym( "%CountTriangles%surface",bbCountTriangles );
	rtSym( "#VertexX%surface%index",bbVertexX );
	rtSym( "#VertexY%surface%index",bbVertexY );
	rtSym( "#VertexZ%surface%index",bbVertexZ );
	rtSym( "#VertexNX%surface%index",bbVertexNX );
	rtSym( "#VertexNY%surface%index",bbVertexNY );
	rtSym( "#VertexNZ%surface%index",bbVertexNZ );
	rtSym( "#VertexRed%surface%index",bbVertexRed );
	rtSym( "#VertexGreen%surface%index",bbVertexGreen );
	rtSym( "#VertexBlue%surface%index",bbVertexBlue );
	rtSym( "#VertexAlpha%surface%index",bbVertexAlpha );
	rtSym( "#VertexU%surface%index%coord_set=0",bbVertexU );
	rtSym( "#VertexV%surface%index%coord_set=0",bbVertexV );
	rtSym( "#VertexW%surface%index%coord_set=0",bbVertexW );
	rtSym( "%TriangleVertex%surface%index%vertex",bbTriangleVertex );

	rtSym( "%CreateCamera%parent=0",bbCreateCamera );
	rtSym( "CameraZoom%camera#zoom",bbCameraZoom );
	rtSym( "CameraRange%camera#near#far",bbCameraRange );
	rtSym( "CameraClsColor%camera#red#green#blue",bbCameraClsColor );
	rtSym( "CameraClsMode%camera%cls_color%cls_zbuffer",bbCameraClsMode );
	rtSym( "CameraProjMode%camera%mode",bbCameraProjMode );
	rtSym( "CameraViewport%camera%x%y%width%height",bbCameraViewport );
	rtSym( "CameraFogColor%camera#red#green#blue",bbCameraFogColor );
	rtSym( "CameraFogRange%camera#near#far",bbCameraFogRange );
	rtSym( "CameraFogMode%camera%mode",bbCameraFogMode );
	rtSym( "CameraProject%camera#x#y#z",bbCameraProject );
	rtSym( "#ProjectedX",bbProjectedX );
	rtSym( "#ProjectedY",bbProjectedY );
	rtSym( "#ProjectedZ",bbProjectedZ );

	rtSym( "%EntityInView%entity%camera",bbEntityInView );
	rtSym( "%EntityVisible%src_entity%dest_entity",bbEntityVisible );

	rtSym( "%EntityPick%entity#range",bbEntityPick );
	rtSym( "%LinePick#x#y#z#dx#dy#dz#radius=0",bbLinePick );
	rtSym( "%CameraPick%camera#viewport_x#viewport_y",bbCameraPick );

	rtSym( "#PickedX",bbPickedX );
	rtSym( "#PickedY",bbPickedY );
	rtSym( "#PickedZ",bbPickedZ );
	rtSym( "#PickedNX",bbPickedNX );
	rtSym( "#PickedNY",bbPickedNY );
	rtSym( "#PickedNZ",bbPickedNZ );
	rtSym( "#PickedTime",bbPickedTime );
	rtSym( "%PickedEntity",bbPickedEntity );
	rtSym( "%PickedSurface",bbPickedSurface );
	rtSym( "%PickedTriangle",bbPickedTriangle );

	rtSym( "%CreateLight%type=1%parent=0",bbCreateLight );
	rtSym( "LightColor%light#red#green#blue",bbLightColor );
	rtSym( "LightRange%light#range",bbLightRange );
	rtSym( "LightConeAngles%light#inner_angle#outer_angle",bbLightConeAngles );

	rtSym( "%CreatePivot%parent=0",bbCreatePivot );

	rtSym( "%CreateSprite%parent=0",bbCreateSprite );
	rtSym( "%LoadSprite$file%texture_flags=1%parent=0",bbLoadSprite );
	rtSym( "RotateSprite%sprite#angle",bbRotateSprite );
	rtSym( "ScaleSprite%sprite#x_scale#y_scale",bbScaleSprite );
	rtSym( "HandleSprite%sprite#x_handle#y_handle",bbHandleSprite );
	rtSym( "SpriteViewMode%sprite%view_mode",bbSpriteViewMode );

	rtSym( "%LoadMD2$file%parent=0",bbLoadMD2 );
	rtSym( "AnimateMD2%md2%mode=1#speed=1%first_frame=0%last_frame=9999#transition=0",bbAnimateMD2 );
	rtSym( "#MD2AnimTime%md2",bbMD2AnimTime );
	rtSym( "%MD2AnimLength%md2",bbMD2AnimLength );
	rtSym( "%MD2Animating%md2",bbMD2Animating );

	rtSym( "%LoadBSP$file#gamma_adj=0%parent=0",bbLoadBSP );
	rtSym( "BSPLighting%bsp%use_lightmaps",bbBSPLighting );
	rtSym( "BSPAmbientLight%bsp#red#green#blue",bbBSPAmbientLight );

	rtSym( "%CreateMirror%parent=0",bbCreateMirror );

	rtSym( "%CreatePlane%segments=1%parent=0",bbCreatePlane );

	rtSym( "%CreateTerrain%grid_size%parent=0",bbCreateTerrain );
	rtSym( "%LoadTerrain$heightmap_file%parent=0",bbLoadTerrain );
	rtSym( "TerrainDetail%terrain%detail_level%morph=0",bbTerrainDetail );
	rtSym( "TerrainShading%terrain%enable",bbTerrainShading );
	rtSym( "#TerrainX%terrain#world_x#world_y#world_z",bbTerrainX );
	rtSym( "#TerrainY%terrain#world_x#world_y#world_z",bbTerrainY );
	rtSym( "#TerrainZ%terrain#world_x#world_y#world_z",bbTerrainZ );
	rtSym( "%TerrainSize%terrain",bbTerrainSize );
	rtSym( "#TerrainHeight%terrain%terrain_x%terrain_z",bbTerrainHeight );
	rtSym( "ModifyTerrain%terrain%terrain_x%terrain_z#height%realtime=0",bbModifyTerrain );

	rtSym( "%CreateListener%parent#rolloff_factor=1#doppler_scale=1#distance_scale=1",bbCreateListener );
	rtSym( "%EmitSound%sound%entity",bbEmitSound );

	rtSym( "%CopyEntity%entity%parent=0",bbCopyEntity );

	rtSym( "#EntityX%entity%global=0",bbEntityX );
	rtSym( "#EntityY%entity%global=0",bbEntityY );
	rtSym( "#EntityZ%entity%global=0",bbEntityZ );
	rtSym( "#EntityPitch%entity%global=0",bbEntityPitch );
	rtSym( "#EntityYaw%entity%global=0",bbEntityYaw );
	rtSym( "#EntityRoll%entity%global=0",bbEntityRoll );
	rtSym( "#GetMatElement%entity%row%column",bbGetMatElement );
	rtSym( "TFormPoint#x#y#z%source_entity%dest_entity",bbTFormPoint );
	rtSym( "TFormVector#x#y#z%source_entity%dest_entity",bbTFormVector );
	rtSym( "TFormNormal#x#y#z%source_entity%dest_entity",bbTFormNormal );
	rtSym( "#TFormedX",bbTFormedX );
	rtSym( "#TFormedY",bbTFormedY );
	rtSym( "#TFormedZ",bbTFormedZ );
	rtSym( "#VectorYaw#x#y#z",bbVectorYaw );
	rtSym( "#VectorPitch#x#y#z",bbVectorPitch );
	rtSym( "#DeltaPitch%src_entity%dest_entity",bbDeltaPitch );
	rtSym( "#DeltaYaw%src_entity%dest_entity",bbDeltaYaw );

	rtSym( "ResetEntity%entity",bbResetEntity );
	rtSym( "EntityType%entity%collision_type%recursive=0",bbEntityType );
	rtSym( "EntityPickMode%entity%pick_geometry%obscurer=1",bbEntityPickMode );
	rtSym( "%GetParent%entity",bbGetParent );
	rtSym( "%GetEntityType%entity",bbGetEntityType );
	rtSym( "EntityRadius%entity#x_radius#y_radius=0",bbEntityRadius );
	rtSym( "EntityBox%entity#x#y#z#width#height#depth",bbEntityBox );
	rtSym( "#EntityDistance%source_entity%destination_entity",bbEntityDistance );
	rtSym( "%EntityCollided%entity%type",bbEntityCollided );

	rtSym( "%CountCollisions%entity",bbCountCollisions );
	rtSym( "#CollisionX%entity%collision_index",bbCollisionX );
	rtSym( "#CollisionY%entity%collision_index",bbCollisionY );
	rtSym( "#CollisionZ%entity%collision_index",bbCollisionZ );
	rtSym( "#CollisionNX%entity%collision_index",bbCollisionNX );
	rtSym( "#CollisionNY%entity%collision_index",bbCollisionNY );
	rtSym( "#CollisionNZ%entity%collision_index",bbCollisionNZ );
	rtSym( "#CollisionTime%entity%collision_index",bbCollisionTime );
	rtSym( "%CollisionEntity%entity%collision_index",bbCollisionEntity );
	rtSym( "%CollisionSurface%entity%collision_index",bbCollisionSurface );
	rtSym( "%CollisionTriangle%entity%collision_index",bbCollisionTriangle );

	rtSym( "MoveEntity%entity#x#y#z",bbMoveEntity );
	rtSym( "TurnEntity%entity#pitch#yaw#roll%global=0",bbTurnEntity );
	rtSym( "TranslateEntity%entity#x#y#z%global=0",bbTranslateEntity );
	rtSym( "PositionEntity%entity#x#y#z%global=0",bbPositionEntity );
	rtSym( "ScaleEntity%entity#x_scale#y_scale#z_scale%global=0",bbScaleEntity );
	rtSym( "RotateEntity%entity#pitch#yaw#roll%global=0",bbRotateEntity );
	rtSym( "PointEntity%entity%target#roll=0",bbPointEntity );
	rtSym( "AlignToVector%entity#vector_x#vector_y#vector_z%axis#rate=1",bbAlignToVector );
	rtSym( "SetAnimTime%entity#time%anim_seq=0",bbSetAnimTime );
	rtSym( "Animate%entity%mode=1#speed=1%sequence=0#transition=0",bbAnimate );
	rtSym( "SetAnimKey%entity%frame%pos_key=1%rot_key=1%scale_key=1",bbSetAnimKey );
	rtSym( "%AddAnimSeq%entity%length",bbAddAnimSeq );
	rtSym( "%ExtractAnimSeq%entity%first_frame%last_frame%anim_seq=0",bbExtractAnimSeq );
	rtSym( "%AnimSeq%entity",bbAnimSeq );
	rtSym( "#AnimTime%entity",bbAnimTime );
	rtSym( "%AnimLength%entity",bbAnimLength );
	rtSym( "%Animating%entity",bbAnimating );

	rtSym( "EntityParent%entity%parent%global=1",bbEntityParent );
	rtSym( "%CountChildren%entity",bbCountChildren );
	rtSym( "%GetChild%entity%index",bbGetChild );
	rtSym( "%FindChild%entity$name",bbFindChild );

	rtSym( "PaintEntity%entity%brush",bbPaintEntity );
	rtSym( "EntityColor%entity#red#green#blue",bbEntityColor );
	rtSym( "EntityAlpha%entity#alpha",bbEntityAlpha );
	rtSym( "EntityShininess%entity#shininess",bbEntityShininess );
	rtSym( "EntityTexture%entity%texture%frame=0%index=0",bbEntityTexture );
	rtSym( "EntityBlend%entity%blend",bbEntityBlend );
	rtSym( "EntityFX%entity%fx",bbEntityFX );
	rtSym( "EntityAutoFade%entity#near#far",bbEntityAutoFade );
	rtSym( "EntityOrder%entity%order",bbEntityOrder );
	rtSym( "HideEntity%entity",bbHideEntity );
	rtSym( "ShowEntity%entity",bbShowEntity );
	rtSym( "FreeEntity%entity",bbFreeEntity );

	rtSym( "NameEntity%entity$name",bbNameEntity );
	rtSym( "$EntityName%entity",bbEntityName );
	rtSym( "$EntityClass%entity",bbEntityClass );
}

#endif
