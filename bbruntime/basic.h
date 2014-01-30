
#ifndef BASIC_H
#define BASIC_H

#include <string>

enum{
	BBTYPE_END=0,
	BBTYPE_INT=1,BBTYPE_FLT=2,
	BBTYPE_STR=3,BBTYPE_CSTR=4,
	BBTYPE_OBJ=5,BBTYPE_VEC=6
};

#pragma pack( push,1 )

struct BBObj;
struct BBStr;
struct BBType;
struct BBObjType;
struct BBVecType;
union  BBField;
struct BBArray;

struct BBObj{
	BBField *fields;
	BBObj *next,*prev;
	BBObjType *type;
	int ref_cnt;
};

struct BBType{
	int type;
	BBType( int n ):type(n){}
};

struct BBObjType : public BBType{
	BBObj used,free;
	int fieldCnt;
	BBType *fieldTypes[1];
};

struct BBVecType : public BBType{
	int size;
	BBType *elementType;
};

union BBField{
	int INT;
	float FLT;
	BBStr *STR;
	char *CSTR;
	BBObj *OBJ;
	void *VEC;
};

struct BBArray{
	void *data;
	int elementType,dims,scales[1];
};

struct BBStr : public std::string{
	BBStr *next,*prev;

	BBStr();
	BBStr( const char *s );
	BBStr( const char *s,int n );
	BBStr( const BBStr &s );
	BBStr( const std::string &s );
	BBStr &operator=( const char *s );
	BBStr &operator=( const BBStr &s );
	BBStr &operator=( const std::string &s );
	~BBStr();

	void *operator new( size_t size );
	void operator delete( void *q );

	void *operator new( size_t size,const char *file,int line ){ return operator new( size ); }
	void operator delete( void *q,const char *file,int line ){ operator delete( q ); }
};

struct BBData{
	int fieldType;
	BBField field;
};

#pragma pack( pop )

void basic_link();

extern BBType _bbIntType;
extern BBType _bbFltType;
extern BBType _bbStrType;
extern BBType _bbCStrType;

BBStr *	 _bbStrLoad( BBStr **var );
void	 _bbStrRelease( BBStr *str );
void	 _bbStrStore( BBStr **var,BBStr *str );
int		 _bbStrCompare( BBStr *lhs,BBStr *rhs );

BBStr *	 _bbStrConcat( BBStr *s1,BBStr *s2 );
int		 _bbStrToInt( BBStr *s );
BBStr *	 _bbStrFromInt( int n );
float	 _bbStrToFloat( BBStr *s );
BBStr *	 _bbStrFromFloat( float n );
BBStr *	 _bbStrConst( const char *s );

void	 _bbDimArray( BBArray *array );
void	 _bbUndimArray( BBArray *array );
void	 _bbArrayBoundsEx();

void *	 _bbVecAlloc( BBVecType *type );
void	 _bbVecFree( void *vec,BBVecType *type );
void	 _bbVecBoundsEx();

BBObj *	 _bbObjNew( BBObjType *t );
void	 _bbObjDelete( BBObj *obj );
void	 _bbObjDeleteEach( BBObjType *type );
void	 _bbObjRelease( BBObj *obj );
void	 _bbObjStore( BBObj **var,BBObj *obj );
BBObj *	 _bbObjNext( BBObj *obj );
BBObj *	 _bbObjPrev( BBObj *obj );
BBObj *	 _bbObjFirst( BBObjType *t );
BBObj *  _bbObjLast( BBObjType *t );
void	 _bbObjInsBefore( BBObj *o1,BBObj *o2 );
void	 _bbObjInsAfter( BBObj *o1,BBObj *o2 );
int		 _bbObjEachFirst( BBObj **var,BBObjType *type );
int		 _bbObjEachNext( BBObj **var );
int		 _bbObjCompare( BBObj *o1,BBObj *o2 );
BBStr *	 _bbObjToStr( BBObj *obj );
int		 _bbObjToHandle( BBObj *obj );
BBObj *	 _bbObjFromHandle( int handle,BBObjType *type );
void	 _bbNullObjEx();

void	 _bbRestore( BBData *data );
int		 _bbReadInt();
float	 _bbReadFloat();
BBStr *	 _bbReadStr();

int		 _bbAbs( int n );
int		 _bbSgn( int n );
int		 _bbMod( int x,int y );
float	 _bbFAbs( float n );
float	 _bbFSgn( float n );
float	 _bbFMod( float x,float y );
float	 _bbFPow( float x,float y );

void	 bbRuntimeStats();

#endif