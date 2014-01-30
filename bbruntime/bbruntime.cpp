
#include "std.h"
#include "bbsys.h"
#include "bbruntime.h"

void  bbEnd(){
	RTEX( 0 );
}
void  bbStop(){
	gx_runtime->debugStop();
	if( !gx_runtime->idle() ) RTEX( 0 );
}

void  bbAppTitle( BBStr *ti,BBStr *cp ){
	gx_runtime->setTitle( *ti,*cp );
	delete ti;delete cp;
}

void  bbRuntimeError( BBStr *str ){
	string t=*str;delete str;
	if( t.size()>255 ) t[255]=0;
	static char err[256];
	strcpy( err,t.c_str() );
	RTEX( err );
}

int   bbExecFile( BBStr *f ){
	string t=*f;delete f;
	int n=gx_runtime->execute( t );
	if( !gx_runtime->idle() ) RTEX( 0 );
	return n;
}

void  bbDelay( int ms ){
	if( !gx_runtime->delay( ms ) ) RTEX( 0 );
}

int  bbMilliSecs(){
	return gx_runtime->getMilliSecs();
}

BBStr * bbCommandLine(){
	return d_new BBStr( gx_runtime->commandLine() );
}

BBStr * bbSystemProperty( BBStr *p ){
	string t=gx_runtime->systemProperty( *p );
	delete p;return d_new BBStr( t );
}

BBStr *  bbGetEnv( BBStr *env_var ){
	char *p=getenv( env_var->c_str() );
	BBStr *val=d_new BBStr( p ? p : "" );
	delete env_var;
	return val;
}

void  bbSetEnv( BBStr *env_var,BBStr *val ){
	string t=*env_var+"="+*val;
	putenv( t.c_str() );
	delete env_var;
	delete val;
}

gxTimer * bbCreateTimer( int hertz ){
	gxTimer *t=gx_runtime->createTimer( hertz );
	return t;
}

int   bbWaitTimer( gxTimer *t ){
	int n=t->wait();
	if( !gx_runtime->idle() ) RTEX( 0 );
	return n;
}

void  bbFreeTimer( gxTimer *t ){
	gx_runtime->freeTimer( t );
}

void  bbDebugLog( BBStr *t ){
	gx_runtime->debugLog( t->c_str() );
	delete t;
}

void  _bbDebugStmt( int pos,const char *file ){
	gx_runtime->debugStmt( pos,file );
	if( !gx_runtime->idle() ) RTEX( 0 );
}

void  _bbDebugEnter( void *frame,void *env,const char *func ){
	gx_runtime->debugEnter( frame,env,func );
}

void  _bbDebugLeave(){
	gx_runtime->debugLeave();
}

bool basic_create();
bool basic_destroy();
void basic_link( void (*rtSym)( const char *sym,void *pc ) );
bool math_create();
bool math_destroy();
void math_link( void (*rtSym)( const char *sym,void *pc ) );
bool string_create();
bool string_destroy();
void string_link( void (*rtSym)( const char *sym,void *pc ) );
bool stream_create();
bool stream_destroy();
void stream_link( void (*rtSym)( const char *sym,void *pc ) );
bool sockets_create();
bool sockets_destroy();
void sockets_link( void (*rtSym)( const char *sym,void *pc ) );
bool filesystem_create();
bool filesystem_destroy();
void filesystem_link( void (*rtSym)( const char *sym,void *pc ) );
bool bank_create();
bool bank_destroy();
void bank_link( void (*rtSym)( const char *sym,void *pc ) );
bool graphics_create();
bool graphics_destroy();
void graphics_link( void (*rtSym)( const char *sym,void *pc ) );
bool input_create();
bool input_destroy();
void input_link( void (*rtSym)( const char *sym,void *pc ) );
bool audio_create();
bool audio_destroy();
void audio_link( void (*rtSym)( const char *sym,void *pc ) );
bool multiplay_create();
bool multiplay_destroy();
void multiplay_link( void (*rtSym)( const char *sym,void *pc ) );
bool userlibs_create();
void userlibs_destroy();
void userlibs_link( void (*rtSym)( const char *sym,void *pc ) );
#ifdef PRO
bool blitz3d_create();
bool blitz3d_destroy();
void blitz3d_link( void (*rtSym)( const char *sym,void *pc ) );
#else
bool blitz3d_create(){ return true; }
bool blitz3d_destroy(){ return true; }
void blitz3d_link( void (*rtSym)( const char *sym,void *pc ) ){}
#endif

void bbruntime_link( void (*rtSym)( const char *sym,void *pc ) ){

	rtSym( "End",bbEnd );
	rtSym( "Stop",bbStop );
	rtSym( "AppTitle$title$close_prompt=\"\"",bbAppTitle );
	rtSym( "RuntimeError$message",bbRuntimeError );
	rtSym( "ExecFile$command",bbExecFile );
	rtSym( "Delay%millisecs",bbDelay );
	rtSym( "%MilliSecs",bbMilliSecs );
	rtSym( "$CommandLine",bbCommandLine );
	rtSym( "$SystemProperty$property",bbSystemProperty );
	rtSym( "$GetEnv$env_var",bbGetEnv );
	rtSym( "SetEnv$env_var$value",bbSetEnv );

	rtSym( "%CreateTimer%hertz",bbCreateTimer );
	rtSym( "%WaitTimer%timer",bbWaitTimer );
	rtSym( "FreeTimer%timer",bbFreeTimer );
	rtSym( "DebugLog$text",bbDebugLog );

	rtSym( "_bbDebugStmt",_bbDebugStmt );
	rtSym( "_bbDebugEnter",_bbDebugEnter );
	rtSym( "_bbDebugLeave",_bbDebugLeave );

	basic_link( rtSym );
	math_link( rtSym );
	string_link( rtSym );
	stream_link( rtSym );
	sockets_link( rtSym );
	filesystem_link( rtSym );
	bank_link( rtSym );
	graphics_link( rtSym );
	input_link( rtSym );
	audio_link( rtSym );
	multiplay_link( rtSym );
	blitz3d_link( rtSym );
	userlibs_link( rtSym );
}

//start up error
static void sue( const char *t ){
	string p=string( "Startup Error: " )+t;
	gx_runtime->debugInfo( p.c_str() );
}

bool bbruntime_create(){
	if( basic_create() ){
		if( math_create() ){
			if( string_create() ){
				if( stream_create() ){
					if( sockets_create() ){
						if( filesystem_create() ){
							if( bank_create() ){
								if( graphics_create() ){
									if( input_create() ){
										if( audio_create() ){
											if( multiplay_create() ){
												if( blitz3d_create() ){
													if( userlibs_create() ){
														return true;
													}
												}else sue( "blitz3d_create failed" );
												multiplay_destroy();
											}else sue( "multiplay_create failed" );
											audio_destroy();
										}else sue( "audio_create failed" );
										input_destroy();
									}else sue( "input_create failed" );
									graphics_destroy();
								}else sue( "graphics_create failed" );
								bank_destroy();
							}else sue( "bank_create failed" );
							filesystem_destroy();
						}else sue( "filesystem_create failed" );
						sockets_destroy();
					}else sue( "sockets_create failed" );
					stream_destroy();
				}else sue( "stream_create failed" );
				string_destroy();
			}else sue( "string_create failed" );
			math_destroy();
		}else sue( "math_create failed" );
		basic_destroy();
	}else sue( "basic_create failed" );
	return false;
}

bool bbruntime_destroy(){
	userlibs_destroy();
	blitz3d_destroy();
	multiplay_destroy();
	audio_destroy();
	input_destroy();
	graphics_destroy();
	bank_destroy();
	filesystem_destroy();
	sockets_destroy();
	stream_destroy();
	string_destroy();
	math_destroy();
	basic_destroy();
	return true;
}

const char *bbruntime_run( gxRuntime *rt,void (*pc)(),bool dbg ){
	debug=dbg;
	gx_runtime=rt;

	if( !bbruntime_create() ) return "Unable to start program";
	const char *t=0;
	try{
		if( !gx_runtime->idle() ) RTEX( 0 );
		pc();
		gx_runtime->debugInfo( "Program has ended" );
	}catch( bbEx x ){
		t=x.err;
	}
	bbruntime_destroy();
	return t;
}

void bbruntime_panic( const char *err ){
	RTEX( err );
}
