
#ifndef NODE_H
#define NODE_H

#include "ex.h"
#include "toker.h"
#include "environ.h"
#include "codegen.h"

struct VarNode;
struct ConstNode;

struct Node{
	virtual ~Node(){}

	//used user funcs...
	static set<string> usedfuncs;

	//helper funcs
	static void ex();
	static void ex( const string &e );
	static void ex( const string &e,int pos );
	static void ex( const string &e,int pos,const string &f );

	static string genLabel();
	static VarNode *genLocal( Environ *e,Type *ty );

	static TNode *compare( int op,TNode *l,TNode *r,Type *ty );
	static ConstNode *constValue( Type *ty );

	static int enumVars( Environ *e );
	static Type *tagType( const string &s,Environ *e );

	static TNode *createVars( Environ *e );
	static TNode *deleteVars( Environ *e );

	static TNode *seq( TNode *l,TNode *r );
	static TNode *move( TNode *src,TNode *dest );
	static TNode *global( const string &s );
	static TNode *local( int offset );
	static TNode *arg( int offset );
	static TNode *mem( TNode *ref );
	static TNode *add( TNode *l,TNode *r );
	static TNode *mul( TNode *l,TNode *r );
	static TNode *iconst( int n );
	static TNode *ret();
	static TNode *jsr( const string &s );
	static TNode *jump( const string &s );
	static TNode *jumpt( TNode *cond,const string &s );
	static TNode *jumpf( TNode *cond,const string &s );
	static TNode *jumpge( TNode *l,TNode *r,const string &s );
	static TNode *call( const string &func,TNode *a0=0,TNode *a1=0,TNode *a2=0 );
	static TNode *fcall( const string &func,TNode *a0=0,TNode *a1=0,TNode *a2=0 );
};

#endif
