
#include "std.h"
#include "planemodel.h"
#include "frustum.h"
#include "camera.h"

static Vector vts[17][17];

extern gxGraphics *gx_graphics;

struct PlaneModel::Rep{

	int ref_cnt;
	gxMesh *mesh;
	int sub_divs;

	Rep( int n ):
	ref_cnt(1),sub_divs(n){
		mesh=gx_graphics->createMesh( 5*sub_divs*sub_divs,3*sub_divs*sub_divs,0 );
	}
	~Rep(){
		gx_graphics->freeMesh( mesh );
	}
	void render( PlaneModel *model,const RenderContext &rc ){

		static Frustum f;
		new( &f ) Frustum( rc.getWorldFrustum(),-model->getRenderTform() );
		const Vector &eye=f.getVertex( Frustum::VERT_EYE );

		if( eye.y<=0 ) return;

		const Vector &tl=f.getVertex( Frustum::VERT_TLFAR );
		const Vector &tr=f.getVertex( Frustum::VERT_TRFAR );
		const Vector &br=f.getVertex( Frustum::VERT_BRFAR );
		const Vector &bl=f.getVertex( Frustum::VERT_BLFAR );

		Transform tex_t( model->getRenderTform().m );

		int x;
		for( x=0;x<=sub_divs;++x ){
			float t=float(x)/float(sub_divs);
			Vector tx=(tr-tl)*t+tl;
			Vector bx=(br-bl)*t+bl;
			for( int y=0;y<=sub_divs;++y ){
				float t=float(y)/float(sub_divs);
				vts[x][y]=(bx-tx)*t+tx;
			}
		}

		Plane plane( Vector( 0,1,0 ),0 );

		mesh->lock( true );
		int v_cnt=0,t_cnt=0;
		for( x=0;x<sub_divs;++x ){
			for( int y=0;y<sub_divs;++y ){

				Vector in_verts[4],out_verts[5];

				in_verts[0]=vts[x][y];
				in_verts[1]=vts[x+1][y];
				in_verts[2]=vts[x+1][y+1];
				in_verts[3]=vts[x][y+1];

				int k,out_cnt=0;
				for( k=0;k<4;++k ){

					const Vector &vert=in_verts[k];
					const Vector &prev_vert=in_verts[(k-1)&3];

					if( vert.y>0 ){
						if( prev_vert.y<=0 ){
							float t=prev_vert.y/(prev_vert.y-vert.y);
							out_verts[out_cnt++]=(vert-prev_vert)*t+prev_vert;
						}
					}else{
						if( prev_vert.y>0 ){
							float t=prev_vert.y/(prev_vert.y-vert.y);
							out_verts[out_cnt++]=(vert-prev_vert)*t+prev_vert;
						}
						out_verts[out_cnt++]=plane.intersect( Line( eye,vert-eye ) );
					}
				}
				if( out_cnt<3 || out_cnt>5 ) continue;

				for( k=0;k<out_cnt;++k ){
					const Vector &v=out_verts[k];
					float tex_coords[2][2]={ {v.x,v.z},{v.x,v.z} };
					mesh->setVertex( v_cnt+k,&v.x,&plane.n.x,tex_coords );
				}
				for( k=2;k<out_cnt;++k ){
					mesh->setTriangle( t_cnt++,v_cnt,v_cnt+k-1,v_cnt+k );
				}
				v_cnt+=out_cnt;
			}
		}
		mesh->unlock();
		if( v_cnt<3 ) return;
		model->enqueue( mesh,0,v_cnt,0,t_cnt );
	}
};

PlaneModel::PlaneModel( int sub_divs ):
rep( d_new Rep(sub_divs) ){
}

PlaneModel::PlaneModel( const PlaneModel &t ):
Model( t ),rep( t.rep ){
	++rep->ref_cnt;
}

PlaneModel::~PlaneModel(){
	if( !--rep->ref_cnt ) delete rep;
}

Plane PlaneModel::getRenderPlane()const{
	return Plane( getRenderTform().v,getRenderTform().m.j.normalized() );
}

bool PlaneModel::render( const RenderContext &rc ){
	rep->render( this,rc );
	return false;
}

bool PlaneModel::collide( const Line &l,float radius,Collision *curr_coll,const Transform &tf ){

	Line line( -tf * l );

	Plane p( Vector( 0,1,0 ),0 );
	p.d-=radius;
	float t=p.t_intersect( line );
	if( t>=curr_coll->time ) return false;

	Vector n=(tf.m.cofactor() * p.n).normalized();

	return curr_coll->update( l,t,n );
}
