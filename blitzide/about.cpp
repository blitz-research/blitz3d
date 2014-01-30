
#include "stdafx.h"
#include "prefs.h"
#include "libs.h"
#include "resource.h"

#include <mmsystem.h>

char _credits[]=
"\r\n"
"Programming and design: Mark Sibly\r\n\r\n"
"Documentation: Mark Sibly, Simon Harrison, Paul Gerfen, Shane Monroe and the Blitz Doc Team\r\n\r\n"
"Testing and support: James Boyd, Simon Armstrong and the Blitz Dev Team\r\n\r\n"
"Image loader courtesy of FreeImage by Floris van den berg\r\n\r\n"
"Please visit www.blitzbasic.com for all your Blitz related needs!";

/*
char _credits[]=
"\r\n"
"Programming and Design: Mark Sibly\r\n\r\n"
"Documentation: Simon Harrison; Simon Armstrong; Mark Sibly\r\n\r\n"
"Many thanks to: Claire Foley; Janet Sibly; Rick, Kay and Robbie Keam; "
"James Boyd; the Blitz Dev Team\r\n\r\n"
"Image loader courtesy of FreeImage by Floris van den Berg\r\n\r\n"
"Please visit www.blitzbasic.com for all your Blitz related needs!";
*/

class Dialog : public CDialog{
	bool _quit;
public:
	Dialog():_quit(false){}

	afx_msg void OnOK(){
		_quit=true;
	}

	void wait(){
		_quit=false;
		MSG msg;
		while( !_quit && GetMessage( &msg,0,0,0 ) ){
			if( !AfxGetApp()->PreTranslateMessage(&msg) ){
				TranslateMessage( &msg );
				DispatchMessage( &msg );
			}
		}
		EndDialog(0);
	}

	void wait( int n ){
		int _expire=(int)timeGetTime()+n;
		for(;;){
			int tm=_expire-(int)timeGetTime();
			if( tm<0 ) tm=0;
			MsgWaitForMultipleObjects( 0,0,false,tm,QS_ALLEVENTS );

			MSG msg;
			if( PeekMessage( &msg,0,0,0,PM_REMOVE ) ){
				if( !AfxGetApp()->PreTranslateMessage(&msg) ){
					TranslateMessage( &msg );
					DispatchMessage( &msg );
				}
			}
			if( !tm ) return;
		}
	}
};

void aboutBlitz( bool delay ){

	AfxGetMainWnd()->EnableWindow(0);

	Dialog about;

	about.Create( IDD_ABOUT );

	string credits;

	credits+=_credits;

	about.GetDlgItem( IDC_CREDITS )->SetWindowText( credits.c_str() );

	int ide_ver=VERSION&0xffff;
	int lnk_ver=linker_ver&0xffff;
	int run_ver=runtime_ver&0xffff;
	string ide_v=itoa(ide_ver/1000)+"."+itoa(ide_ver%1000);
	string lnk_v=itoa(lnk_ver/1000)+"."+itoa(lnk_ver%1000);
	string run_v=itoa(run_ver/1000)+"."+itoa(run_ver%1000);

	string t="";

#ifdef PRO
	t+="Blitz3D";
#else
	t+="Blitz2D";
#endif

#ifdef EDU
	t+=" - Educational Version";
#else
#ifdef DEMO
	t+=" - Demo Version\n\n";
	/*
	int n=shareProtCheck();
	if( n>1 ) t+=itoa(n)+" runs left";
	else if( n ) t+=itoa(n)+" run left";
	else t+="expired";
	t+=")\n\n";
	*/
#else
	t+=" - Release Version\n\n";
#endif
#endif

	about.GetDlgItem( IDC_PRODUCT )->SetWindowText( t.c_str() );

	t="IDE V"+ide_v+"  Linker V"+lnk_v+"  Runtime V"+run_v;

	about.GetDlgItem( IDC_VERSION )->SetWindowText( t.c_str() );

#ifdef DEMO

	if( delay ){
		about.GetDlgItem( IDOK )->ShowWindow( SW_HIDE );
		about.GetDlgItem( IDC_PROGRESS1 )->ShowWindow( SW_SHOW );
		for( int k=0;k<100;++k ){
			((CProgressCtrl*)about.GetDlgItem( IDC_PROGRESS1 ))->SetPos( k+1 );
			about.wait( 50 );
		}
		about.GetDlgItem( IDOK )->ShowWindow( SW_SHOW );
	}

#endif

	about.GetDlgItem( IDC_PROGRESS1 )->ShowWindow( SW_HIDE );
	about.wait();
	about.EndDialog(0);
	AfxGetMainWnd()->EnableWindow(1);
}

