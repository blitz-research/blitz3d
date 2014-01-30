
#include "std.h"
#include "nodes.h"

//////////////////////////////
// Sequence of declarations //
//////////////////////////////
void DeclSeqNode::proto( DeclSeq *d,Environ *e ){
	for( int k=0;k<decls.size();++k ){
		try{ decls[k]->proto( d,e ); }
		catch( Ex &x ){ 
			if( x.pos<0 ) x.pos=decls[k]->pos;
			if(!x.file.size() ) x.file=decls[k]->file;
			throw; 
		}
	}
}

void DeclSeqNode::semant( Environ *e ){
	for( int k=0;k<decls.size();++k ){
		try{ decls[k]->semant( e ); }
		catch( Ex &x ){ 
			if( x.pos<0 ) x.pos=decls[k]->pos;
			if(!x.file.size() ) x.file=decls[k]->file;
			throw; 
		}
	}
}

void DeclSeqNode::translate( Codegen *g ){
	for( int k=0;k<decls.size();++k ){
		try{ decls[k]->translate( g ); }
		catch( Ex &x ){
			if( x.pos<0 ) x.pos=decls[k]->pos;
			if(!x.file.size() ) x.file=decls[k]->file;
			throw; 
		}
	}
}

void DeclSeqNode::transdata( Codegen *g ){
	for( int k=0;k<decls.size();++k ){
		try{ decls[k]->transdata( g ); }
		catch( Ex &x ){ 
			if( x.pos<0 ) x.pos=decls[k]->pos;
			if(!x.file.size() ) x.file=decls[k]->file;
			throw; 
		}
	}
}

////////////////////////////
// Simple var declaration //
////////////////////////////
void VarDeclNode::proto( DeclSeq *d,Environ *e ){

	Type *ty=tagType( tag,e );
	if( !ty ) ty=Type::int_type;
	ConstType *defType=0;

	if( expr ){
		expr=expr->semant( e );
		expr=expr->castTo( ty,e );
		if( constant || (kind&DECL_PARAM) ){
			ConstNode *c=expr->constNode();
			if( !c ) ex( "Expression must be constant" );
			if( ty==Type::int_type ) ty=d_new ConstType( c->intValue() );
			else if( ty==Type::float_type ) ty=d_new ConstType( c->floatValue() );
			else ty=d_new ConstType( c->stringValue() );
			e->types.push_back( ty );
			delete expr;expr=0;
		}
		if( kind&DECL_PARAM ){
			defType=ty->constType();ty=defType->valueType;
		}
	}else if( constant ) ex( "Constants must be initialized" );

	Decl *decl=d->insertDecl( ident,ty,kind,defType );
	if( !decl ) ex( "Duplicate variable name" );
	if( expr ) sem_var=d_new DeclVarNode( decl );
}

void VarDeclNode::semant( Environ *e ){
}

void VarDeclNode::translate( Codegen *g ){
	if( kind & DECL_GLOBAL ){
		g->align_data( 4 );
		g->i_data( 0,"_v"+ident );
	}
	if( expr ) g->code( sem_var->store( g,expr->translate( g ) ) );
}

//////////////////////////
// Function Declaration //
//////////////////////////
void FuncDeclNode::proto( DeclSeq *d,Environ *e ){
	Type *t=tagType( tag,e );if( !t ) t=Type::int_type;
	a_ptr<DeclSeq> decls( d_new DeclSeq() );
	params->proto( decls,e );
	sem_type=d_new FuncType( t,decls.release(),false,false );
	if( !d->insertDecl( ident,sem_type,DECL_FUNC ) ){
		delete sem_type;ex( "duplicate identifier" );
	}
	e->types.push_back( sem_type );
}

void FuncDeclNode::semant( Environ *e ){

	sem_env=d_new Environ( genLabel(),sem_type->returnType,1,e );
	DeclSeq *decls=sem_env->decls;

	int k;
	for( k=0;k<sem_type->params->size();++k ){
		Decl *d=sem_type->params->decls[k];
		if( !decls->insertDecl( d->name,d->type,d->kind ) ) ex( "duplicate identifier" );
	}

	stmts->semant( sem_env );
}

void FuncDeclNode::translate( Codegen *g ){

	//var offsets
	int size=enumVars( sem_env );

	//enter function
	g->enter( "_f"+ident,size );

	//initialize locals
	TNode *t=createVars( sem_env );
	if( t ) g->code( t );
	if( g->debug ){
		string t=genLabel();
		g->s_data( ident,t );
		g->code( call( "__bbDebugEnter",local(0),iconst((int)sem_env),global(t) ) );
	}

	//translate statements
	stmts->translate( g );

	for( int k=0;k<sem_env->labels.size();++k ){
		if( sem_env->labels[k]->def<0 )	ex( "Undefined label",sem_env->labels[k]->ref );
	}

	//leave the function
	g->label( sem_env->funcLabel+"_leave" );
	t=deleteVars( sem_env );
	if( g->debug ) t=d_new TNode( IR_SEQ,call( "__bbDebugLeave" ),t );
	g->leave( t,sem_type->params->size()*4 );
}

//////////////////////
// Type Declaration //
//////////////////////
void StructDeclNode::proto( DeclSeq *d,Environ *e ){
	sem_type=d_new StructType( ident,d_new DeclSeq() );
	if( !d->insertDecl( ident,sem_type,DECL_STRUCT ) ){
		delete sem_type;ex( "Duplicate identifier" );
	}
	e->types.push_back( sem_type );
}

void StructDeclNode::semant( Environ *e ){
	fields->proto( sem_type->fields,e );
	for( int k=0;k<sem_type->fields->size();++k ) sem_type->fields->decls[k]->offset=k*4;
}

void StructDeclNode::translate( Codegen *g ){

	//translate fields
	fields->translate( g );

	//type ID
	g->align_data( 4 );
	g->i_data( 5,"_t"+ident );

	//used and free lists for type
	int k;
	for( k=0;k<2;++k ){
		string lab=genLabel();
		g->i_data( 0,lab );	//fields
		g->p_data( lab );	//next
		g->p_data( lab );	//prev
		g->i_data( 0 );		//type
		g->i_data( -1 );	//ref_cnt
	}

	//number of fields
	g->i_data( sem_type->fields->size() );

	//type of each field
	for( k=0;k<sem_type->fields->size();++k ){
		Decl *field=sem_type->fields->decls[k];
		Type *type=field->type;
		string t;
		if( type==Type::int_type ) t="__bbIntType";
		else if( type==Type::float_type ) t="__bbFltType";
		else if( type==Type::string_type ) t="__bbStrType";
		else if( StructType *s=type->structType() ) t="_t"+s->ident;
		else if( VectorType *v=type->vectorType() ) t=v->label;
		g->p_data( t );
	}

}

//////////////////////
// Data declaration //
//////////////////////
void DataDeclNode::proto( DeclSeq *d,Environ *e ){
	expr=expr->semant( e );
	ConstNode *c=expr->constNode();
	if( !c ) ex( "Data expression must be constant" );
	if( expr->sem_type==Type::string_type ) str_label=genLabel();
}

void DataDeclNode::semant( Environ *e ){
}

void DataDeclNode::translate( Codegen *g ){
	if( expr->sem_type!=Type::string_type ) return;
	ConstNode *c=expr->constNode();
	g->s_data( c->stringValue(),str_label );
}

void DataDeclNode::transdata( Codegen *g ){
	ConstNode *c=expr->constNode();
	if( expr->sem_type==Type::int_type ){
		g->i_data( 1 );g->i_data( c->intValue() );
	}else if( expr->sem_type==Type::float_type ){
		float n=c->floatValue();
		g->i_data( 2 );g->i_data( *(int*)&n );
	}else{
		g->i_data( 4 );g->p_data( str_label );
	}
}

////////////////////////
// Vector declaration //
////////////////////////
void VectorDeclNode::proto( DeclSeq *d,Environ *env ){

	Type *ty=tagType( tag,env );if( !ty ) ty=Type::int_type;

	vector<int> sizes;
	for( int k=0;k<exprs->size();++k ){
		ExprNode *e=exprs->exprs[k]=exprs->exprs[k]->semant( env );
		ConstNode *c=e->constNode();
		if( !c ) ex( "Blitz array sizes must be constant" );
		int n=c->intValue();
		if( n<0 ) ex( "Blitz array sizes must not be negative" );
		sizes.push_back( n+1 );
	}
	string label=genLabel();
	sem_type=d_new VectorType( label,ty,sizes );
	if( !d->insertDecl( ident,sem_type,kind ) ){
		delete sem_type;ex( "Duplicate identifier" );
	}
	env->types.push_back( sem_type );
}

void VectorDeclNode::translate( Codegen *g ){
	//type tag!
	g->align_data( 4 );
	VectorType *v=sem_type->vectorType();
	g->i_data( 6,v->label );
	int sz=1;
	for( int k=0;k<v->sizes.size();++k ) sz*=v->sizes[k];
	g->i_data( sz );
	string t;
	Type *type=v->elementType;
	if( type==Type::int_type ) t="__bbIntType";
	else if( type==Type::float_type ) t="__bbFltType";
	else if( type==Type::string_type ) t="__bbStrType";
	else if( StructType *s=type->structType() ) t="_t"+s->ident;
	else if( VectorType *v=type->vectorType() ) t=v->label;
	g->p_data( t );

	if( kind==DECL_GLOBAL ) g->i_data( 0,"_v"+ident );
}
