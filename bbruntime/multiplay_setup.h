
#ifndef MULTIPLAY_SETUP_H
#define MULTIPLAY_SETUP_H

#include <dplay.h>
#include <dplobby.h>

extern IDirectPlay4 *dirPlay;

void multiplay_setup_create();
void multiplay_setup_destroy();

int		multiplay_setup_open();
int		multiplay_setup_host( const string &game_name );
int		multiplay_setup_join( const string &game_name,const string &ip_add );
void	multiplay_setup_close();

#endif
