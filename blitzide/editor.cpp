
#include "stdafx.h"
#include "blitzide.h"
#include "editor.h"

static bool locked;

#ifdef DEMO
static const int TEXTLIMIT=16384;
#else
static const int TEXTLIMIT=1024*1024-1;
#endif

static const UINT wm_Find=RegisterWindowMessage( FINDMSGSTRING );

IMPLEMENT_DYNAMIC( Editor,CWnd )
BEGIN_MESSAGE_MAP( Editor,CWnd )
	ON_WM_CREATE()
	ON_WM_SIZE()
	ON_WM_SETFOCUS()
	ON_WM_KILLFOCUS()
	ON_WM_PAINT()
	ON_WM_MOUSEMOVE()
	ON_WM_LBUTTONDOWN()
	ON_WM_LBUTTONUP()
	ON_CONTROL( EN_CHANGE,1,en_change )
	ON_CONTROL( EN_UPDATE,1,en_update )
	ON_NOTIFY( EN_SELCHANGE,1,en_selchange )
	ON_NOTIFY( EN_PROTECTED,1,en_protected )
	ON_NOTIFY( EN_MSGFILTER,1,en_msgfilter )
	ON_REGISTERED_MESSAGE( wm_Find,onFind )
END_MESSAGE_MAP()

static int blink;
static set<string> keyWordSet;
static map<string,string> keyWordMap;

static bool isid( int c ){
	return isalnum(c)||c=='_';
}

static bool isfmt( int ch,int nxt ){
	return ch==';' || ch=='\"' || isalpha(ch) || isdigit(ch) || (ch=='$' && isxdigit(nxt));
}

static string rtfbgr( int bgr ){
	return "\\red"+itoa(bgr&0xff)+"\\green"+itoa((bgr>>8)&0xff)+"\\blue"+itoa((bgr>>16)&0xff)+';';
}

DWORD Editor::streamIn( LPBYTE buff,LONG cnt,LONG *done ){
	int n=0;
	while( n<cnt ){
		if( is_curs==is_line.size() ){
			if( is_stream->peek()==EOF ) break;
			is_curs=0;is_line="";int c=0;
			for(;;){
				c=is_stream->get();
				if( c=='\r' || c=='\n' || c==EOF ) break;
				if( c=='\\' || c=='{' || c=='}' ) is_line+='\\';
				is_line+=(char)c;
			}
			formatStreamLine();++is_linenum;
			if( c=='\r' && is_stream->peek()=='\n' ) is_stream->get();
			if( is_stream->peek()==EOF ) is_line+='}';
		}
		int sz=is_line.size()-is_curs;
		if( n+sz>cnt ) sz=cnt-n;
		memcpy( buff+n,is_line.data()+is_curs,sz );
		is_curs+=sz;n+=sz;
	}
	*done=n;
	return 0;
}

DWORD CALLBACK Editor::streamIn( DWORD cookie,LPBYTE buff,LONG cnt,LONG *done ){
	Editor *e=(Editor*)cookie;
	return e->streamIn( buff,cnt,done );
}

DWORD CALLBACK Editor::streamOut( DWORD cookie,LPBYTE buff,LONG cnt,LONG *done ){
	ostream *out=(ostream*)cookie;
	out->write( (char*)buff,cnt );
	*done=cnt;return 0;
}

Editor::Editor( EditorListener *l ):
listener(l),sizing(false),tabber_width(170),
fmtBusy(false),findOnly(false),found(false),
finder(0),selStart(0),selEnd(0),
findFlags(0),lineToFmt(-1){
	findBuff[0]=replaceBuff[0]=0;
	if( !blink ) blink=GetCaretBlinkTime();
	funcList.setListener( this );
	typeList.setListener( this );
	labsList.setListener( this );
}

Editor::~Editor(){
}

void Editor::resized(){
	CRect r;
	GetClientRect( &r );
	int x=0,y=0,w=r.Width(),h=r.Height();
	if( w ){
		if( tabber_width<4 ) tabber_width=4;
		else if( w-64>0 && tabber_width>w-64 ) tabber_width=w-64;
	}
	editCtrl.MoveWindow( x,y,w-tabber_width-4,y+h );
	tabber.MoveWindow( w-tabber_width+4,y,tabber_width-4,y+h );
}

void Editor::OnPaint(){
	CPaintDC dc( this );

	CRect r;
	GetClientRect( &r );
	int x=0,y=0,w=r.Width(),h=r.Height();

	x=w-tabber_width-4;
	w=8;

	CBrush br( GetSysColor( COLOR_3DFACE ) );
	CRect tr( x,y,x+w,y+h );
	dc.FillRect( &tr,&br );

//	CRect ar( x,y,x+w,y+w );
//	dc.DrawFrameControl( &ar,DFC_SCROLL,DFCS_SCROLLRIGHT );
//	y+=w;h-=w;

	CRect dr( x+2,y+2,x+w-2,y+h-2 );
	dc.DrawEdge( &dr,EDGE_RAISED,BF_RECT );
}

void Editor::OnMouseMove( UINT flags,CPoint p ){
	CWnd::OnMouseMove( flags,p );

	CRect r;
	GetClientRect( &r );
	if( sizing ){
		int dx=p.x-point.x,dy=p.y-point.y;
		tabber_width-=dx;
		resized();Invalidate();
		point=p;
	}else if( abs(p.x-(r.Width()-tabber_width))<4 ){
		SetCursor( AfxGetApp()->LoadStandardCursor( IDC_SIZEWE ) );
	}else{
		SetCursor( AfxGetApp()->LoadStandardCursor( IDC_ARROW ) );
	}
}

void Editor::OnSize( UINT type,int sw,int sh ){
	CWnd::OnSize( type,sw,sh );

	resized();
}

void Editor::OnLButtonDown( UINT flags,CPoint p ){

	CRect r;
	GetClientRect( &r );

	if( abs(p.x-(r.Width()-tabber_width))<4 ){
		point=p;
		SetCapture();
		SetCursor( AfxGetApp()->LoadStandardCursor( IDC_SIZEWE ) );
		sizing=true;
	}
}

void Editor::OnLButtonUp( UINT flags,CPoint p ){
	if( sizing ){
		SetCursor( AfxGetApp()->LoadStandardCursor( IDC_ARROW ) );
		ReleaseCapture();
		sizing=false;
	}
	SetFocus();
}

int Editor::OnCreate( LPCREATESTRUCT cs ){
	CWnd::OnCreate( cs );

	CHARFORMAT fmt;fmt.cbSize=sizeof( fmt );
	fmt.dwMask=CFM_COLOR|CFM_PROTECTED;
	fmt.dwEffects=CFE_PROTECTED;
	fmt.crTextColor=prefs.rgb_default;

	PARAFORMAT pf;
	memset( &pf,0,sizeof( pf ) );
	pf.cbSize=sizeof( pf );
	pf.dwMask=PFM_TABSTOPS;
	pf.cTabCount=MAX_TAB_STOPS;
	int tabTwips=1440*8/GetDeviceCaps( ::GetDC(0),LOGPIXELSX ) * prefs.edit_tabs;
	for( int k=0;k<MAX_TAB_STOPS;++k ) pf.rgxTabs[k]=k*tabTwips;

	CRect r( 0,0,0,0 );

	editCtrl.Create( 
		WS_CHILD|WS_VISIBLE|WS_HSCROLL|WS_VSCROLL|WS_BORDER|
		ES_MULTILINE|ES_AUTOHSCROLL|ES_AUTOVSCROLL|ES_NOHIDESEL,
		r,this,1 );
	editCtrl.SetFont( &prefs.editFont );
	editCtrl.SetBackgroundColor( false,prefs.rgb_bkgrnd );
	editCtrl.SetDefaultCharFormat( fmt );
	editCtrl.SetEventMask( ENM_CHANGE|ENM_PROTECTED|ENM_KEYEVENTS|ENM_MOUSEEVENTS|ENM_SELCHANGE );
	editCtrl.SetParaFormat( pf );
	editCtrl.LimitText( TEXTLIMIT );
	if( editCtrl.GetLimitText()!=TEXTLIMIT ) AfxMessageBox( "Unable to set editor text Limit" );
	editCtrl.SetModify(false);

	tabber.Create( WS_CHILD|WS_VISIBLE|TCS_BUTTONS|TCS_HOTTRACK,r,this,2 );
	tabber.SetFont( &prefs.tabsFont );
	tabber.SetPadding( CSize(3,3) );

	tabber.setListener( this );

	int bk=prefs.rgb_bkgrnd;
	int fg=prefs.rgb_ident;

	int ws=WS_CHILD|WS_BORDER;

	funcList.Create( ws|LVS_LIST|LVS_SHOWSELALWAYS,r,&tabber,1 );
	funcList.SetFont( &prefs.debugFont );
	funcList.SetBkColor( bk );
	funcList.SetTextColor( fg );
	funcList.SetTextBkColor( bk );

	typeList.Create( ws|LVS_LIST|LVS_SHOWSELALWAYS,r,&tabber,2 );
	typeList.SetFont( &prefs.debugFont );
	typeList.SetBkColor( bk );
	typeList.SetTextColor( fg );
	typeList.SetTextBkColor( bk );

	labsList.Create( ws|LVS_LIST|LVS_SHOWSELALWAYS,r,&tabber,3 );
	labsList.SetFont( &prefs.debugFont );
	labsList.SetBkColor( bk );
	labsList.SetTextColor( fg );
	labsList.SetTextBkColor( bk );

	tabber.insert( 0,&funcList,"funcs" );
	tabber.insert( 1,&typeList,"types" );
	tabber.insert( 2,&labsList,"labels" );

	cursorMoved();

	return 0;
}

/************************************************* PUBLIC ***********************************************/

void Editor::setName( const string &n ){
	name=n;
}

bool Editor::setText( istream &in ){
//	editCtrl.HideCaret();
	fmtBusy=true;
	EDITSTREAM es;
	es.dwCookie=(DWORD)this;
	es.dwError=0;
	es.pfnCallback=streamIn;
	is_line="{{\\colortbl;"+rtfbgr(prefs.rgb_string)+rtfbgr(prefs.rgb_ident)+
	rtfbgr(prefs.rgb_keyword)+rtfbgr(prefs.rgb_comment)+rtfbgr(prefs.rgb_digit)+
	rtfbgr(prefs.rgb_default)+"}";
	int tabTwips=1440*8/GetDeviceCaps( ::GetDC(0),LOGPIXELSX ) * prefs.edit_tabs;
	for( int k=0;k<MAX_TAB_STOPS;++k ) is_line+="\\tx"+itoa( k*tabTwips )+' ';
	is_stream=&in;
	is_curs=is_linenum=0;
	funcList.clear();
	typeList.clear();
	labsList.clear();
	editCtrl.StreamIn( SF_RTF,es );
	fmtBusy=false;
//	editCtrl.HideCaret();
	caret();
	return es.dwError==0;
}

void Editor::setModified( bool n ){
	editCtrl.SetModify( n );
}

void Editor::setCursor( int n ){
	int row=(n>>16)&0xffff,col=n&0xffff;
	int pos=editCtrl.LineIndex( row )+col;
	editCtrl.SetSel( pos,pos );
}

string Editor::getName()const{
	return name;
}

bool Editor::getText( ostream &out ){
	fixFmt(true);
	EDITSTREAM es;
	es.dwCookie=(DWORD)&out;
	es.dwError=0;
	es.pfnCallback=streamOut;
	editCtrl.StreamOut( SF_TEXT,es );
	return es.dwError==0;
}

void Editor::cut(){
	editCtrl.Cut();
}

void Editor::copy(){
	editCtrl.Copy();
}

void Editor::paste(){
	editCtrl.PasteSpecial( CF_TEXT,0 );
}

bool Editor::canCutCopy(){
	getSel();return selStart!=selEnd;
}

bool Editor::canPaste(){
	return editCtrl.CanPaste() ? true : false;
}

void Editor::print(){

	static const int MARG=720;	//1440=1 inch

	CPrintDialog dlg(false);
	int e=dlg.DoModal();if( e==IDCANCEL ) return;

	HDC hdc=dlg.GetPrinterDC();
	if( !hdc ){
		MessageBox( "Error printing" );
		return;
	}

	int hr=GetDeviceCaps(hdc,HORZRES),vr=GetDeviceCaps(hdc,VERTRES);
	int px=GetDeviceCaps(hdc,LOGPIXELSX),py=GetDeviceCaps(hdc,LOGPIXELSY);

	SetMapMode(hdc,MM_TEXT);

	FORMATRANGE fr={0};
	fr.hdc=fr.hdcTarget=hdc;
	fr.rcPage.left=fr.rcPage.top=0;
	fr.rcPage.right=(hr/px)*1440;
	fr.rcPage.bottom=(vr/py)*1440;

	//margins
	fr.rc.left=fr.rcPage.left+MARG;
	fr.rc.top=fr.rcPage.top+MARG;
	fr.rc.right=fr.rcPage.right-MARG;
	fr.rc.bottom=fr.rcPage.bottom-MARG;

	char buff[MAX_PATH];
	strcpy( buff,name.c_str() );

	DOCINFO di={sizeof(di)};
	di.lpszDocName=buff;

	getSel();
	int start=selStart,end=selEnd;
	if( start==end ){ start=0;end=editCtrl.GetTextLength(); }

	StartDoc(hdc,&di);
	while( start<end ){
		StartPage(hdc);
		fr.chrg.cpMin=start;
		fr.chrg.cpMax=end;
		start=editCtrl.FormatRange(&fr,true);
		EndPage(hdc);
	}
	EndDoc(hdc);

	editCtrl.FormatRange(0,false);
	DeleteDC(hdc);
}

void Editor::find(){
	if( finder ) return;
	finder=new CFindReplaceDialog();
	finder->m_fr.lpstrFindWhat=findBuff;
	finder->m_fr.wFindWhatLen=256;
	finder->m_fr.lpstrReplaceWith=replaceBuff;
	finder->m_fr.wReplaceWithLen=256;
	finder->Create( findOnly=true,0,0,FR_HIDEUPDOWN,this );
	found=false;
}

void Editor::replace(){
	if( finder ) return;
	finder=new CFindReplaceDialog();
	finder->m_fr.lpstrFindWhat=findBuff;
	finder->m_fr.wFindWhatLen=256;
	finder->m_fr.lpstrReplaceWith=replaceBuff;
	finder->m_fr.wReplaceWithLen=256;
	finder->Create( findOnly=false,0,0,FR_HIDEUPDOWN,this );
	found=false;
}

bool Editor::canFind(){
	return finder==0;
}

bool Editor::findNext( bool wrap ){
	long start,end;
	editCtrl.GetSel( start,end );

	FINDTEXTEX t;
	memset( &t,0,sizeof( t ) );
	t.chrg.cpMin=end;
	t.chrg.cpMax=-1;
	t.lpstrText=findBuff;
	if( editCtrl.FindText( findFlags,&t )>=0 ){
		editCtrl.SetSel( t.chrgText.cpMin,t.chrgText.cpMax );
		return true;
	}
	if( !wrap ) return false;
	t.chrg.cpMin=0;
	t.chrg.cpMax=end;
	if( editCtrl.FindText( findFlags,&t )>=0 ){
		editCtrl.SetSel( t.chrgText.cpMin,t.chrgText.cpMax );
		return true;
	}
	string s( "Can't find \"" );s+=findBuff;s+='\"';
	MessageBox( s.c_str(),"Text not found" );
	if( finder ) finder->SetFocus();
	return false;
}

void Editor::hilight( int pos ){
	int row=(pos>>16)&0xffff,col=pos&0xffff;
	pos=editCtrl.LineIndex( row )+col;

	editCtrl.HideSelection( true,false );
	getSel();
	bool quote=false;
	int end=pos,len=editCtrl.GetTextLength();
	while( end<len ){
		char temp[8];
		editCtrl.SetSel( end,end+1 );
		editCtrl.GetSelText( temp );
		if( temp[0]=='\"' ) quote=!quote;
		if( !quote && (temp[0]==':' || !isprint( temp[0] )) ) break;
		++end;
	}
	editCtrl.HideSelection( false,false );
	editCtrl.SetSel( pos,end );
}

void Editor::selectAll(){
	editCtrl.SetSel( 0,-1 );
}

void Editor::lock(){
	locked=true;
}

void Editor::unlock(){
	locked=false;
}

string Editor::getKeyword(){
	fixFmt(true);
	getSel();
	int ln=editCtrl.LineFromChar(selStart);
	int pos=selStart-editCtrl.LineIndex( ln );
	string line=getLine( ln );if( pos>line.size() ) return "";

	//ok, scan back until we have an isapha char preceded by a nonalnum/non '_' char
	for(;;){
		while( pos>0 && ( !isalpha(line[pos]) || isid(line[pos-1]) ) ) --pos;
		if( !isalpha(line[pos]) ) return "";
		int end=pos;while( end<line.size() && isid(line[end]) ) ++end;
		string t=line.substr( pos,end-pos );
		if( keyWordSet.find( t )!=keyWordSet.end() ) return t;
		if( !pos ) return "";
		--pos;
	}
}

bool Editor::getModified(){
	return editCtrl.GetModify() ? true : false;
}

int Editor::getCursor(){
	long start,end;
	editCtrl.GetSel( start,end );
	int row,col;
	row=editCtrl.LineFromChar( start );
	col=start-editCtrl.LineIndex( row );
	return ((row)<<16)|(col);
}

void Editor::getCursor( int *row,int *col ){
	long start,end;
	editCtrl.GetSel( start,end );
	*row=editCtrl.LineFromChar( end );
	*col=end-editCtrl.LineIndex( *row );
}

void Editor::addKeyword( const string &s ){
	keyWordSet.insert( s );
	string t=s;
	for( int k=0;k<t.size();++k ) t[k]=tolower(t[k]);
	keyWordMap[t]=s;
}

/************************************************* PRIVATE ***********************************************/

void Editor::endFind(){
	if( !finder ) return;
	finder->DestroyWindow();
	finder=0;
}

LRESULT Editor::onFind( WPARAM w,LPARAM l ){
	if( !finder ) return 0;

	findFlags=0;
	if( finder->MatchCase() ) findFlags|=FR_MATCHCASE;
	if( finder->MatchWholeWord() ) findFlags|=FR_WHOLEWORD;
	strcpy( findBuff,finder->GetFindString() );
	strcpy( replaceBuff,finder->GetReplaceString() );

	if( finder->FindNext() ){
		found=findNext( true );
		if( found && findOnly ) endFind();
	}else if( finder->ReplaceCurrent() ){
		if( found ) editCtrl.ReplaceSel( replaceBuff,true );
		found=findNext( true );
	}else if( finder->ReplaceAll() ){
		int cnt=0;
		editCtrl.HideSelection( true,false );
		editCtrl.SetSel( 0,0 );
		while( findNext( false ) ){
			editCtrl.ReplaceSel( replaceBuff,true );
			++cnt;
		}
		endFind();
		char buff[32];itoa( cnt,buff,10 );
		string s( buff );s+=" occurances replaced";
		MessageBox( s.c_str(),"Replace All Done" );
		editCtrl.HideSelection( false,false );
	}

	if( finder && finder->IsTerminating() ) endFind();

	return 0;
}

void Editor::caret(){
	if( !prefs.edit_blkcursor ) return;
	long start,end;
	editCtrl.GetSel( start,end );
	if( start==end ){
		editCtrl.CreateSolidCaret( 8,13 );
		editCtrl.ShowCaret();
	}else editCtrl.HideCaret();
}

void Editor::OnSetFocus( CWnd *wnd ){
	if( prefs.edit_blkcursor ) SetCaretBlinkTime( 200 );
	editCtrl.SetFocus();
	caret();
}

void Editor::OnKillFocus( CWnd *wnd ){
	CWnd::OnKillFocus( wnd );
	fixFmt(true);
}

string Editor::getLine( int line ){
	int idx1=editCtrl.LineIndex( line );
	int idx2=editCtrl.LineIndex( line+1 );if( idx2==-1 ) idx2=editCtrl.GetTextLength();
	int len=idx2-idx1;
	char *buff=new char[ len>3 ? len+1 : 4 ];
	*(int*)buff=len;
	int out=editCtrl.GetLine( line,buff );
	buff[len]=0;
	string t=string( buff );
	delete [] buff;
	return t;
}

void Editor::funcSelected( int line ){
	int pos=editCtrl.LineIndex( line );
	editCtrl.SetSel( editCtrl.GetTextLength()-1,editCtrl.GetTextLength()-1 );
	editCtrl.SetSel( pos,pos );
	SetFocus();
}

void Editor::currentSet( Tabber *tabber,int index ){
	SetFocus();
}

void Editor::cursorMoved(){
	listener->cursorMoved( this );
}

void Editor::en_update(){
	caret();
}

void Editor::en_msgfilter( NMHDR *nmhdr,LRESULT *result ){
	if( locked || fmtBusy ){ *result=1;return; }

	*result=0;
	getSel();

	MSGFILTER *msg=(MSGFILTER*)nmhdr;


	if( msg->msg==WM_RBUTTONDOWN ){

		CPoint p( LOWORD(msg->lParam),HIWORD(msg->lParam) );

		ClientToScreen( &p );

		CMenu *menu=blitzIDE.mainFrame->GetMenu();

		CMenu *edit=menu->GetSubMenu(1);

		edit->TrackPopupMenu( TPM_LEFTALIGN,p.x,p.y,blitzIDE.mainFrame );

	}else if( msg->msg==WM_CHAR ){
		if( msg->wParam=='\t' ){
			int lineStart=editCtrl.LineFromChar( selStart );
			int lineEnd=editCtrl.LineFromChar( selEnd );
			if( lineEnd<=lineStart ) return;
			editCtrl.HideSelection( true,false );
			if( GetAsyncKeyState( VK_SHIFT )&0x80000000 ){
				char buff[4];
				for( int line=lineStart;line<lineEnd;++line ){
					int n=editCtrl.LineIndex( line );
					editCtrl.SetSel( n,n+1 );editCtrl.GetSelText( buff );
					if( buff[0]=='\t' ) editCtrl.ReplaceSel( "",true );
				}
			}else{
				for( int line=lineStart;line<lineEnd;++line ){
					int n=editCtrl.LineIndex( line );
					editCtrl.SetSel( n,n );editCtrl.ReplaceSel( "\t",true );
				}
			}
			selStart=editCtrl.LineIndex( lineStart );
			selEnd=editCtrl.LineIndex( lineEnd );
			setSel();*result=1;
			editCtrl.HideSelection( false,false );
		}else if( msg->wParam==13 ){
			if( selStart!=selEnd ) return;
			int k;
			int ln=editCtrl.LineFromChar( selStart );
			int pos=selStart-editCtrl.LineIndex( ln );
			string line=getLine( ln );if( pos>line.size() ) return;
			for( k=0;k<pos && line[k]=='\t';++k ){}
			line="\r\n"+line.substr( 0,k )+'\0';
			editCtrl.ReplaceSel( line.data(),true );
			*result=1;
		}
	}
	caret();
}

void Editor::en_selchange( NMHDR *nmhdr,LRESULT *result ){
	if( !fmtBusy ) fixFmt(false);
	cursorMoved();
	caret();
}

void Editor::en_protected( NMHDR *nmhdr,LRESULT *result ){
	*result=0;if( fmtBusy ) return;
	fmtLineCount=editCtrl.GetLineCount();
	found=false;
}

void Editor::en_change(){
	if( fmtBusy ) return;

	fmtBusy=true;
	editCtrl.HideSelection( true,false );
	getSel();

	int begin=editCtrl.LineFromChar( selStart );
	int end=begin+1;

 	int lineCount=editCtrl.GetLineCount();
	int delta=lineCount-fmtLineCount;

	if( delta>0 ){
		begin-=delta;
		funcList.relocate( begin,delta );
		typeList.relocate( begin,delta );
		labsList.relocate( begin,delta );
		funcList.remove( begin,end );
		typeList.remove( begin,end );
		labsList.remove( begin,end );
	}else if( delta<0 ){
		int t=end-delta;
		funcList.remove( begin,t );
		typeList.remove( begin,t );
		labsList.remove( begin,t );
		funcList.relocate( t,delta );
		typeList.relocate( t,delta );
		labsList.relocate( t,delta );
	}else{
		funcList.remove( begin,end );
		typeList.remove( begin,end );
		labsList.remove( begin,end );
	}

	for( int n=begin;n<end;++n ) formatLine( n );

	setSel();editCtrl.HideSelection( false,false );
	fmtBusy=false;
	cursorMoved();
}

void Editor::setFormat( int from,int to,int color,const string &s ){
	editCtrl.SetSel( from,to );
	if( s.size() ){
		char buff[256];
		editCtrl.GetSelText( buff );buff[to-from]=0;
		if( string(buff)!=s ){
			editCtrl.ReplaceSel( s.c_str() );
			editCtrl.SetSel( from,to );
		}
	}
	CHARFORMAT fmt;
	fmt.cbSize=sizeof( fmt );
	DWORD t=editCtrl.GetSelectionCharFormat( fmt );
	if( (t&CFM_COLOR) && fmt.crTextColor==color ) return;
	fmt.dwMask=CFM_COLOR|CFM_PROTECTED;
	fmt.dwEffects=CFE_PROTECTED;
	fmt.crTextColor=color;
	editCtrl.SetSelectionCharFormat( fmt );
}

void Editor::formatStreamLine(){
	string out;
	char cf='0';
	for( int k=0;k<is_line.size(); ){
		int from=k;
		char pf=cf;
		int c=is_line[k],is_sz=is_line.size();
		if( !isgraph( c ) ){
			for( ++k;k<is_sz && !isgraph(is_line[k]);++k ){}
		}else if( !isfmt( c,k+1<is_sz?is_line[k+1]:0 ) ){
			for( ++k;k<is_sz && !isfmt( is_line[k],k+1<is_sz?is_line[k+1]:0 );++k ){}
			cf='6';
		}else if( c==';' ){					//comment?
			k=is_sz;
			cf='4';
		}else if( c=='\"' ){		//string const?
			for( ++k;k<is_sz && is_line[k]!='\"';++k ){}
			if( k<is_sz ) ++k;
			cf='1';
		}else if( isalpha( c ) ){		//ident?
			for( ++k;k<is_sz && isid(is_line[k]);++k ){}
			if( keyWordSet.find( is_line.substr( from,k-from ) )==keyWordSet.end() ) cf='2';
			else cf='3';
		}else if( c=='$' ){
			for( ++k;k<is_sz && isxdigit(is_line[k]);++k ){}
			cf='5';
		}else if( isdigit( c ) ){	//numeric const?
			for( ++k;k<is_sz && isdigit(is_line[k]);++k ){}
			cf='5';
		}
		if( cf!=pf ){
			out+="\\cf";out+=cf;out+=' ';
		}
		out+=is_line.substr( from,k-from );
	}
	if( is_line[0]=='F' && is_line.find( "Function" )==0 ){
		for( int k=8;k<is_line.size();++k ){
			if( isalpha( is_line[k] ) ){
				int start=k;
				for( ++k;k<is_line.size() && isid(is_line[k]);++k ){}
				funcList.insert( is_linenum,is_line.substr( start,k-start ) );
				break;
			}
		}
	}else if( is_line[0]=='T' && is_line.find( "Type" )==0 ){
		for( int k=4;k<is_line.size();++k ){
			if( isalpha( is_line[k] ) ){
				int start=k;
				for( ++k;k<is_line.size() && isid(is_line[k]);++k ){}
				typeList.insert( is_linenum,is_line.substr( start,k-start ) );
				break;
			}
		}
	}else if( is_line[0]=='.' ){
		for( int k=1;k<is_line.size();++k ){
			if( isalpha( is_line[k] ) ){
				int start=k;
				for( ++k;k<is_line.size() && isid(is_line[k]);++k ){}
				labsList.insert( is_linenum,is_line.substr( start,k-start ) );
				break;
			}
		}
	}
	is_line=out+"\\line ";
}

void Editor::fixFmt( bool fmt ){
	if( lineToFmt<0 || fmtBusy ) return;
	fmtBusy=true;
	editCtrl.HideSelection( true,false );getSel();
	long start=selStart;if( fmt ) selStart=-1;
	formatLine( lineToFmt );
	selStart=start;setSel();editCtrl.HideSelection( false,false );
	fmtBusy=false;
}

void Editor::formatLine( int ln ){
	if( ln<0 || ln>=editCtrl.GetLineCount() ) return;

	lineToFmt=-1;
	int pos=editCtrl.LineIndex( ln );
	string tline=getLine( ln );
	string line=tolower( tline );

	int *cf=0;
	string rep;
	for( int k=0;k<line.size(); ){
		rep.resize(0);
		int *pf=cf;
		int from=k,c=line[k],sz=line.size();
		if( !isgraph( c ) ){
			for( ++k;k<sz && !isgraph(line[k]);++k ){}
		}else if( !isfmt( c,k+1<sz?line[k+1]:0 ) ){
			for( ++k;k<sz && !isfmt(line[k],k+1<sz?line[k+1]:0);++k ){}
			cf=&prefs.rgb_default;
		}else if( c==';' ){					//comment?
			k=sz;
			cf=&prefs.rgb_comment;
		}else if( c=='\"' ){		//string const?
			for( ++k;k<sz && line[k]!='\"';++k ){}
			if( k<sz ) ++k;
			cf=&prefs.rgb_string;
		}else if( isalpha( c ) ){		//ident?
			for( ++k;k<sz && isid(line[k]);++k ){}
			cf=&prefs.rgb_ident;pf=0;
			if( selStart<=pos+from || selStart>pos+k ){
				map<string,string>::iterator it=keyWordMap.find( line.substr( from,k-from ) );
				if( it!=keyWordMap.end() ){
					rep=it->second;cf=&prefs.rgb_keyword; 
				}
			}else lineToFmt=ln;
		}else if( c=='$' && k+1<sz && isxdigit(line[k+1]) ){
			for( ++k;k<sz && isxdigit(line[k]);++k ){}
			cf=&prefs.rgb_digit;
		}else if( isdigit( c ) ){	//numeric const?
			for( ++k;k<sz && isdigit(line[k]);++k ){}
			cf=&prefs.rgb_digit;
		}
		if( cf!=pf ) setFormat( pos+from,pos+k,*cf,rep );
	}
	if( line[0]=='f' && line.find( "function" )==0 ){
		for( int k=8;k<line.size();++k ){
			if( isalpha( line[k] ) ){
				int start=k;
				for( ++k;k<line.size() && isid(line[k]);++k ){}
				funcList.insert( ln,tline.substr( start,k-start ) );
				break;
			}
		}
	}else if( line[0]=='t' && line.find( "type" )==0 ){
		for( int k=4;k<line.size();++k ){
			if( isalpha( line[k] ) ){
				int start=k;
				for( ++k;k<line.size() && isid(line[k]);++k ){}
				typeList.insert( ln,tline.substr( start,k-start ) );
				break;
			}
		}
	}else if( line[0]=='.' ){
		for( int k=1;k<line.size();++k ){
			if( isalpha( line[k] ) ){
				int start=k;
				for( ++k;k<line.size() && isid(line[k]);++k ){}
				labsList.insert( ln,tline.substr( start,k-start ) );
				break;
			}
		}
	}
}
