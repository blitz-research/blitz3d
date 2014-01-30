
#include "../std.h"
#include "codegen_x86.h"
#include "tile.h"

//#define NOOPTS

Codegen_x86::Codegen_x86( ostream &out,bool debug ):Codegen( out,debug ),inCode(false){
}

static string itoa_sgn(int n){
	return n ? (n>0 ? "+"+itoa(n) : itoa(n)) : "";
}

static bool isRelop( int op ){
	return op==IR_SETEQ||op==IR_SETNE||op==IR_SETLT||op==IR_SETGT||op==IR_SETLE||op==IR_SETGE;
}

static bool nodesEqual( TNode *t1,TNode *t2 ){
	if( t1->op!=t2->op ||
		t1->iconst!=t2->iconst ||
		t1->sconst!=t2->sconst ) return false;

	if( t1->l ){
		if( !t2->l || !nodesEqual( t1->l,t2->l ) ) return false;
	}else if( t2->l ) return false;

	if( t1->r ){
		if( !t2->r || !nodesEqual( t1->r,t2->r ) ) return false;
	}else if( t2->r ) return false;

	return true;
}

static bool getShift( int n,int &shift ){
#ifdef NOOPTS
	return false;
#endif

	for( shift=0;shift<32;++shift ){
		if( (1<<shift)==n ) return true;
	}
	return false;
}

static bool matchMEM( TNode *t,string &s ){
#ifdef NOOPTS
	return false;
#endif

	if( t->op!=IR_MEM ) return false;
	t=t->l;
	switch( t->op ){
	case IR_GLOBAL:s="["+t->sconst+"]";return true;
	case IR_LOCAL:s="[ebp"+itoa_sgn(t->iconst)+"]";return true;
	case IR_ARG:s="[esp"+itoa_sgn(t->iconst)+"]";return true;
	}
	return false;
}

static bool matchCONST( TNode *t,string &s ){
#ifdef NOOPTS
	return false;
#endif

	switch( t->op ){
	case IR_CONST:s=itoa( t->iconst );return true;
	case IR_GLOBAL:s=t->sconst;return true;
	}
	return false;
}

static bool matchMEMCONST( TNode *t,string &s ){
#ifdef NOOPTS
	return false;
#endif

	return matchMEM( t,s ) || matchCONST( t,s );
}

Tile *Codegen_x86::genCompare( TNode *t,string &func,bool negate ){

	switch( t->op ){
	case IR_SETEQ:func=negate ? "nz" : "z";break;
	case IR_SETNE:func=negate ? "z" : "nz";break;
	case IR_SETLT:func=negate ? "ge" : "l";break;
	case IR_SETGT:func=negate ? "le" : "g";break;
	case IR_SETLE:func=negate ? "g" : "le";break;
	case IR_SETGE:func=negate ? "l" : "ge";break;
	default:return 0;
	}

	string q,m,c;
	TNode *ql=0,*qr=0;

	if( matchMEM( t->l,m ) ){
		if( matchCONST( t->r,c ) ){
			q="\tcmp\t"+m+","+c+"\n";
		}else{
			q="\tcmp\t"+m+",%l\n";ql=t->r;
		}
	}else{
		if( matchMEMCONST( t->r,m ) ){
			q="\tcmp\t%l,"+m+"\n";ql=t->l;
		}else{
			q="\tcmp\t%l,%r\n";ql=t->l;qr=t->r;
		}
	}

	return d_new Tile( q,ql ? munchReg( ql ) : 0,qr ? munchReg( qr ) : 0 );
}

////////////////////////////////////////////////
// Integer expressions returned in a register //
////////////////////////////////////////////////
Tile *Codegen_x86::munchUnary( TNode *t ){
	string s;
	switch( t->op ){
	case IR_NEG:s="\tneg\t%l\n";break;
	default:return 0;
	}
	return d_new Tile( s,munchReg( t->l ) );
}

Tile *Codegen_x86::munchLogical( TNode *t ){
	string s;
	switch( t->op ){
	case IR_AND:s="\tand\t%l,%r\n";break;
	case IR_OR:s="\tor\t%l,%r\n";break;
	case IR_XOR:s="\txor\t%l,%r\n";break;
	default:return 0;
	}
	return d_new Tile( s,munchReg( t->l ),munchReg( t->r ) );
}

Tile *Codegen_x86::munchArith( TNode *t ){

	if( t->op==IR_DIV ){
		int shift;
		if( t->r->op==IR_CONST ){
			if( getShift( t->r->iconst,shift ) ){
				return d_new Tile( "\tsar\t%l,byte "+itoa(shift)+"\n",munchReg( t->l ) );
			}
		}
		Tile *q=d_new Tile( "\tcdq\n\tidiv\tecx\n",munchReg( t->l ),munchReg( t->r ) );
		q->want_l=EAX;q->want_r=ECX;q->hits=1<<EDX;
		return q;
	}

	if( t->op==IR_MUL ){
		int shift;
		if( t->r->op==IR_CONST ){
			if( getShift( t->r->iconst,shift ) ){
				return d_new Tile( "\tshl\t%l,byte "+itoa(shift)+"\n",munchReg( t->l ) );
			}
		}else if( t->l->op==IR_CONST ){
			if( getShift( t->l->iconst,shift ) ){
				return d_new Tile( "\tshl\t%l,byte "+itoa(shift)+"\n",munchReg( t->r ) );
			}
		}
	}

	string s,op;
	switch( t->op ){
	case IR_ADD:op="\tadd\t";break;
	case IR_SUB:op="\tsub\t";break;
	case IR_MUL:op="\timul\t";break;
	default:return 0;
	}

	if( matchMEMCONST( t->r,s ) ){
		return d_new Tile( op+"%l,"+s+"\n",munchReg( t->l ) );
	}
	if( t->op!=IR_SUB && matchMEMCONST( t->l,s ) ){
		return d_new Tile( op+"%l,"+s+"\n",munchReg( t->r ) );
	}
	return d_new Tile( op+"%l,%r\n",munchReg( t->l ),munchReg( t->r ) );
}

Tile *Codegen_x86::munchShift( TNode *t ){
	string s,op;
	switch( t->op ){
	case IR_SHL:op="\tshl\t";break;
	case IR_SHR:op="\tshr\t";break;
	case IR_SAR:op="\tsar\t";break;
	default:return 0;
	}

	if( matchCONST( t->r,s ) ){
		return d_new Tile( op+"%l,byte "+s+"\n",munchReg( t->l ) );
	}

	Tile *q=d_new Tile( op+"%l,cl\n",munchReg( t->l ),munchReg( t->r ) );
	q->want_r=ECX;return q;
}

Tile *Codegen_x86::munchRelop( TNode *t ){
	string func;
	Tile *q=genCompare( t,func,false );

	q=d_new Tile( "\tset"+func+"\tal\n\tmovzx\teax,al\n",q );
	q->want_l=EAX;
	return q;
}

////////////////////////////////////////////////
// Float expressions returned on the FP stack //
////////////////////////////////////////////////
Tile *Codegen_x86::munchFPUnary( TNode *t ){
	string s;
	switch( t->op ){
	case IR_FNEG:s="\tfchs\n";break;
	default:return 0;
	}
	return d_new Tile( s,munchFP( t->l ) );
}

Tile *Codegen_x86::munchFPArith( TNode *t ){
	string s,s2;
	switch( t->op ){
	case IR_FADD:s="\tfaddp\tst(1)\n";break;
	case IR_FMUL:s="\tfmulp\tst(1)\n";break;
	case IR_FSUB:s="\tfsubrp\tst(1)\n";s2="\tfsubp\tst(1)\n";break;
	case IR_FDIV:s="\tfdivrp\tst(1)\n";s2="\tfdivp\tst(1)\n";break;
	default:return 0;
	}
	return d_new Tile( s,s2,munchFP( t->l ),munchFP( t->r ) );
}

Tile *Codegen_x86::munchFPRelop( TNode *t ){
	string s,s2;
	switch( t->op ){
	case IR_FSETEQ:s="z";s2="z";break;
	case IR_FSETNE:s="nz";s2="nz";break;
	case IR_FSETLT:s="b";s2="a";break;
	case IR_FSETGT:s="a";s2="b";break;
	case IR_FSETLE:s="be";s2="ae";break;
	case IR_FSETGE:s="ae";s2="be";break;
	default:return 0;
	}
	s="\tfucompp\n\tfnstsw\tax\n\tsahf\n\tset"+s+"\tal\n\tmovzx\t%l,al\n";
	s2="\tfucompp\n\tfnstsw\tax\n\tsahf\n\tset"+s2+"\tal\n\tmovzx\t%l,al\n";
	Tile *q=d_new Tile( s,s2,munchFP( t->l ),munchFP( t->r ) );
	q->want_l=EAX;
	return q;
}

///////////////////////////
// Generic Call handling //
///////////////////////////
Tile *Codegen_x86::munchCall( TNode *t ){
	Tile *q;
	if( t->l->op==IR_GLOBAL ){
		q=d_new Tile( "\tcall\t"+t->l->sconst+"\n",t->r ? munchReg( t->r ) : 0  );
	}else{
		q=d_new Tile( "\tcall\t%l\n",munchReg( t->l ),t->r ? munchReg( t->r ) : 0  );
	}
	q->argFrame=t->iconst;
	q->want_l=EAX;
	q->hits=(1<<EAX)|(1<<ECX)|(1<<EDX);
	return q;
}

/////////////////////////////
// munch and dicard result //
/////////////////////////////
Tile *Codegen_x86::munch( TNode *t ){
	if( !t ) return 0;
	Tile *q=0;
	string s;
	switch( t->op ){
	case IR_JSR:
		q=d_new Tile( "\tcall\t"+t->sconst+'\n' );
		break;
	case IR_RET:
		q=d_new Tile( "\tret\n" );
		break;
	case IR_RETURN:
		q=munchReg( t->l );q->want_l=EAX;
		s="\tjmp\t"+t->sconst+'\n';
		q=d_new Tile( s,q );
		break;
	case IR_FRETURN:
		q=munchFP( t->l );
		s="\tjmp\t"+t->sconst+'\n';
		q=d_new Tile( s,q );
		break;
	case IR_CALL:
		q=munchCall( t );
		break;
	case IR_JUMP:
		q=d_new Tile( "\tjmp\t"+t->sconst+'\n' );
		break;
	case IR_JUMPT:
		if( TNode *p=t->l ){
			bool neg=false;
			if( isRelop( p->op ) ){
				string func;
				q=genCompare( p,func,neg );
				q=d_new Tile( "\tj"+func+"\t"+t->sconst+"\n",q );
			}
		}
		break;
	case IR_JUMPF:
		if( TNode *p=t->l ){
			bool neg=true;
			if( isRelop( p->op ) ){
				string func;
				q=genCompare( p,func,neg );
				q=d_new Tile( "\tj"+func+"\t"+t->sconst+"\n",q );
			}
		}
		break;
	case IR_MOVE:
		if( matchMEM( t->r,s ) ){
			string c;
			if( matchCONST( t->l,c ) ){
				q=d_new Tile( "\tmov\t"+s+","+c+"\n" );
			}else if( t->l->op==IR_ADD || t->l->op==IR_SUB ){
				TNode *p=0;
				if( nodesEqual( t->l->l,t->r ) ) p=t->l->r;
				else if( t->l->op==IR_ADD && nodesEqual( t->l->r,t->r ) ) p=t->l->l;
				if( p ){
					string c,op;
					switch( t->l->op ){
					case IR_ADD:op="\tadd\t";break;
					case IR_SUB:op="\tsub\t";break;
					}
					if( matchCONST( p,c ) ){
						q=d_new Tile( op+s+","+c+"\n" );
					}else{
						q=d_new Tile( op+s+",%l\n",munchReg( p ) );
					}
				}
			}
			if( !q ) q=d_new Tile( "\tmov\t"+s+",%l\n",munchReg( t->l ) );
		}
		break;
	}
	if( !q ) q=munchReg( t );
	return q;
}

///////////////////////////////////////////
// munch and return result in a register //
///////////////////////////////////////////
Tile *Codegen_x86::munchReg( TNode *t ){
	if( !t ) return 0;

	string s;
	Tile *q=0;

	switch( t->op ){
	case IR_JUMPT:
		q=d_new Tile( "\tand\t%l,%l\n\tjnz\t"+t->sconst+'\n',munchReg( t->l ) );
		break;
	case IR_JUMPF:
		q=d_new Tile( "\tand\t%l,%l\n\tjz\t"+t->sconst+'\n',munchReg( t->l ) );
		break;
	case IR_JUMPGE:
		q=d_new Tile( "\tcmp\t%l,%r\n\tjnc\t"+t->sconst+'\n',munchReg( t->l ),munchReg( t->r ) );
		break;
	case IR_CALL:
		q=munchCall( t );
		break;
	case IR_MOVE:
		//MUST BE MOVE TO MEM!
		if( matchMEM( t->r,s ) ){
			q=d_new Tile( "\tmov\t"+s+",%l\n",munchReg( t->l ) );
		}else if( t->r->op==IR_MEM ){
			q=d_new Tile( "\tmov\t[%r],%l\n",munchReg( t->l ),munchReg( t->r->l ) );
		}
		break;
	case IR_MEM:
		if( matchMEM( t,s ) ){
			q=d_new Tile( "\tmov\t%l,"+s+"\n" );
		}else{
			q=d_new Tile( "\tmov\t%l,[%l]\n",munchReg( t->l ) );
		}
		break;
	case IR_SEQ:
		q=d_new Tile( "",munch(t->l),munch(t->r) );
		break;
	case IR_ARG:
		q=d_new Tile( "\tlea\t%l,[esp"+itoa_sgn(t->iconst)+"]\n" );
		break;
	case IR_LOCAL:
		q=d_new Tile( "\tlea\t%l,[ebp"+itoa_sgn(t->iconst)+"]\n" );
		break;
	case IR_GLOBAL:
		q=d_new Tile( string( "\tmov\t%l," )+t->sconst+'\n' );
		break;
	case IR_CAST:
		q=munchFP( t->l );
		s="\tpush\t%l\n\tfistp\t[esp]\n\tpop\t%l\n";
		q=d_new Tile( s,q );
		break;
	case IR_CONST:
		q=d_new Tile( "\tmov\t%l,"+itoa(t->iconst)+"\n" );
		break;
	case IR_NEG:
		q=munchUnary( t );
		break;
	case IR_AND:case IR_OR:case IR_XOR:
		q=munchLogical( t );
		break;
	case IR_ADD:case IR_SUB:case IR_MUL:case IR_DIV:
		q=munchArith( t );
		break;
	case IR_SHL:case IR_SHR:case IR_SAR:
		q=munchShift( t );
		break;
	case IR_SETEQ:case IR_SETNE:case IR_SETLT:case IR_SETGT:case IR_SETLE:case IR_SETGE:
		q=munchRelop( t );
		break;
	case IR_FSETEQ:case IR_FSETNE:case IR_FSETLT:case IR_FSETGT:case IR_FSETLE:case IR_FSETGE:
		q=munchFPRelop( t );
		break;
	default:
		q=munchFP( t );if( !q ) return 0;
		s="\tpush\t%l\n\tfstp\t[esp]\n\tpop\t%l\n";
		q=d_new Tile( s,q );
	}
	return q;
}

/////////////////////////////////////////
// munch and return result on FP stack //
/////////////////////////////////////////
Tile *Codegen_x86::munchFP( TNode *t ){
	if( !t ) return 0;

	string s;
	Tile *q=0;

	switch( t->op ){
	case IR_FCALL:
		q=munchCall( t );
		break;
	case IR_FCAST:
		s="\tpush\t%l\n\tfild\t[esp]\n\tpop\t%l\n";
		q=d_new Tile( s,munchReg( t->l ) );
		break;
	case IR_FNEG:
		q=munchFPUnary( t );
		break;
	case IR_FADD:case IR_FSUB:case IR_FMUL:case IR_FDIV:
		q=munchFPArith( t );
		break;
	default:
		q=munchReg( t );if( !q ) return 0;
		s="\tpush\t%l\n\tfld\t[esp]\n\tpop\t%l\n";
		q=d_new Tile( s,q );
	}
	return q;
}
