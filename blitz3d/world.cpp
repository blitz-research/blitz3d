
#include "std.h"
#include <queue>
#include "world.h"

//0=tris compared for collision
//1=max proj err of terrain
float stats3d[10];

extern gxScene *gx_scene;
extern gxRuntime *gx_runtime;

static vector<Object*> _enabled,_visible;

static void enumEnabled(){
	_enabled.clear();
	for( Entity *e=Entity::orphans();e;e=e->successor() ){
		e->enumEnabled( _enabled );
	}
}

static void enumVisible(){
	_visible.clear();
	for( Entity *e=Entity::orphans();e;e=e->successor() ){
		e->enumVisible( _visible );
	}
}

/******************************* Update *******************************/

static vector<Object*> _objsByType[1000];

static vector<ObjCollision*> free_colls,used_colls;

static ObjCollision *allocObjColl( Object *with,const Vector &coords,const Collision &coll ){
	ObjCollision *c;
	if( free_colls.size() ){
		c=free_colls.back();
		free_colls.pop_back();
	}else{
		c=new ObjCollision();
	}
	used_colls.push_back( c );
	c->with=with;
	c->coords=coords;
	c->collision=coll;
	return c;
}

static void collided( Object *src,Object *dest,const Line &line,const Collision &coll,float y_scale ){

	ObjCollision *c;
	const Vector &coords=line*coll.time-coll.normal*src->getCollisionRadii().x;

	c=allocObjColl( dest,coords,coll );
	c->coords.y*=y_scale;
	src->addCollision( c );

	c=allocObjColl( src,coords,coll );
	c->coords.y*=y_scale;
	dest->addCollision( c );
}

void World::clearCollisions(){
	for( int k=0;k<1000;++k ){
		_collInfo[k].clear();
	}
}

void World::addCollision( int src_type,int dst_type,int method,int response ){

	vector<CollInfo> &info=_collInfo[src_type];
	for( int k=0;k<info.size();++k ){
		const CollInfo &t=info[k];
		if( dst_type==t.dst_type ) return;
	}

	CollInfo co={dst_type,method,response};
	_collInfo[src_type].push_back(co);
}

bool World::hitTest( const Line &line,float radius,Object *obj,const Transform &tf,int method,Collision *curr_coll  ){
	switch( method ){
	case COLLISION_METHOD_SPHERE:
		return curr_coll->sphereCollide( line,radius,tf.v,obj->getCollisionRadii().x );
	case COLLISION_METHOD_POLYGON:
		return obj->collide( line,radius,curr_coll,tf );
	case COLLISION_METHOD_BOX:
		Transform t=tf;
		t.m.i.normalize();t.m.j.normalize();t.m.k.normalize();
		if( curr_coll->boxCollide( ~t*line,radius,obj->getCollisionBox() ) ){
			curr_coll->normal=t.m*curr_coll->normal;
			return true;
		}
	}
	return false;
}

bool World::checkLOS( Object *src,Object *dest ){

	enumEnabled();

	Object *coll_obj=0;
	Collision curr_coll;

	Line line( src->getWorldPosition(),dest->getWorldPosition()-src->getWorldPosition() );

	vector<Object*>::const_iterator it;

	for( it=_enabled.begin();it!=_enabled.end();++it ){
		Object *obj=*it;

		if( obj==src || obj==dest || !obj->getPickGeometry() || !obj->getObscurer() ) continue;

		if( hitTest( line,0,obj,obj->getWorldTform(),obj->getPickGeometry(),&curr_coll ) ){
			return false;
		}
	}
	return true;
}

Object *World::traceRay( const Line &line,float radius,ObjCollision *curr_coll ){

	enumEnabled();

	Object *coll_obj=0;

	vector<Object*>::const_iterator it;
	for( it=_enabled.begin();it!=_enabled.end();++it ){
		Object *obj=*it;

		if( !obj->getPickGeometry() ) continue;

		if( hitTest( line,radius,obj,obj->getWorldTform(),obj->getPickGeometry(),&curr_coll->collision ) ){
			coll_obj=obj;
		}
	}
	if( curr_coll->with=coll_obj ){
		curr_coll->coords=line*curr_coll->collision.time-curr_coll->collision.normal*radius;
	}
	return coll_obj;
}

//
// NEW VERSION
//
void World::collide( Object *src ){

	static const int MAX_HITS=10;

	Vector dv=src->getWorldTform().v;
	Vector sv=src->getPrevWorldTform().v;

	if( sv==dv ){
		if( dv.x!=sv.x || dv.y!=sv.y || dv.z!=sv.z ){
			src->setWorldPosition( sv );
		}
		return;
	}

	Vector panic=sv;

	static Transform y_tform;

	const Vector &radii=src->getCollisionRadii();

	float radius=radii.x,inv_y_scale;
	float y_scale=inv_y_scale=y_tform.m.j.y=1;

	if( radii.x!=radii.y ){
		y_scale=y_tform.m.j.y=radius/radii.y;
		inv_y_scale=1/y_scale;
		sv.y*=y_scale;
		dv.y*=y_scale;
	}

	int n_hit=0;
	Plane planes[2];
	Line coll_line( sv,dv-sv );
	Vector dir=coll_line.d;

	float td=coll_line.d.length();
	float td_xz=Vector( coll_line.d.x,0,coll_line.d.z ).length();

	const vector<CollInfo> &collinfos=_collInfo[src->getCollisionType()];

	int hits=0;
	for(;;){

		Collision coll;
		Object *coll_obj=0;
		vector<CollInfo>::const_iterator coll_it,coll_info;

		for( coll_it=collinfos.begin();coll_it!=collinfos.end();++coll_it ){

			vector<Object*>::const_iterator dst_it;
			
			const vector<Object*> &dst_objs=_objsByType[coll_it->dst_type];

			for( dst_it=dst_objs.begin();dst_it!=dst_objs.end();++dst_it ){

				Object *dst=*dst_it;

				if( src==dst ) continue;

				const Transform &dst_tform=dst->getPrevWorldTform();

				if( y_scale==1 ){
					if( hitTest( 
					coll_line,radius,dst,dst_tform,
					coll_it->method,&coll ) ){
						coll_obj=dst;
						coll_info=coll_it;
					}
				}else{
					if( hitTest( 
					coll_line,radius,dst,y_tform * dst_tform,
					coll_it->method,&coll ) ){
						coll_obj=dst;
						coll_info=coll_it;
					}
				}
			}
		}
		if( !coll_obj ) break;

		//register collision
		if( ++hits==MAX_HITS ){
//			exit(0);
			break;
		}

		collided( src,coll_obj,coll_line,coll,inv_y_scale );

		Plane coll_plane( coll_line*coll.time,coll.normal );

		coll_plane.d-=COLLISION_EPSILON;
		coll.time=coll_plane.t_intersect( coll_line );

		if( coll.time>0 ){// && fabs(coll.normal.dot( coll_line.d ))>EPSILON ){
			//update source position - ONLY IF AHEAD!
			sv=coll_line*coll.time;
			td*=1-coll.time;
			td_xz*=1-coll.time;
		}

		if( coll_info->response==COLLISION_RESPONSE_STOP ){
			dv=sv;
			break;
		}

		//find nearest point on plane to dest
		Vector nv=coll_plane.nearest( dv );

		if( n_hit==0 ){
			dv=nv;
		}else if( n_hit==1 ){
			if( planes[0].distance(nv)>=0 ){
				dv=nv;n_hit=0;
			}else if( fabs( planes[0].n.dot( coll_plane.n ) )<1-EPSILON ){
				dv=coll_plane.intersect( planes[0] ).nearest( dv );
			}else{
				//SQUISHED!
				//exit(0);
				hits=MAX_HITS;break;
			}
		}else if( planes[0].distance(nv)>=0 && planes[1].distance(nv)>=0 ){
			dv=nv;n_hit=0;
		}else{
			dv=sv;break;
		}

		Vector dd( dv-sv );

		//going behind initial direction? really necessary?
		if( dd.dot( dir )<=0 ){ dv=sv;break; }

		if( coll_info->response==COLLISION_RESPONSE_SLIDE ){
			float d=dd.length();
			if( d<=EPSILON ){ dv=sv;break; }
			if( d>td ) dd*=td/d;
		}else if( coll_info->response==COLLISION_RESPONSE_SLIDEXZ ){
			float d=Vector( dd.x,0,dd.z ).length();
			if( d<=EPSILON ){ dv=sv;break; }
			if( d>td_xz ) dd*=td_xz/d;
		}

		coll_line.o=sv;
		coll_line.d=dd;dv=sv+dd;
		planes[n_hit++]=coll_plane;
	}

	if( hits ){
		if( hits<MAX_HITS ){
			dv.y*=inv_y_scale;
			src->setWorldPosition( dv );
		}else{
			src->setWorldPosition( panic );
		}
	}
}

/*
//
// OLD VERSION
//
void World::collide( Object *src ){

	static const int MAX_HITS=100;

	Vector dv=src->getWorldTform().v;
	Vector sv=src->getPrevWorldTform().v;

	if( sv==dv ){
		if( dv.x!=sv.x || dv.y!=sv.y || dv.z!=sv.z ){
			src->setWorldPosition( sv );
		}
		return;
	}

	Vector panic=sv;

	static Transform y_tform;

	const Vector &radii=src->getCollisionRadii();

	float radius=radii.x,inv_y_scale;
	float y_scale=inv_y_scale=y_tform.m.j.y=1;

	if( radii.x!=radii.y ){
		y_scale=y_tform.m.j.y=radius/radii.y;
		inv_y_scale=1/y_scale;
		sv.y*=y_scale;
		dv.y*=y_scale;
	}

	int n_hit=0;
	Plane planes[2];
	Line coll_line( sv,dv-sv );
	Vector dir=coll_line.d;

	float td=coll_line.d.length();
	float td_xz=Vector( coll_line.d.x,0,coll_line.d.z ).length();

	const vector<CollInfo> &collinfos=_collInfo[src->getCollisionType()];

	int hits=0;
	while( hits<MAX_HITS ){

		Collision coll;
		Object *coll_obj=0;
		vector<CollInfo>::const_iterator coll_it,coll_info;

		for( coll_it=collinfos.begin();coll_it!=collinfos.end();++coll_it ){

			vector<Object*>::const_iterator dst_it;
			
			const vector<Object*> &dst_objs=_objsByType[coll_it->dst_type];

			for( dst_it=dst_objs.begin();dst_it!=dst_objs.end();++dst_it ){

				Object *dst=*dst_it;

				if( src==dst ) continue;

				const Transform &dst_tform=dst->getPrevWorldTform();

				if( y_scale==1 ){

					if( hitTest( 
					coll_line,radius,dst,dst_tform,
					coll_it->method,&coll ) ){
						coll_obj=dst;
						coll_info=coll_it;
					}
				}else{

					if( hitTest( 
					coll_line,radius,dst,y_tform * dst_tform,
					coll_it->method,&coll ) ){
						coll_obj=dst;
						coll_info=coll_it;
					}
				}
			}
		}
		if( !coll_obj ) break;

		//register collision
		++hits;
		collided( src,coll_obj,coll_line,coll,inv_y_scale );

		//create collision plane
		Plane coll_plane( coll_line*coll.time,coll.normal );

		//move plane out a bit (cough)
		coll_plane.d-=.001f;

		if( fabs(coll.normal.dot( coll_line.d ))>EPSILON ){
			float t=coll_plane.t_intersect( coll_line );
			//update source position - ONLY IF AHEAD!
			if( t>0 ){
				sv=coll_line*t;
				td*=1-coll.time;
				td_xz*=1-coll.time;
			}
		}

		//STOP?
		if( coll_info->response==COLLISION_RESPONSE_STOP ){
			dv=sv;
			break; 
		}

		//find nearest point on plane to dest
		Vector nv=coll_plane.nearest( dv );

		//SLIDE!
		if( n_hit==0 ){
			dv=nv;
		}else if( n_hit==1 ){
			if( planes[0].distance(nv)>=0 ){
				dv=nv;n_hit=0;
			}else if( fabs( planes[0].n.dot( coll_plane.n ) )<1-EPSILON ){
				dv=coll_plane.intersect( planes[0] ).nearest( dv );
			}else{
				hits=MAX_HITS;break;
			}
		}else if( planes[0].distance(nv)>=0 && planes[1].distance(nv)>=0 ){
			dv=nv;n_hit=0;
		}else{
			dv=sv;break;
		}

		Vector dd( dv-sv );

		//going behind initial direction? really necessary?
		if( dd.dot( dir )<=0 ){ dv=sv;break; }

		if( coll_info->response==COLLISION_RESPONSE_SLIDE ){
			float d=dd.length();
			if( d<=EPSILON ){ dv=sv;break; }
			if( d>td ) dd*=td/d;
		}else if( coll_info->response==COLLISION_RESPONSE_SLIDEXZ ){
			float d=Vector( dd.x,0,dd.z ).length();
			if( d<=EPSILON ){ dv=sv;break; }
			if( d>td_xz ) dd*=td_xz/d;
		}

		coll_line.o=sv;
		coll_line.d=dd;dv=sv+dd;
		planes[n_hit++]=coll_plane;
	}

	if( hits ){
		if( hits<MAX_HITS ){
			dv.y*=inv_y_scale;
			src->setWorldPosition( dv );
		}else{
			src->setWorldPosition( panic );
		}
	}
}
*/

void World::update( float elapsed ){

	stats3d[0]=0;

	for( ;used_colls.size();used_colls.pop_back() ){
		free_colls.push_back( used_colls.back() );
	}

	enumEnabled();

	vector<Object*>::const_iterator it;
	for( it=_enabled.begin();it!=_enabled.end();++it ){
		Object *o=*it;

		if( int n=o->getCollisionType() ){
			_objsByType[n].push_back(o);
		}
	}

	for( it=_enabled.begin();it!=_enabled.end();++it ){
		Object *o=*it;

		o->beginUpdate( elapsed );

		if( o->getCollisionType() ) collide( o );

		o->endUpdate();
	}

	for( int k=0;k<1000;++k ){
		_objsByType[k].clear();
	}
}

/****************************** Render *********************************/

static Transform cam_tform;		//current camera transform

static vector<gxLight*> _lights;
static vector<Mirror*> _mirrors;
static vector<Listener*> _listeners;

struct OrderComp{
	bool operator()( Object *a,Object *b ){
		return a->getOrder()<b->getOrder();
	}
};

struct TransComp{
	bool operator()( Model *a,Model *b )const{
		return 
		cam_tform.v.distance( a->getRenderTform().v )<
		cam_tform.v.distance( b->getRenderTform().v );
	}
};

static vector<Model*> ord_mods,unord_mods;

static priority_queue<Model*,vector<Model*>,OrderComp> ord_que;

static priority_queue<Camera*,vector<Camera*>,OrderComp> cam_que;

static priority_queue<Model*,vector<Model*>,TransComp> transparents;

void World::capture(){

	enumVisible();

	vector<Object*>::const_iterator it;
	for( it=_visible.begin();it!=_visible.end();++it ){
		(*it)->capture();
	}
}

void World::render( float tween ){

	//set render tweens, and build ordered and unordered model lists...
	ord_mods.clear();
	unord_mods.clear();

	_visible.clear();
	_lights.clear();
	_mirrors.clear();
	_listeners.clear();

	enumVisible();

	vector<Object*>::const_iterator it;
	for( it=_visible.begin();it!=_visible.end();++it ){
		Object *o=*it;

		if( !o->beginRender(tween) ) continue;

		if( Light *t=o->getLight() ) _lights.push_back(t->getGxLight());
		else if( Camera *t=o->getCamera() ) cam_que.push(t);
		else if( Mirror *t=o->getMirror() ) _mirrors.push_back(t);
		else if( Listener *t=o->getListener() ) _listeners.push_back(t);
		else if( Model *t=o->getModel() ){
			if( t->getOrder() ) ord_que.push( t );
			else unord_mods.push_back( t );
		}
	}

	for( ;ord_que.size();ord_que.pop() ) ord_mods.push_back( ord_que.top() );

//	gx_runtime->debugLog( "RenderWorld" );

	if( !gx_scene->begin( _lights ) ) return;

	for( ;cam_que.size();cam_que.pop() ){

		Camera *cam=cam_que.top();

		if( !cam->beginRenderFrame() ) continue;

		vector<Mirror*>::const_iterator mir_it;
		for( mir_it=_mirrors.begin();mir_it!=_mirrors.end();++mir_it ){
			render( cam,*mir_it );
		}

		render( cam,0 );
	}

	gx_scene->end();

//	gx_runtime->debugLog( "End RenderWorld" );

	vector<Listener*>::const_iterator lis_it;
	for( lis_it=_listeners.begin();lis_it!=_listeners.end();++lis_it ){
		(*lis_it)->renderListener();
	}
}

void World::render( Camera *cam,Mirror *mirror ){

	if( mirror ){
		const Transform &t=mirror->getRenderTform();
		cam_tform=t * Transform( scaleMatrix( 1,-1,1 ) ) * -t * cam->getRenderTform();
		gx_scene->setFlippedTris( true );
	}else{
		cam_tform=cam->getRenderTform();
		gx_scene->setFlippedTris( false );
	}

	//set camera matrix
	gx_scene->setViewMatrix( (gxScene::Matrix*)&(-cam_tform) );

	//initialize render context
	RenderContext rc( cam_tform,cam->getFrustum(),mirror!=0 );

	//draw everything in order
	int ord=0;
	gx_scene->setZMode( gxScene::ZMODE_DISABLE );
	while( ord<ord_mods.size() && ord_mods[ord]->getOrder()>0 ){
		Model *mod=ord_mods[ord++];
		if( !mod->doAutoFade( cam_tform.v ) ) continue;
		render( mod,rc );
		flushTransparent();
	}

	gx_scene->setZMode( gxScene::ZMODE_NORMAL );
	for( int k=0;k<unord_mods.size();++k ){
		Model *mod=unord_mods[k];
		if( !mod->doAutoFade( cam_tform.v ) ) continue;
		render( mod,rc );
	}
	gx_scene->setZMode( gxScene::ZMODE_CMPONLY );
	flushTransparent();

	gx_scene->setZMode( gxScene::ZMODE_DISABLE );
	while( ord<ord_mods.size() ){
		Model *mod=ord_mods[ord++];
		if( !mod->doAutoFade( cam_tform.v ) ) continue;
		render( mod,rc );
		flushTransparent();
	}
}

void World::render( Model *mod,const RenderContext &rc ){

	bool trans=mod->render( rc );

	if( mod->queueSize( Model::QUEUE_OPAQUE ) ){
		if( mod->getRenderSpace()==Model::RENDER_SPACE_LOCAL ){
			gx_scene->setWorldMatrix( (gxScene::Matrix*)&mod->getRenderTform() );
		}else{
			gx_scene->setWorldMatrix( 0 );
		}
		mod->renderQueue( Model::QUEUE_OPAQUE );
	}

	if( trans || mod->queueSize( Model::QUEUE_TRANSPARENT ) ){
		transparents.push( mod );
	}
}

void World::flushTransparent(){

	bool local=true;

	for( ;transparents.size();transparents.pop() ){
		Model *mod=transparents.top();
		if( mod->getRenderSpace()==Model::RENDER_SPACE_LOCAL ){
			gx_scene->setWorldMatrix( (gxScene::Matrix*)&mod->getRenderTform() );
			local=true;
		}else if( local ){
			gx_scene->setWorldMatrix( 0 );
			local=false;
		}
		mod->renderQueue( Model::QUEUE_TRANSPARENT );
	}
}
