
/*

  Note - does not appear to like DPSESSION_MULTICASTSERVER very much!

  */

#include "std.h"
#include "multiplay.h"
#include "multiplay_setup.h"

struct Player;

static bool host;

static map<DPID,Player*> player_map;
static list<Player*> players,new_players;

static int msg_type;
static string msg_data;
static DPID msg_from,msg_to;

static char *recv_buff;
static int recv_buff_sz;

static char *send_buff;
static int send_buff_sz;

#pragma pack( push,1 )
struct bbMsg{
	DPID from,to;
	char type;
};
#pragma pack( pop )

struct Player{
	DPID id;
	string name;
	bool remote;

	Player( DPID i,const string &n,bool r ):id(i),name(n),remote(r){
		players.push_back( this );
		if( remote ) new_players.push_back( this );
		player_map.clear();
	}

	Player::~Player(){
		new_players.remove( this );
		players.remove( this );
		player_map.clear();
	}
};

static void chk(){
	if( !dirPlay ){
		RTEX( "Multiplayer game not started" );
	}
}

static void clearPlayers(){
	while( players.size() ) delete players.back();
	new_players.clear();
	player_map.clear();
}

static Player *findPlayer( DPID id ){
	if( !player_map.size() ){
		list<Player*>::iterator it;
		for( it=players.begin();it!=players.end();++it ){
			player_map.insert( pair<DPID,Player*>( (*it)->id,(*it) ) );
		}
	}
	map<DPID,Player*>::iterator it=player_map.find( id );
	return it==player_map.end() ? 0 : it->second;
}

static BOOL FAR PASCAL enumPlayer( DPID id,DWORD type,LPCDPNAME name,DWORD flags,LPVOID context ){
	Player *p=findPlayer( id );if( p ) return TRUE;
	p=d_new Player( id,string( name->lpszShortNameA ),true );
	return TRUE;
}

void multiplay_link( void(*rtSym)(const char*,void*) ){
	rtSym( "%StartNetGame",bbStartNetGame );
	rtSym( "%HostNetGame$game_name",bbHostNetGame );
	rtSym( "%JoinNetGame$game_name$ip_address",bbJoinNetGame );
	rtSym( "StopNetGame",bbStopNetGame );

	rtSym( "%CreateNetPlayer$name",bbCreateNetPlayer );
	rtSym( "DeleteNetPlayer%player",bbDeleteNetPlayer );
	rtSym( "$NetPlayerName%player",bbNetPlayerName );
	rtSym( "%NetPlayerLocal%player",bbNetPlayerLocal );

	rtSym( "%SendNetMsg%type$msg%from_player%to_player=0%reliable=1",bbSendNetMsg );

	rtSym( "%RecvNetMsg",bbRecvNetMsg );
	rtSym( "%NetMsgType",bbNetMsgType );
	rtSym( "%NetMsgFrom",bbNetMsgFrom );
	rtSym( "%NetMsgTo",bbNetMsgTo );
	rtSym( "$NetMsgData",bbNetMsgData );
}

bool multiplay_create(){

	recv_buff_sz=send_buff_sz=1024;
	recv_buff=d_new char[recv_buff_sz];
	send_buff=d_new char[send_buff_sz];

	multiplay_setup_create();

	return true;
}

bool multiplay_destroy(){

	bbStopNetGame();

	multiplay_setup_destroy();

	delete[] recv_buff;recv_buff=0;
	delete[] send_buff;send_buff=0;

	return true;
}

static int startGame( int n ){
	clearPlayers();
	if( !n ) return 0;
	if( dirPlay->EnumPlayers( 0,enumPlayer,0,0 )>=0 ){
		host=n==2;
		return n;
	}
	multiplay_setup_close();
	return 0;
}

int bbStartNetGame(){
	if( dirPlay ){
		RTEX( "Multiplayer game already started" );
	}
	return startGame( multiplay_setup_open() );
}

int bbHostNetGame( BBStr *name ){
	if( dirPlay ){
		RTEX( "Multiplayer game already started" );
	}
	string n=*name;delete name;
	return startGame( multiplay_setup_host( n ) );
}

int bbJoinNetGame( BBStr *name,BBStr *address ){
	if( dirPlay ){
		RTEX( "Multiplayer game already started" );
	}
	string n=*name,a=*address;delete name;delete address;
	return startGame( multiplay_setup_join( n,a ) );
}

void bbStopNetGame(){
	multiplay_setup_close();
	clearPlayers();
}

DPID bbCreateNetPlayer( BBStr *nm ){
	chk();

	string t=*nm;
	string t0=t+'\0';
	delete nm;

	DPID id;
	DPNAME name;
	memset( &name,0,sizeof( name ) );
	name.dwSize=sizeof(name);name.lpszShortNameA=(char*)t0.data();

	if( dirPlay->CreatePlayer( &id,&name,0,0,0,0 )<0 ) return 0;

	Player *p=d_new Player( id,t,false );

	if( players.size()==1 ){
		if( dirPlay->EnumPlayers( 0,enumPlayer,0,0 )<0 ){
			dirPlay->DestroyPlayer( id );
			delete p;
			return 0;
		}
	}
	return id;
}

void bbDeleteNetPlayer( DPID player ){
	chk();

	if( Player *p=findPlayer( player ) ){
		dirPlay->DestroyPlayer( player );
		delete p;
	}
}

BBStr *bbNetPlayerName( DPID player ){
	if( !player ) return d_new BBStr( "<all>" );
	Player *p=findPlayer( player );
	return d_new BBStr( p ? p->name : "<unknown>" );
}

int bbNetPlayerLocal( DPID player ){
	if( Player *p=findPlayer( player ) ) return p->remote ? 0 : 1;
	return 0;
}

int bbRecvNetMsg(){
	chk();

	msg_type=0;
	msg_data.resize(0);
	msg_from=DPID_UNKNOWN;msg_to=DPID_ALLPLAYERS;

 	while( !msg_type ){

		if( new_players.size() ){
			msg_from=new_players.front()->id;
			new_players.pop_front();
			msg_type=100;
			return 1;
		}

		DPID from,to;
		DWORD sz=recv_buff_sz;
		int n=dirPlay->Receive( &from,&to,0,recv_buff,&sz );

		if( n==DPERR_BUFFERTOOSMALL ){
			sz=recv_buff_sz=sz/2+sz;
			delete[] recv_buff;recv_buff=d_new char[recv_buff_sz];
			n=dirPlay->Receive( &from,&to,0,recv_buff,&sz );
		}

		if( n!=DP_OK ) return 0;

		if( from==DPID_SYSMSG ){
			switch( *(DWORD*)recv_buff ){
			case DPSYS_CREATEPLAYERORGROUP:
				if( DPMSG_CREATEPLAYERORGROUP *msg=(DPMSG_CREATEPLAYERORGROUP*)recv_buff ){
					if( findPlayer( from=msg->dpId ) ) continue;
					d_new Player( from,string( msg->dpnName.lpszShortNameA ),true );
					continue;
				}
				break;
			case DPSYS_DESTROYPLAYERORGROUP:
				if( DPMSG_DESTROYPLAYERORGROUP *msg=(DPMSG_DESTROYPLAYERORGROUP*)recv_buff ){
					Player *p=findPlayer( msg->dpId );if( !p ) continue;
					delete p;msg_from=msg->dpId;msg_type=101;
				}
				break;
			case DPSYS_HOST:
				if( !host ){
					host=true;msg_type=102;
				}
				break;
			case DPSYS_SESSIONLOST:
				msg_type=200;
				break;
			}
		}else{
			bbMsg *m=(bbMsg*)recv_buff;
			Player *p=findPlayer( m->from );
			if( p && !p->remote ) continue;
			msg_data=string( (char*)(m+1),sz-sizeof(bbMsg) );
			msg_from=m->from;msg_to=m->to;
			msg_type=m->type;
		}
	}
	return 1;
}

int bbNetMsgType(){
	return msg_type;
}

BBStr *bbNetMsgData(){
	return d_new BBStr( msg_data );
}

DPID bbNetMsgFrom(){
	return msg_from;
}

DPID bbNetMsgTo(){
	return msg_to;
}

int bbSendNetMsg( int type,BBStr *msg,DPID from,DPID to,int reliable ){
	chk();

	int sz=msg->size()+sizeof(bbMsg);
	if( sz>send_buff_sz ){
		send_buff_sz=sz/2+sz;
		delete send_buff;send_buff=d_new char[send_buff_sz];
	}
	bbMsg *m=(bbMsg*)send_buff;
	m->type=type;m->from=from;m->to=to;

	memcpy( m+1,msg->data(),msg->size() );

	if( !to ) to=DPID_ALLPLAYERS;
	int n=dirPlay->Send( from,to,reliable ? DPSEND_GUARANTEED : 0,send_buff,sz );
	delete msg;

	return n>=0;
}
