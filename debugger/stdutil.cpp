
#include "stdafx.h"

int atoi( const string &s ){
	return atoi( s.c_str() );
}

double atof( const string &s ){
	return atof( s.c_str() );
}

string itoa( int n ){
	char buff[32];itoa( n,buff,10 );
	return string( buff );
}

static int _finite( double n ){		// definition: exponent anything but 2047.

	int e;					// 11 bit exponent
	const int eMax = 2047;	// 0x7ff, all bits = 1	
	
	int *pn = (int *) &n;

	e = *++pn;				// Intel order!
	e = ( e >> 20 ) & eMax;

	return e != eMax;
}

static int _isnan( double n ){		// definition: exponent 2047, nonzero fraction.

	int e;					// 11 bit exponent
	const int eMax = 2047;	// 0x7ff, all bits = 1	
	
	int *pn = (int *) &n;

	e = *++pn;				// Intel order!
	e = ( e >> 20 ) & eMax;

	if ( e != 2047 ) return 0;	// almost always return here

	int fHi, fLo;				// 52 bit fraction

	fHi = ( *pn ) & 0xfffff;	// first 20 bits
	fLo = *--pn;				// last 32 bits

	return  ( fHi | fLo ) != 0;	// returns 0,1 not just 0,nonzero
}

/////////////
//By FLOYD!//
/////////////
string ftoa( float n ){

	static const int digits=6;

	int eNeg = -4, ePos = 8;	// limits for e notation.

	char buffer[50]; // from MSDN example, 25 would probably suffice
	string t;
	int dec, sign;

	if ( _finite( n ) ){

//		if ( digits < 1 ) digits = 1;	// less than one digit is nonsense
//		if ( digits > 8 ) digits = 8;	// practical maximum for float
		
		t = _ecvt( n, digits, &dec, &sign );

		if ( dec <= eNeg + 1 || dec > ePos ){

			_gcvt( n, digits, buffer );
			t = buffer;
			return t;
		}
		
		// Here is the tricky case. We want a nicely formatted
		// number with no e-notation or multiple trailing zeroes.
	
		if ( dec <= 0 ){

			t = "0." + string( -dec, '0' ) + t;
			dec = 1;	// new location for decimal point

		}
		else if( dec < digits ){

			t = t.substr( 0, dec ) + "." + t.substr( dec );

		}
		else{

			t = t + string( dec - digits, '0' ) + ".0";
			dec += dec - digits;

		}
	
		// Finally, trim off excess zeroes.

		int dp1 = dec + 1, p = t.length();	
		while( --p > dp1 && t[p] == '0' );
		t = string( t, 0, ++p );

		return sign ? "-" + t : t;

	}	// end of finite case

	if ( _isnan( n ) )	return "NaN";
	if ( n > 0.0 )		return "Infinity";
	if ( n < 0.0 )		return "-Infinity";

	abort();
}

/*
string ftoa( float n ){

	static const float min=.000001f,max=9999999.0f;

	int i=*(int*)&n;
	int e=(i>>23)&0xff;
	int f=i&0x007fffff;

	if( e==0xff && f ) return "NAN";

	string t;
	int s=(i>>31)&0x01;

	if( e==0xff ){
		t="INFINITY";
	}else if( !e && !f ){
		t="0.000000";
	}else if( n>=min && n<=max ){
		int dec,sgn;
		t=_fcvt( fabs(n),6,&dec,&sgn );
		if( dec<=0 ){
			t="0."+string( -dec,'0' )+t;
		}else if( dec<t.size() ){
			t=t.substr( 0,dec )+"."+t.substr( dec );
		}else{
			t=t+string( '0',dec-t.size() )+".000000";
		}
	}else{
		char buff[32];
		_gcvt( fabs(n),7,buff );
		t=buff;
	}
	return s ? "-"+t : t;
}
*/

string tolower( const string &s ){
	string t=s;
	for( int k=0;k<t.size();++k ) t[k]=tolower(t[k]);
	return t;
}

string toupper( const string &s ){
	string t=s;
	for( int k=0;k<t.size();++k ) t[k]=toupper(t[k]);
	return t;
}

string fullfilename( const string &t ){
	char buff[MAX_PATH+1],*p;
	GetFullPathName( t.c_str(),MAX_PATH,buff,&p );
	return string(buff);
}

string filenamepath( const string &t ){
	char buff[MAX_PATH+1],*p;
	GetFullPathName( t.c_str(),MAX_PATH,buff,&p );
	if( !p ) return "";
	*p=0;return string(buff);
}

string filenamefile( const string &t ){
	char buff[MAX_PATH+1],*p;
	GetFullPathName( t.c_str(),MAX_PATH,buff,&p );
	if( !p ) return "";
	return string( p );
}
