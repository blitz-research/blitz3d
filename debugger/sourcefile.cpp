
#include "stdafx.h"
#include "sourcefile.h"
#include "prefs.h"

IMPLEMENT_DYNAMIC( SourceFile,CRichEditCtrl )
BEGIN_MESSAGE_MAP( SourceFile,CRichEditCtrl )
	ON_WM_CREATE()
END_MESSAGE_MAP()

SourceFile::SourceFile(){
}

SourceFile::~SourceFile(){
}

int SourceFile::OnCreate( LPCREATESTRUCT lpCreateStruct ){
	CRichEditCtrl::OnCreate( lpCreateStruct );

	SetReadOnly( true );
	SetFont( &prefs.editFont );
	SetBackgroundColor( false,prefs.rgb_bkgrnd );

	CHARFORMAT fmt={sizeof( fmt )};
	fmt.dwMask=CFM_COLOR;
	fmt.crTextColor=prefs.rgb_default;

	SetSel( 0,-1 );
	SetDefaultCharFormat( fmt );
	SetSelectionCharFormat( fmt );
	SetSel( 0,0 );

	return 0;
}

void SourceFile::highLight( int row,int col ){
	int pos=LineIndex( row )+col;
	HideSelection( true,false );
	bool quote=false;
	int end=pos,len=GetTextLength();
	while( end<len ){
		char temp[8];
		SetSel( end,end+1 );
		GetSelText( temp );
		if( temp[0]=='\"' ) quote=!quote;
		if( !quote && (temp[0]==':' || !isprint( temp[0] )) ) break;
		++end;
	}
	HideSelection( false,false );
	SetSel( pos,end );
}
