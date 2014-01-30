
#include "std.h"
#include "surface.h"

extern gxGraphics *gx_graphics;

static Surface::Monitor nop_mon;

Surface::Surface():
mesh(0),mesh_vs(0),mesh_ts(0),valid_vs(0),valid_ts(0),mon( &nop_mon ){
}

Surface::Surface( Monitor *m ):
mesh(0),mesh_vs(0),mesh_ts(0),valid_vs(0),valid_ts(0),mon(m){
}

Surface::~Surface(){
	if( mesh ) gx_graphics->freeMesh( mesh );
}

void Surface::setBrush( const Brush &b ){
	brush=b;
	++mon->brush_changes;
}

void Surface::setName( const string &n ){
	name=n;
}

void Surface::clear( bool verts,bool tris ){
	if( verts ){ vertices.clear();valid_vs=0; }
	if( tris ){ triangles.clear();valid_ts=0; }
	++mon->geom_changes;
}

void Surface::addVertices( const vector<Vertex> &verts ){
	vertices.insert( vertices.end(),verts.begin(),verts.end() );
	++mon->geom_changes;
}

void Surface::setColor( int n,const Vector &v ){
	int r=floor(v.x*255);if(r<0)r=0;else if(r>255)r=255;
	int g=floor(v.y*255);if(g<0)g=0;else if(g>255)g=255;
	int b=floor(v.z*255);if(b<0)b=0;else if(b>255)b=255;

	unsigned argb=0xff000000|(r<<16)|(g<<8)|b;

	vertices[n].color=argb;
	if( n<valid_vs ) valid_vs=n;
}

Vector Surface::getColor( int n )const{
	float r=(vertices[n].color&0x00ff0000)>>16;
	float g=(vertices[n].color&0x0000ff00)>>8;
	float b= vertices[n].color&0x000000ff;
	return Vector( r/255.0f,g/255.0f,b/255.0f );
}

void Surface::addTriangles( const vector<Triangle> &tris ){
	triangles.insert( triangles.end(),tris.begin(),tris.end() );
}

void Surface::updateNormals(){
	int k;
	map<Vector,Vector> norm_map;
	for( k=0;k<triangles.size();++k ){
		const Triangle &t=triangles[k];
		const Vector &v0=vertices[t.verts[0]].coords;
		const Vector &v1=vertices[t.verts[1]].coords;
		const Vector &v2=vertices[t.verts[2]].coords;
		Vector n=(v1-v0).cross(v2-v0);
		if( n.length()<=EPSILON ) continue;
		n.normalize();
		norm_map[v0]+=n;
		norm_map[v1]+=n;
		norm_map[v2]+=n;
	}
	for( k=0;k<vertices.size();++k ){
		Vertex *v=&vertices[k];
		v->normal=norm_map[v->coords].normalized();
	}
}

gxMesh *Surface::getMesh(){
	if( mesh && mesh->dirty() ) valid_vs=0;

	if( valid_vs==vertices.size() && valid_ts==triangles.size() ) return mesh;

	valid_vs=valid_ts=0;

	if( mesh_vs<vertices.size() || mesh_ts<triangles.size() ){
		if( mesh ){
			gx_graphics->freeMesh( mesh );
			mesh_vs=vertices.size()+mesh_vs/2;
			mesh_ts=triangles.size()+mesh_ts/2;
		}else{
			mesh_vs=vertices.size();
			mesh_ts=triangles.size();
		}
		mesh=gx_graphics->createMesh( mesh_vs,mesh_ts,0 );
	}

	mesh->lock( true );
	for( ;valid_vs<vertices.size();++valid_vs ){
		mesh->setVertex( valid_vs,&vertices[valid_vs] );
	}
	for( ;valid_ts<triangles.size();++valid_ts ){
		const Triangle &t=triangles[valid_ts];
		mesh->setTriangle( valid_ts,t.verts[0],t.verts[1],t.verts[2] );
	}
	mesh->unlock();
	return mesh;
}

gxMesh *Surface::getMesh( const vector<Bone> &bones ){

	valid_vs=valid_ts=0;

	if( mesh_vs<vertices.size() || mesh_ts<triangles.size() ){
		if( mesh ) gx_graphics->freeMesh( mesh );
		mesh_vs=vertices.size();
		mesh_ts=triangles.size();
		mesh=gx_graphics->createMesh( mesh_vs,mesh_ts,0 );
	}

	mesh->lock( true );
	for( ;valid_vs<vertices.size();++valid_vs ){
		const Vertex &v=vertices[valid_vs];
		if( v.bone_bones[0]==255 ){
			//no bone!
			const Bone &bone=bones[0];
			mesh->setVertex( valid_vs,bone.coord_tform * v.coords,bone.normal_tform * v.normal,v.color,v.tex_coords );
		}else if( v.bone_bones[1]==255 ){
			//one bone only
			const Bone &bone=bones[v.bone_bones[0]];
			mesh->setVertex( valid_vs,bone.coord_tform * v.coords,bone.normal_tform * v.normal,v.color,v.tex_coords );
		}else{
			const Vertex &v=vertices[valid_vs];
			//two or more bones
			Vector tv,tn;
			for( int n=0;n<MAX_SURFACE_BONES;++n ){
				if( v.bone_bones[n]==255 ) break;
				const Bone &bone=bones[v.bone_bones[n]];
				tv+=bone.coord_tform * v.coords * v.bone_weights[n];
				tn+=bone.normal_tform * v.normal * v.bone_weights[n];
			}
			mesh->setVertex( valid_vs,tv,tn.normalized(),v.color,v.tex_coords );
		}
	}
	for( ;valid_ts<triangles.size();++valid_ts ){
		const Triangle &t=triangles[valid_ts];
		mesh->setTriangle( valid_ts,t.verts[0],t.verts[1],t.verts[2] );
	}
	mesh->unlock();
	return mesh;
}

/*
gxMesh *Surface::getMesh(){
	if( mesh && mesh->dirty() ) valid_vs=0;

	if( valid_vs==vertices.size() && valid_ts==triangles.size() ) return mesh;

	valid_vs=0;
	valid_ts=0;

	if( mesh ){
		int maxvs=mesh->maxVerts(),maxts=mesh->maxTris();
		if( maxvs<vertices.size() || maxts<triangles.size() ){
			gx_graphics->freeMesh( mesh );
			mesh=gx_graphics->createMesh( vertices.size()+maxvs/2,triangles.size()+maxts/2,0 );
		}
	}else if( vertices.size() || triangles.size() ){
		mesh=gx_graphics->createMesh( vertices.size(),triangles.size(),0 );
	}

	mesh->lock( true );
	for( ;valid_vs<vertices.size();++valid_vs ){
		mesh->setVertex( valid_vs,&vertices[valid_vs] );
	}
	for( ;valid_ts<triangles.size();++valid_ts ){
		const Triangle &t=triangles[valid_ts];
		mesh->setTriangle( valid_ts,t.verts[0],t.verts[1],t.verts[2] );
	}
	mesh->unlock();
	return mesh;
}

gxMesh *Surface::getMesh( const vector<Bone> &bones,gxMesh *mesh ){

	valid_vs=0;
	valid_ts=0;

	if( mesh ){
		int maxvs=mesh->maxVerts(),maxts=mesh->maxTris();
		if( maxvs<vertices.size() || maxts<triangles.size() ){
			gx_graphics->freeMesh( mesh );
			mesh=gx_graphics->createMesh( vertices.size(),triangles.size(),0 );

		}
	}else if( vertices.size() || triangles.size() ){
		mesh=gx_graphics->createMesh( vertices.size(),triangles.size(),0 );
	}

	mesh->lock( true );
	for( ;valid_vs<vertices.size();++valid_vs ){
		const Vertex &v=vertices[valid_vs];
		if( v.bone_bones[0]==255 ){
			//no bone!
			const Bone &bone=bones[0];
			mesh->setVertex( valid_vs,bone.coord_tform * v.coords,bone.normal_tform * v.normal,v.color,v.tex_coords );
		}else if( v.bone_bones[1]==255 ){
			//one bone only
			const Bone &bone=bones[v.bone_bones[0]];
			mesh->setVertex( valid_vs,bone.coord_tform * v.coords,bone.normal_tform * v.normal,v.color,v.tex_coords );
		}else{
			const Vertex &v=vertices[valid_vs];
			//two or more bones
			Vector tv,tn;
			for( int n=0;n<MAX_SURFACE_BONES;++n ){
				if( v.bone_bones[n]==255 ) break;
				const Bone &bone=bones[v.bone_bones[n]];
				tv+=bone.coord_tform * v.coords * v.bone_weights[n];
				tn+=bone.normal_tform * v.normal * v.bone_weights[n];
			}
			mesh->setVertex( valid_vs,tv,tn.normalized(),v.color,v.tex_coords );
		}
	}
	for( ;valid_ts<triangles.size();++valid_ts ){
		const Triangle &t=triangles[valid_ts];
		mesh->setTriangle( valid_ts,t.verts[0],t.verts[1],t.verts[2] );
	}
	mesh->unlock();
	return mesh;
}

*/

