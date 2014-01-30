
#include "std.h"
#include "type.h"

static struct v_type : public Type{
	bool canCastTo( Type *t ){
		return t==Type::void_type;
	}
}v;

static struct i_type : public Type{
	bool intType(){ 
		return true; 
	}
	bool canCastTo( Type *t ){
		return t==Type::int_type || t==Type::float_type || t==Type::string_type;
	}
}i;

static struct f_type : public Type{
	bool floatType(){
		return true;
	}
	bool canCastTo( Type *t ){
		return t==Type::int_type || t==Type::float_type || t==Type::string_type;
	}
}f;

static struct s_type : public Type{
	bool stringType(){
		return true;
	}
	bool canCastTo( Type *t ){
		return t==Type::int_type || t==Type::float_type || t==Type::string_type;
	}
}s;

bool StructType::canCastTo( Type *t ){
	return t==this || t==Type::null_type || (this==Type::null_type && t->structType());
}

bool VectorType::canCastTo( Type *t ){
	if( this==t ) return true;
	if( VectorType *v=t->vectorType() ){
		if( elementType!=v->elementType ) return false;
		if( sizes.size()!=v->sizes.size() ) return false;
		for( int k=0;k<sizes.size();++k ){
			if( sizes[k]!=v->sizes[k] ) return false;
		}
		return true;
	}
	return false;
}

static StructType n( "Null" );

Type *Type::void_type=&v;
Type *Type::int_type=&i;
Type *Type::float_type=&f;
Type *Type::string_type=&s;
Type *Type::null_type=&n;
