
#ifndef CODEGEN_H
#define CODEGEN_H

#include "std.h"

enum{
	IR_JUMP,IR_JUMPT,IR_JUMPF,IR_JUMPGE,

	IR_SEQ,IR_MOVE,IR_MEM,IR_LOCAL,IR_GLOBAL,IR_ARG,IR_CONST,

	IR_JSR,IR_RET,IR_AND,IR_OR,IR_XOR,IR_SHL,IR_SHR,IR_SAR,

	IR_CALL,IR_RETURN,IR_CAST,
	IR_NEG,IR_ADD,IR_SUB,IR_MUL,IR_DIV,
	IR_SETEQ,IR_SETNE,IR_SETLT,IR_SETGT,IR_SETLE,IR_SETGE,

	IR_FCALL,IR_FRETURN,IR_FCAST,
	IR_FNEG,IR_FADD,IR_FSUB,IR_FMUL,IR_FDIV,
	IR_FSETEQ,IR_FSETNE,IR_FSETLT,IR_FSETGT,IR_FSETLE,IR_FSETGE,
};

struct TNode{

	int op;				//opcode
	TNode *l,*r;		//args
	int iconst;			//for CONST type_int
	string sconst;		//for CONST type_string

	TNode( int op,TNode *l=0,TNode *r=0 ):op(op),l(l),r(r),iconst(0){}
	TNode( int op,TNode *l,TNode *r,int i ):op(op),l(l),r(r),iconst(i){}
	TNode( int op,TNode *l,TNode *r,const string &s ):op(op),l(l),r(r),iconst(0),sconst(s){}
	~TNode(){ delete l;delete r; }

	void log();
};

class Codegen{
public:
	ostream &out;
	bool debug;
	Codegen( ostream &out,bool debug ):out( out ),debug( debug ){}

	virtual void enter( const string &l,int frameSize )=0;
	virtual void code( TNode *code )=0;
	virtual void leave( TNode *cleanup,int pop_sz )=0;
	virtual void label( const string &l )=0;
	virtual void i_data( int i,const string &l="" )=0;
	virtual void s_data( const string &s,const string &l="" )=0;
	virtual void p_data( const string &p,const string &l="" )=0;
	virtual void align_data( int n )=0;
	virtual void flush()=0;
};

#endif