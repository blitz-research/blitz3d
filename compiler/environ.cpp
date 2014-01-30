
#include "std.h"
#include "environ.h"

Environ::Environ( const string &f,Type *r,int l,Environ *gs )
:funcLabel(f),returnType(r),level(l),globals(gs){
	decls=d_new DeclSeq();
	typeDecls=d_new DeclSeq();
	funcDecls=d_new DeclSeq();
	if( globals ) globals->children.push_back( this );
}

Environ::~Environ(){
	if( globals ) globals->children.remove( this );
	while( children.size() ) delete children.back();
	for( ;labels.size();labels.pop_back() ) delete labels.back();

	//delete all types
	delete decls;
	delete funcDecls;
	delete typeDecls;

	for( int k=0;k<types.size();++k ) delete types[k];
}

Decl *Environ::findDecl( const string &s ){
	for( Environ *e=this;e;e=e->globals ){
		if( Decl *d=e->decls->findDecl( s ) ){
			if( d->kind&(DECL_LOCAL|DECL_PARAM) ){
				if( e==this ) return d;
			}else return d;
		}
	}
	return 0;
}

Decl *Environ::findFunc( const string &s ){
	for( Environ *e=this;e;e=e->globals ){
		if( Decl *d=e->funcDecls->findDecl( s ) ) return d;
	}
	return 0;
}

Type *Environ::findType( const string &s ){
	if( s=="%" ) return Type::int_type;
	if( s=="#" ) return Type::float_type;
	if( s=="$" ) return Type::string_type;
	for( Environ *e=this;e;e=e->globals ){
		if( Decl *d=e->typeDecls->findDecl( s ) ) return d->type->structType();
	}
	return 0;
}

Label *Environ::findLabel( const string &s ){
	for( int k=0;k<labels.size();++k ) if( labels[k]->name==s ) return labels[k];
	return 0;
}

Label *Environ::insertLabel( const string &s,int def,int src,int sz ){
	Label *l=d_new Label( s,def,src,sz );
	labels.push_back( l );return l;
}

string Environ::setBreak( const string &s ){
	string t=breakLabel;
	breakLabel=s;return t;
}
