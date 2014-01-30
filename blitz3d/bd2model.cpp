
#include "bd2model.h"

struct BD2Vert{
	unsigned char x,y,z,n,u,v;
};

struct BD2Tri{
	unsigned short v0,v1,v2;
};

struct BD2Frame{
	float x_scale,y_scale,z_scale;
	float x_offset,y_offset,z_offset;
	BD2Vert verts[1];
};

struct BD2File{
	int id;	//'BD2F'
	float u_scale,v_scale;
	int n_verts,n_tris,n_frames;
	BD2Tri tris[1];
	BD2Frame frames[1];
};

struct BD2Model::Rep{
};