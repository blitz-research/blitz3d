
#ifndef BBINPUT_H
#define BBINPUT_H

#include <vector>

#include "bbsys.h"
#include "../gxruntime/gxinput.h"

extern gxInput *gx_input;
extern gxDevice *gx_mouse;
extern gxDevice *gx_keyboard;
extern std::vector<gxDevice*> gx_joysticks;

//keyboard
int   bbKeyDown( int n );
int   bbKeyHit( int n );
int   bbGetKey();
int   bbWaitKey();
void  bbFlushKeys();

//mouse
int   bbMouseDown( int n );
int   bbMouseHit( int n );
int   bbGetMouse();
int   bbWaitMouse();
int   bbMouseX();
int   bbMouseY();
int   bbMouseXSpeed();
int   bbMouseYSpeed();
void  bbMoveMouse( int x,int y );
void  bbFlushMouse();

//joysticks
int		 bbJoyType( int port );
int		 bbJoyDown( int n,int port );
int		 bbJoyHit( int n,int port );
int		 bbGetJoy( int port );
int		 bbWaitJoy( int port );
float	 bbJoyX( int port );
float	 bbJoyY( int port );
float	 bbJoyZ( int port );
float	 bbJoyU( int port );
float	 bbJoyV( int port );
float	 bbJoyPitch( int port );
float	 bbJoyYaw( int port );
float	 bbJoyRoll( int port );
int		 bbJoyXDir( int port );
int		 bbJoyYDir( int port );
int		 bbJoyZDir( int port );
int		 bbJoyUDir( int port );
int		 bbJoyVDir( int port );
void	 bbFlushJoy();

#endif