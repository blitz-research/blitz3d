
#include "std.h"
#include "bbbank.h"
#include "bbstream.h"

struct bbBank{
	char *data;
	int size,capacity;

	bbBank( int sz ):size(sz){
		capacity=(size+15)&~15;
		data=d_new char[capacity];
		memset( data,0,size );
	}
	virtual ~bbBank(){
		delete[] data;
	}
	void resize( int n ){
		if( n>size ){
			if( n>capacity ){
				capacity=capacity*3/2;
				if( n>capacity ) capacity=n;
				capacity=(capacity+15)&~15;
				char *p=d_new char[capacity];
				memcpy( p,data,size );
				delete[] data;
				data=p;
			}else memset( data+size,0,n-size );
		}
		size=n;
	}
};

static set<bbBank*> bank_set;

static inline void debugBank( bbBank *b ){
	if( debug ){
		if( !bank_set.count( b ) ) RTEX( "bbBank does not exist" );
	}
}

static inline void debugBank( bbBank *b,int offset ){
	if( debug ){
		debugBank( b );
		if( offset>=b->size ) RTEX( "Offset out of range" );
	}
}

bbBank *bbCreateBank( int size ){
	bbBank *b=d_new bbBank( size );
	bank_set.insert( b );
	return b;
}

void bbFreeBank( bbBank *b ){
	if( bank_set.erase( b ) ) delete b;
}

int bbBankSize( bbBank *b ){
	debugBank( b );
	return b->size;
}

void  bbResizeBank( bbBank *b,int size ){
	debugBank( b );
	b->resize( size );
}

void  bbCopyBank( bbBank *src,int src_p,bbBank *dest,int dest_p,int count ){
	if( debug ){ debugBank( src,src_p+count-1 );debugBank( dest,dest_p+count-1 ); }
	memmove( dest->data+dest_p,src->data+src_p,count );
}

int  bbPeekByte( bbBank *b,int offset ){
	debugBank( b,offset );
	return *(unsigned char*)(b->data+offset);
}

int  bbPeekShort( bbBank *b,int offset ){
	debugBank( b,offset+1 );
	return *(unsigned short*)(b->data+offset);
}

int  bbPeekInt( bbBank *b,int offset ){
	debugBank( b,offset+3 );
	return *(int*)(b->data+offset);
}

float  bbPeekFloat( bbBank *b,int offset ){
	debugBank( b,offset+3 );
	return *(float*)(b->data+offset);
}

void  bbPokeByte( bbBank *b,int offset,int value ){
	debugBank( b,offset );
	*(char*)(b->data+offset)=value;
}

void  bbPokeShort( bbBank *b,int offset,int value ){
	debugBank( b,offset );
	*(unsigned short*)(b->data+offset)=value;
}

void  bbPokeInt( bbBank *b,int offset,int value ){
	debugBank( b,offset );
	*(int*)(b->data+offset)=value;
}

void  bbPokeFloat( bbBank *b,int offset,float value ){
	debugBank( b,offset );
	*(float*)(b->data+offset)=value;
}

int   bbReadBytes( bbBank *b,bbStream *s,int offset,int count ){
	if( debug ){
		debugBank( b,offset+count-1 );
		debugStream( s );
	}
	return s->read( b->data+offset,count );
}

int   bbWriteBytes( bbBank *b,bbStream *s,int offset,int count ){
	if( debug ){
		debugBank( b,offset+count-1 );
		debugStream( s );
	}
	return s->write( b->data+offset,count );
}

int  bbCallDLL( BBStr *dll,BBStr *fun,bbBank *in,bbBank *out ){
	if( debug ){
		if( in ) debugBank( in );
		if( out ) debugBank( out );
	}
	int t=gx_runtime->callDll( *dll,*fun,
		in ? in->data : 0,in ? in->size : 0,
		out ? out->data : 0,out ? out->size : 0 );
	delete dll;delete fun;
	return t;
}

bool bank_create(){
	return true;
}

bool bank_destroy(){
	while( bank_set.size() ) bbFreeBank( *bank_set.begin() );
	return true;
}

void bank_link( void(*rtSym)(const char*,void*) ){
	rtSym( "%CreateBank%size=0",bbCreateBank );
	rtSym( "FreeBank%bank",bbFreeBank );
	rtSym( "%BankSize%bank",bbBankSize );
	rtSym( "ResizeBank%bank%size",bbResizeBank );
	rtSym( "CopyBank%src_bank%src_offset%dest_bank%dest_offset%count",bbCopyBank );
	rtSym( "%PeekByte%bank%offset",bbPeekByte );
	rtSym( "%PeekShort%bank%offset",bbPeekShort );
	rtSym( "%PeekInt%bank%offset",bbPeekInt );
	rtSym( "#PeekFloat%bank%offset",bbPeekFloat );
	rtSym( "PokeByte%bank%offset%value",bbPokeByte );
	rtSym( "PokeShort%bank%offset%value",bbPokeShort );
	rtSym( "PokeInt%bank%offset%value",bbPokeInt );
	rtSym( "PokeFloat%bank%offset#value",bbPokeFloat );
	rtSym( "%ReadBytes%bank%file%offset%count",bbReadBytes );
	rtSym( "%WriteBytes%bank%file%offset%count",bbWriteBytes );
	rtSym( "%CallDLL$dll_name$func_name%in_bank=0%out_bank=0",bbCallDLL );
}


