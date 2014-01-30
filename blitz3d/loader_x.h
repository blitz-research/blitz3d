
#ifndef LOADER_X_H
#define LOADER_X_H

#include "meshloader.h"

class Loader_X : public MeshLoader{
public:
	MeshModel *load( const string &f,const Transform &conv,int hint );
};

#endif
