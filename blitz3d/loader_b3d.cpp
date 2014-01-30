
#include "std.h"
#include "loader_b3d.h"
#include "meshmodel.h"
#include "pivot.h"
#include "meshutil.h"

//#define SHOW_BONES

static FILE *in;
static vector<int> chunk_stack;
static vector<Texture> textures;
static vector<Brush> brushes;
static vector<Object*> bones;

static bool collapse;
static bool animonly;

static int swap_endian( int n ){
	return ((n&0xff)<<24)|((n&0xff00)<<8)|((n&0xff0000)>>8)|((n&0xff000000)>>24);
}

static void clear(){
	bones.clear();
	brushes.clear();
	textures.clear();
	chunk_stack.clear();
}

static int readChunk(){
	int header[2];
	if( fread( header,8,1,in )<1 ) return 0;
	chunk_stack.push_back( ftell( in )+header[1] );
	return swap_endian( header[0] );
}

static void exitChunk(){
	fseek( in,chunk_stack.back(),SEEK_SET );
	chunk_stack.pop_back();
}

static int chunkSize(){
	return chunk_stack.back()-ftell( in );
}

static void read( void *buf,int n ){
	fread( buf,n,1,in );
}

static void skip( int n ){
	fseek( in,n,SEEK_CUR );
}

static int readInt(){
	int n;
	read( &n,4 );
	return n;
}

static void readIntArray( int t[],int n ){
	read( t,n*4 );
}

static float readFloat(){
	float n;
	read( &n,4 );
	return n;
}

static void readFloatArray( float t[],int n ){
	read( t,n*4 );
}

static void readColor( unsigned *t ){
	float r=readFloat();if(r<0) r=0;else if(r>1) r=1;
	float g=readFloat();if(g<0) g=0;else if(g>1) g=1;
	float b=readFloat();if(b<0) b=0;else if(b>1) b=1;
	float a=readFloat();if(a<0) a=0;else if(a>1) a=1;
	*t=(int(a*255)<<24)|(int(r*255)<<16)|(int(g*255)<<8)|int(b*255);
}

static string readString(){
	string t;
	for(;;){
		char c;
		read( &c,1 );
		if( !c ) return t;
		t+=c;
	}
}

static void readTextures(){
	while( chunkSize() ){
		string name=readString();
		int flags=readInt();
		int blend=readInt();
		float pos[2],scl[2];
		readFloatArray( pos,2 );
		readFloatArray( scl,2 );
		float rot=readFloat();

		//create texture
		Texture tex( name,flags & 0xffff );

		tex.setBlend( blend );
		if( flags & 0x10000 ) tex.setFlags( gxScene::TEX_COORDS2 );

		if( pos[0]!=0 || pos[1]!=0 ) tex.setPosition( pos[0],pos[1] );
		if( scl[0]!=1 || scl[1]!=1 ) tex.setScale( scl[0],scl[1] );
		if( rot!=0 ) tex.setRotation( rot );

		textures.push_back( tex );
	}
}

static void readBrushes(){
	int n_texs=readInt();

	int tex_id[8]={-1,-1,-1,-1,-1,-1,-1,-1};

	while( chunkSize() ){
		string name=readString();
		float col[4];
		readFloatArray( col,4 );
		float shi=readFloat();
		int blend=readInt();
		int fx=readInt();
		readIntArray( tex_id,n_texs );

		Brush bru;

		bru.setColor( Vector( col[0],col[1],col[2] ) );
		bru.setAlpha( col[3] );
		bru.setShininess( shi );
		bru.setBlend( blend );
		bru.setFX( fx );

		for( int k=0;k<8;++k ){
			if( tex_id[k]<0 ) continue;
			bru.setTexture( k,textures[tex_id[k]],0 );
		}

		brushes.push_back( bru );
	}
}

static int readVertices(){

	int flags=readInt();
	int tc_sets=readInt();
	int tc_size=readInt();

	float tc[4]={0};

	Surface::Vertex t;
	while( chunkSize() ){
		readFloatArray( t.coords,3 );
		if( flags&1 ){
			readFloatArray( t.normal,3 );
		}
		if( flags&2 ){
			readColor( &t.color );
		}
		for( int k=0;k<tc_sets;++k ){
			readFloatArray( tc,tc_size );
			if( k<2 ) memcpy( t.tex_coords[k],tc,8 );
		}
		MeshLoader::addVertex( t );
	}

	return flags;
}

static void readTriangles(){
	int brush_id=readInt();
	Brush b=brush_id>=0 ? brushes[brush_id] : Brush();
	while( chunkSize() ){
		int verts[3];
		readIntArray( verts,3 );
		MeshLoader::addTriangle( verts,b );
	}
}

static int readMesh(){
	int flags=0;
	while( chunkSize() ){
		switch( readChunk() ){
		case 'VRTS':
			flags=readVertices();
			break;
		case 'TRIS':
			readTriangles();
			break;
		}
		exitChunk();
	}
	return flags;
}

static Object *readBone(){

#ifdef SHOW_BONES
	Brush b;
	b.setColor( Vector( 1,0,0 ) );
	b.setAlpha( .75f );
	MeshModel *bone=MeshUtil::createSphere( b,16 );
	Transform t;
	t.m.i.x=.1f;
	t.m.j.y=.1f;
	t.m.k.z=.1f;
	bone->transform( t );
#else
	Pivot *bone=d_new Pivot();
#endif

	bones.push_back( bone );

	while( chunkSize() ){
		int vert=readInt();
		float weight=readFloat();
		MeshLoader::addBone( vert,weight,bones.size() );
	}
	return bone;
}

static void readKeys( Animation &anim ){
	int flags=readInt();
	while( chunkSize() ){
		int frame=readInt();
		if( flags&1 ){
			float pos[3];
			readFloatArray( pos,3 );
			anim.setPositionKey( frame,Vector(pos[0],pos[1],pos[2]) );
		}
		if( flags&2 ){
			float scl[3];
			readFloatArray( scl,3 );
			anim.setScaleKey( frame,Vector(scl[0],scl[1],scl[2]) );
		}
		if( flags&4 ){
			float rot[4];
			readFloatArray( rot,4 );
			anim.setRotationKey( frame,Quat(rot[0],Vector(rot[1],rot[2],rot[3])) );
		}
	}
}

static Object *readObject( Object *parent ){

	Object *obj=0;

	string name=readString();
	float pos[3],scl[3],rot[4];
	readFloatArray( pos,3 );
	readFloatArray( scl,3 );
	readFloatArray( rot,4 );

	Animation keys;
	int anim_len=0;
	MeshModel *mesh=0;
	int mesh_flags,mesh_brush;

	while( chunkSize() ){
		switch( readChunk() ){
		case 'MESH':
			MeshLoader::beginMesh();
			obj=mesh=d_new MeshModel();
			mesh_brush=readInt();
			mesh_flags=readMesh();
			break;
		case 'BONE':
			obj=readBone();
			break;
		case 'KEYS':
			readKeys( keys );
			break;
		case 'ANIM':
			readInt();
			anim_len=readInt();
			readFloat();
			break;
		case 'NODE':
			if( !obj ) obj=d_new MeshModel();
			readObject( obj );
			break;
		}
		exitChunk();
	}

	if( !obj ) obj=d_new MeshModel();

	obj->setName( name );
	obj->setLocalPosition( Vector( pos[0],pos[1],pos[2] ) );
	obj->setLocalScale( Vector( scl[0],scl[1],scl[2] ) );
	obj->setLocalRotation( Quat( rot[0],Vector( rot[1],rot[2],rot[3] ) ) );
	obj->setAnimation( keys );

	if( mesh ){
		MeshLoader::endMesh( mesh );
		if( !(mesh_flags&1) ) mesh->updateNormals();
		if( mesh_brush!=-1 ) mesh->setBrush( brushes[mesh_brush] );
	}

	if( mesh && bones.size() ){
		bones.insert( bones.begin(),mesh );
		mesh->setAnimator( d_new Animator( bones,anim_len ) );
		mesh->createBones();
		bones.clear();
	}else if( anim_len ){
		obj->setAnimator( d_new Animator( obj,anim_len ) );
	}

	if( parent ) obj->setParent( parent );

	return obj;
}

MeshModel *Loader_B3D::load( const string &f,const Transform &conv,int hint ){

	collapse=!!(hint&MeshLoader::HINT_COLLAPSE);
	animonly=!!(hint&MeshLoader::HINT_ANIMONLY);

	in=fopen( f.c_str(),"rb" );
	if( !in ) return 0;

	::clear();

	int tag=readChunk();
	if( tag!='BB3D' ){
		fclose( in );
		return 0;
	}

	int version=readInt();
	if( version>1 ){
		fclose( in );
		return 0;
	}

	Object *obj=0;
	while( chunkSize() ){
		switch( readChunk() ){
		case 'TEXS':
			readTextures();
			break;
		case 'BRUS':
			readBrushes();
			break;
		case 'NODE':
			obj=readObject( 0 );
			break;
		}
		exitChunk();
	}
	fclose( in );

	::clear();

	return obj ? obj->getModel()->getMeshModel() : 0;
}
