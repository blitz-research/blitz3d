
#include "std.h"
#include "loader_3ds.h"
#include "meshmodel.h"
#include "animation.h"

extern gxRuntime *gx_runtime;

#ifdef BETA
#define _log( X ) gx_runtime->debugLog( (string(X)).c_str() );
#else
#define _log( X )
#endif

static filebuf in;
static int chunk_end;
static vector<int> parent_end;
static unsigned short anim_len;

static bool conv,flip_tris;
static Transform conv_tform;
static bool collapse,animonly;

struct Face3DS{
	int verts[3];
	Brush brush;
};

static vector<Face3DS> faces;
//static vector<Surface::Vertex> vertices;

static map<string,Brush> materials_map;
static map<string,MeshModel*> name_map;
static map<int,MeshModel*> id_map;

static int nextChunk(){
	in.pubseekoff( chunk_end,ios_base::beg );
	if( chunk_end==parent_end.back() ) return 0;
	unsigned short id;int len;
	in.sgetn( (char*)&id,2 );
	in.sgetn( (char*)&len,4 );
	chunk_end=(int)in.pubseekoff( 0,ios_base::cur )+len-6;
	return id;
}

static void enterChunk(){
	parent_end.push_back( chunk_end );
	chunk_end=(int)in.pubseekoff( 0,ios_base::cur );
}

static void leaveChunk(){
	chunk_end=parent_end.back();
	parent_end.pop_back();
}

static string parseString(){
	string t;
	while( int c=in.sbumpc() ) t+=char(c);
	return t;
}

enum {
	CHUNK_RGBF      	= 0x0010,
	CHUNK_RGBB      	= 0x0011,
//  CHUNK_RBGB2     	= 0x0012,	// ?? NOT HLS.
	CHUNK_MAIN      	= 0x4D4D,
     CHUNK_SCENE        = 0x3D3D,
      CHUNK_BKGCOLOR    = 0x1200,
	  CHUNK_AMBCOLOR  	= 0x2100,
	  CHUNK_OBJECT  	= 0x4000,
	   CHUNK_TRIMESH   	= 0x4100,
		CHUNK_VERTLIST  = 0x4110,
		CHUNK_FACELIST  = 0x4120,
		CHUNK_FACEMAT   = 0x4130,
		CHUNK_MAPLIST   = 0x4140,
		CHUNK_SMOOLIST  = 0x4150,
		CHUNK_TRMATRIX  = 0x4160,
	   CHUNK_LIGHT     	= 0x4600,
		CHUNK_SPOTLIGHT = 0x4610,
	   CHUNK_CAMERA    	= 0x4700,
	 CHUNK_MATERIAL  	= 0xAFFF,
	  CHUNK_MATNAME   	= 0xA000,
	  CHUNK_AMBIENT   	= 0xA010,
	  CHUNK_DIFFUSE   	= 0xA020,
	  CHUNK_SPECULAR  	= 0xA030,
	  CHUNK_TEXTURE   	= 0xA200,
	  CHUNK_BUMPMAP   	= 0xA230,
	  CHUNK_MAPFILE   	= 0xA300,
	 CHUNK_KEYFRAMER 	= 0xB000,
	  CHUNK_MESHINFO	= 0xB002,
	   CHUNK_HIERPOS	= 0xB030,
	   CHUNK_HIERINFO	= 0xB010,
	  CHUNK_FRAMES      = 0xB008
};

static Vector parseColor(){
	Vector v;
	unsigned char rgb[3];
	enterChunk();
	while( int id=nextChunk() ){
		switch( id ){
		case CHUNK_RGBF:
			in.sgetn( (char*)&v,12 );
			break;
		case CHUNK_RGBB:
			in.sgetn( (char*)rgb,3 );
			v=Vector( rgb[0]/255.0f,rgb[1]/255.0f,rgb[2]/255.0f );
		}
	}
	leaveChunk();
	return v;
}

static void parseVertList(){
	unsigned short cnt;
	in.sgetn( (char*)&cnt,2 );
	_log( "VertList cnt="+itoa(cnt) );
	while( cnt-- ){
		Surface::Vertex v;
		in.sgetn( (char*)&v.coords,12 );
		if( conv ) v.coords=conv_tform * v.coords;
		MeshLoader::addVertex( v );
	}
}

static void parseFaceMat(){
	string name=parseString();
	_log( "FaceMat: "+name );
	Brush mat=materials_map[name];
	unsigned short cnt;
	in.sgetn( (char*)&cnt,2 );
	while( cnt-- ){
		unsigned short face;
		in.sgetn( (char*)&face,2 );
		faces[face].brush=mat;
	}
}

static void parseFaceList(){
	unsigned short cnt;
	in.sgetn( (char*)&cnt,2 );
	_log( "FaceList cnt="+itoa(cnt) );
	while( cnt-- ){
		unsigned short v[4];
		in.sgetn( (char*)v,8 );
		Face3DS face;
		face.verts[0]=v[0];
		face.verts[1]=v[1];
		face.verts[2]=v[2];
		if( flip_tris ) std::swap( face.verts[1],face.verts[2] );
		faces.push_back( face );
	}
	enterChunk();
	while( int id=nextChunk() ){
		switch( id ){
		case CHUNK_FACEMAT:
			parseFaceMat();
			break;
		}
	}
	leaveChunk();
}

static void parseMapList(){
	_log( "MapList" );
	unsigned short cnt;
	in.sgetn( (char*)&cnt,2 );
	for( int k=0;k<cnt;++k ){
		float uv[2];
		in.sgetn( (char*)uv,8 );
		Surface::Vertex &v=MeshLoader::refVertex( k );
		v.tex_coords[0][0]=v.tex_coords[1][0]=uv[0];
		v.tex_coords[0][1]=v.tex_coords[1][1]=1-uv[1];
//		v->tex_coords[0]=v->tex_coords[1]=Vector( uv[0],1-uv[1],1 );
	}
}

static void parseTriMesh( MeshModel *mesh ){
	_log( "TriMesh" );
	enterChunk();
	Transform tform;

	faces.clear();

	MeshLoader::beginMesh();

	while( int id=nextChunk() ){
		switch( id ){
		case CHUNK_VERTLIST:
			if( !animonly ) parseVertList();
			break;
		case CHUNK_MAPLIST:
			if( !animonly ) parseMapList();
			break;
		case CHUNK_FACELIST:
			if( !animonly ) parseFaceList();
			break;
		case CHUNK_TRMATRIX:
			in.sgetn( (char*)&tform,48 );
			if( conv ) tform=conv_tform * tform * -conv_tform;
			break;
		}
	}
	leaveChunk();

	//should really do something here...
//	bool neg_x=tform.m.j.cross(tform.m.k).dot(tform.m.i)<0;

	int k;

	mesh->setWorldTform( tform );

	if( animonly ){
		MeshLoader::endMesh( 0 );
		return;
	}

	Transform inv_tform=-tform;
	for( k=0;k<MeshLoader::numVertices();++k ){
		Surface::Vertex &v=MeshLoader::refVertex( k );
		v.coords=inv_tform * v.coords;
	}

	for( k=0;k<faces.size();++k ){
		const Face3DS &f=faces[k];
		MeshLoader::addTriangle( f.verts,f.brush );
	}

	MeshLoader::endMesh( mesh );
	mesh->updateNormals();

	faces.clear();
}

static void parseObject( MeshModel *root ){
	//skip name
	string name=parseString();
	_log( "Object:"+name );
	MeshModel *mesh=0;

	enterChunk();
	while( int id=nextChunk() ){
		switch( id ){
		case CHUNK_TRIMESH:
			mesh=d_new MeshModel();
			mesh->setName( name );
			mesh->setParent( root );
			name_map[name]=mesh;
			parseTriMesh( mesh );
			break;
		}
	}
	leaveChunk();
}

static void parseMaterial(){
	_log( "Material" );
	Brush mat;
	string name,tex_name;
	enterChunk();
	while( int id=nextChunk() ){
		switch( id ){
		case CHUNK_MATNAME:
			name=parseString();
			break;
		case CHUNK_DIFFUSE:
			mat.setColor( parseColor() );
			break;
		case CHUNK_AMBIENT:
			break;
		case CHUNK_SPECULAR:
			break;
		case CHUNK_TEXTURE:
			enterChunk();
			while( int id=nextChunk() ){
				switch( id ){
				case CHUNK_MAPFILE:
					tex_name=parseString();
					break;
				}
			}
			leaveChunk();
			break;
		}
	}
	if( tex_name.size() ){
		mat.setTexture( 0,Texture( tex_name,0 ),0 ); 
		mat.setColor( Vector( 1,1,1 ) );
	}
	if( name.size() ){
		materials_map[name]=mat;
	}
	leaveChunk();
}

static void parseScene( MeshModel *root ){
	_log( "Scene" );
	enterChunk();
	while( int id=nextChunk() ){
		switch( id ){
		case CHUNK_OBJECT:
			parseObject( root );
			break;
		case CHUNK_MATERIAL:
			if( !animonly ) parseMaterial();
			break;
		}
	}
	leaveChunk();
}

static void parseAnimKeys( Animation *anim,int type ){

	int cnt=0;
	short t_flags;
	in.sgetn( (char*)&t_flags,2 );
	in.pubseekoff( 8,ios_base::cur );
	in.sgetn( (char*)&cnt,2 );
	in.pubseekoff( 2,ios_base::cur );
	_log( "ANIM_TRACK: frames="+itoa( cnt ) );
	Vector pos,axis,scale;
	float angle;
	Quat quat;
	for( int k=0;k<cnt;++k ){
		int time;
		short flags;
		in.sgetn( (char*)&time,4 );
		in.sgetn( (char*)&flags,2 );
		float tens=0,cont=0,bias=0,ease_to=0,ease_from=0;
		if( flags & 1 ) in.sgetn( (char*)&tens,4 );
		if( flags & 2 ) in.sgetn( (char*)&cont,4 );
		if( flags & 4 ) in.sgetn( (char*)&bias,4 );
		if( flags & 8 ) in.sgetn( (char*)&ease_to,4 );
		if( flags & 16 ) in.sgetn( (char*)&ease_from,4 );
		switch( type ){
		case 0xb020:	//POS_TRACK_TAG
			in.sgetn( (char*)&pos,12 );
			if( conv ) pos=conv_tform*pos;
//			_log( "POS_KEY: time="+itoa(time)+" pos="+ftoa( pos.x )+","+ftoa( pos.y )+","+ftoa( pos.z ) );
			if( time<=anim_len ) anim->setPositionKey( time,pos );
			break;
		case 0xb021:	//ROT_TRACK_TAG
			in.sgetn( (char*)&angle,4 );
			in.sgetn( (char*)&axis,12 );
//			_log( "ROT_KEY: time="+itoa(time)+" angle="+ftoa(angle)+" axis="+ftoa(axis.x)+","+ftoa(axis.y)+","+ftoa(axis.z) );
			if( axis.length()>EPSILON ){
				if( flip_tris ) angle=-angle;
				if( conv ) axis=conv_tform.m*axis;
				quat=Quat( cosf( angle/2 ),axis.normalized()*sinf( angle/2 ) )*quat;
				quat.normalize();
			}
			if( time<=anim_len ) anim->setRotationKey( time,quat );
			break;
		case 0xb022:	//SCL_TRACK_TAG
			in.sgetn( (char*)&scale,12 );
			if( conv ) scale=conv_tform.m*scale;
//			scale.x=fabs(scale.x);scale.y=fabs(scale.y);scale.z=fabs(scale.z);
			_log( "SCL_KEY: time="+itoa(time)+" scale="+ftoa( scale.x )+","+ftoa( scale.y )+","+ftoa( scale.z ) );
			if( time<=anim_len ) anim->setScaleKey( time,scale );
			break;
		}
	}
}

static void parseMeshInfo( MeshModel *root,float curr_time ){
	_log( "OBJECT_NODE_TAG" );
	enterChunk();
	string name,inst;
	Vector pivot;
	Animation anim;
	unsigned short id=65535,parent=65535,flags1,flags2;
	Box box( Vector(),Vector() );
	Vector box_centre;
	while( int chunk_id=nextChunk() ){
		switch( chunk_id ){
		case 0xb030:	//NODE_ID
			in.sgetn( (char*)&id,2 );
			_log( "NODE_ID: "+itoa(id) );
			break;
		case 0xb010:	//NODE_HDR
			name=parseString();
			in.sgetn( (char*)&flags1,2 );
			in.sgetn( (char*)&flags2,2 );
			in.sgetn( (char*)&parent,2 );
			_log( "NODE_HDR: name="+name+" parent="+itoa(parent) );
			break;
		case 0xb011:	//INSTANCE NAME
			inst=parseString();
			_log( "INSTANCE_NAME: "+inst );
			break;
		case 0xb013:	//PIVOT
			in.sgetn( (char*)&pivot,12 );
			if( conv ) pivot=conv_tform * pivot;
			_log( "PIVOT: "+ftoa(pivot.x)+","+ftoa(pivot.y)+","+ftoa(pivot.z) );
			break;
		case 0xb014:	//BOUNDBOX
			in.sgetn( (char*)&box.a,12 );
			in.sgetn( (char*)&box.b,12 );
			box_centre=box.centre();
			if( conv ) box_centre=conv_tform * box_centre;
			_log( "BOUNDBOX: min="+ftoa(box.a.x)+","+ftoa(box.a.y)+","+ftoa(box.a.z)+" max="+ftoa(box.b.x)+","+ftoa(box.b.y)+","+ftoa(box.b.z) );
			break;
		case 0xb020:	//POS_TRACK_TAG
		case 0xb021:	//ROT_TRACK_TAG
		case 0xb022:	//SCALE_TRACK_TAG
			if( !collapse ) parseAnimKeys( &anim,chunk_id );
			break;
		}
	}
	leaveChunk();

	MeshModel *p=root;
	if( parent!=65535 ){
		map<int,MeshModel*>::const_iterator it=id_map.find( parent );
		if( it==id_map.end() ) return;
		p=it->second;
	}
	MeshModel *mesh=0;
	if( name=="$$$DUMMY" ){
		mesh=d_new MeshModel();
		mesh->setName( inst );
		mesh->setParent( p );
	}else{
		map<string,MeshModel*>::const_iterator it=name_map.find( name );
		if( it==name_map.end() ) return;
		mesh=it->second;
		name_map.erase( name );
		if( pivot!=Vector() ){
			mesh->transform( -pivot );
		}
		Transform t=
		mesh->getWorldTform();
		mesh->setParent( p );
		mesh->setWorldTform( t );
	}

	mesh->setAnimation( anim );

	if( id!=65535 ) id_map[id]=mesh;
}

static void parseKeyFramer( MeshModel *root ){
	_log( "KeyFramer" );
	enterChunk();
	string file_3ds;
	unsigned short rev,curr_time=0;
	while( int id=nextChunk() ){
		switch( id ){
		case 0xb009:	//CURR_TIME
			in.sgetn( (char*)&curr_time,2 );
			_log( "CURR_TIME: "+itoa(curr_time) );
			break;
		case 0xb00a:	//KFHDR
			in.sgetn( (char*)&rev,2 );
			file_3ds=parseString();
			in.sgetn( (char*)&anim_len,2 );
			_log( "KFHDR: revision="+itoa(rev)+" 3dsfile="+file_3ds+" anim_len="+itoa(anim_len) );
			break;
		case 0xb002:	//object keyframer data...
			parseMeshInfo( root,curr_time );
			break;
		}
	}

	if( !collapse ){
		root->setAnimator( d_new Animator( root,anim_len ) );
	}

	leaveChunk();
}

static MeshModel *parseFile(){
	unsigned short id;int len;
	in.sgetn( (char*)&id,2 );
	in.sgetn( (char*)&len,4 );
	if( id!=CHUNK_MAIN ) return 0;
	chunk_end=(int)in.pubseekoff( 0,ios_base::cur )+len-6;

	enterChunk();
	MeshModel *root=d_new MeshModel();
	while( int id=nextChunk() ){
		switch( id ){
		case CHUNK_SCENE:
			parseScene( root );
			break;
		case CHUNK_KEYFRAMER:
			parseKeyFramer( root );
			break;
		}
	}
	leaveChunk();
	return root;
}

MeshModel *Loader_3DS::load( const string &filename,const Transform &t,int hint ){

	conv_tform=t;
	conv=flip_tris=false;
	if( conv_tform!=Transform() ){
		conv=true;
		if( conv_tform.m.i.cross(conv_tform.m.j).dot(conv_tform.m.k)<0 ) flip_tris=true;
	}

	collapse=!!(hint&MeshLoader::HINT_COLLAPSE);
	animonly=!!(hint&MeshLoader::HINT_ANIMONLY);

	if( !in.open( filename.c_str(),ios_base::in|ios_base::binary ) ){
		return 0;
	}

	MeshModel *root=parseFile();
	in.close();

	materials_map.clear();
	name_map.clear();
	id_map.clear();

	return root;
}
