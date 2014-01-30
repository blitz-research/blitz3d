
#include "stdafx.h"
#include "prefs.h"
#include "debuggerapp.h"
#include <fstream>

#define SWAPRB(x) ( (((x)>>16)&0xff) | ((x)&0xff00) | (((x)&0xff)<<16) )

Prefs prefs;

void Prefs::open(){

	homeDir=getenv( "blitzpath" );

	AddFontResource( (homeDir+"/cfg/blitz.fon").c_str() );
	setDefault();

	bool prg_windowed;

	ifstream in( (homeDir+"/cfg/blitzide.prefs").c_str() );
	if( !in.good() ) return;

	while( !in.eof() ){
		string t;in>>t;
		if( !t.size() ) continue;
		while( in.peek()=='\t' ) in.ignore();
		if( t=="prg_debug" ) in>>prg_debug;
		else if( t=="prg_lastbuild" ) getline( in,prg_lastbuild );
		else if( t=="prg_windowed" ) in>>prg_windowed;
		else if( t=="win_maximized" ) in>>win_maximized;
		else if( t=="win_notoolbar" ) in>>win_notoolbar;
		else if( t=="win_rect" ){
			in>>win_rect.left;in>>win_rect.top;
			in>>win_rect.right;in>>win_rect.bottom;
		}else if( t.substr( 0,5 )=="font_" ){
			string s;int h;in>>s;in>>h;
			t=t.substr( 5 );
			if( t=="editor" ){
				font_editor=s;font_editor_height=h;
			}else if( t=="tabs" ){
				font_tabs=s;font_tabs_height=h;
			}else if( t=="debug" ){
				font_debug=s;font_debug_height=h;
			}
		}else if( t.substr( 0,4 )=="rgb_" ){
			t=t.substr(4);
			string s;in>>s;int rgb=0;
			for( int k=0;k<s.size();++k ){
				int n=s[k];rgb=(rgb<<4)|(n<='9'?n-'0':(n&31)+9);
			}
			rgb=SWAPRB(rgb);

			if( t=="bkgrnd" ) rgb_bkgrnd=rgb;
			else if( t=="string" ) rgb_string=rgb;
			else if( t=="ident" ) rgb_ident=rgb;
			else if( t=="keyword" ) rgb_keyword=rgb;
			else if( t=="comment" ) rgb_comment=rgb;
			else if( t=="digit" ) rgb_digit=rgb;
			else if( t=="default" ) rgb_default=rgb;
		}else if( t=="edit_tabs" ){
			in>>edit_tabs;
		}else if( t=="edit_blkcursor" ){
			in>>edit_blkcursor;
		}else if( t=="edit_backup" ){
			in>>edit_backup;
		}else if( t=="img_toolbar" ){
			getline( in,img_toolbar );
		}else if( t=="cmd_line" ){
			getline( in,cmd_line );
		}else if( t=="file_recent" ){
			string l;getline( in,l );
			if( recentFiles.size()<10 ) recentFiles.push_back( l );
		}else{
			string s="Unrecognized option '"+t+"' in blitzide.prefs";
			MessageBox( 0,s.c_str(),"Error in preferences",MB_OK );
			setDefault();
			return;
		}
	}
	createFonts();
}

void Prefs::close(){

	ofstream out( (homeDir+"cfg\\blitzide.prefs").c_str() );
	if( !out.good() ) return;

	out<<"prg_debug\t"<<prg_debug<<endl;
	out<<"prg_lastbuild\t"<<prg_lastbuild<<endl;
	out<<"win_maximized\t"<<win_maximized<<endl;
	out<<"win_notoolbar\t"<<win_notoolbar<<endl;
	out<<"win_rect\t"<<win_rect.left<<' '<<win_rect.top<<' '<<win_rect.right<<' '<<win_rect.bottom<<endl;
	out<<"font_editor\t"<<font_editor<<' '<<font_editor_height<<endl;
	out<<"font_tabs\t"<<font_tabs<<' '<<font_tabs_height<<endl;
	out<<"font_debug\t"<<font_debug<<' '<<font_debug_height<<endl;
	out<<hex;
	out<<"rgb_bkgrnd\t"<<SWAPRB(rgb_bkgrnd)<<endl;
	out<<"rgb_string\t"<<SWAPRB(rgb_string)<<endl;
	out<<"rgb_ident\t"<<SWAPRB(rgb_ident)<<endl;
	out<<"rgb_keyword\t"<<SWAPRB(rgb_keyword)<<endl;
	out<<"rgb_comment\t"<<SWAPRB(rgb_comment)<<endl;
	out<<"rgb_digit\t"<<SWAPRB(rgb_digit)<<endl;
	out<<"rgb_default\t"<<SWAPRB(rgb_default)<<endl;
	out<<"edit_tabs\t"<<edit_tabs<<endl;
	out<<"edit_blkcursor\t"<<edit_blkcursor<<endl;
	out<<"edit_backup\t"<<edit_backup<<endl;
	out<<"img_toolbar\t"<<img_toolbar<<endl;
	out<<"cmd_line\t"<<cmd_line<<endl;
	for( int k=0;k<recentFiles.size();++k ){
		out<<"file_recent\t"<<recentFiles[k]<<endl;
	}
	out<<dec;

	RemoveFontResource( (homeDir+"cfg\\blitz.fon").c_str() );
}

void Prefs::setDefault(){

	prg_debug=true;

	win_rect.left=win_rect.top=0;
	win_rect.right=640;win_rect.bottom=480;
	win_maximized=false;
	win_notoolbar=false;

	font_editor="blitz";
	font_editor_height=12;
	font_tabs="verdana";
	font_tabs_height=8;
	font_debug="verdana";
	font_debug_height=8;

#ifdef PRO
	rgb_bkgrnd=RGB( 0x22,0x55,0x88 );
	rgb_string=RGB( 0x00,0xff,0x66 );
	rgb_ident=RGB( 0xff,0xff,0xff );
	rgb_keyword=RGB( 0xaa,0xff,0xff );
	rgb_comment=RGB( 0xff,0xee,0x00 );
	rgb_digit=RGB( 0x33,0xff,0xdd );
	rgb_default=RGB( 0xee,0xee,0xee );
	rgb_unsel=RGB( 0x88,0x88,0x88 );
#else
	rgb_bkgrnd=RGB( 32,96,96 );
	rgb_string=RGB( 0,255,0 );
	rgb_ident=RGB( 255,255,255 );
	rgb_keyword=RGB( 255,231,95 );
	rgb_comment=RGB( 0,255,255 );
	rgb_digit=RGB( 200,240,255 );
	rgb_default=RGB( 255,240,200 );
#endif

	edit_tabs=4;
	edit_blkcursor=false;
	edit_backup=2;

	img_toolbar="toolbar.bmp";

	recentFiles.clear();

	createFonts();
}

void Prefs::createFonts(){

	editFont.Detach();
	tabsFont.Detach();
	debugFont.Detach();
	conFont.Detach();

	editFont.CreatePointFont( font_editor_height*10,font_editor.c_str() );
	tabsFont.CreatePointFont( font_tabs_height*10,font_tabs.c_str() );
	debugFont.CreatePointFont( font_debug_height*10,font_debug.c_str() );
	conFont.CreatePointFont( 80,"courier" );
}
