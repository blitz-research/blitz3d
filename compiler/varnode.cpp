
#include "std.h"
#include "nodes.h"

//////////////////////////////////
// Common get/set for variables //
//////////////////////////////////
TNode *VarNode::load( Codegen *g ){
	TNode *t=translate( g );
	if( sem_type==Type::string_type ) return call( "__bbStrLoad",t );
	return mem( t );
}

TNode *VarNode::store( Codegen *g,TNode *n ){
	TNode *t=translate( g );
	if( sem_type->structType() ) return call( "__bbObjStore",t,n );
	if( sem_type==Type::string_type ) return call( "__bbStrStore",t,n );
	return move( n,mem( t ) );
}

bool VarNode::isObjParam(){
	return false;
}

//////////////////
// Declared var //
//////////////////
void DeclVarNode::semant( Environ *e ){
}

TNode *DeclVarNode::translate( Codegen *g ){
	if( sem_decl->kind==DECL_GLOBAL ) return global( "_v"+sem_decl->name );
	return local( sem_decl->offset );
}

TNode *DeclVarNode::store( Codegen *g,TNode *n ){
	if( isObjParam() ){
		TNode *t=translate( g );
		return move( n,mem( t ) );
	}
	return VarNode::store( g,n );
}

bool DeclVarNode::isObjParam(){
	return sem_type->structType() && sem_decl->kind==DECL_PARAM;
}

///////////////
// Ident var //
///////////////
void IdentVarNode::semant( Environ *e ){
	if( sem_decl ) return;
	Type *t=tagType( tag,e );if( !t ) t=Type::int_type;
	if( sem_decl=e->findDecl( ident ) ){
		if( !(sem_decl->kind&(DECL_GLOBAL|DECL_LOCAL|DECL_PARAM)) ){
			ex( "Identifier '"+sem_decl->name+"' may not be used like this" );
		}
		Type *ty=sem_decl->type;
		if( ty->constType() ) ty=ty->constType()->valueType;
		if( tag.size() && t!=ty ) ex( "Variable type mismatch" );
	}else{
		//ugly auto decl!
		sem_decl=e->decls->insertDecl( ident,t,DECL_LOCAL );
	}
	sem_type=sem_decl->type;
}

/////////////////
// Indexed Var //
/////////////////
void ArrayVarNode::semant( Environ *e ){
	exprs->semant( e );
	exprs->castTo( Type::int_type,e );
	Type *t=e->findType( tag );
	sem_decl=e->findDecl( ident );
	if( !sem_decl || !(sem_decl->kind&DECL_ARRAY) ) ex( "Array not found" );
	ArrayType *a=sem_decl->type->arrayType();
	if( t && t!=a->elementType ) ex( "array type mismtach" );
	if( a->dims!=exprs->size() ) ex( "incorrect number of dimensions" );
	sem_type=a->elementType;
}

TNode *ArrayVarNode::translate( Codegen *g ){
	TNode *t=0;
	for( int k=0;k<exprs->size();++k ){
		TNode *e=exprs->exprs[k]->translate( g );
		if( k ){
			TNode *s=mem( add( global( "_a"+ident ),iconst( k*4+8 ) ) );
			e=add( t,mul( e,s ) );
		}
		if( g->debug ){
			TNode *s=mem( add( global( "_a"+ident ),iconst( k*4+12 ) ) );
			t=jumpge( e,s,"__bbArrayBoundsEx" );
		}else t=e;
	}
	t=add( mem( global( "_a"+ident ) ),mul( t,iconst( 4 ) ) );
	return t;
}

///////////////
// Field var //
///////////////
void FieldVarNode::semant( Environ *e ){
	expr=expr->semant( e );
	StructType *s=expr->sem_type->structType();
	if( !s ) ex( "Variable must be a Type" );
	sem_field=s->fields->findDecl( ident );
	if( !sem_field ) ex( "Type field not found" );
	sem_type=sem_field->type;
}

TNode *FieldVarNode::translate( Codegen *g ){
	TNode *t=expr->translate( g );
	if( g->debug ) t=jumpf( t,"__bbNullObjEx" );
	t=mem( t );if( g->debug ) t=jumpf( t,"__bbNullObjEx" );
	return add( t,iconst( sem_field->offset ) );
}

////////////////
// Vector var //
////////////////
void VectorVarNode::semant( Environ *e ){
	expr=expr->semant( e );
	vec_type=expr->sem_type->vectorType();
	if( !vec_type ) ex( "Variable must be a Blitz array" );
	if( vec_type->sizes.size()!=exprs->size() ) ex( "Incorrect number of subscripts" );
	exprs->semant( e );
	exprs->castTo( Type::int_type,e );
	for( int k=0;k<exprs->size();++k ){
		if( ConstNode *t=exprs->exprs[k]->constNode() ){
			if( t->intValue()>=vec_type->sizes[k] ){
				ex( "Blitz array subscript out of range" );
			}
		}
	}
	sem_type=vec_type->elementType;
}

TNode *VectorVarNode::translate( Codegen *g ){
	int sz=4;
	TNode *t=0;
	for( int k=0;k<exprs->size();++k ){
		TNode *p;
		ExprNode *e=exprs->exprs[k];
		if( ConstNode *t=e->constNode() ){
			p=iconst( t->intValue() * sz );
		}else{
			p=e->translate( g );
			if( g->debug ){
				p=jumpge( p,iconst( vec_type->sizes[k] ),"__bbVecBoundsEx" );
			}
			p=mul( p,iconst( sz ) );
		}
		sz=sz*vec_type->sizes[k];
		t=t ? add( t,p ) : p;
	}
	return add( t,expr->translate( g ) );
}
