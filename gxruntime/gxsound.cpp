
#include "std.h"
#include "gxsound.h"
#include "gxaudio.h"

gxSound::gxSound( gxAudio *a,FSOUND_SAMPLE *s ):
audio(a),sample(s),defs_valid(true){
	FSOUND_Sample_GetDefaults( sample,&def_freq,&def_vol,&def_pan,&def_pri );
}

gxSound::~gxSound(){
	FSOUND_Sample_Free( sample );
}

void gxSound::setDefaults(){
	if( !defs_valid ){
		FSOUND_Sample_SetDefaults( sample,def_freq,def_vol,def_pan,def_pri );
		defs_valid=true;
	}
}

gxChannel *gxSound::play(){
	setDefaults();
	return audio->play( sample );
}

gxChannel *gxSound::play3d( const float pos[3],const float vel[3] ){
	setDefaults();
	return audio->play3d( sample,pos,vel );
}

void gxSound::setLoop( bool loop ){
	FSOUND_Sample_SetMode( sample,loop ? FSOUND_LOOP_NORMAL : FSOUND_LOOP_OFF );
}

void gxSound::setPitch( int hertz ){
	def_freq=hertz;
	defs_valid=false;
}

void gxSound::setVolume( float volume ){
	def_vol=volume*255.0f;
	defs_valid=false;
}

void gxSound::setPan( float pan ){
	def_pan=(pan+1.0f)*127.5f;
	defs_valid=false;
}

