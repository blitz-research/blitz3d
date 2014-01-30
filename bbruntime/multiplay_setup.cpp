
#include "std.h"
#include "bbsys.h"
#include "resource.h"
#include "multiplay_setup.h"

IDirectPlay4 *dirPlay;

struct Connection{
	GUID guid;
	string name;
	void *data;

	Connection( const GUID &g,const string &n,void *d,int sz ):guid(g),name(n){
		data=d_new char[sz];memcpy( data,d,sz );
	}

	~Connection(){
		delete[] data;
	}
};

struct Session{
	GUID guid;
	string name;
	int max_players,curr_players,data1,data2;

	Session( const DPSESSIONDESC2 *desc ){
		guid=desc->guidInstance;
		name=string( desc->lpszSessionNameA );
		max_players=desc->dwMaxPlayers;
		curr_players=desc->dwCurrentPlayers;
		data1=desc->dwUser1;data2=desc->dwUser2;
	}
};

static int timer;
static vector<Connection*> connections;
static vector<Session*> sessions;

static void clearSessions(){
	for( ;sessions.size();sessions.pop_back() ) delete sessions.back();
}

static void clearConnections(){
	for( ;connections.size();connections.pop_back() ) delete connections.back();
}

static bool openDirPlay( HWND hwnd ){
	if( dirPlay ) return true;
	if( CoCreateInstance( CLSID_DirectPlay,0,CLSCTX_ALL,IID_IDirectPlay4A,(void**)&dirPlay )>=0 ) return true;
	MessageBox( hwnd,"Error opening DirectPlay","DirectPlay Error",MB_ICONWARNING );
	return false;
}

static bool closeDirPlay( HWND hwnd ){
	if( hwnd && timer ) KillTimer( hwnd,timer );
	timer=0;if( !dirPlay ) return true;
	dirPlay->Close();
	int n=dirPlay->Release();
	dirPlay=0;return n==0;
}

static BOOL FAR PASCAL enumConnection( LPCGUID guid,LPVOID conn,DWORD size,LPCDPNAME name,DWORD flags,LPVOID context ){
	IDirectPlay4 *dp;
	if( CoCreateInstance( CLSID_DirectPlay,0,CLSCTX_ALL,IID_IDirectPlay4A,(void**)&dp )<0 ) return FALSE;
	int n=dp->InitializeConnection( conn,0 );
	dp->Release();if( n<0 ) return TRUE;

	Connection *c=d_new Connection( *guid,string( strdup( name->lpszShortNameA ) ),conn,size );
	connections.push_back( c );

	return TRUE;
}

static BOOL FAR PASCAL enumSession( LPCDPSESSIONDESC2 desc,LPDWORD timeout,DWORD flags,LPVOID lpContext ){

	if( !desc ) return FALSE;
	sessions.push_back( d_new Session( desc ) );
	return TRUE;
}

static bool startGame( HWND hwnd ){
	if( !dirPlay ) return false;

	char buff[MAX_PATH];
	int n=GetWindowText( GetDlgItem( hwnd,IDC_GAMENAME ),buff,MAX_PATH );
	if( !n ){
		MessageBox( hwnd,"Please enter a name for the new game","DirectPlay Request",MB_SETFOREGROUND|MB_TOPMOST|MB_ICONINFORMATION|MB_OK );
		return false;
	}

	string name=string( buff )+'\0';

	DPSESSIONDESC2 desc;
	memset(&desc,0,sizeof(desc));
	desc.dwSize=sizeof(desc);
	desc.guidApplication=GUID_NULL;
	desc.dwFlags=
		DPSESSION_KEEPALIVE|
		DPSESSION_MIGRATEHOST|
		DPSESSION_NOMESSAGEID|
		DPSESSION_OPTIMIZELATENCY|
		DPSESSION_DIRECTPLAYPROTOCOL;
	desc.lpszSessionNameA=(char*)name.data();

	if( dirPlay->Open( &desc,DPOPEN_CREATE )<0 ){
		MessageBox( hwnd,"Unable to create new game","DirPlay Error",MB_ICONWARNING );
		return false;
	}
	return true;
}

static bool joinGame( HWND hwnd ){
	if( !dirPlay ) return false;

	int ses=SendDlgItemMessage( hwnd,IDC_GAMELIST,LB_GETCURSEL,0,0 );
	if( ses<0 || ses>=sessions.size() ) return false;

	DPSESSIONDESC2 desc;
	memset(&desc,0,sizeof(desc));
	desc.dwSize=sizeof(desc);
	desc.guidInstance=sessions[ses]->guid;

	if( dirPlay->Open( &desc,DPOPEN_JOIN )<0 ){
		MessageBox( hwnd,"Unable to join game","DirPlay Error",MB_ICONWARNING );
		return false;
	}
	return true;
}

static bool enumSessions( HWND hwnd ){
	if( !dirPlay ) return false;

	clearSessions();
	EnableWindow( GetDlgItem( hwnd,IDC_GAMELIST ),true );
	SendDlgItemMessage( hwnd,IDC_GAMELIST,LB_RESETCONTENT,0,0 );

	DPSESSIONDESC2 desc;
	memset(&desc,0,sizeof(desc));
	desc.dwSize=sizeof(desc);
	desc.guidApplication=GUID_NULL;

	int n=dirPlay->EnumSessions( &desc,0,enumSession,0,DPENUMSESSIONS_ASYNC );
	if( n>=0 ){
		if( !timer ) SetTimer( hwnd,timer=1,1000,0 );
		for( int k=0;k<sessions.size();++k ){
			SendDlgItemMessage( hwnd,IDC_GAMELIST,LB_ADDSTRING,0,(LPARAM)strdup( sessions[k]->name.c_str() ) );
		}
		if( !sessions.size() ){
			SendDlgItemMessage( hwnd,IDC_GAMELIST,LB_ADDSTRING,0,(LPARAM)"<no games found>" );
			EnableWindow( GetDlgItem( hwnd,IDC_GAMELIST ),false );
		}
		return true;
	}
	closeDirPlay( hwnd );
	if( n==DPERR_USERCANCEL ) return false;
	MessageBox( hwnd,"Unable to enumerate sessions","DirPlay Error",MB_ICONWARNING );
	return false;
}

static bool connect( HWND hwnd ){
	int con=SendDlgItemMessage( hwnd,IDC_CONNECTIONS,CB_GETCURSEL,0,0 );
	if( con<1 || con>=connections.size() ) return false;

	closeDirPlay( hwnd );
	if( openDirPlay( hwnd ) ){
		int n=dirPlay->InitializeConnection( connections[con]->data,0 );
		if( n>=0 ){
			if( enumSessions( hwnd ) ) return true;
		}else{
			if( n!=DPERR_USERCANCEL ){
				string t="Unable to open "+connections[con]->name;
				MessageBox( hwnd,t.c_str(),"DirPlay Error",MB_ICONWARNING );
			}
		}
		closeDirPlay( hwnd );
	}
	return false;
}

static void endDialog( HWND hwnd,int rc ){
	if( timer ) KillTimer( hwnd,timer );
	timer=0;
	if( !rc ) closeDirPlay( hwnd );
	EndDialog( hwnd,rc );
}

static BOOL CALLBACK dialogProc( HWND hwnd,UINT msg,WPARAM wparam,LPARAM lparam ){

	int k,lo=LOWORD(wparam),hi=HIWORD(wparam);

	bool reset=false;

	switch( msg ){
	case WM_INITDIALOG:
		SetForegroundWindow( hwnd );
		clearConnections();
		connections.push_back( d_new Connection( GUID_NULL,"<no connection>","",0 ) );
		if( openDirPlay( hwnd ) ){
			if( dirPlay->EnumConnections( 0,enumConnection,0,0 )<0 ){
				MessageBox( hwnd,"Failed to enumerate connections","DirectPlay Error",MB_SETFOREGROUND|MB_TOPMOST|MB_ICONWARNING|MB_OK );
			}
			closeDirPlay( hwnd );
		}
		for( k=0;k<connections.size();++k ){
			string t=connections[k]->name;
			SendDlgItemMessage( hwnd,IDC_CONNECTIONS,CB_ADDSTRING,0,(LPARAM)t.c_str() );
		}
		timer=0;
		reset=true;
		break;
	case WM_TIMER:	//refresh sessions list!
		if( timer && wparam==timer && !enumSessions( hwnd ) ) reset=true;
		break;
	case WM_CLOSE:
		endDialog( hwnd,0 );
		break;
	case WM_COMMAND:
		switch( hi ){
		case BN_CLICKED:
			switch( lo ){
			case IDC_CANCEL:
				endDialog( hwnd,0 );
				break;
			case IDC_GAMENAME:case IDC_HOSTGAME:
				if( startGame( hwnd ) ){
					endDialog( hwnd,2 );
				}
				break;
			}
			break;
		case LBN_DBLCLK:
			switch( lo ){
			case IDC_GAMELIST:
				if( joinGame( hwnd ) ){
					endDialog( hwnd,1 );
				}
				break;
			}
			break;
		case CBN_SELCHANGE:
			switch( lo ){
			case IDC_CONNECTIONS:
				if( connect( hwnd ) ){
					EnableWindow( GetDlgItem( hwnd,IDC_GAMENAME ),true );
					EnableWindow( GetDlgItem( hwnd,IDC_HOSTGAME ),true );
					break;
				}else{
					reset=true;
				}
				break;
			}
			break;
		}
		break;
	default:
		return 0;
	}

	if( reset ){
		closeDirPlay( hwnd );
		SendDlgItemMessage( hwnd,IDC_CONNECTIONS,CB_SETCURSEL,0,0 );
		EnableWindow( GetDlgItem( hwnd,IDC_GAMELIST ),false );
		EnableWindow( GetDlgItem( hwnd,IDC_HOSTGAME ),false );
		EnableWindow( GetDlgItem( hwnd,IDC_GAMENAME ),false );
		SetWindowText( GetDlgItem( hwnd,IDC_GAMENAME ),"" );
		SendDlgItemMessage( hwnd,IDC_GAMELIST,LB_RESETCONTENT,0,0 );
	}
	return 1;
}

void multiplay_setup_create(){
	dirPlay=0;
}

void multiplay_setup_destroy(){
	multiplay_setup_close();
}

int multiplay_setup_open(){
	gx_runtime->idle();

	int n=DialogBox( GetModuleHandle( "runtime" ),MAKEINTRESOURCE( IDD_MULTIPLAYER ),GetDesktopWindow(),dialogProc );

	if( n!=1 && n!=2 ) n=0;

	clearSessions();
	clearConnections();

	//NAUGHTY!
	gx_runtime->asyncRun();
	gx_runtime->idle();
	return n;
}

void multiplay_setup_close(){
	closeDirPlay( 0 );
}

int multiplay_setup_host( const string &game_name ){
	int ret=0;
	IDirectPlayLobby *lobby;
	IDirectPlayLobby3 *lobby3;
	if( CoCreateInstance( CLSID_DirectPlay,0,CLSCTX_ALL,IID_IDirectPlay4A,(void**)&dirPlay )>=0 ){
		if( DirectPlayLobbyCreate( 0,&lobby,0,0,0 )>=0 ){
			if( lobby->QueryInterface( IID_IDirectPlayLobby3,(void**)&lobby3 )>=0 ){
				//ok, create an address for initializeconnection
				string ip( "\0" );
				char address[256];DWORD sz=256;
				if( lobby3->CreateAddress( DPSPGUID_TCPIP,DPAID_INet,ip.data(),ip.size(),address,&sz )>=0 ){
					if( dirPlay->InitializeConnection( address,0 )>=0 ){
						string name=game_name+'\0';
						DPSESSIONDESC2 desc;
						memset(&desc,0,sizeof(desc));
						desc.dwSize=sizeof(desc);
						desc.guidApplication=GUID_NULL;
						desc.dwFlags=
							DPSESSION_KEEPALIVE|
							DPSESSION_MIGRATEHOST|
							DPSESSION_NOMESSAGEID|
							DPSESSION_OPTIMIZELATENCY|
							DPSESSION_DIRECTPLAYPROTOCOL;
						desc.lpszSessionNameA=(char*)name.data();
						if( dirPlay->Open( &desc,DPOPEN_CREATE )>=0 ){
							ret=2;
						}
					}
				}
				lobby3->Release();
			}
			lobby->Release();
		}
		if( !ret ){
			dirPlay->Release();
			dirPlay=0;
		}
	}
	return ret;
}

int multiplay_setup_join( const string &game_name,const string &ip_add ){
	int ret=0;
	IDirectPlayLobby *lobby;
	IDirectPlayLobby3 *lobby3;
	if( CoCreateInstance( CLSID_DirectPlay,0,CLSCTX_ALL,IID_IDirectPlay4A,(void**)&dirPlay )>=0 ){
		if( DirectPlayLobbyCreate( 0,&lobby,0,0,0 )>=0 ){
			if( lobby->QueryInterface( IID_IDirectPlayLobby3,(void**)&lobby3 )>=0 ){
				//ok, create an address for initializeconnection
				string ip=ip_add+'\0';
				char address[256];DWORD sz=256;
				if( lobby3->CreateAddress( DPSPGUID_TCPIP,DPAID_INet,ip.data(),ip.size(),address,&sz )>=0 ){
					if( dirPlay->InitializeConnection( address,0 )>=0 ){
						DPSESSIONDESC2 desc;
						memset(&desc,0,sizeof(desc));
						desc.dwSize=sizeof(desc);
						desc.guidApplication=GUID_NULL;
						if( dirPlay->EnumSessions( &desc,0,enumSession,0,0 )>=0 ){
							for( int k=0;k<sessions.size();++k ){
								if( sessions[k]->name!=game_name ) continue;
								desc.guidInstance=sessions[k]->guid;
								if( dirPlay->Open( &desc,DPOPEN_JOIN )>=0 ){
									ret=1;
								}
								break;
							}
						}
						clearSessions();
					}
				}
				lobby3->Release();
			}
			lobby->Release();
		}
		if( !ret ){
			dirPlay->Release();
			dirPlay=0;
		}
	}
	return ret;
}
