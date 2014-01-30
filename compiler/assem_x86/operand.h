
#ifndef OPERAND_H
#define OPERAND_H

#include <string>
#include <iostream>

using namespace std;

struct Operand{

	int mode;
	int reg,imm,offset;
	string immLabel,baseLabel;
	int baseReg,indexReg,shift;

	Operand();
	Operand( const string &s );

	void parse();

private:
	string s;
	bool parseSize( int *sz );
	bool parseChar( char c );
	bool parseReg( int *reg );
	bool parseFPReg( int *reg );
	bool parseLabel( string *t );
	bool parseConst( int *iconst );
};

#endif
