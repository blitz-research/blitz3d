
#ifndef EX_H
#define EX_H

struct Ex{
	string ex;		//what happened
	int pos;		//source offset
	string file;
	Ex( const string &ex ):ex(ex),pos(-1){}
	Ex( const string &ex,int pos,const string &t ):ex(ex),pos(pos),file(t){}
};

#endif