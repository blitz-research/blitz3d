
#ifndef GXDEVICE_H
#define GXDEVICE_H

class gxDevice{
public:
	float axis_states[32];

	gxDevice();
	virtual ~gxDevice();

	virtual void update(){}

	void reset();
	void downEvent( int key );
	void upEvent( int key );
	void setDownState( int key,bool down );

private:
	enum{
		QUE_SIZE=32,QUE_MASK=QUE_SIZE-1
	};
	int hit_count[256];			//how many hits of key
	bool down_state[256];			//time key went down
	int que[QUE_SIZE],put,get;

	/***** GX INTERFACE *****/
public:
	void flush();

	bool keyDown( int key );

	int keyHit( int key );

	int getKey();

	float getAxisState( int axis );
};

#endif