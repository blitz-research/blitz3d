
#include "stdafx.h"
#include "htmlhelp.h"
#include "mainframe.h"
#include "libs.h"

IMPLEMENT_DYNAMIC( HtmlHelp,CHtmlView )
BEGIN_MESSAGE_MAP( HtmlHelp,CHtmlView )
	ON_WM_ERASEBKGND()
END_MESSAGE_MAP()

string HtmlHelp::getTitle(){
	return title;
}

void HtmlHelp::OnTitleChange( LPCTSTR t ){
	listener->helpTitleChange( this,title=t );
}

void HtmlHelp::OnBeforeNavigate2( LPCTSTR url,DWORD flags,LPCTSTR target,CByteArray& posted,LPCTSTR headers,BOOL* cancel ){
	string t( url );
	int attr=GetFileAttributes( url );if( attr==-1 ) attr=0;
	if( (attr & FILE_ATTRIBUTE_DIRECTORY) ||
		(t.rfind( ".bb" )+3==t.size()) ||
		(isMediaFile( t )) ){

		listener->helpOpen( this,t );
		*cancel=true;
		return;

	}
	*cancel=false;
}

BOOL HtmlHelp::OnEraseBkgnd( CDC *dc ){
	return true;
}

