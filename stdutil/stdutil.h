
#ifndef STDUTIL_H
#define STDUTIL_H

#pragma warning(disable:4786)

#include "../config/config.h"

#include <string>
#include <iostream>

#ifdef MEMDEBUG

void * _cdecl operator new( size_t size );
void * _cdecl operator new[]( size_t size );
void * _cdecl operator new( size_t size,const char *file,int line );
void * _cdecl operator new[]( size_t size,const char *file,int line );
void _cdecl operator delete( void *q );
void _cdecl operator delete[]( void *q );
void _cdecl operator delete( void *q,const char *file,int line );
void _cdecl operator delete[]( void *q,const char *file,int line );
#define d_new new( __FILE__,__LINE__ )

#else

#define d_new new

#endif

void trackmem( bool enable );
void checkmem( std::ostream &out );

//some stuff that should be in std libs
int atoi( const std::string &s );
double atof( const std::string &s );
std::string itoa( int n );
std::string ftoa( float n );
std::string tolower( const std::string &s );
std::string toupper( const std::string &s );
std::string fullfilename( const std::string &t );
std::string filenamepath( const std::string &t );
std::string filenamefile( const std::string &t );

//lazy version of auto_ptr
template<class T>
class a_ptr{
public:
	a_ptr(T *t=0):t(t){}
	~a_ptr(){delete t;}
	a_ptr &operator=(T *t){this->t=t;return *this;}
	T &operator*()const{return *t;}
	T *operator->()const{return t;}
	operator T&()const{return *t;}
	operator T*()const{return t;}
	T *release(){ T *tt=t;t=0;return tt; }
private:
	T *t;
};

//Speed-up for SLOW sstream
class qstreambuf : public std::streambuf{
public:
	qstreambuf();
	~qstreambuf();
	int size();		//bytes unread
	char *data();	//start of bytes unread
private:
	char *buf;
	int_type underflow();
	int_type overflow( int_type c );
};

template<class T>
class pool{
	T *free;
	enum{ N=512 };
public:
	typedef size_t size_type;
	typedef ptrdiff_t difference_type;
	typedef T *pointer;
	typedef const T *const_pointer;
	typedef T &reference;
	typedef const T &const_reference;
	typedef T value_type;
	pointer address( reference q )const{ return &q; }
	const_pointer address( const_reference q )const{ return &q; }
	pool():free(0){}
	pointer allocate( size_type n,const void *){
		clog<<"Allocating "<<n<<endl;
		if( n>1 ) return d_new T[n];
		if( !free ){
			free=(T*)d_new char[sizeof(T)*N];
			for( int k=0;k<N-1;++k ) *(T**)(free+k)=free+k+1;
			*(T**)(free+N-1)=0;
		}
		T *t=free;
		free=*(T**)t;
		return t;
	}
	void deallocate( pointer q,size_type n ){
		clog<<"Deallocating "<<n<<endl;
		while( n-->0 ){
			*(T**)q=free;
			*(T**)free=q;
			++q;
		}
	}
	void construct( pointer p,const T &q ){ new(p)T(q); }
	void destroy( pointer p ){ p->~T(); }
};

#endif
