
#ifndef BBAUDIO_H
#define BBAUDIO_H

#include "bbsys.h"
#include "../gxruntime/gxaudio.h"

extern gxAudio *gx_audio;

gxSound *	 bbLoadSound( BBStr *file );
void		 bbFreeSound( gxSound *sound );
gxChannel *	 bbPlaySound( gxSound *sound );
void		 bbLoopSound( gxSound *sound );
void		 bbSoundPitch( gxSound *sound,int pitch );
void		 bbSoundVolume( gxSound *sound,float volume );
void		 bbSoundPan( gxSound *sound,float pan );
gxChannel *	 bbPlayMusic( BBStr *s );
gxChannel *  bbPlayCDTrack( int track,int mode );
void		 bbStopChannel( gxChannel *channel );
void		 bbPauseChannel( gxChannel *channel );
void		 bbResumeChannel( gxChannel *channel );
void		 bbChannelPitch( gxChannel *channel,int pitch );
void		 bbChannelVolume( gxChannel *channel,float volume );
void		 bbChannelPan( gxChannel *channel,float pan );
int			 bbChannelPlaying( gxChannel *channel );

#endif

