
#ifndef GXCHANNEL_H
#define GXCHANNEL_H

class gxChannel{

	/***** GX INTERFACE *****/
public:
	virtual ~gxChannel();
	//modifiers
	virtual void stop()=0;
	virtual void setPaused( bool paused )=0;
	virtual void setPitch( int pitch )=0;
	virtual void setVolume( float volume )=0;
	virtual void setPan( float pan )=0;
	virtual void set3d( const float pos[3],const float vel[3] )=0;

	virtual bool isPlaying()=0;
};

#endif