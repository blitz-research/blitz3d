// asmcoder.cpp
// by simon@acid.co.nz

// testroutine for asmcoder class
// see asmcoder.h for readme

#define WIN32_LEAN_AND_MEAN
#include <windows.h>

#include <stdio.h>
#include <time.h>

#include "asmcoder.h"

void (__fastcall *plot)(void *s,int argb);
int	(__fastcall *point)(void*s);
void (__fastcall *span)(void *s,int *argb,int n);

void main() 
{
	void	*pixmap;
	int		n;

	AsmCoder *coder=new AsmCoder();

	pixmap=new int[640*480];

	plot=(void (__fastcall *)(void*,int))new char[96];
	point=(int (__fastcall *)(void*))new char[96];
	span=(void (__fastcall *)(void*,int*,int))new char[96];

//	n=coder->CodePlot(plot,16,0,0xf800,0x07e0,0x001f);		//0:5:6:5
//	printf("plotcode size=%d\n",n);
//	n=coder->CodePoint(point,16,0,0xf800,0x07e0,0x001f);		//0:5:6:5
//	printf("pointcode size=%d\n",n);

	n=coder->CodePlot(plot,24,0,0xff0000,0xff00,0xff);		//0:5:6:5
	printf("plotcode size=%d\n",n);

	n=coder->CodePoint(point,24,0,0xff0000,0xff00,0x00ff);		//0:5:6:5
	printf("pointcode size=%d\n",n);
	
	n=coder->CodeSpan(span,24,0,0xff0000,0xff00,0x00ff);		//0:5:6:5
	printf("scancode size=%d\n",n);

	printf("&plot=0x%x\n",(int)plot);
	printf("&pixmap=0x%x\n",(int)pixmap);

	plot(pixmap,0x818283);
//	short *b=(short *)pixmap;
	int *b=(int *)pixmap;
	printf("b[0]=0x%x\n",b[0]);
	n=point(pixmap);
	printf("point(0,0)=0x%x\n",n);

	int pdat[]={0x8080,0x707070,0x606060};
	span(pixmap,pdat,3);

	delete (void *)plot;
	delete (void *)point;
	delete pixmap;
	delete coder;
	return;
}

//	void (__fastcall *plot)(int x,int y,int argb,Surface *s)=(void (__fastcall *)(int,int,int,Surface*))code;

void ortest()
{
__asm{
		or		eax,0xff00ff00
		or		eax,-100
	}
}
