
/*

  This generates a source code file for x86 instruction formats.

  Thanks NASM!

*/

#include <conio.h>
#include <string>
#include <fstream>
#include <iostream>

using namespace std;

static const char *optype( const string &s ){

	if( s=="AL" ) return "AL";
	if( s=="AX" ) return "AX";
	if( s=="EAX" ) return "EAX";
	if( s=="CL" ) return "CL";
	if( s=="CX" ) return "CX";
	if( s=="ECX" ) return "ECX";
	if( s=="imm" ) return "IMM";
	if( s=="imm8" ) return "IMM8";
	if( s=="imm16" ) return "IMM16";
	if( s=="imm32" ) return "IMM32";
	if( s=="reg" ) return "REG";
	if( s=="reg8" ) return "REG8";
	if( s=="reg16" ) return "REG16";
	if( s=="reg32" ) return "REG32";
	if( s=="mem" ) return "MEM";
	if( s=="mem8" ) return "MEM8";
	if( s=="mem16" ) return "MEM16";
	if( s=="mem32" ) return "MEM32";
	if( s=="r/m" ) return "R_M";
	if( s=="r/m8" ) return "R_M8";
	if( s=="r/m16" ) return "R_M16";
	if( s=="r/m32" ) return "R_M32";
	if( s=="fpureg" ) return "FPUREG";
	if( s=="ST0" ) return "ST0";
	return 0;
}

void main(){

	string name,lhs,rhs,byte,bytes,flags,last;

	ifstream in( "nasm_insts.txt" );
	ofstream out( "..\\compiler\\assem_x86\\asm_insts.cpp" );

	out<<"//\n//This is generated code - do not modify!!!!!\n//\n";
	out<<"\n#include \"..\\std.h\"\n\n";
	out<<"\n#include \"insts.h\"\n\n";
	out<<"Inst insts[]={\n";

	for( ;!in.eof();in.ignore( INT_MAX,'\n' ) ){

		name.resize(0);

		in>>name;
		if( !name.size() || name[0]=='/' ) continue;

		lhs.resize(0);rhs.resize(0);
		const char *lop="NONE",*rop="NONE";

		while( isspace( in.peek() ) ) in.get();
		getline( in,lhs,';' );

		if( int i=lhs.size() ){
			while( i && isspace( lhs[i-1] ) ) --i;
			if( i ){
				lhs.resize( i );
				i=lhs.find( ',' );
				if( i!=string::npos ){
					rhs=lhs.substr( i+1 );
					lhs=lhs.substr( 0,i );
				}
				lop=optype( lhs );if( !lop ) continue;
				if( rhs.size() && !(rop=optype( rhs )) ) continue;
			}
		}

		bytes.resize(0);

		flags='0';
		bool fail=false;
		while( !fail ){
			in>>byte;
			if( byte[0]=='[' ) break;
			     if( byte=="/r" ) flags+="|_R";
			else if( byte=="/0" ) flags+="|_0";
			else if( byte=="/1" ) flags+="|_1";
			else if( byte=="/2" ) flags+="|_2";
			else if( byte=="/3" ) flags+="|_3";
			else if( byte=="/4" ) flags+="|_4";
			else if( byte=="/5" ) flags+="|_5";
			else if( byte=="/6" ) flags+="|_6";
			else if( byte=="/7" ) flags+="|_7";
			else if( byte=="o16" ) flags+="|O16";
			else if( byte=="o32" ) flags+="|O32";
			else if( byte=="ow/od" ) flags+="|OW_OD";
			else if( byte=="ib" ) flags+="|IB";
			else if( byte=="iw" ) flags+="|IW";
			else if( byte=="id" ) flags+="|ID";
			else if( byte=="rw/rd" ) flags+="|RW_RD";
			else if( isxdigit( byte[0] ) ){
				string sub=byte.substr( 2 );
				if( sub.size() ){
					if( sub=="+r" ) flags+="|PLUSREG";
					else if( sub=="+cc" ) flags+="|PLUSCC";
					else fail=true;
				}
				bytes=bytes+"\\x"+byte[0]+byte[1];
			}else fail=true;
		}
		if( fail ) continue;

		if( flags.find( "0|" )==0 ) flags=flags.substr( 2 );
		for( int k=0;k<name.size();++k ) name[k]=tolower( name[k] );

		if( name==last ) name="0";
		else{
			last=name;
			name='\"'+name+'\"';
		}

		char bf[4];itoa( bytes.size()/3,bf,16 );
		bytes="\\x"+string( bf )+bytes;

		out<<name<<','<<lop<<','<<rop<<','<<flags<<",\""<<bytes<<"\",\n";
	}
	out<<"\"\",0,0,0,0\n};\n";
	out.flush();
	out.close();
	cout<<"All done!\n";
	getch();
}
