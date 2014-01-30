
#include "std.h"
#include "bbstream.h"

static set<bbStream*> stream_set;

void debugStream( bbStream *s ){
	if( stream_set.count(s) ) return;
	RTEX( "Stream does not exist" );
}

bbStream::bbStream(){
	stream_set.insert( this );
}

bbStream::~bbStream(){
	stream_set.erase( this );
}

int bbEof( bbStream *s ){
	if( debug ) debugStream( s );
	return s->eof();
}

int bbReadAvail( bbStream *s ){
	if( debug ) debugStream( s );
	return s->avail();
}

int bbReadByte( bbStream *s ){
	if( debug ) debugStream( s );
	int n=0;
	s->read( (char*)&n,1 );
	return n;
}

int bbReadShort( bbStream *s ){
	if( debug ) debugStream( s );
	int n=0;
	s->read( (char*)&n,2 );
	return n;
}

int bbReadInt( bbStream *s ){
	if( debug ) debugStream( s );
	int n=0;
	s->read( (char*)&n,4 );
	return n;
}

float bbReadFloat( bbStream *s ){
	if( debug ) debugStream( s );
	float n=0;
	s->read( (char*)&n,4 );
	return n;
}

BBStr *bbReadString( bbStream *s ){
	if( debug ) debugStream( s );
	int len;
	BBStr *str=d_new BBStr();
	if( s->read( (char*)&len,4 ) ){
		char *buff=d_new char[len];
		if( s->read( buff,len ) ){
			*str=string( buff,len );
		}
		delete[] buff;
	}
	return str;
}

BBStr *bbReadLine( bbStream *s ){
	if( debug ) debugStream( s );
	unsigned char c;
	BBStr *str=d_new BBStr();
	for(;;){
		if( s->read( (char*)&c,1 )!=1 ) break;
		if( c=='\n' ) break;
		if( c!='\r' ) *str+=c;
	}
	return str;
}

void bbWriteByte( bbStream *s,int n ){
	if( debug ) debugStream( s );
	s->write( (char*)&n,1 );
}

void bbWriteShort( bbStream *s,int n ){
	if( debug ) debugStream( s );
	s->write( (char*)&n,2 );
}

void bbWriteInt( bbStream *s,int n ){
	if( debug ) debugStream( s );
	s->write( (char*)&n,4 );
}

void bbWriteFloat( bbStream *s,float n ){
	if( debug ) debugStream( s );
	s->write( (char*)&n,4 );
}

void bbWriteString( bbStream *s,BBStr *t ){
	if( debug ) debugStream( s );
	int n=t->size();
	s->write( (char*)&n,4 );
	s->write( t->data(),t->size() );
	delete t;
}

void bbWriteLine( bbStream *s,BBStr *t ){
	if( debug ) debugStream( s );
	s->write( t->data(),t->size() );
	s->write( "\r\n",2 );
	delete t;
}

void bbCopyStream( bbStream *s,bbStream *d,int buff_size ){
	if( debug ){
		debugStream( s );debugStream( d ); 
		if( buff_size<1 || buff_size>1024*1024 ) RTEX( "Illegal buffer size" );
	}
	char *buff=d_new char[buff_size];
	while( s->eof()==0 && d->eof()==0 ){
		int n=s->read( buff,buff_size );
		d->write( buff,n );
		if( n<buff_size ) break;
	}
	delete buff;
}

bool stream_create(){
	return true;
}

bool stream_destroy(){
	return true;
}

void stream_link( void(*rtSym)(const char*,void*) ){
	rtSym( "%Eof%stream",bbEof );
	rtSym( "%ReadAvail%stream",bbReadAvail );
	rtSym( "%ReadByte%stream",bbReadByte );
	rtSym( "%ReadShort%stream",bbReadShort );
	rtSym( "%ReadInt%stream",bbReadInt );
	rtSym( "#ReadFloat%stream",bbReadFloat );
	rtSym( "$ReadString%stream",bbReadString );
	rtSym( "$ReadLine%stream",bbReadLine );
	rtSym( "WriteByte%stream%byte",bbWriteByte );
	rtSym( "WriteShort%stream%short",bbWriteShort );
	rtSym( "WriteInt%stream%int",bbWriteInt );
	rtSym( "WriteFloat%stream#float",bbWriteFloat );
	rtSym( "WriteString%stream$string",bbWriteString );
	rtSym( "WriteLine%stream$string",bbWriteLine );
	rtSym( "CopyStream%src_stream%dest_stream%buffer_size=16384",bbCopyStream );
}



