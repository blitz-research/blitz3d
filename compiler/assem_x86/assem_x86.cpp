
/*

  BlitzPC assembler.

  This REALLY needs some work - very slow.

*/

#include "../std.h"
#include "../ex.h"
#include "assem_x86.h"

#include <iomanip>

typedef map<string,Inst*> InstMap;
typedef InstMap::value_type InstPair;
typedef InstMap::const_iterator InstIter;

static InstMap instMap;

//#define LOG

Assem_x86::Assem_x86( istream &in,Module *mod ):Assem(in,mod){

	//build instruction map, if not built already.
	if( !instMap.size() ){
		for( int k=0;!insts[k].name || insts[k].name[0];++k ){
			if( insts[k].name ) instMap.insert( InstPair( insts[k].name,&insts[k] ) );
		}
	}
}

static int findCC( const string &s ){
	if( s=="o" ) return 0;
	if( s=="no" ) return 1;
	if( s=="b"||s=="c"||s=="nae" ) return 2;
	if( s=="ae"||s=="nb"||s=="nc" ) return 3;
	if( s=="e"||s=="z" ) return 4;
	if( s=="ne"||s=="nz" ) return 5;
	if( s=="be"||s=="na" ) return 6;
	if( s=="a"||s=="nbe" ) return 7;
	if( s=="s" ) return 8;
	if( s=="ns" ) return 9;
	if( s=="p"||s=="pe" ) return 10;
	if( s=="ne"||s=="po" ) return 11;
	if( s=="l"||s=="nge" ) return 12;
	if( s=="ge"||s=="nl" ) return 13;
	if( s=="le"||s=="ng" ) return 14;
	if( s=="g"||s=="nle" ) return 15;
	return -1;
}

void Assem_x86::align( int n ){
	int pc=mod->getPC();
	int sz=(pc+(n-1))/n*n-pc;
	while( sz-- ) mod->emit( 0x90 );
}

void Assem_x86::emit( int n ){
#ifdef LOG
	clog<<hex<<(int(n)&0xff)<<dec<<' ';
#endif
	mod->emit( n );
}

void Assem_x86::emitw( int n ){
	emit( n );
	emit( (n>>8) );
}

void Assem_x86::emitd( int n ){
	emitw( n );
	emitw( n>>16 );
}

void Assem_x86::emitImm( const Operand &o,int size ){

	if( size<4 && o.immLabel.size() ) throw Ex( "immediate value cannot by a label" );

	switch( size ){
	case 1:emit( o.imm );return;
	case 2:emitw( o.imm );return;
	case 4:a_reloc( o.immLabel );emitd( o.imm );return;
	}
}

void Assem_x86::emitImm( const string &s,int size ){

	Operand op(s);op.parse();
	if( !(op.mode&IMM) ) throw Ex( "operand must be immediate" );
	emitImm( op,size );
}

void Assem_x86::r_reloc( const string &s ){
	if( !s.size() ) return;
	mod->addReloc( s.c_str(),mod->getPC(),true );
}

void Assem_x86::a_reloc( const string &s ){
	if( !s.size() ) return;
	mod->addReloc( s.c_str(),mod->getPC(),false );
}

void Assem_x86::assemInst( const string &name,const string &lhs,const string &rhs ){

	//parse operands
	Operand lop( lhs ),rop( rhs );
	lop.parse();rop.parse();

	//find instruction
	int cc=-1;
	Inst *inst=0;

	//kludge for condition code instructions...
	if( name[0]=='j' ){
		if( (cc=findCC(name.substr(1)))>=0 ){
			static Inst jCC={ "jCC",IMM,NONE,RW_RD|PLUSCC,"\x2\x0F\x80" };
			inst=&jCC;
		}
	}else if( name[0]=='s' && name.substr( 0,3 )=="set" ){
		if( (cc=findCC(name.substr(3)))>=0 ){
			static Inst setCC={ "setne",R_M8,NONE,_2|PLUSCC,"\x2\x0F\x90" };
			inst=&setCC;
		}
	}

	if( inst ){
		if( !(lop.mode&inst->lmode) || !(rop.mode&inst->rmode) ) throw Ex( "illegal addressing mode" );
	}else{
		InstIter it=instMap.find( name );
		if( it==instMap.end() ) throw Ex( "unrecognized instruction" );
		inst=it->second;
		for(;;){
			if( (lop.mode&inst->lmode) && (rop.mode&inst->rmode) ) break;
			if( (++inst)->name ) throw Ex( "illegal addressing mode" );
		}
	}

	//16/32 bit modifier - NOP for now
	if( inst->flags & (O16|O32) ){}

	int k,n=inst->bytes[0];
	for( k=1;k<n;++k ) emit( inst->bytes[k] );
	if( inst->flags&PLUSREG ) emit( inst->bytes[k]+lop.reg );
	else if( inst->flags&PLUSCC ) emit( inst->bytes[k]+cc );
	else emit( inst->bytes[k] );

	if( inst->flags&(_0|_1|_2|_3|_4|_5|_6|_7|_R ) ){

		//find the memop;
		const Operand &mop=
		(inst->rmode&(MEM|MEM8|MEM16|MEM32|R_M|R_M8|R_M16|R_M32))?rop:lop;

		//find the spare field value.
		int rm=0;
		switch( inst->flags&(_0|_1|_2|_3|_4|_5|_6|_7|_R ) ){
		case _0:rm=0;break;case _1:rm=1;break;case _2:rm=2;break;case _3:rm=3;break;
		case _4:rm=4;break;case _5:rm=5;break;case _6:rm=6;break;case _7:rm=7;break;
		case _R:rm=(inst->rmode&(REG8|REG16|REG32))?rop.reg:lop.reg;break;
		}
		rm<<=3;
		if( mop.mode & REG ){			//reg
			emit( 0xc0|rm|mop.reg );
		}else if( mop.baseReg>=0 ){		//base, index?
			int mod=mop.offset ? 0x40 : 0x00;
			if( mop.baseLabel.size() || mop.offset<-128 || mop.offset>127 ) mod=0x80;
			if( mop.baseReg==5 && !mod ) mod=0x40;
			if( mop.indexReg>=0 ){		//base, index!
				emit( mod|rm|4 );
				emit( (mop.shift<<6)|(mop.indexReg<<3)|mop.baseReg );
			}else{						//base, no index!
				if( mop.baseReg!=4 ) emit( mod|rm|mop.baseReg);
				else{
					emit( mod|rm|4 );emit( (4<<3)|mop.baseReg );
				}
			}
			if( (mod&0xc0)==0x40 ) emit( mop.offset );
			else if( (mod&0xc0)==0x80 ){
				//reloc
				a_reloc( mop.baseLabel );emitd( mop.offset );
			}
		}else if( mop.indexReg>=0 ){	//index, no base!
			emit( rm|4 );
			emit( (mop.shift<<6)|(mop.indexReg<<3)|5 );
			//reloc
			a_reloc( mop.baseLabel );emitd( mop.offset );
		}else{							//[disp]
			emit( rm|5 );
			//reloc
			a_reloc( mop.baseLabel );emitd( mop.offset );
		}
	}

	if( inst->flags&RW_RD ){
		r_reloc( lop.immLabel );emitd( lop.imm-4 );
	}

	if( inst->flags&IB ){
		if( lop.mode&IMM ) emitImm( lop,1 );else emitImm( rop,1 );
	}else if( inst->flags&IW ){
		if( lop.mode&IMM ) emitImm( lop,2 );else emitImm( rop,2 );
	}else if( inst->flags&ID ){
		if( lop.mode&IMM ) emitImm( lop,4 );else emitImm( rop,4 );
	}
}

void Assem_x86::assemDir( const string &name,const string &op ){

	if( !op.size() ) throw Ex( "operand error" );

	if( name==".db" ){
		if( op[0]!='\"' ) emitImm( op,1 );
		else{
			if( op.size()<2 || op[op.size()-1]!='\"' ) throw Ex( "operand error" );
			for( int k=1;k<op.size()-1;++k ) emit( op[k] );
		}
	}else if( name==".dw" ){
		emitImm( op,2 );
	}else if( name==".dd" ){
		emitImm( op,4 );
	}else if( name==".align" ){
		Operand o( op );o.parse();
		if( !(o.mode&IMM) ) throw Ex( "operand must be immediate" );
		align( o.imm );
	}else{
		throw Ex( "unrecognized assembler directive" );
	}
}

void Assem_x86::assemLine( const string &line ){

	int i=0;
	string name;
	vector<string> ops;

	//label?
	if( !isspace( line[i] ) ){
		while( !isspace( line[i] ) ) ++i;
		string lab=line.substr( 0,i );
		if( !mod->addSymbol( lab.c_str(),mod->getPC() ) ) throw Ex( "duplicate label" );
	}

	//skip space
	while( isspace( line[i] ) && line[i]!='\n' ) ++i;
	if( line[i]=='\n' || line[i]==';' ) return;

	//fetch instruction name
	int from=i;for( ++i;!isspace( line[i] );++i ){}
	name=line.substr( from,i-from );

	for(;;){

		//skip space
		while( isspace( line[i] ) && line[i]!='\n' ) ++i;
		if( line[i]=='\n' || line[i]==';' ) break;

		int from=i;
		if( line[i]=='\"' ){
			for( ++i;line[i]!='\"' && line[i]!='\n';++i ){}
			if( line[i++]!='\"' ) throw Ex( "missing close quote" );
		}else{
			for( ++i;line[i]!=',' && line[i]!=';' && line[i]!='\n';++i ){}
		}

		//back-up over space
		while( i && isspace( line[i-1] ) ) --i;
		ops.push_back( line.substr( from,i-from ) );

		//skip space
		while( isspace( line[i] ) && line[i]!='\n' ) ++i;
		if( line[i]=='\n' || line[i]==';' ) break;

		if( line[i++]!=',' ) throw Ex( "expecting ','" );
	}

	//pseudo op?
	if( name[0]=='.' ){
		for( int k=0;k<ops.size();++k ) assemDir( name,ops[k] );
		return;
	}

	//normal instruction!
	if( ops.size()>2 ) throw Ex( "Too many operands" );
	ops.push_back( "" );ops.push_back( "" );
	assemInst( name,ops[0],ops[1] );
}

void Assem_x86::assemble(){

	string line;

	while( !in.eof() ){
		try{
			getline( in,line );
			line+='\n';
#ifdef LOG
			clog<<line;
#endif
			assemLine( line );
#ifdef LOG
			clog<<endl;
#endif
		}catch( Ex &x ){
			throw Ex( line+x.ex );
		}
	}
}
