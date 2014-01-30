
#ifndef MULTIPLAY_H
#define MULTIPLAY_H

#include "bbsys.h"
#include <dplay.h>

void multiplay_link();
bool multiplay_create();
bool multiplay_destroy();

int		 bbStartNetGame();
int		 bbHostNetGame( BBStr *name );
int		 bbJoinNetGame( BBStr *name,BBStr *address );
void	 bbStopNetGame();

DPID	 bbCreateNetPlayer( BBStr *name );
void	 bbDeleteNetPlayer( DPID player );
BBStr *	 bbNetPlayerName( DPID player );
int		 bbNetPlayerLocal( DPID player );

int		 bbSendNetMsg( int type,BBStr *msg,DPID from,DPID to,int reliable );

int		 bbRecvNetMsg();
int		 bbNetMsgType();
BBStr *	 bbNetMsgData();
DPID	 bbNetMsgFrom();
DPID	 bbNetMsgTo();

#endif
