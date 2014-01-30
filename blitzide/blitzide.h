
#ifndef BLITZIDE_H
#define BLITZIDE_H

#include "prefs.h"
#include "mainframe.h"

class BlitzIDE : public CWinApp{
public:
	MainFrame *mainFrame;

	virtual BOOL InitInstance();
	virtual int ExitInstance();
};

extern BlitzIDE blitzIDE;

#endif