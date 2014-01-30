
#ifndef CACHEDTEXTURE_H
#define CACHEDTEXTURE_H

#include "../gxruntime/gxcanvas.h"

class CachedTexture{
public:
	CachedTexture( int w,int h,int flags,int cnt );
	CachedTexture( const string &f,int flags,int w,int h,int first,int cnt );
	CachedTexture( const CachedTexture &t );
	~CachedTexture();

	CachedTexture &operator=( const CachedTexture &t );

	string getName()const;

	const vector<gxCanvas*> &getFrames()const;

	bool operator<( const CachedTexture &t )const{ return rep<t.rep; }

	static void setPath( const string &t );

private:
	struct Rep;
	Rep *rep;

	Rep *findRep( const string &f,int flags,int w,int h,int first,int cnt );

	static set<Rep*> rep_set;
};

#endif
