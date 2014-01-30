
#include "std.h"
#include "gxdir.h"

gxDir::gxDir( HANDLE h,const WIN32_FIND_DATA &f ):handle(h),findData(f){
}

gxDir::~gxDir(){
	if( handle!=INVALID_HANDLE_VALUE ) FindClose( handle );
}

string gxDir::getNextFile(){
	if( handle==INVALID_HANDLE_VALUE ) return "";
	string t=findData.cFileName;
	if( !FindNextFile( handle,&findData ) ){
		FindClose( handle );
		handle=INVALID_HANDLE_VALUE;
	}
	return t;
}