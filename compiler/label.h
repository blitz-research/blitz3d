
#ifndef LABEL_H
#define LABEL_H

struct Label{
	string name;		//name of label
	int def,ref;		//pos of defn and goto/restore src
	int data_sz;		//size of data at this label.

	Label( const string &n,int d,int r,int sz ):name(n),def(d),ref(r),data_sz(sz){}
};

#endif