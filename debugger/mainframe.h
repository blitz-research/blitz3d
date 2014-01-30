
#ifndef MAINFRAME_H
#define MAINFRAME_H

#include "tabber.h"
#include "debugger.h"
#include "sourcefile.h"
#include "debugtree.h"

class MainFrame : public CFrameWnd,public Debugger{

	Tabber tabber;
	Tabber tabber2;
	CToolBar toolBar;
	SourceFile debug_log;
	ConstsTree consts_tree;
	GlobalsTree globals_tree;
	LocalsTree locals_tree;
	map<const char*,int> file_tabs;
	map<const char*,SourceFile*> files;

	int state,step_level,cur_pos;
	const char *cur_file;

	bool shouldRun()const{ return step_level<locals_tree.size(); }

public:
	MainFrame();
	~MainFrame();

	void debugRun();
	void debugStop();
	void debugStmt( int srcpos,const char *file );
	void debugEnter( void *frame,void *env,const char *func );
	void debugLeave();
	void debugLog( const char *msg );
	void debugMsg( const char *msg,bool serious );
	void debugSys( void *msg );

	void showCurStmt();
	void setState( int n );
	void setRuntime( void *mod,void *env );
	SourceFile *sourceFile(const char*file);

	DECLARE_DYNAMIC( MainFrame )
	DECLARE_MESSAGE_MAP()

	afx_msg int  OnCreate( LPCREATESTRUCT lpCreateStruct );
	afx_msg void OnSize( UINT type,int w,int h );
	afx_msg void OnClose();

	afx_msg void cmdStop();
	afx_msg void cmdRun();
	afx_msg void cmdStepOver();
	afx_msg void cmdStepInto();
	afx_msg void cmdStepOut();
	afx_msg void cmdEnd();

	afx_msg void updateCmdUI( CCmdUI *ui );

	afx_msg void OnWindowPosChanging( WINDOWPOS *pos );
};

#endif
