
#ifndef LINKER_H
#define LINKER_H

class Module{
public:
	virtual ~Module(){}

	virtual void *link( Module *libs )=0;
	virtual bool createExe( const char *exe_file,const char *dll_file )=0;

	virtual int getPC()=0;

	virtual void emit( int byte )=0;
	virtual void emitw( int word )=0;
	virtual void emitd( int dword )=0;
	virtual void emitx( void *data,int sz )=0;

	virtual bool addSymbol( const char *sym,int pc )=0;
	virtual bool addReloc( const char *dest_sym,int pc,bool pcrel )=0;

	virtual bool findSymbol( const char *sym,int *pc )=0;
};

class Linker{
public:
	virtual int version();
	virtual bool canCreateExe();
	virtual Module *createModule();
	virtual void deleteModule( Module *mod );
};

extern "C" _declspec(dllexport) Linker * _cdecl linkerGetLinker();

#endif
