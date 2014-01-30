
#include "../codegen.h"

struct Tile;

class Codegen_x86 : public Codegen{
public:
	Codegen_x86( ostream &out,bool debug );

	virtual void enter( const string &l,int frameSize );
	virtual void code( TNode *code );
	virtual void leave( TNode *cleanup,int pop_sz );
	virtual void label( const string &l );
	virtual void i_data( int i,const string &l );
	virtual void s_data( const string &s,const string &l );
	virtual void p_data( const string &p,const string &l );
	virtual void align_data( int n );
	virtual void flush();

private:
	bool inCode;

	Tile *genCompare( TNode *t,string &func,bool negate );

	Tile *munch( TNode *t );		//munch and discard result
	Tile *munchReg( TNode *t );		//munch and put result in a CPU reg
	Tile *munchFP( TNode *t );		//munch and put result on FP stack

	Tile *munchCall( TNode *t );
	Tile *munchUnary( TNode *t );
	Tile *munchLogical( TNode *t );
	Tile *munchArith( TNode *t );
	Tile *munchShift( TNode *t );
	Tile *munchRelop( TNode *t );
	Tile *munchFPUnary( TNode *t );
	Tile *munchFPArith( TNode *t );
	Tile *munchFPRelop( TNode *t );
};