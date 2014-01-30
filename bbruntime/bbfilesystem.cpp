
#include "std.h"
#include "bbfilesystem.h"
#include "bbstream.h"
#include <fstream>

gxFileSystem *gx_filesys;

struct bbFile : public bbStream{
	filebuf *buf;
	bbFile( filebuf *f ):buf(f){
	}
	~bbFile(){
		delete buf;
	}
	int read( char *buff,int size ){
		return buf->sgetn( (char*)buff,size );
	}
	int write( const char *buff,int size ){
		return buf->sputn( (char*)buff,size );
	}
	int avail(){
		return buf->in_avail();
	}
	int eof(){
		return buf->sgetc()==EOF;
	}
};

static set<bbFile*> file_set;

static inline void debugFile( bbFile *f ){
	if( debug ){
		if( !file_set.count( f ) ) RTEX( "File does not exist" );
	}
}

static inline void debugDir( gxDir *d ){
	if( debug ){
		if( !gx_filesys->verifyDir( d ) ) RTEX( "Directory does not exist" );
	}
}

static bbFile *open( BBStr *f,int n ){
	string t=*f;
	filebuf *buf=d_new filebuf();
	if( buf->open( t.c_str(),n|ios_base::binary ) ){
		bbFile *f=d_new bbFile( buf );
		file_set.insert( f );
		return f;
	}
	delete buf;
	return 0;
}

bbFile *bbReadFile( BBStr *f ){
	return open( f,ios_base::in );
}

bbFile *bbWriteFile( BBStr *f ){
	return open( f,ios_base::out|ios_base::trunc );
}

bbFile *bbOpenFile( BBStr *f ){
	return open( f,ios_base::in|ios_base::out );
}

void bbCloseFile( bbFile *f ){
	debugFile( f );
	file_set.erase( f );
	delete f;
}

int bbFilePos( bbFile *f ){
	return f->buf->pubseekoff( 0,ios_base::cur );
}

int bbSeekFile( bbFile *f,int pos ){
	return f->buf->pubseekoff( pos,ios_base::beg );
}

gxDir *bbReadDir( BBStr *d ){
	string t=*d;delete d;
	return gx_filesys->openDir( t,0 );
}

void bbCloseDir( gxDir *d ){
	gx_filesys->closeDir( d );
}

BBStr *bbNextFile( gxDir *d ){
	debugDir( d );
	return d_new BBStr( d->getNextFile() );
}

BBStr *bbCurrentDir(){
	return d_new BBStr( gx_filesys->getCurrentDir() );
}

void bbChangeDir( BBStr *d ){
	gx_filesys->setCurrentDir( *d );
	delete d;
}

void bbCreateDir( BBStr *d ){
	gx_filesys->createDir( *d );
	delete d;
}

void bbDeleteDir( BBStr *d ){
	gx_filesys->deleteDir( *d );
	delete d;
}

int bbFileType( BBStr *f ){
	string t=*f;delete f;
	int n=gx_filesys->getFileType( t );
	return n==gxFileSystem::FILE_TYPE_FILE ? 1 : (n==gxFileSystem::FILE_TYPE_DIR ? 2 : 0);
}

int	bbFileSize( BBStr *f ){
	string t=*f;delete f;
	return gx_filesys->getFileSize( t );
}

void bbCopyFile( BBStr *f,BBStr *to ){
	string src=*f,dest=*to;
	delete f;delete to;
	gx_filesys->copyFile( src,dest );
}

void bbDeleteFile( BBStr *f ){
	gx_filesys->deleteFile( *f );
	delete f;
}

bool filesystem_create(){
	if( gx_filesys=gx_runtime->openFileSystem( 0 ) ){
		return true;
	}
	return false;
}

bool filesystem_destroy(){
	while( file_set.size() ) bbCloseFile( *file_set.begin() );
	gx_runtime->closeFileSystem( gx_filesys );
	return true;
}

void filesystem_link( void(*rtSym)(const char*,void*) ){
	rtSym( "%OpenFile$filename",bbOpenFile );
	rtSym( "%ReadFile$filename",bbReadFile );
	rtSym( "%WriteFile$filename",bbWriteFile );
	rtSym( "CloseFile%file_stream",bbCloseFile );
	rtSym( "%FilePos%file_stream",bbFilePos );
	rtSym( "%SeekFile%file_stream%pos",bbSeekFile );

	rtSym( "%ReadDir$dirname",bbReadDir );
	rtSym( "CloseDir%dir",bbCloseDir );
	rtSym( "$NextFile%dir",bbNextFile );
	rtSym( "$CurrentDir",bbCurrentDir );
	rtSym( "ChangeDir$dir",bbChangeDir );
	rtSym( "CreateDir$dir",bbCreateDir );
	rtSym( "DeleteDir$dir",bbDeleteDir );

	rtSym( "%FileSize$file",bbFileSize );
	rtSym( "%FileType$file",bbFileType );
	rtSym( "CopyFile$file$to",bbCopyFile );
	rtSym( "DeleteFile$file",bbDeleteFile );
}
