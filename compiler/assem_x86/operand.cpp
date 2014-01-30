
#include "../std.h"
#include "../ex.h"
#include "operand.h"
#include "insts.h"

static const char *regs[]={
	"al","cl","dl","bl","ah","ch","dh","bh",
	"ax","cx","dx","bx","sp","bp","si","di",
	"eax","ecx","edx","ebx","esp","ebp","esi","edi"
};

static void opError(){
	throw Ex( "error in operand" );
}

static void sizeError(){
	throw Ex( "illegal operand size" );
}

Operand::Operand()
:mode(NONE),reg(-1),imm(0),offset(0),baseReg(-1),indexReg(-1),shift(0){
}

Operand::Operand( const string &s )
:mode(NONE),reg(-1),imm(0),offset(0),baseReg(-1),indexReg(-1),shift(0),s(s){
}

bool Operand::parseSize( int *sz ){

	if( !s.size() ) return false;
	if( s.find( "byte " )==0 ){
		*sz=1;s=s.substr( 5 );
	}else if( s.find( "word " )==0 ){
		*sz=2;s=s.substr( 5 );
	}else if( s.find( "dword " )==0 ){
		*sz=4;s=s.substr( 6 );
	}else return false;

	return true;
}

bool Operand::parseChar( char c ){
	if( !s.size() || s[0]!=c ) return false;
	s=s.substr( 1 );return true;
}

bool Operand::parseReg( int *reg ){
	int i;
	for( i=0;i<s.size() && isalpha( s[i] );++i ){}
	if( !i ) return false;
	string t=s.substr(0,i);
	for( int j=0;j<24;++j ){
		if( t==regs[j] ){ *reg=j;s=s.substr( i );return true; }
	}
	return false;
}

bool Operand::parseFPReg( int *reg ){

	//eg: st(0)
	if( s.size()<5 ) return false;
	if( s[0]!='s' || s[1]!='t' || s[2]!='(' || s[4]!=')' ) return false;
	if( s[3]<'0' || s[3]>'7' ) return false;
	*reg=s[3]-'0';s=s.substr( 5 );return true;
}

bool Operand::parseLabel( string *label ){
	if( !s.size() || (!isalpha( s[0] ) && s[0]!='_') ) return false;
	int i;
	for( i=1;i<s.size() && (isalnum( s[i] ) || s[i]=='_');++i ){}
	*label=s.substr(0,i);s=s.substr( i );return true;
}

bool Operand::parseConst( int *iconst ){
	int i,sgn=s.size() && (s[0]=='-'||s[0]=='+');
	for( i=sgn;i<s.size() && isdigit( s[i] );++i ){}
	if( i==sgn ) return false;
	int n=atoi( s.c_str() );
	*iconst=n;s=s.substr( i );return true;
}

void Operand::parse(){

	if( !s.size() ) return;

	int sz;if( !parseSize( &sz  ) ) sz=0;

	if( s[0]!='[' ){
		int r;
		if( parseReg( &r ) ){
			mode=REG|R_M;
			if( r<8 ){
				if( sz && sz!=1 ) sizeError();
				mode|=REG8|R_M8;
				if( r==0 ) mode|=AL;else if( r==1 ) mode|=CL;
			}else if( r<16 ){
				if( sz && sz!=2 ) sizeError();
				mode|=REG16|R_M16;
				if( r==8 ) mode|=AX;else if( r==9 ) mode|=CX;
			}else{
				if( sz && sz!=4 ) sizeError();
				mode|=REG32|R_M32;
				if( r==16 ) mode|=EAX;else if( r==17 ) mode|=ECX;
			}
			reg=r&7;
		}else if( parseFPReg( &r ) ){
			mode=FPUREG;
			if( !r ) mode|=ST0;
			reg=r;
		}else if( parseLabel( &immLabel ) ){
			if( sz && sz!=4 ) sizeError();
			mode=IMM|IMM32;
		}else if( parseConst( &imm ) ){
			mode=IMM;
			if( sz==1 ) mode|=IMM8;
			else if( sz==2 ) mode|=IMM16;
			else mode|=IMM32;
		}else opError();
		if( s.size() ) opError();
		return;
	}

	if( s[s.size()-1]!=']' ) opError();
	s=s.substr( 1,s.size()-2 );

	mode=MEM|R_M;
	if( sz==1 ) mode|=MEM8|R_M8;
	else if( sz==2 ) mode|=MEM16|R_M16;
	else mode|=MEM32|R_M32;

	for(;;){
		int n;string l;
		if( parseReg( &n ) ){
			if( n<16 ) throw Ex( "register must be 32 bit" );
			n&=7;
			if( parseChar( '*' ) ){
				if( n==4 ) break;		//esp cannot be index reg!
				if( indexReg>=0 ) break;
				if( parseChar( '1' ) ) shift=0;
				else if( parseChar( '2' ) ) shift=1;
				else if( parseChar( '4' ) ) shift=2;
				else if( parseChar( '8' ) ) shift=3;
				else break;
				indexReg=n;
			}else{
				if( baseReg<0 ) baseReg=n;
				else if( indexReg<0 ){ indexReg=n; }
				else break;
			}
		}else if( parseLabel( &l ) ){
			if( baseLabel.size() ) opError();
			baseLabel=l;
		}else if( parseConst( &n ) ){
			offset+=n;
		}else break;
		if( !s.size() ) return;
	}
	opError();
}
