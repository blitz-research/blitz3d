
#include "std.h"
#include "md2model.h"
#include "md2norms.h"

static Vector *normals;
static float white[]={1,1,1};

extern gxRuntime *gx_runtime;
extern gxGraphics *gx_graphics;

struct MD2Model::Rep{

	struct md2_header{
		int magic; 
		int version; 
		int skinWidth; 
		int skinHeight; 
		int frameSize; 
		int numSkins; 
		int numVertices; 
		int numTexCoords; 
		int numTriangles; 
		int numGlCommands; 
		int numFrames; 
		int offsetSkins; 
		int offsetTexCoords; 
		int offsetTriangles; 
		int offsetFrames; 
		int offsetGlCommands; 
		int offsetEnd;
	};
	
	struct md2_vertex{
		unsigned char x,y,z,n;
	};

	struct md2_texcoord{
		unsigned short s,t;
	};

	struct md2_triangle{
		unsigned short verts[3],tex_coords[3];
	};

	struct Frame{
		Vector scale,trans;
		vector<md2_vertex> verts;
	};

	struct TexCoords{
		float u,v;
	};

	int ref_cnt;

	int num_verts,num_frames;

	vector<Frame> frames;
	vector<TexCoords> tex_coords;
	gxMesh *mesh;

	Box box;

	ModelModel::Rep( const string &file ):
	ref_cnt(1),mesh(0){

		filebuf in;
		Header header;

		if( !in.open( file.c_str(),ios_base::in|ios_base::binary ) ) return;
		if( in.sgetn( (char*)&header,sizeof(header) )!=sizeof(header) ) return;
		if( header.magic!='2PDI' || header.version!=8 ) return;

		//read tex coords
		in.pubseekpos( header.offsetTexCoords );
		TexCoord *coords=d_new TexCoord[header.numTexCoords];
		in.sgetn( (char*)coords,header.numTexCoords*sizeof(TexCoord) );

		vector<VertInfo> verts;
		map<VertInfo,int> info_map;

		//build triangles
		vector<Triangle> triangles;
		in.pubseekpos( header.offsetTriangles );
		triangles.resize( header.numTriangles );
		for( k=0;k<triangles.size();++k ){
			unsigned short v[3],t[3];
			in.sgetn( (char*)v,6 );
			in.sgetn( (char*)t,6 );
			for( int j=0;j<3;++j ){
				unsigned char tu=coords[ t[j] ].s*256.0f/header.skinWidth;
				unsigned char tv=coords[ t[j] ].t*256.0f/header.skinHeight;
				VertInfo i( v[j],tu,tv );
				map<VertInfo,int>::iterator it=info_map.find( i );
				if( it==info_map.end() ){
					info_map[i]=triangles[k].v[j]=verts.size();
					verts.push_back( i );
				}else{
					triangles[k].v[j]=it->second;
				}
			}
		}
		delete coords;

		//load frames
		string tt="MD2 Frames:"+itoa( header.numFrames );
		gx_runtime->debugLog( tt.c_str() );

		in.pubseekpos( header.offsetFrames );
		frames.resize( header.numFrames );
		MD2Vertex *md2_verts=d_new MD2Vertex[header.numVertices];

		for( k=0;k<frames.size();++k ){

			Frame &frame=frames[k];

			//read frame header;
			in.sgetn( (char*)&frame.scale,12 );
			in.sgetn( (char*)&frame.trans,12 );
			in.sgetn( frame.name,16 );

			frame.trans=Vector(frame.trans.y,frame.trans.z,frame.trans.x);
			frame.scale=Vector(frame.scale.y,frame.scale.z,frame.scale.x);

			//read frame verts...
			in.sgetn( (char*)md2_verts,header.numVertices*4 );

			frame.vertices.resize( verts.size() );
			for( int j=0;j<verts.size();++j ){
				Vertex &v=frame.vertices[j];
				const VertInfo &i=verts[j];
				const MD2Vertex &m=md2_verts[i.index];
				v.x=m.y;v.y=m.z;v.z=m.x;
				v.u=i.u;v.v=i.v;v.n=m.n;
			}
		}
		delete md2_verts;

		//create initial mesh
		mesh=gx_graphics->createMesh( verts.size(),triangles.size(),0 );
		mesh->lock();
		for( k=0;k<triangles.size();++k ){
			const Triangle &t=triangles[k];
			mesh->setTriangle( k,t.v[0],t.v[2],t.v[1] );
		}
		mesh->unlock();

		//calculate bounding box.
		for( k=0;k<header.numFrames;++k ){
			const Frame &frame=frames[k];
			const Vector &scale=frame.scale;
			const Vector &trans=frame.trans;
			for( int n=0;n<frame.vertices.size();++n ){
				const Vertex &v=frame.vertices[n];
				box.update( Vector( v.x,v.y,v.z ) * scale + trans );
			}
		}

		if( !normals ){
			normals=(Vector*)md2norms;
			for( int k=0;k<sizeof(md2norms)/12;++k ){
				normals[k]=Vector(normals[k].y,normals[k].z,normals[k].x);
			}
		}



#pragma pack( push,1 )

	struct Header{
		int magic; 
		int version; 
		int skinWidth; 
		int skinHeight; 
		int frameSize; 
		int numSkins; 
		int numVertices; 
		int numTexCoords; 
		int numTriangles; 
		int numGlCommands; 
		int numFrames; 
		int offsetSkins; 
		int offsetTexCoords; 
		int offsetTriangles; 
		int offsetFrames; 
		int offsetGlCommands; 
		int offsetEnd;
	};

	struct MD2Vertex{
		unsigned char x,y,z,n;
	};

	struct Vertex{
		unsigned char x,y,z,u,v,n;
	};

	struct Triangle{
		unsigned short v[3];	//index into vertices
	};

	struct Frame{
		Vector scale;
		Vector trans;
		char name[16];
		vector<Vertex> vertices;
	};

	struct TexCoord{
		short s,t;
	};

	struct VertInfo{
		unsigned short index;
		unsigned char u,v;
		VertInfo( unsigned short i,char u,char v ):index(i),u(u),v(v){
		}
		bool operator<( const VertInfo &t )const{
			if( index<t.index ) return true;
			if( t.index<index ) return false;
			if( u<t.u ) return true;
			if( t.u<u ) return false;
			return v<t.v;
		}
	};

#pragma pack( pop )

	int ref_cnt;
	Header header;
	vector<Frame> frames;
	gxMesh *mesh;
	Box box;

	Rep( const string &file );
	~Rep();

	void render( MD2Model *model,float render_t,int render_a,int render_b );
};

MD2Model::Rep::Rep( const string &file ):
ref_cnt(1),mesh(0){

	filebuf in;
	if( !in.open( file.c_str(),ios_base::in|ios_base::binary ) ){
		return;
	}

	if( in.sgetn( (char*)&header,sizeof(header) )!=sizeof(header) ){
		return;
	}

	if( header.magic!='2PDI' || header.version!=8 ){
		return;
	}

	int k;

	//read tex coords
	in.pubseekpos( header.offsetTexCoords );
	TexCoord *coords=d_new TexCoord[header.numTexCoords];
	in.sgetn( (char*)coords,header.numTexCoords*sizeof(TexCoord) );

	vector<VertInfo> verts;
	map<VertInfo,int> info_map;

	//build triangles
	vector<Triangle> triangles;
	in.pubseekpos( header.offsetTriangles );
	triangles.resize( header.numTriangles );
	for( k=0;k<triangles.size();++k ){
		unsigned short v[3],t[3];
		in.sgetn( (char*)v,6 );
		in.sgetn( (char*)t,6 );
		for( int j=0;j<3;++j ){
			unsigned char tu=coords[ t[j] ].s*256.0f/header.skinWidth;
			unsigned char tv=coords[ t[j] ].t*256.0f/header.skinHeight;
			VertInfo i( v[j],tu,tv );
			map<VertInfo,int>::iterator it=info_map.find( i );
			if( it==info_map.end() ){
				info_map[i]=triangles[k].v[j]=verts.size();
				verts.push_back( i );
			}else{
				triangles[k].v[j]=it->second;
			}
		}
	}
	delete coords;

	//load frames
	string tt="MD2 Frames:"+itoa( header.numFrames );
	gx_runtime->debugLog( tt.c_str() );

	in.pubseekpos( header.offsetFrames );
	frames.resize( header.numFrames );
	MD2Vertex *md2_verts=d_new MD2Vertex[header.numVertices];

	for( k=0;k<frames.size();++k ){

		Frame &frame=frames[k];

		//read frame header;
		in.sgetn( (char*)&frame.scale,12 );
		in.sgetn( (char*)&frame.trans,12 );
		in.sgetn( frame.name,16 );

		frame.trans=Vector(frame.trans.y,frame.trans.z,frame.trans.x);
		frame.scale=Vector(frame.scale.y,frame.scale.z,frame.scale.x);

		//read frame verts...
		in.sgetn( (char*)md2_verts,header.numVertices*4 );

		frame.vertices.resize( verts.size() );
		for( int j=0;j<verts.size();++j ){
			Vertex &v=frame.vertices[j];
			const VertInfo &i=verts[j];
			const MD2Vertex &m=md2_verts[i.index];
			v.x=m.y;v.y=m.z;v.z=m.x;
			v.u=i.u;v.v=i.v;v.n=m.n;
		}
	}
	delete md2_verts;

	//create initial mesh
	mesh=gx_graphics->createMesh( verts.size(),triangles.size(),0 );
	mesh->lock();
	for( k=0;k<triangles.size();++k ){
		const Triangle &t=triangles[k];
		mesh->setTriangle( k,t.v[0],t.v[2],t.v[1] );
	}
	mesh->unlock();

	//calculate bounding box.
	for( k=0;k<header.numFrames;++k ){
		const Frame &frame=frames[k];
		const Vector &scale=frame.scale;
		const Vector &trans=frame.trans;
		for( int n=0;n<frame.vertices.size();++n ){
			const Vertex &v=frame.vertices[n];
			box.update( Vector( v.x,v.y,v.z ) * scale + trans );
		}
	}

	if( !normals ){
		normals=(Vector*)md2norms;
		for( int k=0;k<sizeof(md2norms)/12;++k ){
			normals[k]=Vector(normals[k].y,normals[k].z,normals[k].x);
		}
	}
}

MD2Model::Rep::~Rep(){
	if( mesh ) gx_graphics->freeMesh( mesh );
}

void MD2Model::Rep::render( MD2Model *model,float render_t,int render_a,int render_b ){

	const Frame &frame_a=frames[render_a];
	const Vector &scale_a=frame_a.scale;
	const Vector &trans_a=frame_a.trans;

	const Frame &frame_b=frames[render_b];
	const Vector &scale_b=frame_b.scale;
	const Vector &trans_b=frame_b.trans;

	mesh->lock();
	int k;
	for( k=0;k<frame_a.vertices.size();++k ){
		const Vertex &v_a=frame_a.vertices[k];
		const Vector &n_a=normals[v_a.n];
		Vector t_a( v_a.x*scale_a.x+trans_a.x,v_a.y*scale_a.y+trans_a.y,v_a.z*scale_a.z+trans_a.z );

		const Vertex &v_b=frame_b.vertices[k];
		const Vector &n_b=normals[v_b.n];
		Vector t_b( v_b.x*scale_b.x+trans_b.x,v_b.y*scale_b.y+trans_b.y,v_b.z*scale_b.z+trans_b.z );

		Vector t=(t_b-t_a)*render_t+t_a;
		Vector n=(n_b-n_a)*render_t+n_a;
		float tex_coords[]={ v_a.u/256.0f,v_a.v/256.0f,1,0,0,1 };
		mesh->setVertex( k,&t.x,&n.x,white,tex_coords );
	}
	mesh->unlock();

	model->enqueue( mesh,0,frame_a.vertices.size(),0,header.numTriangles );
}

MD2Model::MD2Model( const string &f ):
rep( d_new Rep( f ) ),
anim_mode(0),anim_time(0),
render_a(0),render_b(0),render_t(0){
}

MD2Model::MD2Model( const MD2Model &t ):
Model(t),rep( t.rep ),
anim_mode(0),anim_time(0),
render_a(0),render_b(0),render_t(0){
	++rep->ref_cnt;
}

MD2Model::~MD2Model(){
	if( !--rep->ref_cnt ) delete rep;
}

void MD2Model::startMD2Anim( int first,int last,int mode,float speed ){
	if( !speed && !mode ){ anim_mode=0;return; }
	if( first<0 ) first=0;
	else if( first>=rep->header.numFrames ) first=rep->header.numFrames-1;
	if( last<0 ) last=0;
	else if( last>=rep->header.numFrames ) last=rep->header.numFrames-1;
	if( first==last ){ anim_mode=0;render_a=render_b=first;render_t=0;return; }
	if( last<first ) std::swap( first,last );
	anim_first=first;
	anim_last=last;
	anim_len=last-first;
	anim_speed=speed;
	anim_time=speed>0 ? first : last;
	anim_mode=mode;
}

void MD2Model::animate( float e ){
	Model::animate( e );
	if( !anim_mode ) return;
	anim_time=anim_time+anim_speed * e;
	if( anim_time<anim_first ){
		switch( anim_mode ){
		case ANIM_MODE_LOOP:
			anim_time+=anim_len;
			break;
		case ANIM_MODE_PINGPONG:
			anim_time=anim_first+(anim_first-anim_time);
			anim_speed=-anim_speed;
			break;
		default:
			anim_time=anim_first;
			anim_mode=0;
			break;
		}
	}else if( anim_time>=anim_last ){
		switch( anim_mode ){
		case ANIM_MODE_LOOP:
			anim_time-=anim_len;
			break;
		case ANIM_MODE_PINGPONG:
			anim_time=anim_last-(anim_time-anim_last);
			anim_speed=-anim_speed;
			break;
		default:
			anim_time=anim_last;
			anim_mode=0;
			break;
		}
	}
	render_a=floor(anim_time);render_b=render_a+1;
	if( anim_mode==ANIM_MODE_LOOP && render_b==anim_last ) render_b=anim_first;
	render_t=anim_time-render_a;
}

void MD2Model::render( const RenderContext &rc ){
	static Frustum f;
	new( &f ) Frustum( rc.getWorldFrustum(),-getRenderTform() );
	if( !f.cull( rep->box ) ) return;

	rep->render( this,render_t,render_a,render_b );
}

int MD2Model::getMD2AnimLength()const{
	return rep->frames.size();
}

bool MD2Model::getValid()const{
	return rep->mesh!=0;
}
