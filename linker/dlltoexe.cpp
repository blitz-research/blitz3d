
#include "std.h"
#include "dlltoexe.h"

#pragma pack( push,1 )
struct Head{
	short machine,num_sects;
	int timedata,sym_table,num_syms;
	short opt_size,chars;
};

struct Opt1{
	short magic;
	char major,minor;
	int code_size,data_size,udata_size;
	int entry,code_base,data_base;
};

struct Sect{
	char name[8];
	int virt_size,virt_addr;	//in mem
	int data_size,data_addr;	//on disk
	int relocs,lines;			//file ptrs
	short num_relocs,num_lines;
	int chars;
};
#pragma pack( pop )

bool dllToExe( const char *exe_file,const char *dll_file,const char *entry_func ){

	//find proc address of bbWinMain
	HMODULE hmod=LoadLibrary( dll_file );if( !hmod ) return false;
	int proc=(int)GetProcAddress( hmod,entry_func );
	int entry=proc-(int)hmod;FreeLibrary( hmod );
	if( !proc ) return false;

	//Convert dll to exe
	fstream in( dll_file,ios_base::binary|ios_base::in );if( !in.is_open() ) return false;
	fstream out( exe_file,ios::binary|ios_base::out|ios_base::trunc );if( !out.is_open() ) return false;

	int offs;
	in.seekg( 0x3c );
	in.read( (char*)&offs,4 );

	//copy first bit...
	in.seekg( 0 );
	for( int k=0;k<offs+4;++k ) out.put( in.get() );

	//reader file header
	Head head={0};
	in.read( (char*)&head,sizeof(head) );
	//change DLL to EXE
	head.chars=0x10e;
	out.write( (char*)&head,sizeof(head) );

	//read opts 1
	Opt1 opt1={0};
	in.read( (char*)&opt1,sizeof(opt1) );
	opt1.entry=entry;
	out.write( (char*)&opt1,sizeof(opt1) );

	//copy rest of file...
	while( !in.eof() ){
		out.put( in.get() );
	}

	out.close();
	in.close();

	return true;
}

