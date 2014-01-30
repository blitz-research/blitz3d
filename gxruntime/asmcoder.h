// asmcoder.h 
// by simon@acid.co.nz

// v0.5
// point sets nonused bits to 1

// v0.4
// direct pixel address now expected
// x,y args removed span removed 

// v0.3
// span changed to bytes
// pixfield indirect dropped, passed directy to plot
// ret(n) added to remove args from fastcall stack
// CodePoint() method added

/*

asmcoder provides inline assembly generator for high speed runtime tasks

methods:

CodePlot(codebase,depth,amask,rmask,gmask,bmask)
CodePoint(codebase,depth,amask,rmask,gmask,bmask)
CodeSpan(codebase,depth,amask,rmask,gmask,bmask)

	codebase=executable memory (64 bytes)
	depth=pixel depth (8,16,24,32)
	amask=32bit alpha pixel mask
	rmask=32bit red pixel mask
	gmask=32bit green pixel mask
	bmask=32bit blue pixel mask

	returns number of bytes of code generated (max 64 bytes)
	
	call resulting code by typecasting codebase with 

	void (__fastcall *plot)(void *pix,int argb)
	int (__fastcall *point)(void *pix)
	void (__fastcall *span)(void *pix,int *argb,int count)

ToDo:

*/

#ifndef ASMCODER_H
#define ASMCODER_H

class IA32
{
public:
	char	*ptr;
	int		off;

	enum Reg32{eax,ecx,edx,ebx,esp,ebp,esi,edi};

	void Reset(void *p) {ptr=(char *)p;off=0;}

	void Code(int c){if (ptr) ptr[off]=c;off++;}
	void Code16(int c){if (ptr) *(short*)(ptr+off)=c;off+=2;}
	void Code32(int c){if (ptr) *(int*)(ptr+off)=c;off+=4;}

	void push(Reg32 reg) {Code(0x50+reg);}
	void pop(Reg32 reg) {Code(0x58+reg);}
	void ret(int n=0) {if (n==0) Code(0xc3);else {Code(0xc2);Code(n);Code(0);}}
	void mov(Reg32 dest,Reg32 src) {Code(0x8b);Code((0xc0)|(dest<<3)|(src));}
	void or(Reg32 dest,Reg32 src) {Code(0x0b);Code((0xc0)|(dest<<3)|(src));}
	void add(Reg32 dest,Reg32 src) {Code(0x03);Code((0xc0)|(dest<<3)|(src));}
	void load32(Reg32 dest,Reg32 src,int disp=0)
	{
		if (disp==0 && src!=ebp) {Code(0x8b);Code((dest<<3)|(src));} 
		else {Code(0x8b);Code((0x40)|(dest<<3)|(src));Code(disp);}
	}
	void load16(Reg32 dest,Reg32 src,int disp=0) {Code(0x66);load32(dest,src,disp);}
	void load8(Reg32 dest,Reg32 src,int disp=0)
	{
		Code(0x8a);if (disp==0 && src!=ebp) {Code((dest<<3)|(src));} else {Code((0x40)|(dest<<3)|(src));Code(disp);}
	}
	void store32(Reg32 dest,int disp,Reg32 src) 
	{
		if (disp==0 && dest!=ebp) {Code(0x89);Code((src<<3)|(dest));Code(0);}
		else {Code(0x89);Code((0x40)|(src<<3)|(dest));Code(disp);}
	}
	void store16(Reg32 dest,int disp,Reg32 src) {Code(0x66);store32(dest,disp,src);}
	void store8(Reg32 dest,int disp,Reg32 src)
	{
		Code(0x88);
		if (disp==0 && dest!=ebp) {Code((src<<3)|(dest));Code(0);} else {Code((0x40)|(src<<3)|(dest));Code(disp);}
	}
	void lea(Reg32 dest,int scale,Reg32 src1,Reg32 src2,int disp=0)
	{
		int n,mod,sib;
		if (disp==0) n=0;else {if (disp>=-128 && disp<128) n=1;else n=2;}
		mod=(n<<6)|4;
		sib=(src1<<3)|src2;
		if (scale==2) sib|=0x40;
		if (scale==4) sib|=0x80;
		if (scale==8) sib|=0xc0;
		Code(0x8d);Code(mod);Code(sib);
		if (n==1) Code(disp);else if (n==2) Code32(disp);
	}
	void lea(Reg32 dest,int scale,Reg32 src,int disp=0)
	{
		int mod,sib;		
		sib=(src<<3)|5;
		mod=(dest<<3)|4;
		if (scale==2) sib|=0x40;
		if (scale==4) sib|=0x80;
		if (scale==8) sib|=0xc0;
		Code(0x8d);Code(mod);Code(sib);Code32(disp);
	}
	void imul(Reg32 dest,int imm,Reg32 src)
	{
		Code(0x69);Code((0xc0)|(dest<<3)|(src));Code32(imm);
	}
	void shift(Reg32 reg,int imm)	//+imm=left -imm=right
	{
		int		op;
		if (imm==0) return;
		if (imm>0) {op=0xe0+reg;} else {op=0xe8+reg;imm=-imm;}
		if (imm==1) {Code(0xd1);Code(op);} else {Code(0xc1);Code(op);Code(imm);}
	}
	void and(Reg32 reg,int imm)
	{
		if (imm==0xffffffff) return;
		if (imm>=-128 && imm<128) 
		{
			Code(0x83);Code(0xe0+reg);Code(imm);
		}
		else
		{
			if (reg==eax) {Code(0x25);} else {Code(0x81);Code(0xe0+reg);}Code32(imm);
		}
	}
	void or(Reg32 reg,int imm)
	{
		if (imm==0) return;
		if (imm>=-128 && imm<128) 
		{
			Code(0x83);Code(0xc0+reg);Code(imm);
		}
		else
		{
			if (reg==eax) {Code(0x0d);} else {Code(0x81);Code(0xc0+reg);}
			Code32(imm);
		}
	}
	void jnz(int label) {Code(0x75);Code(label-(off+1));}
	void add(Reg32 reg,int imm) {Code(0x83);Code(0xc0+reg);Code(imm);}
	void neg(Reg32 reg) {Code(0xf7);Code(0xd8+reg);}
};

class AsmCoder : IA32
{
public:
	int	amsb,rmsb,gmsb,bmsb;

	void CalcMSBs(int amask,int rmask,int gmask,int bmask)
	{
		unsigned int u;
		amsb=0;if (u=amask) {while (u!=1) {u>>=1;amsb++;}}
		rmsb=0;if (u=rmask) {while (u!=1) {u>>=1;rmsb++;}}
		gmsb=0;if (u=gmask) {while (u!=1) {u>>=1;gmsb++;}}
		bmsb=0;if (u=bmask) {while (u!=1) {u>>=1;bmsb++;}}
	}

// ecx=void *t pixel
// edx=int argb

	int CodePlot(void *code,int depth,int amask,int rmask,int gmask,int bmask)
	{
		Reset(code);
		CalcMSBs(amask,rmask,gmask,bmask);

		push	(ebx);
		push	(ebp);
		mov		(eax,edx);
		mov		(ebp,ecx);
		if (rmask==0xff0000 && gmask==0xff00 && bmask==0xff)
		{
			if (amask==0 && depth>24) and(eax,0xffffff);
		}
		else
		{
			mov		(ebx,eax);				//eax=b ebx=g ecx=r edx=a			
			shift	(eax,bmsb-7);			//3-0);			
			mov		(ecx,ebx);
			shift	(ebx,gmsb-15);
if (amask)	mov		(edx,ecx);				//alph
if (amask)	shift	(edx,amsb-31);			//alph
			and		(eax,bmask);
			shift	(ecx,rmsb-23);		
if (amask)	and		(edx,amask);			//alph
			and		(ebx,gmask);
if (amask)	or		(eax,edx);				//alph
			and		(ecx,rmask);
			or		(eax,ebx);			
			or		(eax,ecx);			
		}
		switch (depth)
		{
			case 16:store16(ebp,0,eax);break;
//			case 24:store16(ebp,0,eax);shift(eax,-16);store8(ebp,2,eax);break;
			case 24:store8(ebp,0,eax);shift(eax,-8);store16(ebp,1,eax);break;
			case 32:store32(ebp,0,eax);break;
			default:store8(ebp,0,eax);
		}
		pop(ebp);
		pop(ebx);
		ret();
		return off;
	}

// ecx=void*pix

	int CodePoint(void *code,int depth,int amask,int rmask,int gmask,int bmask)
	{
		Reset(code);
		CalcMSBs(amask,rmask,gmask,bmask);

		push	(ebx);
		switch (depth)
		{
			case 16:load16(eax,ecx);break;
//			case 24:load16(eax,ecx);shift(eax,-16);load8(eax,ecx,2);break;
			case 24:load16(eax,ecx,1);shift(eax,8);load8(eax,ecx);break;
			case 32:load32(eax,ecx);break;
			default:load8(eax,ecx);
		}
		if ((amask==0 || amask==0xff000000) && (rmask==0xff0000 && gmask==0xff00 && bmask==0xff))
		{
			//
		}
		else
		{
			mov		(ebx,eax);		//eax=b ebx=g ecx=r edx=a			
			mov		(ecx,eax);
if (amask)	mov		(edx,eax);
			and		(eax,bmask);
			shift	(eax,7-bmsb);			
			and		(ebx,gmask);
			shift	(ebx,15-gmsb);			
			and		(ecx,rmask);
			shift	(ecx,23-rmsb);			
if (amask)	and		(edx,amask);
			or		(eax,ebx);
if (amask)	shift	(edx,31-amsb);
			or		(eax,ecx);
if (amask)	or		(eax,edx);
		}
		pop(ebx);
		int oor=0;
		if( !amask ) oor|=0xff000000;
		if( !rmask ) oor|=0x00ff0000;
		if( !gmask ) oor|=0x0000ff00;
		if( !bmask ) oor|=0x000000ff;
		if( oor ) or( eax,oor );
		ret();
		return off;
	}	

// ecx=void *t pixel
// edx=int *argb
// 8(esp)=count

	int CodeSpan(void *code,int depth,int amask,int rmask,int gmask,int bmask)
	{
		int		loop;

		Reset(code);
		CalcMSBs(amask,rmask,gmask,bmask);

		push	(ebp);
		mov		(ebp,esp);
		push	(ebx);
		push	(esi);
		push	(edi);

		load32	(edi,ebp,8);		//edi=count
		mov		(esi,edx);			//esi=[argb]
		mov		(ebp,ecx);			//ebp=[pix]
		neg		(edi);
		
		loop=off;
// loop		
		load32	(eax,esi);
		add		(esi,4);

		if (rmask==0xff0000 && gmask==0xff00 && bmask==0xff)
		{
			if (amask==0 && depth>24) and(eax,0xffffff);
		}
		else
		{
			mov		(ebx,eax);				//eax=b ebx=g ecx=r edx=a			
			shift	(eax,bmsb-7);			//3-0);			
			mov		(ecx,ebx);
			shift	(ebx,gmsb-15);
if (amask)	mov		(edx,ecx);				//alph
if (amask)	shift	(edx,amsb-31);			//alph
			and		(eax,bmask);
			shift	(ecx,rmsb-23);		
if (amask)	and		(edx,amask);			//alph
			and		(ebx,gmask);
if (amask)	or		(eax,edx);				//alph
			and		(ecx,rmask);
			or		(eax,ebx);			
			or		(eax,ecx);			
		}
		switch (depth)
		{
			case 16:store16(ebp,0,eax);add(ebp,2);break;
			case 24:store8(ebp,0,eax);shift(eax,-8);store16(ebp,1,eax);add(ebp,3);break;
			case 32:store32(ebp,0,eax);add(ebp,4);break;
			default:store8(ebp,0,eax);add(ebp,1);
		}
		add		(edi,1);
		jnz		(loop);

		pop		(edi);
		pop		(esi);
		pop		(ebx);
		pop		(ebp);
		ret		(4);
		return off;
	}

};

#endif
