
#include "std.h"
#include "bbmath.h"

static int rnd_state;
static const int RND_A=48271;
static const int RND_M=2147483647;
static const int RND_Q=44488;
static const int RND_R=3399;

static const float dtor=0.0174532925199432957692369076848861f;
static const float rtod=57.2957795130823208767981548141052f;

float bbSin( float n ){ return (float)sin(n*dtor); }
float bbCos( float n ){ return (float)cos(n*dtor); }
float bbTan( float n ){ return (float)tan(n*dtor); }
float bbASin( float n ){ return (float)asin(n)*rtod; }
float bbACos( float n ){ return (float)acos(n)*rtod; }
float bbATan( float n ){ return (float)atan(n)*rtod; }
float bbATan2( float n,float t ){ return (float)atan2(n,t)*rtod; }
float bbSqr( float n ){ return (float)sqrt(n); }
float bbFloor( float n ){ return (float)floor(n); }
float bbCeil( float n ){ return (float)ceil(n); }
float bbExp( float n ){ return (float)exp(n); }
float bbLog( float n ){ return (float)log(n); }
float bbLog10( float n ){ return (float)log10(n); }

//return rand float from 0...1
static inline float rnd(){
	rnd_state=RND_A*(rnd_state%RND_Q)-RND_R*(rnd_state/RND_Q);
	if( rnd_state<0 ) rnd_state+=RND_M;
	return (rnd_state&65535)/65536.0f+(.5f/65536.0f);
}

float bbRnd( float from,float to ){
	return rnd()*(to-from)+from;
}

int bbRand( int from,int to ){
	if( to<from ) std::swap( from,to );
	return int(rnd()*(to-from+1))+from;
}

void bbSeedRnd( int seed ){
	seed&=0x7fffffff;
	rnd_state=seed ? seed : 1;
}

int  bbRndSeed(){
	return rnd_state;
}

bool math_create(){
	bbSeedRnd( 0x1234 );
	return true;
}

bool math_destroy(){
	return true;
}

void math_link( void (*rtSym)( const char *sym,void *pc ) ){
	rtSym( "#Sin#degrees",bbSin );
	rtSym( "#Cos#degrees",bbCos );
	rtSym( "#Tan#degrees",bbTan );
	rtSym( "#ASin#float",bbASin );
	rtSym( "#ACos#float",bbACos );
	rtSym( "#ATan#float",bbATan );
	rtSym( "#ATan2#floata#floatb",bbATan2 );
	rtSym( "#Sqr#float",bbSqr );
	rtSym( "#Floor#float",bbFloor );
	rtSym( "#Ceil#float",bbCeil );
	rtSym( "#Exp#float",bbExp );
	rtSym( "#Log#float",bbLog );
	rtSym( "#Log10#float",bbLog10 );
	rtSym( "#Rnd#from#to=0",bbRnd );
	rtSym( "%Rand%from%to=1",bbRand );
	rtSym( "SeedRnd%seed",bbSeedRnd );
	rtSym( "%RndSeed",bbRndSeed );
}
