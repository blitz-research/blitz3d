
#ifdef DEMO

#include <string>
#include <fstream>
#include <windows.h>
#include "shareprot.h"
#include "stdutil.h"

using namespace std;

#ifdef PRO	//Blitz3D?
#define PRODUCT		0xce57abd1
#else
#ifdef PLUS	//BlitzPlus?
#define PRODUCT		0x7abd57ec
#else		//Blitz2D!
#define PRODUCT		0x4feb9567
#endif
#endif

#define DEMO_RUNS	30

#define REG_MUNG	(PRODUCT^0xce57abd7)
#define SYS_MUNG	(PRODUCT^0x7adb57ec)

#define REG_PRODUCT	(PRODUCT^0x677fcb37)
#define SYS_PRODUCT	(PRODUCT^0x4feba567)

static void expired(){
	MessageBox( 0,

	"This demo verson has expired!\n\n"
	"Please visit www.blitzbasic.com to buy the full version.",

	"Demo expired",MB_OK );

	ExitProcess(0);
}

static int tohex( int n ){
	return n<10 ? n+'0' : n+'a'-10;
}

static string getSysFileName(){
	//get system file...
	char dir[MAX_PATH];
	GetSystemDirectory( dir,MAX_PATH );
	char file[128];
	char *p=file,t=(char)0xbd;
	*p++='\\'^t;
	*p++='m'^t;
	*p++='s'^t;
	*p++='v'^t;
	*p++='c'^t;
	*p++=tohex((SYS_PRODUCT)&0xf)^t;
	*p++=tohex((SYS_PRODUCT>>8)&0xf)^t;
	*p++=tohex((SYS_PRODUCT>>16)&0xf)^t;
	*p++=tohex((SYS_PRODUCT>>24)&0xf)^t;
	*p++='.'^t;
	*p++='s'^t;
	*p++='y'^t;
	*p++='s'^t;
	*p++=0;
	for( p=file;*p;++p ) *p^=t;
	return string( dir )+string( file );
}

static string getRegKeyName(){
	return string( "software\\Brlconfig\\"+itoa(REG_PRODUCT) );
}

static bool setCount( int cnt ){

	HKEY hkey;
	if( RegCreateKey( HKEY_LOCAL_MACHINE,getRegKeyName().c_str(),&hkey )==ERROR_SUCCESS ){
		int val=cnt^REG_MUNG;
		LONG n=RegSetValueEx( hkey,0,0,REG_DWORD,(const BYTE*)&val,4 );
		RegCloseKey( hkey );
		if( n!=ERROR_SUCCESS ) return false;
	}else{
		return false;
	}

	if( FILE *f=fopen( getSysFileName().c_str(),"wb" ) ){
		int val=cnt^SYS_MUNG;
		int n=fwrite( &val,4,1,f );
		fclose(f);
		if( n!=1 ) return false;
	}else{
		return false;
	}

	return true;
}

static int getCount(){

	int reg_cnt=DEMO_RUNS+1,sys_cnt=DEMO_RUNS+1;

	HKEY hkey;
	if( RegOpenKey( HKEY_LOCAL_MACHINE,getRegKeyName().c_str(),&hkey )==ERROR_SUCCESS ){
		int ty=0,sz=4;
		RegQueryValueEx( hkey,0,0,(DWORD*)&ty,(BYTE*)&reg_cnt,(DWORD*)&sz );
		if( ty!=REG_DWORD ) reg_cnt=0;
		else{
			reg_cnt^=REG_MUNG;
			if( reg_cnt<0 || reg_cnt>DEMO_RUNS ) reg_cnt=0;
		}
		RegCloseKey( hkey );
	}

	if( FILE *f=fopen( getSysFileName().c_str(),"rb" ) ){
		if( fread( &sys_cnt,4,1,f )!=1 ) sys_cnt=0;
		else{
			sys_cnt^=SYS_MUNG;
			if( sys_cnt<0 || sys_cnt>DEMO_RUNS ) sys_cnt=0;
		}
		fclose(f);
	}

	return sys_cnt==reg_cnt ? sys_cnt : 0;
}

void shareProtStart(){

	if( int cnt=getCount() ) setCount( cnt-1 );
}

int shareProtCheck(){

	return getCount();
}

void shareProtAssert(){

	if( !getCount() ) shareProtJump( expired );
}

_declspec(naked) void _stdcall shareProtJump( void(*target)() ){
	_asm{
		mov	eax,4[esp]
		mov [esp],0x12345678   ;return 
		mov 4[esp],0x12345678  ;param
		mov	8[esp],0x12345678  ;more...
		call eax
	}
}

#endif
