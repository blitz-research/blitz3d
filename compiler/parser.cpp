
#include "std.h"
#include <cstdlib>
#include "parser.h"

#ifdef DEMO
static const int TEXTLIMIT=16384;
#else
static const int TEXTLIMIT=1024*1024-1;
#endif

enum{
	STMTS_PROG,STMTS_BLOCK,STMTS_LINE
};

static bool isTerm( int c ){ return c==':' || c=='\n'; }

Parser::Parser( Toker &t ):toker(&t),main_toker(&t){
}

ProgNode *Parser::parse( const string &main ){

	incfile=main;

	consts=d_new DeclSeqNode();
	structs=d_new DeclSeqNode();
	funcs=d_new DeclSeqNode();
	datas=d_new DeclSeqNode();
	StmtSeqNode *stmts=0;

	try{
		stmts=parseStmtSeq( STMTS_PROG );
		if( toker->curr()!=EOF ) exp( "end-of-file" );
	}catch( Ex ){
		delete stmts;delete datas;delete funcs;delete structs;delete consts;
		throw;
	}

	return d_new ProgNode( consts,structs,funcs,datas,stmts );
}

void Parser::ex( const string &s ){
	throw Ex( s,toker->pos(),incfile );
}

void Parser::exp( const string &s ){
	switch( toker->curr() ){
	case NEXT:ex( "'Next' without 'For'" );
	case WEND:ex( "'Wend' without 'While'" );
	case ELSE:case ELSEIF:ex( "'Else' without 'If'" );
	case ENDIF:ex( "'Endif' without 'If'" );
	case ENDFUNCTION:ex( "'End Function' without 'Function'" );
	case UNTIL:ex( "'Until' without 'Repeat'" );
	case FOREVER:ex( "'Forever' without 'Repeat'" );
	case CASE:ex( "'Case' without 'Select'" );
	case ENDSELECT:ex( "'End Select' without 'Select'" );
	}
	ex( "Expecting "+s );
}

string Parser::parseIdent(){
	if( toker->curr()!=IDENT ) exp( "identifier" );
	string t=toker->text();
	toker->next();
	return t;
}

void Parser::parseChar( int c ){
	if( toker->curr()!=c ) exp( string( "'" )+char(c)+string( "'" ) );
	toker->next();
}

StmtSeqNode *Parser::parseStmtSeq( int scope ){
	a_ptr<StmtSeqNode> stmts( d_new StmtSeqNode( incfile ) );
	parseStmtSeq( stmts,scope );
	return stmts.release();
}

void Parser::parseStmtSeq( StmtSeqNode *stmts,int scope ){

	for(;;){
		while( toker->curr()==':' || (scope!=STMTS_LINE && toker->curr()=='\n') ) toker->next();
		StmtNode *result=0;

		int pos=toker->pos();

#ifdef DEMO
		if( Toker::chars_toked>TEXTLIMIT ){
			ex( "Demo version source limit exceeded" );
		}
#endif
		switch( toker->curr() ){
		case INCLUDE:
			{
				if( toker->next()!=STRINGCONST ) exp( "include filename" );
				string inc=toker->text();toker->next();
				inc=inc.substr( 1,inc.size()-2 );

				//WIN32 KLUDGE//
				char buff[MAX_PATH],*p;
				if( GetFullPathName( inc.c_str(),MAX_PATH,buff,&p ) ) inc=buff;
				inc=tolower(inc);

				if( included.find( inc )!=included.end() ) break;

				ifstream i_stream( inc.c_str() );
				if( !i_stream.good() ) ex( "Unable to open include file" );

				Toker i_toker( i_stream );

				string t_inc=incfile;incfile=inc;
				Toker *t_toker=toker;toker=&i_toker;

				included.insert( incfile );

				a_ptr<StmtSeqNode> ss( parseStmtSeq( scope ) );
				if( toker->curr()!=EOF ) exp( "end-of-file" );

				result=d_new IncludeNode( incfile,ss.release() );

				toker=t_toker;
				incfile=t_inc;
			}
			break;
		case IDENT:
			{
				string ident=toker->text();
				toker->next();string tag=parseTypeTag();
				if( arrayDecls.find(ident)==arrayDecls.end() 
					&& toker->curr()!='=' && toker->curr()!='\\' && toker->curr()!='[' ){
					//must be a function
					ExprSeqNode *exprs;
					if( toker->curr()=='(' ){
						//ugly lookahead for optional '()' around statement params
						int nest=1,k;
						for( k=1;;++k ){
							int c=toker->lookAhead( k );
							if( isTerm( c ) ) ex( "Mismatched brackets" );
							else if( c=='(' ) ++nest;
							else if( c==')' && !--nest ) break;
						}
						if( isTerm( toker->lookAhead( ++k ) ) ){
							toker->next();
							exprs=parseExprSeq();
							if( toker->curr()!=')' ) exp( "')'" );
							toker->next();
						}else exprs=parseExprSeq();
					}else exprs=parseExprSeq();
					CallNode *call=d_new CallNode( ident,tag,exprs );
					result=d_new ExprStmtNode( call );
				}else{
					//must be a var
					a_ptr<VarNode> var( parseVar( ident,tag ) );
					if( toker->curr()!='=' ) exp( "variable assignment" );
					toker->next();ExprNode *expr=parseExpr( false );
					result=d_new AssNode( var.release(),expr );
				}
			}
			break;
		case IF:
			{
				toker->next();result=parseIf();
				if( toker->curr()==ENDIF ) toker->next();
			}
			break;
		case WHILE:
			{
				toker->next();
				a_ptr<ExprNode> expr( parseExpr( false ) );
				a_ptr<StmtSeqNode> stmts( parseStmtSeq( STMTS_BLOCK ) );
				int pos=toker->pos();
				if( toker->curr()!=WEND ) exp( "'Wend'" );
				toker->next();
				result=d_new WhileNode( expr.release(),stmts.release(),pos );
			}
			break;
		case REPEAT:
			{
				toker->next();ExprNode *expr=0;
				a_ptr<StmtSeqNode> stmts( parseStmtSeq( STMTS_BLOCK ) );
				int curr=toker->curr();
				int pos=toker->pos();
				if( curr!=UNTIL && curr!=FOREVER ) exp( "'Until' or 'Forever'" );
				toker->next();if( curr==UNTIL ) expr=parseExpr( false );
				result=d_new RepeatNode( stmts.release(),expr,pos );
			}
			break;
		case SELECT:
			{
				toker->next();ExprNode *expr=parseExpr( false );
				a_ptr<SelectNode> selNode( d_new SelectNode( expr ) );
				for(;;){
					while( isTerm( toker->curr() ) ) toker->next();
					if( toker->curr()==CASE ){
						toker->next();
						a_ptr<ExprSeqNode> exprs( parseExprSeq() );
						if( !exprs->size() ) exp( "expression sequence" );
						a_ptr<StmtSeqNode> stmts( parseStmtSeq( STMTS_BLOCK ) );
						selNode->push_back( d_new CaseNode( exprs.release(),stmts.release() ) );
						continue;
					}else if( toker->curr()==DEFAULT ){
						toker->next();
						a_ptr<StmtSeqNode> stmts( parseStmtSeq( STMTS_BLOCK ) );
						if( toker->curr()!=ENDSELECT ) exp( "'End Select'" );
						selNode->defStmts=stmts.release();
						break;
					}else if( toker->curr()==ENDSELECT ){
						break;
					}
					exp( "'Case', 'Default' or 'End Select'" );
				}
				toker->next();
				result=selNode.release();
			}
			break;
		case FOR:
			{
				a_ptr<VarNode> var;
				a_ptr<StmtSeqNode> stmts;
				toker->next();var=parseVar();
				if( toker->curr()!='=' ) exp( "variable assignment" );
				if( toker->next()==EACH ){
					toker->next();
					string ident=parseIdent();
					stmts=parseStmtSeq( STMTS_BLOCK );
					int pos=toker->pos();
					if( toker->curr()!=NEXT ) exp( "'Next'" );
					toker->next();
					result=d_new ForEachNode( var.release(),ident,stmts.release(),pos );
				}else{
					a_ptr<ExprNode> from,to,step;
					from=parseExpr( false );
					if( toker->curr()!=TO ) exp( "'TO'" );
					toker->next();to=parseExpr( false );
					//step...
					if( toker->curr()==STEP ){
						toker->next();step=parseExpr( false );
					}else step=d_new IntConstNode( 1 );
					stmts=parseStmtSeq( STMTS_BLOCK );
					int pos=toker->pos();
					if( toker->curr()!=NEXT ) exp( "'Next'" );
					toker->next();
					result=d_new ForNode( var.release(),from.release(),to.release(),step.release(),stmts.release(),pos );
				}
			}
			break;
		case EXIT:
			{
				toker->next();result=d_new ExitNode();
			}
			break;
		case GOTO:
			{
				toker->next();string t=parseIdent();result=d_new GotoNode( t );
			}
			break;
		case GOSUB:
			{
				toker->next();string t=parseIdent();result=d_new GosubNode( t );
			}
			break;
		case RETURN:
			{
				toker->next();result=d_new ReturnNode( parseExpr( true ) );
			}
			break;
		case BBDELETE:
			{
				if( toker->next()==EACH ){
					toker->next();string t=parseIdent();
					result=d_new DeleteEachNode( t );
				}else{
					ExprNode *expr=parseExpr( false );
					result=d_new DeleteNode( expr );
				}
			}
			break;
		case INSERT:
			{
				toker->next();
				a_ptr<ExprNode> expr1( parseExpr( false ) );
				if( toker->curr()!=BEFORE && toker->curr()!=AFTER ) exp( "'Before' or 'After'" );
				bool before=toker->curr()==BEFORE;toker->next();
				a_ptr<ExprNode> expr2( parseExpr( false ) );
				result=d_new InsertNode( expr1.release(),expr2.release(),before );
			}
			break;
		case READ:
			do{
				toker->next();VarNode *var=parseVar();
				StmtNode *stmt=d_new ReadNode( var );
				stmt->pos=pos;pos=toker->pos();
				stmts->push_back( stmt );
			}while( toker->curr()==',' );
			break;
		case RESTORE:
			if( toker->next()==IDENT ){
				result=d_new RestoreNode( toker->text() );toker->next();
			}else result=d_new RestoreNode( "" );
			break;
		case DATA:
			if( scope!=STMTS_PROG ) ex( "'Data' can only appear in main program" );
			do{
				toker->next();
				ExprNode *expr=parseExpr( false );
				datas->push_back( d_new DataDeclNode( expr ) );
			}while( toker->curr()==',' );
			break;
		case TYPE:
			if( scope!=STMTS_PROG ) ex( "'Type' can only appear in main program" );
			toker->next();structs->push_back( parseStructDecl() );
			break;
		case BBCONST:
			if( scope!=STMTS_PROG ) ex( "'Const' can only appear in main program" );
			do{
				toker->next();consts->push_back( parseVarDecl( DECL_GLOBAL,true ) );
			}while( toker->curr()==',' );
			break;
		case FUNCTION:
			if( scope!=STMTS_PROG ) ex( "'Function' can only appear in main program" );
			toker->next();funcs->push_back( parseFuncDecl() );
			break;
		case DIM:
			do{
				toker->next();
				StmtNode *stmt=parseArrayDecl();
				stmt->pos=pos;pos=toker->pos();
				stmts->push_back( stmt );
			}while( toker->curr()==',' );
			break;
		case LOCAL:
			do{
				toker->next();
				DeclNode *d=parseVarDecl( DECL_LOCAL,false );
				StmtNode *stmt=d_new DeclStmtNode( d );
				stmt->pos=pos;pos=toker->pos();
				stmts->push_back( stmt );
			}while( toker->curr()==',' );
			break;
		case GLOBAL:
			if( scope!=STMTS_PROG ) ex( "'Global' can only appear in main program" );
			do{
				toker->next();
				DeclNode *d=parseVarDecl( DECL_GLOBAL,false );
				StmtNode *stmt=d_new DeclStmtNode( d );
				stmt->pos=pos;pos=toker->pos();
				stmts->push_back( stmt );
			}while( toker->curr()==',' );
			break;
		case '.':
			{
				toker->next();string t=parseIdent();
				result=d_new LabelNode( t,datas->size() );
			}
			break;
		default:
			return;
		}

		if( result ){
			result->pos=pos;
			stmts->push_back( result );
		}
	}
}

string Parser::parseTypeTag(){
	switch( toker->curr() ){
	case '%':toker->next();return "%";
	case '#':toker->next();return "#";
	case '$':toker->next();return "$";
	case '.':toker->next();return parseIdent();
	}
	return "";
}

VarNode *Parser::parseVar(){
	string ident=parseIdent();
	string tag=parseTypeTag();
	return parseVar( ident,tag );
}

VarNode *Parser::parseVar( const string &ident,const string &tag ){
	a_ptr<VarNode> var;
	if( toker->curr()=='(' ){
		toker->next();
		a_ptr<ExprSeqNode> exprs( parseExprSeq() );
		if( toker->curr()!=')' ) exp( "')'" );
		toker->next();
		var=d_new ArrayVarNode( ident,tag,exprs.release() );
	}else var=d_new IdentVarNode( ident,tag );

	for(;;){
		if( toker->curr()=='\\' ){
			toker->next();
			string ident=parseIdent();
			string tag=parseTypeTag();
			ExprNode *expr=d_new VarExprNode( var.release() );
			var=d_new FieldVarNode( expr,ident,tag );
		}else if( toker->curr()=='[' ){
			toker->next();
			a_ptr<ExprSeqNode> exprs( parseExprSeq() );
			if( exprs->exprs.size()!=1 || toker->curr()!=']' ) exp( "']'" );
			toker->next();
			ExprNode *expr=d_new VarExprNode( var.release() );
			var=d_new VectorVarNode( expr,exprs.release() );
		}else{
			break;
		}
	}
	return var.release();
}

DeclNode *Parser::parseVarDecl( int kind,bool constant ){
	int pos=toker->pos();
	string ident=parseIdent();
	string tag=parseTypeTag();
	DeclNode *d;
	if( toker->curr()=='[' ){
		if( constant ) ex( "Blitz arrays may not be constant" );
		toker->next();
		a_ptr<ExprSeqNode> exprs( parseExprSeq() );
		if( exprs->size()!=1 || toker->curr()!=']' ) exp( "']'" );
		toker->next();
		d=d_new VectorDeclNode( ident,tag,exprs.release(),kind );
	}else{
		ExprNode *expr=0;
		if( toker->curr()=='=' ){
			toker->next();expr=parseExpr( false );
		}else if( constant ) ex( "Constants must be initialized" );
		d=d_new VarDeclNode( ident,tag,kind,constant,expr );
	}
	d->pos=pos;d->file=incfile;
	return d;
}

DimNode *Parser::parseArrayDecl(){
	int pos=toker->pos();
	string ident=parseIdent();
	string tag=parseTypeTag();
	if( toker->curr()!='(' ) exp( "'('" );
	toker->next();a_ptr<ExprSeqNode> exprs( parseExprSeq() );
	if( toker->curr()!=')' ) exp( "')'" );
	if( !exprs->size() ) ex( "can't have a 0 dimensional array" );
	toker->next();
	DimNode *d=d_new DimNode( ident,tag,exprs.release() );
	arrayDecls[ident]=d;
	d->pos=pos;
	return d;
}

DeclNode *Parser::parseFuncDecl(){
	int pos=toker->pos();
	string ident=parseIdent();
	string tag=parseTypeTag();
	if( toker->curr()!='(' ) exp( "'('" );
	a_ptr<DeclSeqNode> params( d_new DeclSeqNode() );
	if( toker->next()!=')' ){
		for(;;){
			params->push_back( parseVarDecl( DECL_PARAM,false ) );
			if( toker->curr()!=',' ) break;
			toker->next();
		}
		if( toker->curr()!=')' ) exp( "')'" );
	}
	toker->next();
	a_ptr<StmtSeqNode> stmts( parseStmtSeq( STMTS_BLOCK ) );
	if( toker->curr()!=ENDFUNCTION ) exp( "'End Function'" );
	StmtNode *ret=d_new ReturnNode(0);ret->pos=toker->pos();
	stmts->push_back( ret );toker->next();
	DeclNode *d=d_new FuncDeclNode( ident,tag,params.release(),stmts.release() );
	d->pos=pos;d->file=incfile;
	return d;
}

DeclNode *Parser::parseStructDecl(){
	int pos=toker->pos();
	string ident=parseIdent();
	while( toker->curr()=='\n' ) toker->next();
	a_ptr<DeclSeqNode> fields( d_new DeclSeqNode() );
	while( toker->curr()==FIELD ){
		do{
			toker->next();
			fields->push_back( parseVarDecl( DECL_FIELD,false ) );
		}while( toker->curr()==',' );
		while( toker->curr()=='\n' ) toker->next();
	}
	if( toker->curr()!=ENDTYPE ) exp( "'Field' or 'End Type'" );
	toker->next();
	DeclNode *d=d_new StructDeclNode( ident,fields.release() );
	d->pos=pos;d->file=incfile;
	return d;
}

IfNode *Parser::parseIf(){
	a_ptr<ExprNode> expr;
	a_ptr<StmtSeqNode> stmts,elseOpt;

	expr=parseExpr( false );
	if( toker->curr()==THEN ) toker->next();

	bool blkif=isTerm( toker->curr() );
	stmts=parseStmtSeq( blkif ? STMTS_BLOCK : STMTS_LINE );

	if( toker->curr()==ELSEIF ){
		int pos=toker->pos();
		toker->next();
		IfNode *ifnode=parseIf();
		ifnode->pos=pos;
		elseOpt=d_new StmtSeqNode( incfile );
		elseOpt->push_back( ifnode );
	}else if( toker->curr()==ELSE ){
		toker->next();
		elseOpt=parseStmtSeq( blkif ? STMTS_BLOCK : STMTS_LINE );
	}
	if( blkif ){
		if( toker->curr()!=ENDIF ) exp( "'EndIf'" );
	}else if( toker->curr()!='\n' ) exp( "end-of-line" );

	return d_new IfNode( expr.release(),stmts.release(),elseOpt.release() );
}

ExprSeqNode *Parser::parseExprSeq(){
	a_ptr<ExprSeqNode> exprs( d_new ExprSeqNode() );
	bool opt=true;
	while( ExprNode *e=parseExpr( opt ) ){
		exprs->push_back( e );
		if( toker->curr()!=',' ) break;
		toker->next();opt=false;
	}
	return exprs.release();
}

ExprNode *Parser::parseExpr( bool opt ){
	if( toker->curr()==NOT ){
		toker->next();
		ExprNode *expr=parseExpr1( false );
		return d_new RelExprNode( '=',expr,d_new IntConstNode( 0 ) );
	}
	return parseExpr1( opt );
}

ExprNode *Parser::parseExpr1( bool opt ){

	a_ptr<ExprNode> lhs( parseExpr2( opt ) );
	if( !lhs ) return 0;
	for(;;){
		int c=toker->curr();
		if( c!=AND && c!=OR && c!=XOR ) return lhs.release();
		toker->next();ExprNode *rhs=parseExpr2( false );
		lhs=d_new BinExprNode( c,lhs.release(),rhs );
	}
}

ExprNode *Parser::parseExpr2( bool opt ){

	a_ptr<ExprNode> lhs( parseExpr3( opt ) );
	if( !lhs ) return 0;
	for(;;){
		int c=toker->curr();
		if( c!='<' && c!='>' && c!='=' && c!=LE && c!=GE && c!=NE ) return lhs.release();
		toker->next();ExprNode *rhs=parseExpr3( false );
		lhs=d_new RelExprNode( c,lhs.release(),rhs );
	}
}

ExprNode *Parser::parseExpr3( bool opt ){

	a_ptr<ExprNode> lhs( parseExpr4( opt ) );
	if( !lhs ) return 0;
	for(;;){
		int c=toker->curr();
		if( c!='+' && c!='-' ) return lhs.release();
		toker->next();ExprNode *rhs=parseExpr4( false );
		lhs=d_new ArithExprNode( c,lhs.release(),rhs );
	}
}

ExprNode *Parser::parseExpr4( bool opt ){
	a_ptr<ExprNode> lhs( parseExpr5( opt ) );
	if( !lhs ) return 0;
	for(;;){
		int c=toker->curr();
		if( c!=SHL && c!=SHR && c!=SAR ) return lhs.release();
		toker->next();ExprNode *rhs=parseExpr5( false );
		lhs=d_new BinExprNode( c,lhs.release(),rhs );
	}
}

ExprNode *Parser::parseExpr5( bool opt ){

	a_ptr<ExprNode> lhs( parseExpr6( opt ) );
	if( !lhs ) return 0;
	for(;;){
		int c=toker->curr();
		if( c!='*' && c!='/' && c!=MOD ) return lhs.release();
		toker->next();ExprNode *rhs=parseExpr6( false );
		lhs=d_new ArithExprNode( c,lhs.release(),rhs );
	}
}

ExprNode *Parser::parseExpr6( bool opt ){

	a_ptr<ExprNode> lhs( parseUniExpr( opt ) );
	if( !lhs ) return 0;
	for(;;){
		int c=toker->curr();
		if( c!='^' ) return lhs.release();
		toker->next();ExprNode *rhs=parseUniExpr( false );
		lhs=d_new ArithExprNode( c,lhs.release(),rhs );
	}
}

ExprNode *Parser::parseUniExpr( bool opt ){

	ExprNode *result=0;
	string t;

	int c=toker->curr();
	switch( c ){
	case BBINT:
		if( toker->next()=='%' ) toker->next();
		result=parseUniExpr( false );
		result=d_new CastNode( result,Type::int_type );
		break;
	case BBFLOAT:
		if( toker->next()=='#' ) toker->next();
		result=parseUniExpr( false );
		result=d_new CastNode( result,Type::float_type );
		break;
	case BBSTR:
		if( toker->next()=='$' ) toker->next();
		result=parseUniExpr( false );
		result=d_new CastNode( result,Type::string_type );
		break;
	case OBJECT:
		if( toker->next()=='.' ) toker->next();
		t=parseIdent();
		result=parseUniExpr( false );
		result=d_new ObjectCastNode( result,t );
		break;
	case BBHANDLE:
		toker->next();
		result=parseUniExpr( false );
		result=d_new ObjectHandleNode( result );
		break;
	case BEFORE:
		toker->next();
		result=parseUniExpr( false );
		result=d_new BeforeNode( result );
		break;
	case AFTER:
		toker->next();
		result=parseUniExpr( false );
		result=d_new AfterNode( result );
		break;
	case '+':case '-':case '~':case ABS:case SGN:
		toker->next();
		result=parseUniExpr( false );
		if( c=='~' ){
			result=d_new BinExprNode( XOR,result,d_new IntConstNode( -1 ) );
		}else{
			result=d_new UniExprNode( c,result );
		}
		break;
	default:
		result=parsePrimary( opt );
	}
	return result;
}

ExprNode *Parser::parsePrimary( bool opt ){

	a_ptr<ExprNode> expr;
	string t,ident,tag;
	ExprNode *result=0;
	int n,k;

	switch( toker->curr() ){
	case '(':
		toker->next();
		expr=parseExpr( false );
		if( toker->curr()!=')' ) exp( "')'" );
		toker->next();
		result=expr.release();
		break;
	case BBNEW:
		toker->next();t=parseIdent();
		result=d_new NewNode( t );
		break;
	case FIRST:
		toker->next();t=parseIdent();
		result=d_new FirstNode( t );
		break;
	case LAST:
		toker->next();t=parseIdent();
		result=d_new LastNode( t );
		break;
	case BBNULL:
		result=d_new NullNode();
		toker->next();
		break;
	case INTCONST:
		result=d_new IntConstNode( atoi( toker->text() ) );
		toker->next();
		break;
	case FLOATCONST:
		result=d_new FloatConstNode( atof( toker->text() ) );
		toker->next();
		break;
	case STRINGCONST:
		t=toker->text();
		result=d_new StringConstNode( t.substr( 1,t.size()-2 ) );
		toker->next();
		break;
	case BINCONST:
		n=0;t=toker->text();
		for( k=1;k<t.size();++k ) n=(n<<1)|(t[k]=='1');
		result=d_new IntConstNode( n );
		toker->next();
		break;
	case HEXCONST:
		n=0;t=toker->text();
		for( k=1;k<t.size();++k ) n=(n<<4)|( isdigit(t[k]) ? t[k]&0xf : (t[k]&7)+9 );
		result=d_new IntConstNode( n );
		toker->next();
		break;
	case PI:
		result=d_new FloatConstNode( 3.1415926535897932384626433832795f );
		toker->next();break;
	case BBTRUE:
		result=d_new IntConstNode( 1 );
		toker->next();break;
	case BBFALSE:
		result=d_new IntConstNode( 0 );
		toker->next();break;
	case IDENT:
		ident=toker->text();
		toker->next();tag=parseTypeTag();
		if( toker->curr()=='(' && arrayDecls.find(ident)==arrayDecls.end() ){
			//must be a func
			toker->next();
			a_ptr<ExprSeqNode> exprs( parseExprSeq() );
			if( toker->curr()!=')' ) exp( "')'" );
			toker->next();
			result=d_new CallNode( ident,tag,exprs.release() );
		}else{
			//must be a var
			VarNode *var=parseVar( ident,tag );
			result=d_new VarExprNode( var );
		}
		break;
	default:
		if( !opt ) exp( "expression" );
	}
	return result;
}
