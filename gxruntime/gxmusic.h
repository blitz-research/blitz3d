
#ifndef GXMUSIC_H
#define GXMUSIC_H

class gxAudio;

struct FMUSIC_MODULE;
struct FSOUND_STREAM;

class gxMusic{
public:
	gxAudio *audio;

	gxMusic( gxAudio *audio,FMUSIC_MODULE *module,FSOUND_STREAM *stream );
	~gxMusic();

private:
	FMUSIC_MODULE *module;
	FSOUND_STREAM *stream;
	int stream_channel;

	/***** GX INTERFACE *****/
public:
	//modifiers
	void play();
	void stop();
	void setPaused( bool paused );
	void setVolume( float volume );

	//accessors
	bool isPlaying()const;
};

#endif