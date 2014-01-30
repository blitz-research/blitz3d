
#ifndef DEBUGTREE_H
#define DEBUGTREE_H

#include "../linker/linker.h"
#include "../compiler/environ.h"

class DebugTree : public CTreeCtrl{
	int st_nest;
protected:

	HTREEITEM insertVar( void *var,Decl *d,const string &name,HTREEITEM it,HTREEITEM parent );

public:
	DebugTree();
	~DebugTree();

	DECLARE_DYNAMIC( DebugTree )
	DECLARE_MESSAGE_MAP()

	afx_msg int OnCreate( LPCREATESTRUCT lpCreateStruct );
};

class ConstsTree : public DebugTree{
public:
	ConstsTree();

	void reset( Environ *env );
};

class GlobalsTree : public DebugTree{
	Module *module;
	Environ *envron;
public:
	GlobalsTree();

	void reset( Module *mod,Environ *env );

	void refresh();
};

class LocalsTree : public DebugTree{
	Module *module;
	Environ *envron;
	struct Frame{
		void *frame;
		Environ *env;
		const char *func;
		HTREEITEM item;
		Frame( void *f,Environ *e,const char *fn ):frame(f),env(e),func(fn),item(0){}
	};
	vector<Frame> frames;

	void refreshFrame( const Frame &f );

public:
	LocalsTree();

	void reset( Environ *env );

	void refresh();

	void pushFrame( void *frame,void *env,const char *func );

	void popFrame();

	int size()const{ return frames.size(); }
};

#endif
