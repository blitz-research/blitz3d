
#include "stdafx.h"
#include "libs.h"
#include "editor.h"
#include "blitzide.h"

static map<string,string> keyhelps;

int linker_ver,runtime_ver;

static string execProc( const string &proc ){
	HANDLE rd,wr;

	SECURITY_ATTRIBUTES sa={sizeof(sa),0,true};

	if( CreatePipe( &rd,&wr,&sa,0 ) ){
		STARTUPINFO si={sizeof(si)};
		si.dwFlags=STARTF_USESTDHANDLES;
		si.hStdOutput=si.hStdError=wr;
		PROCESS_INFORMATION pi={0};
		if( CreateProcess( 0,(char*)proc.c_str(),0,0,true,DETACHED_PROCESS,0,0,&si,&pi ) ){
			CloseHandle( pi.hProcess );
			CloseHandle( pi.hThread );
			CloseHandle( wr );

			string t;
			char *buf=new char[1024];
			for(;;){
				unsigned long sz;
				int n=ReadFile( rd,buf,1024,&sz,0 );
				if( !n && GetLastError()==ERROR_BROKEN_PIPE ) break;
				if( !n ){ t="";break; }
				if( !sz ) break;
				t+=string( buf,sz );
			}
			delete[] buf;
			CloseHandle(rd);
			return t;
		}
		CloseHandle( rd );
		CloseHandle( wr );
	}
	AfxMessageBox( (proc+" failed").c_str() );
	ExitProcess(0);
	return "";
}

int version( string vers,string t ){
	t+=" version:";
	int n=vers.find( t );n+=t.size();
	int maj=atoi( vers.substr(n) );n=vers.find( '.',n )+1;
	int min=atoi( vers.substr(n) );
	return maj*100+min;
}

void initLibs(){

	string valid=execProc( prefs.homeDir+"/bin/blitzcc -q" );
	if( valid.size() ){
		AfxMessageBox( ("Compiler environment error: "+valid).c_str() );
		ExitProcess(0);
	}

	string vers=tolower( execProc( prefs.homeDir+"/bin/blitzcc -v" ) );
	linker_ver=version( vers,"linker" );
	runtime_ver=version( vers,"runtime" );

	//generate keywords!
	string kws=execProc( prefs.homeDir+"/bin/blitzcc +k" );

	if( !kws.size() ){
		AfxMessageBox( "Error generating keywords" );
		ExitProcess(0);
	}

	int pos=0,n;
	while( (n=kws.find( '\n',pos ))!=string::npos ){
		string t=kws.substr( pos,n-pos-1 );
		for( int q=0;(q=t.find('\r',q))!=string::npos; ) t=t.replace( q,1,"" );

		string help=t;
		int i=t.find(' ');
		if( i!=string::npos ){
			t=t.substr(0,i);if( !t.size() ){
				AfxMessageBox( "Error in keywords" );
				ExitProcess(0);
			}
			if( !isalnum(t[t.size()-1]) ) t=t.substr(0,t.size()-1);
		}

		Editor::addKeyword(t);
		keyhelps[t]=help;
		pos=n+1;
	}
}

string quickHelp( const string &kw ){
	map<string,string>::const_iterator it=keyhelps.find(kw);
	return it==keyhelps.end() ? "" : it->second;
}

bool isMediaFile( const string &f ){

#ifndef PRO
	return false;
#endif

	static char *exts[]={
		"bmp","jpg","png","tga","iff","pcx",
		"wav","mid","mp3","mod","s3m","xm","it","rmi","sgt",
		"x","3ds",0
	};

	int i=f.rfind( '.' );
	if( i==string::npos || i+1==f.size() ) return false;
	string ext=f.substr( i+1 );
	char **p=exts;
	while( const char *e=*p++ ){
		string t(e);
		if( i+t.size()+1!=f.size() ) continue;
		if( ext==t ) return true;
	}
	return false;
}
