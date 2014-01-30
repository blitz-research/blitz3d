
#ifndef SOURCEFILE_H
#define SOURCEFILE_H

class SourceFile : public CRichEditCtrl{
public:
	SourceFile();
	~SourceFile();

	void highLight( int row,int col );

	DECLARE_DYNAMIC( SourceFile )
	DECLARE_MESSAGE_MAP()

	afx_msg int  OnCreate( LPCREATESTRUCT lpCreateStruct );
};

#endif