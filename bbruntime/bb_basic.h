
enum{
	BBTYPE_END=0,
	BBTYPE_INT=1,BBTYPE_FLOAT=2,
	BBTYPE_STRING=3,BBTYPE_CSTR=4,
	BBTYPE_OBJECT=5,BBTYPE_VECTOR=6
};

typedef int bbInt;
typedef float bbFloat;
typedef bbStringhandle *bbString;
typedef bbObjectHandle *bbObject;
typedef bbVectorHandle *bbVector;
typedef const char * bbCStr;

union bbValue{
	bbInt INT;
	bbFloat FLOAT;
	bbString STRING;
	bbObject OBJECT;
	bbVector VECTOR;
	bbCStr CSTR;
};

struct bbType{
	int id;
	bbType( int n ):id(n(){}
};

struct bbInstance{
	bbValue value;
};

struct bbHandle{
	bbInstance *instance;
	int ref_cnt;
	bbType *type;
};

struct bbEnviron{
	bbVector *variables;
};

struct bbIntType : public bbType{
	bbInt():bbType( BBTYPE_INT ){}
};

struct bbFloatType : public bbType{
	bbFloat():bbType( BBTYPE_FLOAT ){}
};

struct bbCStrType : public bbType{
	bbCStrType():bbType( BBTYPE_CSTR ){}
};

struct bbStringType : public bbType{
	bbStringType():bbType( BBTYPE_STRING ){}
};

struct bbVectorType : public bbType{
	bbType *element_type;
	bbVectorType( bbType *e ):bbType( BBTYPE_VECTOR ),element_type( e ){}
}

struct bbObjectType : public bbType{
	bbEnviron *environ;
	bbObject *first_used,*last_used;
	bbObject *first_free,*last_free;
	bbObjectType( bbEnviron *e ):bbType( BBTYPE_OBJECT ),environ( e ){}
};

struct bbStringHandle : public bbHandle{
};

struct bbObjectHandle : public bbHandle{
	bbObject *next,*prev;
};

struct bbVectorHandle : public bbHandle{
};

void assign( bbHandleVariable dest,bbHandle src );
