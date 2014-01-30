
#ifndef FUNCLIST_H
#define FUNCLIST_H

class FuncList;

class FuncListListener{
public:
	virtual void funcSelected( int line ){}
};

class FuncList : public CListCtrl{
public:
	FuncList();

	void setListener( FuncListListener *l );

	void clear();

	void insert( int line,const string &func );
	void remove( int begin,int end );
	void relocate( int begin,int offset );

DECLARE_DYNAMIC( FuncList )
DECLARE_MESSAGE_MAP()

	afx_msg int  OnCreate( LPCREATESTRUCT lpCreateStruct );
	afx_msg void nm_click( NMHDR *nmhdr,LRESULT *result );

private:

	typedef list<int> Funcs;

	Funcs funcs;

	FuncListListener *listener;
};

#endif