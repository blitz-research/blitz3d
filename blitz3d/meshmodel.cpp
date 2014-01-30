
#include "std.h"
#include "meshmodel.h"
#include "meshcollider.h"

extern gxGraphics *gx_graphics;

struct MeshModel::Rep : public Surface::Monitor{

	int ref_cnt;
	mutable Box box,cullBox;
	mutable MeshCollider *collider;
	mutable int box_valid,coll_valid,norms_valid;

	SurfaceList surfaces;
	vector<Transform> bone_tforms;

	Rep():
	ref_cnt(1),collider(0),box_valid(-1),coll_valid(-1),norms_valid(-1){
		geom_changes=brush_changes=0;
	}

	~Rep(){
		delete collider;
		for( int k=0;k<surfaces.size();++k ) delete surfaces[k];
	}

	Surface *createSurface( const Brush &b ){
		Surface *t=d_new Surface( this );
		surfaces.push_back( t );
		t->setBrush( b );
		return t;
	}

	Surface *findSurface( const Brush &b ){
		for( int k=0;k<surfaces.size();++k ){
			Surface *s=surfaces[k];
			if( s->getBrush()<b || b<s->getBrush() ) continue;
			return s;
		}
		return 0;
	}

	void paint( const Brush &b ){
		for( int k=0;k<surfaces.size();++k ){
			Surface *s=surfaces[k];
			s->setBrush( b );
		}
	}

	void add( Rep *t ){
		if( cullBox.empty() && !t->cullBox.empty() ){
			setCullBox( t->cullBox );
		}
		for( int k=0;k<t->surfaces.size();++k ){
			Surface *src=t->surfaces[k];
			Surface *dest=findSurface( src->getBrush() );
			if( !dest ) dest=createSurface( src->getBrush() );
			int j;
			for( j=0;j<src->numTriangles();++j ){
				Surface::Triangle t=src->getTriangle( j );
				t.verts[0]+=dest->numVertices();
				t.verts[1]+=dest->numVertices();
				t.verts[2]+=dest->numVertices();
				dest->addTriangle( t );
			}
			for( j=0;j<src->numVertices();++j ){
				dest->addVertex( src->getVertex( j ) );
			}
		}
	}

	void transform( const Transform &t ){
		Matrix co=t.m.cofactor();
		for( int k=0;k<surfaces.size();++k ){
			Surface *s=surfaces[k];
			for( int j=0;j<s->numVertices();++j ){
				const Vector &v=s->getVertex(j).coords;
				const Vector &n=s->getVertex(j).normal;
				s->setCoords( j,t*v );
				s->setNormal( j,co*n );
			}
		}
	}

	void flip(){
		for( int k=0;k<surfaces.size();++k ){
			Surface *s=surfaces[k];
			int j;
			for( j=0;j<s->numVertices();++j ){
				s->setNormal( j,-s->getVertex(j).normal );
			}
			for( j=0;j<s->numTriangles();++j ){
				Surface::Triangle t=s->getTriangle(j);
				std::swap( t.verts[1],t.verts[2] );
				s->setTriangle( j,t );
			}
		}
	}

	void setCullBox( const Box &t ){
		cullBox=t;
	}

	void updateNormals(){
		if( norms_valid!=geom_changes ){
			for( int k=0;k<surfaces.size();++k ){
				Surface *s=surfaces[k];
				s->updateNormals();
			}
			norms_valid=geom_changes;
		}
	}

	const Box &getBox()const{
		if( box_valid!=geom_changes ){
			box.clear();
			for( int k=0;k<surfaces.size();++k ){
				Surface *s=surfaces[k];
				for( int j=0;j<s->numVertices();++j ){
					box.update( s->getVertex(j).coords );
				}
			}
			box_valid=geom_changes;
		}
		return box;
	}

	const Box &getCullBox()const{
		return cullBox.empty() ? getBox() : cullBox;
	}

	MeshCollider *getCollider()const{
		if( coll_valid!=geom_changes ){
			delete collider;
			vector<MeshCollider::Vertex> verts;
			vector<MeshCollider::Triangle> tris;
			for( int k=0;k<surfaces.size();++k ){
				Surface *s=surfaces[k];
				int j;
				for( j=0;j<s->numTriangles();++j ){
					MeshCollider::Triangle q;
					q.verts[0]=s->getTriangle(j).verts[0]+verts.size();
					q.verts[1]=s->getTriangle(j).verts[1]+verts.size();
					q.verts[2]=s->getTriangle(j).verts[2]+verts.size();
					q.surface=s;
					q.index=j;
					tris.push_back( q );
				}
				for( j=0;j<s->numVertices();++j ){
					MeshCollider::Vertex q;
					q.coords=s->getVertex(j).coords;
					verts.push_back( q );
				}
			}
			collider=d_new MeshCollider( verts,tris );
			coll_valid=geom_changes;
		}
		return collider;
	}
};

MeshModel::MeshModel():
rep( d_new Rep() ),brush_changes(0){
}

MeshModel::MeshModel( const MeshModel &t ):Model( t ),
rep( t.rep ),brush_changes( rep->brush_changes-1 ){
	++rep->ref_cnt;
	surf_bones.resize( t.surf_bones.size() );
	/*
	if( t.surf_bones.size() ){
		surf_bones.resize( t.surf_bones.size() );
		if( rep->bone_tforms.size() ){
			setRenderSpace( RENDER_SPACE_WORLD );
		}
	}
	*/
}

MeshModel::~MeshModel(){
	if( !--rep->ref_cnt ) delete rep;
}

void MeshModel::updateNormals(){
	rep->updateNormals();
}

void MeshModel::setCullBox( const Box &box ){
	rep->setCullBox( box );
}

void MeshModel::setRenderBrush( const Brush &b ){
	--brush_changes;
	render_brush=b;
}

void MeshModel::createBones(){
	setRenderSpace( RENDER_SPACE_WORLD );
	const vector<Object*> &bones=getAnimator()->getObjects();

	surf_bones.resize( bones.size() );

	rep->bone_tforms.resize( bones.size() );

	for( int k=0;k<bones.size();++k ){
		rep->bone_tforms[k]=-bones[k]->getWorldTform();
	}
}

bool MeshModel::render( const RenderContext &rc ){

	const Box &b=rep->getCullBox();
	if( b.empty() ) return false;

	static Frustum model_frustum;
	new( &model_frustum ) Frustum( rc.getWorldFrustum(),-getRenderTform() );
	if( !model_frustum.cull( b ) ) return false;

	if( brush_changes!=rep->brush_changes ){
		brushes.clear();
		for( int k=0;k<rep->surfaces.size();++k ){
			Surface *s=rep->surfaces[k];
			brushes.push_back( Brush( s->getBrush(),render_brush ) );
		}
		brush_changes=rep->brush_changes;
	}

	if( !surf_bones.size() ){
		for( int k=0;k<rep->surfaces.size();++k ){
			Surface *s=rep->surfaces[k];
			if( gxMesh *mesh=s->getMesh() ){
				enqueue( mesh,0,s->numVertices(),0,s->numTriangles(),brushes[k] );
			}
		}
		return false;
	}

	//OK, its boned!
	const vector<Object*> &bones=getAnimator()->getObjects();

	int k;
	for( k=0;k<bones.size();++k ){
		Transform t=
		bones[k]->getRenderTform() * rep->bone_tforms[k];
		surf_bones[k].coord_tform=t;
		surf_bones[k].normal_tform=t.m.cofactor();
	}

	bool trans=false;
	for( k=0;k<rep->surfaces.size();++k ){
		Surface *s=rep->surfaces[k];
		if( brushes[k].getBlend()==gxScene::BLEND_REPLACE ){
			if( gxMesh *mesh=s->getMesh( surf_bones ) ){
				enqueue( mesh,0,s->numVertices(),0,s->numTriangles(),brushes[k] );
			}
		}else{
			trans=true;
		}
	}
	return trans;
}

void MeshModel::renderQueue( int type ){
	if( type==QUEUE_TRANSPARENT && surf_bones.size() ){
		for( int k=0;k<rep->surfaces.size();++k ){
			Surface *s=rep->surfaces[k];
			if( brushes[k].getBlend()!=gxScene::BLEND_REPLACE ){
				if( gxMesh *mesh=s->getMesh( surf_bones ) ){
					enqueue( mesh,0,s->numVertices(),0,s->numTriangles(),brushes[k] );
				}
			}
		}
	}
	Model::renderQueue( type );
}

Surface *MeshModel::createSurface( const Brush &b ){
	return rep->createSurface( b );
	--brush_changes;
}

void MeshModel::flipTriangles(){
	rep->flip();
}

void MeshModel::transform( const Transform &t ){
	rep->transform( t );
}

void MeshModel::add( const MeshModel &t ){
	rep->add( t.rep );
}

const MeshModel::SurfaceList &MeshModel::getSurfaces()const{
	return rep->surfaces;
}

void MeshModel::paint( const Brush &b ){
	rep->paint( b );
}

const Box &MeshModel::getBox()const{
	return rep->getBox();
}

MeshCollider *MeshModel::getCollider()const{
	return rep->getCollider();
}

Surface *MeshModel::findSurface( const Brush &b )const{
	return rep->findSurface( b );
}

bool MeshModel::collide( const Line &line,float radius,Collision *curr_coll,const Transform &t ){
	return getCollider()->collide( line,radius,curr_coll,t );
}

bool MeshModel::intersects( const MeshModel &m )const{
	return getCollider()->intersects( *m.getCollider(),-m.getWorldTform()*getWorldTform() );
}
