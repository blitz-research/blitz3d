
#include "std.h"
#include "loader_x.h"
#include "meshmodel.h"
#include "animation.h"
#include "pivot.h"

#include <dxfile.h>
#include <rmxfguid.h>
#include <rmxftmpl.h>

extern gxRuntime *gx_runtime;
static map<string,MeshModel*> frames_map;
static int anim_len;

static bool conv,flip_tris;
static Transform conv_tform;
static bool collapse,animonly;

static void parseAnimKey( IDirectXFileData *fileData,MeshModel *e ){

	DWORD sz;int *data;
	if( fileData->GetData( 0,&sz,(void**)&data )<0 ) return;

	int type=*data++;
	int cnt=*data++;
	Animation anim=e->getAnimation();
	for( int k=0;k<cnt;++k ){
		int time=*data++;
		int n=*data++;
		if( time>anim_len ) anim_len=time;
		switch( type ){
		case 0:
			if( n==4 ){
				Quat rot=*(Quat*)data;
				if( conv ){
					if( fabs(rot.w)<1-EPSILON ){
						rot.normalize();
						//quat-to-axis/angle
						float half=acosf( rot.w );
						if( flip_tris ) half=-half;
						rot=Quat( cosf( half ),(conv_tform.m*rot.v).normalized()*sinf( half ) );
					}else rot=Quat();
				}
				anim.setRotationKey( time,rot );
			}
			break;
		case 1:
			if( n==3 ){
				Vector scl=*(Vector*)data;
				if( conv ) scl=conv_tform.m * scl;
				scl.x=fabs(scl.x);scl.y=fabs(scl.y);scl.z=fabs(scl.z);
				anim.setScaleKey( time,scl );
			}
			break;
		case 2:
			if( n==3 ){
				Vector pos=*(Vector*)data;
				if( conv ) pos=conv_tform*pos;
				anim.setPositionKey( time,pos );
			}
			break;
		}
		data+=n;
	}
	e->setAnimation( anim );
}

static void parseAnim( IDirectXFileData *fileData ){
	const GUID *guid;
	IDirectXFileObject *childObj;
	IDirectXFileData *childData;
	IDirectXFileDataReference *childRef;
	MeshModel *frame=0;

	//find the frame reference
	for( ;fileData->GetNextObject( &childObj )>=0;childObj->Release() ){
		if( childObj->QueryInterface( IID_IDirectXFileDataReference,(void**)&childRef )>=0 ){
			if( childRef->Resolve( &childData )>=0 ){
				if( childData->GetType( &guid )>=0 ){
					if( *guid==TID_D3DRMFrame ){
						char name[80];DWORD len=80;
						if( childData->GetName( name,&len )>=0 ){
							map<string,MeshModel*>::iterator it=frames_map.find( name );
							if( it!=frames_map.end() ) frame=it->second;
						}
					}
				}
				childData->Release();
			}
			childRef->Release();
		}else if( frame && childObj->QueryInterface( IID_IDirectXFileData,(void**)&childData )>=0 ){
			if( childData->GetType( &guid )>=0 ){
				if( *guid==TID_D3DRMAnimationKey ){
					parseAnimKey( childData,frame );
				}
			}
			childData->Release();
		}
	}
}

static void parseAnimSet( IDirectXFileData *fileData ){
	const GUID *guid;
	IDirectXFileObject *childObj;
	IDirectXFileData *childData;

	for( ;fileData->GetNextObject( &childObj )>=0;childObj->Release() ){
		if( childObj->QueryInterface( IID_IDirectXFileData,(void**)&childData )<0 ) continue;
		if( childData->GetType( &guid )>=0 ){
			if( *guid==TID_D3DRMAnimation ){
				parseAnim( childData );
			}
		}
		childData->Release();
	}
}

static Brush parseMaterial( IDirectXFileData *fileData ){
	const GUID *guid;
	IDirectXFileObject *childObj;
	IDirectXFileData *childData;

	Brush brush;

	DWORD sz;float *data;
	if( fileData->GetData( 0,&sz,(void**)&data )<0 ) return brush;

	brush.setColor( Vector( data[0],data[1],data[2] ) );
	if( data[3] ) brush.setAlpha( data[3] );

	for( ;fileData->GetNextObject( &childObj )>=0;childObj->Release() ){
		if( childObj->QueryInterface( IID_IDirectXFileData,(void**)&childData )<0 ) continue;
		if( childData->GetType( &guid )>=0 ){
			if( *guid==TID_D3DRMTextureFilename ){
				DWORD sz;char **data;
				if( childData->GetData( 0,&sz,(void**)&data )>=0 ){
					brush.setTexture( 0,Texture( *data,0 ),0 );
					brush.setColor( Vector( 1,1,1 ) );
				}
			}
		}
		childData->Release();
	}

	return brush;
}

static void parseMaterialList( IDirectXFileData *fileData,vector<Brush> &mats ){

	const GUID *guid;
	IDirectXFileObject *childObj;
	IDirectXFileData *childData;
	IDirectXFileDataReference *childRef;

	//iterate through child objects...
	for( ;fileData->GetNextObject( &childObj )>=0;childObj->Release() ){
		if( childObj->QueryInterface( IID_IDirectXFileData,(void**)&childData )>=0 ){
			if( childData->GetType( &guid )>=0 ){
				if( *guid==TID_D3DRMMaterial ){
					mats.push_back( parseMaterial( childData ) );
				}
            }
			childData->Release();
		}else if( childObj->QueryInterface( IID_IDirectXFileDataReference,(void**)&childRef )>=0 ){
			if( childRef->Resolve( &childData )>=0 ){
				if( childData->GetType( &guid )>=0 ){
					if( *guid==TID_D3DRMMaterial ){
						mats.push_back( parseMaterial( childData ) );
					}
				}
				childData->Release();
			}
			childRef->Release();
		}
	}
}

struct FaceX{
	int *data,mat_index;
	FaceX( int *d ):data(d),mat_index(0){}
};

static void parseMesh( IDirectXFileData *fileData,MeshModel *mesh ){

	const GUID *guid;
	IDirectXFileObject *childObj;
	IDirectXFileData *childData;

	DWORD sz;int *data;
	if( fileData->GetData( 0,&sz,(void**)&data )<0 ) return;

	//stuff...
	vector<FaceX> faces;
	vector<Brush> mats;

	MeshLoader::beginMesh();

	//setup vertices
	int num_verts=*data++;
	int k;
	for( k=0;k<num_verts;++k ){
		Surface::Vertex v;
		v.coords=*(Vector*)data;
		if( conv ) v.coords=conv_tform * v.coords;
		v.color=0xffffffff;//Vector(1,1,1);
		MeshLoader::addVertex( v );
		data+=3;
	}

	//setup faces
	int num_faces=*data++;
	for( k=0;k<num_faces;++k ){
		faces.push_back( FaceX( data ) );
		data+=*data+1;
	}

	bool normals=false;

	//get material and texture info
	for( ;fileData->GetNextObject( &childObj )>=0;childObj->Release() ){
		if( childObj->QueryInterface( IID_IDirectXFileData,(void**)&childData )<0 ) continue;
		if( childData->GetType( &guid )>=0 ){
			DWORD sz;int *data;
			if( childData->GetData( 0,&sz,(void**)&data )>=0 ){
				if( *guid==TID_D3DRMMeshMaterialList ){
					int num_mats=*data++;
					int num_faces=*data++;
					for( int k=0;k<num_faces;++k ){
						faces[k].mat_index=*data++;
					}
					parseMaterialList( childData,mats );
				}else if( *guid==TID_D3DRMMeshTextureCoords ){
					int num_coords=*data++;
					if( num_coords==num_verts ){
						float *coords=(float*)data;
						for( int k=0;k<num_coords;++k ){
							Surface::Vertex &v=MeshLoader::refVertex(k);
							float tu=*coords++;float tv=*coords++;
							v.tex_coords[0][0]=v.tex_coords[1][0]=tu;
							v.tex_coords[0][1]=v.tex_coords[1][1]=tv;
						}
					}
				}else if( *guid==TID_D3DRMMeshVertexColors ){
					int num_colors=*data++;
					if( num_colors==num_verts ){
						for( int k=0;k<num_colors;++k ){
							Surface::Vertex &v=MeshLoader::refVertex(*data++);
							float *t=(float*)data;
							v.color=0xff000000|(int(t[0]*255)<<16)|(int(t[1]*255)<<8)|int(t[2]*255);
//							v.color=Vector( t[0],t[1],t[2] );
							data+=4;
						}
					}
				}else if( *guid==TID_D3DRMMeshNormals ){
					int num_normals=*data++;
					if( num_normals==num_verts ){
						Matrix co=conv_tform.m.cofactor();
						for( int k=0;k<num_normals;++k ){
							Surface::Vertex &v=MeshLoader::refVertex(k);
							v.normal=(co * *(Vector*)data).normalized();
							data+=3;
						}
						normals=true;
					}
				}
			}
		}
		childData->Release();
	}
	if( !mats.size() ) mats.push_back( Brush() );

	for( k=0;k<faces.size();++k ){
		const FaceX &f=faces[k];
		int *data=f.data;
		int cnt=*data++;if( cnt<3 ) continue;
		int tri[3];
		tri[0]=data[0];
		for( int j=2;j<cnt;++j ){
			tri[1]=data[j-1+flip_tris];
			tri[2]=data[j-flip_tris];
			MeshLoader::addTriangle( tri,mats[f.mat_index] );
		}
	}

	MeshLoader::endMesh( mesh );

	if( !normals ) mesh->updateNormals();
}

static MeshModel *parseFrame( IDirectXFileData *fileData ){

	MeshModel *e=d_new MeshModel();

	const GUID *guid;
	IDirectXFileObject *childObj;
	IDirectXFileData *childData;

	char name[80];DWORD len=80;
	if( fileData->GetName( name,&len )<0 ) return e;

	e->setName( name );
	frames_map[name]=e;

	//iterate through child objects...
	for( ;fileData->GetNextObject( &childObj )>=0;childObj->Release() ){
		if( childObj->QueryInterface( IID_IDirectXFileData,(void**)&childData )<0 ) continue;
		if( childData->GetType( &guid )>=0 ){
			if( *guid==TID_D3DRMFrameTransformMatrix ){
				DWORD size;D3DMATRIX *data;
				if( childData->GetData( 0,&size,(void**)&data )>=0 ){
					Transform tform=Transform( Matrix(
						Vector( data->_11,data->_12,data->_13 ),
						Vector( data->_21,data->_22,data->_23 ),
						Vector( data->_31,data->_32,data->_33 ) ),
						Vector( data->_41,data->_42,data->_43 ) );
					if( conv ) tform=conv_tform * tform * -conv_tform;
					e->setLocalTform( tform );
				}
			}else if( *guid==TID_D3DRMMesh ){
				if( !animonly ) parseMesh( childData,e );
			}else if( *guid==TID_D3DRMFrame ){
				MeshModel *t=parseFrame( childData );
				t->setParent( e );
			}
		}
		childData->Release();
	}
	return e;
}

static MeshModel *parseFile( const string &file ){

	const GUID *guid;
	IDirectXFile *xfile;
	IDirectXFileData *fileData;
	IDirectXFileEnumObject *enumObj;

	if( DirectXFileCreate( &xfile )<0 ) return 0;

    if( xfile->RegisterTemplates( (VOID*)D3DRM_XTEMPLATES,D3DRM_XTEMPLATE_BYTES )<0 ){
		xfile->Release();return 0;
	}
	if( xfile->CreateEnumObject( (void*)file.c_str(),DXFILELOAD_FROMFILE,&enumObj )<0 ){
		xfile->Release();return 0;
	}

	anim_len=0;
	MeshModel *e=d_new MeshModel();
	for( ;enumObj->GetNextDataObject( &fileData )>=0;fileData->Release() ){
		if( fileData->GetType( &guid )<0 ) continue;

		if( *guid==TID_D3DRMMesh ){
			if( !animonly) parseMesh( fileData,e );
		}else if( *guid==TID_D3DRMFrame ){
			MeshModel *t=parseFrame( fileData );
			t->setParent( e );
		}else if( *guid==TID_D3DRMAnimationSet ){
			if( !collapse ) parseAnimSet( fileData );
		}
    }

	if( !collapse ){
		e->setAnimator( d_new Animator( e,anim_len ) );
	}

	enumObj->Release();
	xfile->Release();
	return e;
}

MeshModel *Loader_X::load( const string &filename,const Transform &t,int hint ){
	conv_tform=t;
	conv=flip_tris=false;
	if( conv_tform!=Transform() ){
		conv=true;
		if( conv_tform.m.i.cross(conv_tform.m.j).dot(conv_tform.m.k)<0 ) flip_tris=true;
	}
	collapse=!!(hint&MeshLoader::HINT_COLLAPSE);
	animonly=!!(hint&MeshLoader::HINT_ANIMONLY);

	MeshModel *e=parseFile( filename );
	frames_map.clear();
	return e;
}
