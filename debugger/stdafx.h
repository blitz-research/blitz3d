
#ifndef STDAFX_H
#define STDAFX_H

#pragma warning(disable:4786)

#include <afxwin.h>         // Core
#include <afxcmn.h>			// Common Controls
#include <afxrich.h>		// CRich edit

#include <map>
#include <list>
#include <vector>
#include <string>

using namespace std;

//some stuff that should be in std libs
int atoi( const string &s );
double atof( const string &s );
string itoa( int n );
string ftoa( float n );
string tolower( const string &s );
string toupper( const string &s );
string fullfilename( const string &t );
string filenamepath( const string &t );
string filenamefile( const string &t );

#endif