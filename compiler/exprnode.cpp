
#include "std.h"
#include "nodes.h"

#include <math.h>
#include <float.h>

//////////////////////////////////
// Cast an expression to a type //
//////////////////////////////////
ExprNode *ExprNode::castTo( Type *ty,Environ *e ){
	if( !sem_type->canCastTo( ty ) ){
		ex( "Illegal type conversion" );
	}

	ExprNode *cast=d_new CastNode( this,ty );
	return cast->semant( e );
}

ExprNode *CastNode::semant( Environ *e ){
	if( !expr->sem_type ){
		expr=expr->semant( e );
	}

	if( ConstNode *c=expr->constNode() ){
		ExprNode *e;
		if( type==Type::int_type ) e=d_new IntConstNode( c->intValue() );
		else if( type==Type::float_type ) e=d_new FloatConstNode( c->floatValue() );
		else e=d_new StringConstNode( c->stringValue() );
		delete this;
		return e;
	}

	sem_type=type;
	return this;
}

//////////////////////////////////
// Cast an expression to a type //
//////////////////////////////////
TNode *CastNode::translate( Codegen *g ){
	TNode *t=expr->translate( g );
	if( expr->sem_type==Type::float_type && sem_type==Type::int_type ){
		//float->int
		return d_new TNode( IR_CAST,t,0 );
	}
	if( expr->sem_type==Type::int_type && sem_type==Type::float_type ){
		//int->float
		return d_new TNode( IR_FCAST,t,0 );
	}
	if( expr->sem_type==Type::string_type && sem_type==Type::int_type ){
		//str->int
		return call( "__bbStrToInt",t );
	}
	if( expr->sem_type==Type::int_type && sem_type==Type::string_type ){
		//int->str
		return call( "__bbStrFromInt",t );
	}
	if( expr->sem_type==Type::string_type && sem_type==Type::float_type ){
		//str->float
		return fcall( "__bbStrToFloat",t );
	}
	if( expr->sem_type==Type::float_type && sem_type==Type::string_type ){
		//float->str
		return call( "__bbStrFromFloat",t );
	}
	if( expr->sem_type->structType() && sem_type==Type::string_type ){
		//obj->str
		return call( "__bbObjToStr",t );
	}
	return t;
}

/////////////////////////////
// Sequence of Expressions //
/////////////////////////////
void ExprSeqNode::semant( Environ *e ){
	for( int k=0;k<exprs.size();++k ){
		if( exprs[k] ) exprs[k]=exprs[k]->semant( e );
	}
}

TNode *ExprSeqNode::translate( Codegen *g,bool cfunc ){
	TNode *t=0,*l=0;
	for( int k=0;k<exprs.size();++k ){

		TNode *q=exprs[k]->translate(g);

		if( cfunc ){
			Type *ty=exprs[k]->sem_type;
			if( ty->stringType() ){
				q=call( "__bbStrToCStr",q );
			}else if( ty->structType() ){
				q=d_new TNode( IR_MEM,q );
			}else if( ty==Type::void_type ){
				q=d_new TNode( IR_MEM,add(q,iconst(4)) );
			}
		}

		TNode *p;
		p=d_new TNode( IR_ARG,0,0,k*4 );
		p=d_new TNode( IR_MEM,p,0 );
		p=d_new TNode( IR_MOVE,q,p );
		p=d_new TNode( IR_SEQ,p,0 );
		if( l ) l->r=p;
		else t=p;
		l=p;
	}
	return t;
}

void ExprSeqNode::castTo( DeclSeq *decls,Environ *e,bool cfunc ){
	if( exprs.size()>decls->size() ) ex( "Too many parameters" );
	for( int k=0;k<decls->size();++k ){
		Decl *d=decls->decls[k];
		if( k<exprs.size() && exprs[k] ){

			if( cfunc && d->type->structType() ){
				if( exprs[k]->sem_type->structType() ){
				}else if( exprs[k]->sem_type->intType() ){
					exprs[k]->sem_type=Type::void_type;
				}else{
					ex( "Illegal type conversion" );
				}
				continue;
			}

			exprs[k]=exprs[k]->castTo( d->type,e );

		}else{
			if( !d->defType ) ex( "Not enough parameters" );
			ExprNode *expr=constValue( d->defType );
			if( k<exprs.size() ) exprs[k]=expr;
			else exprs.push_back( expr );
		}
	}
}

void ExprSeqNode::castTo( Type *t,Environ *e ){
	for( int k=0;k<exprs.size();++k ){
		exprs[k]=exprs[k]->castTo( t,e );
	}
}

///////////////////
// Function call //
///////////////////
ExprNode *CallNode::semant( Environ *e ){
	Type *t=e->findType( tag );
	sem_decl=e->findFunc( ident );
	if( !sem_decl || !(sem_decl->kind & DECL_FUNC) ) ex( "Function '"+ident+"' not found" );
	FuncType *f=sem_decl->type->funcType();
	if( t && f->returnType!=t ) ex( "incorrect function return type" );
	exprs->semant( e );
	exprs->castTo( f->params,e,f->cfunc );
	sem_type=f->returnType;
	return this;
}

TNode *CallNode::translate( Codegen *g ){

	FuncType *f=sem_decl->type->funcType();

	TNode *t;
	TNode *l=global( "_f"+ident );
	TNode *r=exprs->translate( g,f->cfunc );

	if( f->userlib ){
		l=d_new TNode( IR_MEM,l );
		usedfuncs.insert( ident );
	}

	if( sem_type==Type::float_type ){
		t=d_new TNode( IR_FCALL,l,r,exprs->size()*4 );
	}else{
		t=d_new TNode( IR_CALL,l,r,exprs->size()*4 );
	}

	if( f->returnType->stringType() ){
		if( f->cfunc ){
			t=call( "__bbCStrToStr",t );
		}
	}
	return t;
}

/////////////////////////
// Variable expression //
/////////////////////////
ExprNode *VarExprNode::semant( Environ *e ){
	var->semant( e );
	sem_type=var->sem_type;
	ConstType *c=sem_type->constType();
	if( !c ) return this;
	ExprNode *expr=constValue( c );
	delete this;return expr;
}

TNode *VarExprNode::translate( Codegen *g ){
	return var->load( g );
}

//////////////////////
// Integer constant //
//////////////////////
IntConstNode::IntConstNode( int n ):value(n){
	sem_type=Type::int_type;
}

TNode *IntConstNode::translate( Codegen *g ){
	return d_new TNode( IR_CONST,0,0,value );
}

int IntConstNode::intValue(){
	return value;
}

float IntConstNode::floatValue(){
	return value;
}

string IntConstNode::stringValue(){
	return itoa( value );
}

////////////////////
// Float constant //
////////////////////
FloatConstNode::FloatConstNode( float f ):value(f){
	sem_type=Type::float_type;
}

TNode *FloatConstNode::translate( Codegen *g ){
	return d_new TNode( IR_CONST,0,0,*(int*)&value );
}

int FloatConstNode::intValue(){
	float flt=value;
	int temp;
	_control87( _RC_NEAR|_PC_24|_EM_INVALID|_EM_ZERODIVIDE|_EM_OVERFLOW|_EM_UNDERFLOW|_EM_INEXACT|_EM_DENORMAL,0xfffff );
	_asm{
		fld [flt];
		fistp [temp];
	}
	_control87( _CW_DEFAULT,0xfffff );
	return temp;
}

float FloatConstNode::floatValue(){
	return value;
}

string FloatConstNode::stringValue(){
	return ftoa( value );
}

/////////////////////
// String constant //
/////////////////////
StringConstNode::StringConstNode( const string &s ):value(s){
	sem_type=Type::string_type;
}

TNode *StringConstNode::translate( Codegen *g ){
	string lab=genLabel();
	g->s_data( value,lab );
	return call( "__bbStrConst",global( lab ) );
}

int StringConstNode::intValue(){
	return atoi( value );
}

float StringConstNode::floatValue(){
	return (float)atof( value );
}

string StringConstNode::stringValue(){
	return value;
}

////////////////////
// Unary operator //
////////////////////
ExprNode *UniExprNode::semant( Environ *e ){
	expr=expr->semant( e );
	sem_type=expr->sem_type;
	if( sem_type!=Type::int_type && sem_type!=Type::float_type ) ex( "Illegal operator for type" );
	if( ConstNode *c=expr->constNode() ){
		ExprNode *e;
		if( sem_type==Type::int_type ){
			switch( op ){
			case '+':e=d_new IntConstNode( +c->intValue() );break;
			case '-':e=d_new IntConstNode( -c->intValue() );break;
			case ABS:e=d_new IntConstNode( c->intValue()>=0 ? c->intValue() : -c->intValue() );break;
			case SGN:e=d_new IntConstNode( c->intValue()>0 ? 1 : (c->intValue()<0 ? -1 : 0) );break;
			}
		}else{
			switch( op ){
			case '+':e=d_new FloatConstNode( +c->floatValue() );break;
			case '-':e=d_new FloatConstNode( -c->floatValue() );break;
			case ABS:e=d_new FloatConstNode( c->floatValue()>=0 ? c->floatValue() : -c->floatValue() );break;
			case SGN:e=d_new FloatConstNode( c->floatValue()>0 ? 1 : (c->floatValue()<0 ? -1 : 0) );break;
			}
		}
		delete this;
		return e;
	}
	return this;
}

TNode *UniExprNode::translate( Codegen *g ){
	int n=0;
	TNode *l=expr->translate( g );
	if( sem_type==Type::int_type ){
		switch( op ){
		case '+':return l;
		case '-':n=IR_NEG;break;
		case ABS:return call( "__bbAbs",l );
		case SGN:return call( "__bbSgn",l );
		}
	}else{
		switch( op ){
		case '+':return l;
		case '-':n=IR_FNEG;break;
		case ABS:return fcall( "__bbFAbs",l );
		case SGN:return fcall( "__bbFSgn",l );
		}
	}
	return d_new TNode( n,l,0 );
}

/////////////////////////////////////////////////////
// boolean expression - accepts ints, returns ints //
/////////////////////////////////////////////////////
ExprNode *BinExprNode::semant( Environ *e ){
	lhs=lhs->semant(e);lhs=lhs->castTo( Type::int_type,e );
	rhs=rhs->semant(e);rhs=rhs->castTo( Type::int_type,e );
	ConstNode *lc=lhs->constNode(),*rc=rhs->constNode();
	if( lc && rc ){
		ExprNode *expr;
		switch( op ){
		case AND:expr=d_new IntConstNode( lc->intValue() & rc->intValue() );break;
		case OR: expr=d_new IntConstNode( lc->intValue() | rc->intValue() );break;
		case XOR:expr=d_new IntConstNode( lc->intValue() ^ rc->intValue() );break;
		case SHL:expr=d_new IntConstNode( lc->intValue()<< rc->intValue() );break;
		case SHR:expr=d_new IntConstNode( (unsigned)lc->intValue()>>rc->intValue() );break;
		case SAR:expr=d_new IntConstNode( lc->intValue()>> rc->intValue() );break;
		}
		delete this;
		return expr;
	}
	sem_type=Type::int_type;
	return this;
}

TNode *BinExprNode::translate( Codegen *g ){
	TNode *l=lhs->translate( g );
	TNode *r=rhs->translate( g );
	int n=0;
	switch( op ){
	case AND:n=IR_AND;break;case OR:n=IR_OR;break;case XOR:n=IR_XOR;break;
	case SHL:n=IR_SHL;break;case SHR:n=IR_SHR;break;case SAR:n=IR_SAR;break;
	}
	return d_new TNode( n,l,r );
}

///////////////////////////
// arithmetic expression //
///////////////////////////
ExprNode *ArithExprNode::semant( Environ *e ){
	lhs=lhs->semant(e);
	rhs=rhs->semant(e);
	if( lhs->sem_type->structType() || rhs->sem_type->structType() ){
		ex( "Arithmetic operator cannot be applied to custom type objects" );
	}
	if( lhs->sem_type==Type::string_type || rhs->sem_type==Type::string_type ){
		//one side is a string - only + operator...
		if( op!='+' ) ex( "Operator cannot be applied to strings" );
		sem_type=Type::string_type;
	}else if( op=='^' || lhs->sem_type==Type::float_type || rhs->sem_type==Type::float_type ){
		//It's ^, or one side is a float
		sem_type=Type::float_type;
	}else{
		//must be 2 ints
		sem_type=Type::int_type;
	}
	lhs=lhs->castTo( sem_type,e );
	rhs=rhs->castTo( sem_type,e );
	ConstNode *lc=lhs->constNode(),*rc=rhs->constNode();
	if( rc && (op=='/' || op==MOD) ){
		if( (sem_type==Type::int_type && !rc->intValue()) || (sem_type==Type::float_type && !rc->floatValue()) ){
			ex( "Division by zero" );
		}
	}
	if( lc && rc ){
		ExprNode *expr;
		if( sem_type==Type::string_type ){
			expr=d_new StringConstNode( lc->stringValue()+rc->stringValue() );
		}else if( sem_type==Type::int_type ){
			switch( op ){
			case '+':expr=d_new IntConstNode( lc->intValue()+rc->intValue() );break;
			case '-':expr=d_new IntConstNode( lc->intValue()-rc->intValue() );break;
			case '*':expr=d_new IntConstNode( lc->intValue()*rc->intValue() );break;
			case '/':expr=d_new IntConstNode( lc->intValue()/rc->intValue() );break;
			case MOD:expr=d_new IntConstNode( lc->intValue()%rc->intValue() );break;
			}
		}else{
			switch( op ){
			case '+':expr=d_new FloatConstNode( lc->floatValue()+rc->floatValue() );break;
			case '-':expr=d_new FloatConstNode( lc->floatValue()-rc->floatValue() );break;
			case '*':expr=d_new FloatConstNode( lc->floatValue()*rc->floatValue() );break;
			case '/':expr=d_new FloatConstNode( lc->floatValue()/rc->floatValue() );break;
			case MOD:expr=d_new FloatConstNode( fmod( lc->floatValue(),rc->floatValue() ) );break;
			case '^':expr=d_new FloatConstNode( pow( lc->floatValue(),rc->floatValue() ) );break;
			}
		}
		delete this;
		return expr;
	}
	return this;
}

TNode *ArithExprNode::translate( Codegen *g ){
	TNode *l=lhs->translate( g );
	TNode *r=rhs->translate( g );
	if( sem_type==Type::string_type ){
		return call( "__bbStrConcat",l,r );
	}
	int n=0;
	if( sem_type==Type::int_type ){
		switch( op ){
		case '+':n=IR_ADD;break;case '-':n=IR_SUB;break;
		case '*':n=IR_MUL;break;case '/':n=IR_DIV;break;
		case MOD:return call( "__bbMod",l,r );
		}
	}else{
		switch( op ){
		case '+':n=IR_FADD;break;case '-':n=IR_FSUB;break;
		case '*':n=IR_FMUL;break;case '/':n=IR_FDIV;break;
		case MOD:return fcall( "__bbFMod",l,r );
		case '^':return fcall( "__bbFPow",l,r );
		}
	}
	return d_new TNode( n,l,r );
}

/////////////////////////
// relation expression //
/////////////////////////
ExprNode *RelExprNode::semant( Environ *e ){
	lhs=lhs->semant(e);
	rhs=rhs->semant(e);
	if( lhs->sem_type->structType() || rhs->sem_type->structType() ){
		if( op!='=' && op!=NE ) ex( "Illegal operator for custom type objects" );
		opType=lhs->sem_type!=Type::null_type ? lhs->sem_type : rhs->sem_type;
	}else if( lhs->sem_type==Type::string_type || rhs->sem_type==Type::string_type ){
		opType=Type::string_type;
	}else if( lhs->sem_type==Type::float_type || rhs->sem_type==Type::float_type ){
		opType=Type::float_type;
	}else{
		opType=Type::int_type;
	}
	sem_type=Type::int_type;
	lhs=lhs->castTo( opType,e );
	rhs=rhs->castTo( opType,e );
	ConstNode *lc=lhs->constNode(),*rc=rhs->constNode();
	if( lc && rc ){
		ExprNode *expr;
		if( opType==Type::string_type ){
			switch( op ){
			case '<':expr=d_new IntConstNode( lc->stringValue()< rc->stringValue() );break;
			case '=':expr=d_new IntConstNode( lc->stringValue()==rc->stringValue() );break;
			case '>':expr=d_new IntConstNode( lc->stringValue()> rc->stringValue() );break;
			case LE: expr=d_new IntConstNode( lc->stringValue()<=rc->stringValue() );break;
			case NE: expr=d_new IntConstNode( lc->stringValue()!=rc->stringValue() );break;
			case GE: expr=d_new IntConstNode( lc->stringValue()>=rc->stringValue() );break;
			}
		}else if( opType==Type::float_type ){
			switch( op ){
			case '<':expr=d_new IntConstNode( lc->floatValue()< rc->floatValue() );break;
			case '=':expr=d_new IntConstNode( lc->floatValue()==rc->floatValue() );break;
			case '>':expr=d_new IntConstNode( lc->floatValue()> rc->floatValue() );break;
			case LE: expr=d_new IntConstNode( lc->floatValue()<=rc->floatValue() );break;
			case NE: expr=d_new IntConstNode( lc->floatValue()!=rc->floatValue() );break;
			case GE: expr=d_new IntConstNode( lc->floatValue()>=rc->floatValue() );break;
			}
		}else{
			switch( op ){
			case '<':expr=d_new IntConstNode( lc->intValue()< rc->intValue() );break;
			case '=':expr=d_new IntConstNode( lc->intValue()==rc->intValue() );break;
			case '>':expr=d_new IntConstNode( lc->intValue()> rc->intValue() );break;
			case LE: expr=d_new IntConstNode( lc->intValue()<=rc->intValue() );break;
			case NE: expr=d_new IntConstNode( lc->intValue()!=rc->intValue() );break;
			case GE: expr=d_new IntConstNode( lc->intValue()>=rc->intValue() );break;
			}
		}
		delete this;
		return expr;
	}
	return this;
}

TNode *RelExprNode::translate( Codegen *g ){
	TNode *l=lhs->translate( g );
	TNode *r=rhs->translate( g );
	return compare( op,l,r,opType );
}

////////////////////
// New expression //
////////////////////
ExprNode *NewNode::semant( Environ *e ){
	sem_type=e->findType( ident );
	if( !sem_type ) ex( "custom type name not found" );
	if( sem_type->structType()==0 ) ex( "type is not a custom type" );
	return this;
}

TNode *NewNode::translate( Codegen *g ){
	return call( "__bbObjNew",global( "_t"+ident ) );
}

////////////////////
// First of class //
////////////////////
ExprNode *FirstNode::semant( Environ *e ){
	sem_type=e->findType( ident );
	if( !sem_type ) ex( "custom type name name not found" );
	return this;
}

TNode *FirstNode::translate( Codegen *g ){
	return call( "__bbObjFirst",global( "_t"+ident ) );
}

///////////////////
// Last of class //
///////////////////
ExprNode *LastNode::semant( Environ *e ){
	sem_type=e->findType( ident );
	if( !sem_type ) ex( "custom type name not found" );
	return this;
}

TNode *LastNode::translate( Codegen *g ){
	return call( "__bbObjLast",global( "_t"+ident ) );
}

////////////////////
// Next of object //
////////////////////
ExprNode *AfterNode::semant( Environ *e ){
	expr=expr->semant( e );
	if( expr->sem_type==Type::null_type ) ex( "'After' cannot be used on 'Null'" );
	if( expr->sem_type->structType()==0 ) ex( "'After' must be used with a custom type object" );
	sem_type=expr->sem_type;
	return this;
}

TNode *AfterNode::translate( Codegen *g ){
	TNode *t=expr->translate( g );
	if( g->debug ) t=jumpf( t,"__bbNullObjEx" );
	return call( "__bbObjNext",t );
}

////////////////////
// Prev of object //
////////////////////
ExprNode *BeforeNode::semant( Environ *e ){
	expr=expr->semant( e );
	if( expr->sem_type==Type::null_type ) ex( "'Before' cannot be used with 'Null'" );
	if( expr->sem_type->structType()==0 ) ex( "'Before' must be used with a custom type object" );
	sem_type=expr->sem_type;
	return this;
}

TNode *BeforeNode::translate( Codegen *g ){
	TNode *t=expr->translate( g );
	if( g->debug ) t=jumpf( t,"__bbNullObjEx" );
	return call( "__bbObjPrev",t );
}

/////////////////
// Null object //
/////////////////
ExprNode *NullNode::semant( Environ *e ){
	sem_type=Type::null_type;
	return this;
}

TNode *NullNode::translate( Codegen *g ){
	return d_new TNode( IR_CONST,0,0,0 );
}

/////////////////
// Object cast //
/////////////////
ExprNode *ObjectCastNode::semant( Environ *e ){
	expr=expr->semant( e );
	expr=expr->castTo( Type::int_type,e );
	sem_type=e->findType( type_ident );
	if( !sem_type ) ex( "custom type name not found" );
	if( !sem_type->structType() ) ex( "type is not a custom type" );
	return this;
}

TNode *ObjectCastNode::translate( Codegen *g ){
	TNode *t=expr->translate( g );
	t=call( "__bbObjFromHandle",t,global( "_t"+sem_type->structType()->ident ) );
	return t;
}

///////////////////
// Object Handle //
///////////////////
ExprNode *ObjectHandleNode::semant( Environ *e ){
	expr=expr->semant( e );
	if( !expr->sem_type->structType() ) ex( "'ObjectHandle' must be used with an object" );
	sem_type=Type::int_type;
	return this;
}

TNode *ObjectHandleNode::translate( Codegen *g ){
	TNode *t=expr->translate( g );
	return call( "__bbObjToHandle",t );
}
