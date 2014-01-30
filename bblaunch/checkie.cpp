
#include "checkie.h"
#include <windows.h>

int getIEVersion(){
	int version=0;

	HKEY key=HKEY_LOCAL_MACHINE;
	if( RegOpenKey( key,"Software\\Microsoft\\Internet Explorer",&key )==ERROR_SUCCESS ){
		char buff[MAX_PATH];DWORD type,sz=MAX_PATH;
		if( RegQueryValueEx( key,"Version",0,&type,(PBYTE)buff,&sz )==ERROR_SUCCESS ){
			version=4;
			/*
			char temp[4];
			if( isdigit(buff[0]) && buff[1]=='.' && isdigit(buff[2]) && isdigit(buff[3]) && buff[4]=='.' ){
				temp[0]=buff[0];temp[1]=buff[2];temp[2]=buff[3];temp[3]=0;
				if( atoi(temp)>=471 ) version=4;
			}
			*/
		}
		RegCloseKey( key );
	}
	return version;
}