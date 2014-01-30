
#ifndef PREFS_H
#define PREFS_H

class Prefs{
public:
	bool prg_debug;
	string prg_lastbuild;

	RECT win_rect;
	bool win_maximized;
	bool win_notoolbar;

	string font_editor,font_tabs,font_debug;
	int font_editor_height,font_tabs_height,font_debug_height;

	int rgb_bkgrnd;		//0
	int rgb_string;		//1
	int rgb_ident;		//2
	int rgb_keyword;	//3
	int rgb_comment;	//4
	int rgb_digit;		//5
	int rgb_default;	//6

	int edit_tabs;
	bool edit_blkcursor;
	int edit_backup;

	string img_toolbar;

	string homeDir;
	CFont conFont,editFont,tabsFont,debugFont;

	vector<string> recentFiles;

	string cmd_line;

	void open();
	void close();

private:

	void setDefault();
	void createFonts();
};

extern Prefs prefs;

#endif