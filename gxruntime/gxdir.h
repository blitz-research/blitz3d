
#ifndef GXDIR_H
#define GXDIR_H

#include <string>
#include <windows.h>

class gxDir{
public:
	gxDir( HANDLE h,const WIN32_FIND_DATA &f );
	~gxDir();

private:
	HANDLE handle;
	WIN32_FIND_DATA findData;

	/***** GX INTERFACE *****/
public:
	std::string getNextFile();
};

#endif