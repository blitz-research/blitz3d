
#include "../std.h"
#include "codegen_x86.h"
#include "tile.h"

//reduce to 3 for stress test
static const int NUM_REGS=6;

static const string regs[]=
{"???","eax","ecx","edx","edi","esi","ebx"};

//array of 'used' flags
static bool regUsed[NUM_REGS+1];

//size of locals in function
static int frameSize,maxFrameSize;

//code fragments
static vector<string> codeFrags,dataFrags;

//name of function
static string funcLabel;

static void resetRegs(){
	for( int n=1;n<=NUM_REGS;++n ) regUsed[n]=false;
}

static int allocReg( int n ){
	if( !n || regUsed[n] ){
		for( n=NUM_REGS;n>=1 && regUsed[n];--n ){}
		if( !n ) return 0;
	}
	regUsed[n]=true;
	return n;
}

static void freeReg( int n ){
	regUsed[n]=false;
}

static void pushReg( int n ){
	frameSize+=4;
	if( frameSize>maxFrameSize ) maxFrameSize=frameSize;
	char buff[32];itoa( frameSize,buff,10 );
	string s="\tmov\t[ebp-";s+=buff;s+="],";s+=regs[n];s+='\n';
	codeFrags.push_back( s );
}

static void popReg( int n ){
	char buff[32];itoa( frameSize,buff,10 );
	string s="\tmov\t";s+=regs[n];s+=",[ebp-";s+=buff;s+="]\n";
	codeFrags.push_back( s );
	frameSize-=4;
}

static void moveReg( int d,int s ){
	string t="\tmov\t"+regs[d]+','+regs[s]+'\n';
	codeFrags.push_back( t );
}

static void swapRegs( int d,int s ){
	string t="\txchg\t"+regs[d]+','+regs[s]+'\n';
	codeFrags.push_back( t );
}

Tile::Tile( const string &a,Tile *l,Tile *r )
:assem(a),l(l),r(r),want_l(0),want_r(0),hits(0),need(0),argFrame(0){
}

Tile::Tile( const string &a,const string &a2,Tile *l,Tile *r )
:assem(a),assem2(a2),l(l),r(r),want_l(0),want_r(0),hits(0),need(0),argFrame(0){
}

Tile::~Tile(){
	delete l;delete r;
}

void Tile::label(){
	if( !l ){
		need=1;
	}else if( !r ){
		l->label();
		need=l->need;
	}else{
		l->label();r->label();
		if( l->need==r->need ) need=l->need+1;
		else if( l->need>r->need ) need=l->need;
		else need=r->need;
	}
}

int Tile::eval( int want ){
	//save any hit registers
	int spill=hits;
	if( want_l ) spill|=1<<want_l;
	if( want_r ) spill|=1<<want_r;
	if( spill ){
		for( int n=1;n<=NUM_REGS;++n ){
			if( spill&(1<<n) ){
				if( regUsed[n] ) pushReg( n );
				else spill&=~(1<<n);
			}
		}
	}

	//if tile needs an argFrame...
	if( argFrame ){
		codeFrags.push_back( "-"+itoa(argFrame) );
	}

	int got_l=0,got_r=0;
	if( want_l ) want=want_l;

	string *as=&assem;

	if( !l ){
		got_l=allocReg( want );
	}else if( !r ){
		got_l=l->eval( want );
	}else{
		if( l->need>=NUM_REGS && r->need>=NUM_REGS ){
			got_r=r->eval( 0 );
			pushReg( got_r );freeReg( got_r );
			got_l=l->eval( want );
			got_r=allocReg( want_r );popReg( got_r );
		}else if( r->need>l->need ){
			got_r=r->eval( want_r );
			got_l=l->eval( want );
		}else{
			got_l=l->eval( want );
			got_r=r->eval( want_r );
			if( assem2.size() ) as=&assem2;
		}
		if( want_l==got_r || want_r==got_l ){
			swapRegs( got_l,got_r );
			int t=got_l;got_l=got_r;got_r=t;
		}
	}

	if( !want_l ) want_l=got_l;
	else if( want_l!=got_l ) moveReg( want_l,got_l );

	if( !want_r ) want_r=got_r;
	else if( want_r!=got_r ) moveReg( want_r,got_r );

	int i;
	while( (i=as->find( "%l" ))!=string::npos ) as->replace( i,2,regs[want_l] );
	while( (i=as->find( "%r" ))!=string::npos ) as->replace( i,2,regs[want_r] );

	codeFrags.push_back( *as );

	freeReg( got_r );
	if( want_l!=got_l ) moveReg( got_l,want_l );

	//cleanup argFrame
	if( argFrame ){
		//***** Not needed for STDCALL *****
//		codeFrags.push_back( "+"+itoa(argFrame) );
	}

	//restore spilled regs
	if( spill ){
		for( int n=NUM_REGS;n>=1;--n ){
			if( spill&(1<<n) ) popReg( n );
		}
	}
	return got_l;
}

void Codegen_x86::flush(){
	vector<string>::iterator it;
	for( it=dataFrags.begin();it!=dataFrags.end();++it ) out<<*it;
	dataFrags.clear();
}

void Codegen_x86::enter( const string &l,int frameSize ){

	inCode=true;
	::frameSize=maxFrameSize=frameSize;
	codeFrags.clear();funcLabel=l;
}

void Codegen_x86::code( TNode *stmt ){
	resetRegs();
	Tile *q=munch( stmt );
	q->label();
	q->eval( 0 );
	delete q;
	delete stmt;
}

static string fixEsp( int esp_off ){
	if( esp_off<0 ) return "\tsub\tesp,"+itoa(-esp_off)+"\n";
	return "\tadd\tesp,"+itoa(esp_off)+"\n";
}

void Codegen_x86::leave( TNode *cleanup,int pop_sz ){
	if( cleanup ){
		resetRegs();
		allocReg( EAX );
		Tile *q=munch( cleanup );
		q->label();
		q->eval( 0 );
		delete q;
	}

	out<<"\t.align\t16\n";

	if( funcLabel.size() ) out<<funcLabel<<'\n';

	out<<"\tpush\tebx\n";
	out<<"\tpush\tesi\n";
	out<<"\tpush\tedi\n";
	out<<"\tpush\tebp\n";
	out<<"\tmov\tebp,esp\n";
	if( maxFrameSize ) out<<"\tsub\tesp,"<<maxFrameSize<<'\n';

	int esp_off=0;
	vector<string>::iterator it=codeFrags.begin();
	for( it=codeFrags.begin();it!=codeFrags.end();++it ){
		const string &t=*it;
		if( t[0]=='+' ){
			esp_off+=atoi(t.substr(1));
		}else if( t[0]=='-' ){
			//***** Still needed for STDCALL *****
			esp_off-=atoi(t.substr(1));
		}else{
			if( esp_off ){
				out<<fixEsp( esp_off );
				esp_off=0;
			}
			out<<*it;
		}
	}
	if( esp_off ) out<<fixEsp( esp_off );

	out<<"\tmov\tesp,ebp\n";
	out<<"\tpop\tebp\n";
	out<<"\tpop\tedi\n";
	out<<"\tpop\tesi\n";
	out<<"\tpop\tebx\n";
	out<<"\tret\tword "<<pop_sz<<"\n";

	delete cleanup;
	inCode=false;
}

void Codegen_x86::label( const string &l ){
	string t=l+'\n';
	if( inCode ) codeFrags.push_back( t );
	else dataFrags.push_back( t );
}

void Codegen_x86::align_data( int n ){
	char buff[32];itoa( n,buff,10 );
	dataFrags.push_back( string( "\t.align\t" )+buff+'\n' );
}

void Codegen_x86::i_data( int i,const string &l ){
	if( l.size() ) dataFrags.push_back( l );
	char buff[32];itoa( i,buff,10 );
	dataFrags.push_back( string( "\t.dd\t" )+buff+'\n' );
}

void Codegen_x86::s_data( const string &s,const string &l ){
	if( l.size() ) dataFrags.push_back( l );
	dataFrags.push_back( string( "\t.db\t\"" )+s+"\",0\n" );
}

void Codegen_x86::p_data( const string &p,const string &l ){
	if( l.size() ) dataFrags.push_back( l );
	dataFrags.push_back( string( "\t.dd\t" )+p+'\n' );
}
