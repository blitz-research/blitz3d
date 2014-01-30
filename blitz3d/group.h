
#ifndef GROUP_H
#define GROUP_H

class Group{

	vector<Object*> _objs;

public:
	Group( Object *obj );
	~Group();

	const vector<Object*> objs()const{ return _objs; }
};

#endif