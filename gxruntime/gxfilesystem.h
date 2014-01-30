
#ifndef GXFILESYSTEM_H
#define GXFILESYSTEM_H

#include <string>

#include "gxdir.h"

class gxFileSystem{
public:
	gxFileSystem();
	~gxFileSystem();

	/***** GX INTERFACE *****/
public:
	enum{
		FILE_TYPE_NONE=0,FILE_TYPE_FILE=1,FILE_TYPE_DIR=2
	};

	bool createDir( const std::string &dir );
	bool deleteDir( const std::string &dir );
	bool createFile( const std::string &file );
	bool deleteFile( const std::string &file );
	bool copyFile( const std::string &src,const std::string &dest );
	bool renameFile( const std::string &src,const std::string &dest );
	bool setCurrentDir( const std::string &dir );

	std::string getCurrentDir()const;
	int getFileSize( const std::string &name )const;
	int getFileType( const std::string &name )const;

	gxDir *openDir( const std::string &name,int flags );
	gxDir *verifyDir( gxDir *d );
	void closeDir( gxDir *dir );
};

#endif