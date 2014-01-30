
#ifndef DEBUGGERAPP_H
#define DEBUGGERAPP_H

#include "mainframe.h"

class DebuggerApp : public CWinApp{
	MainFrame *main_frame;

public:
	~DebuggerApp();

	virtual BOOL InitInstance();
	virtual int ExitInstance();

	MainFrame *mainFrame();
};

extern DebuggerApp debuggerApp;

#endif