
#include "std.h"
#include "gxmusic.h"

gxMusic::gxMusic( gxAudio *a,FMUSIC_MODULE *m,FSOUND_STREAM *s ):
audio(a),module(m),stream(s),stream_channel(-1){
}

gxMusic::~gxMusic(){
	if( module ) FMUSIC_FreeSong( module );
	else FSOUND_Stream_Close( stream );
}

void gxMusic::play(){
	if( module ){
		FMUSIC_PlaySong( module );
	}else{
		stream_channel=FSOUND_Stream_Play( FSOUND_FREE,stream );
	}
}

void gxMusic::stop(){
	if( module ){
		FMUSIC_StopSong( module );
	}else{
		FSOUND_Stream_Stop( stream );
		stream_channel=-1;
	}
}

void gxMusic::setVolume( float volume ){
	if( module ){
		FMUSIC_SetMasterVolume( module,volume*255.0f );
	}else{
		FSOUND_SetVolume( stream_channel,volume*255.0f );
	}
}

void gxMusic::setPaused( bool paused){
	if( module ){
		FMUSIC_SetPaused( module,paused );
	}else{
		FSOUND_SetPaused( stream_channel,paused );
	}
}

bool gxMusic::isPlaying()const{
	if( module ){
		return FMUSIC_IsPlaying( module ) ? true : false;
	}else{
		return FSOUND_IsPlaying( stream_channel ) ? true : false;

	}
	return false;
}

