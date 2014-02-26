;Texture Paint
;David Bird
;dave@birdie72.freeserve.co.uk
Graphics3D 640,480
HWMultiTex True

HidePointer
SetBuffer BackBuffer()
lit=CreateLight()
LightColor lit,0,60,0
PositionEntity lit,0,0,-2

cam=CreateCamera()
CameraRange cam,.1,1000
PositionEntity cam,0,0,-3

tex=LoadTexture("tex0.bmp",1)
bloodtex=CreateTexture(256,256)

cu=CreateSphere(30)
EntityTexture cu,tex,0,0
EntityTexture cu,bloodtex,0,1
TextureBlend bloodtex,3
EntityPickMode cu,2
;

spr=CreateSprite()
EntityTexture spr,tex,0,0
EntityTexture spr,bloodtex,0,1
PositionEntity spr,4,4,4

Global ent
Global tria
Global surf
Global sx0#,sx1#,sx2#,sxp#
Global sy0#,sy1#,sy2#,syp#
Global un0#,vn0#
Global un1#,vn1#

Global u0#,v0#
Global u1#,v1#
Global u2#,v2#

;WireFrame True
While Not KeyDown(1)
	EntityTexture spr,bloodtex
	xm=MouseX()
	ym=MouseY()
	If MouseDown(1) Then
		Check_Hit(cam,xm,ym,bloodtex)
	End If
	TurnEntity cu,0,.1,0
	UpdateWorld 
	RenderWorld

	;draw mouse cursor
	Line xm-4,ym,xm+4,ym
	Line xm,ym-4,xm,ym+4
	
	;draw picked triangle
	Line sx0,sy0,sx1,sy1
	Line sx1,sy1,sx2,sy2
	Line sx2,sy2,sx0,sy0

	;plot picked point on triangle
	Plot sxp,syp

;	Text 0,50,"0X:"+sx0+" Y: "+sy0
;	Text 0,65,"1X:"+sx1+" Y: "+sy1
;	Text 0,80,"2X:"+sx2+" Y: "+sy2
;	Text 0,95,"PX:"+sxp+" Y: "+syp
;	Text 0,110,un0+" "+vn0
;	Text 0,125,un1+" "+vn1
;	Text 0,140,"U0:"+u0#+" V0:"+v0#
;	Text 0,155,"U1:"+u1#+" V1:"+v1#
;	Text 0,170,"U2:"+u2#+" V2:"+v2#

	Flip
Wend

FreeEntity lit
FreeEntity cam
EndGraphics
End

Function Check_Hit(cam,x,y,tex)
	ent=CameraPick(cam,x,y)
	surf=PickedSurface()
	tria=PickedTriangle()
	If ent<>0 Then
		;Get three tex coords from triangle
		u0#=VertexU(surf,TriangleVertex(surf,tria,0))
		u1#=VertexU(surf,TriangleVertex(surf,tria,1))-u0
		u2#=VertexU(surf,TriangleVertex(surf,tria,2))-u0
		
		v0#=VertexV(surf,TriangleVertex(surf,tria,0))
		v1#=VertexV(surf,TriangleVertex(surf,tria,1))-v0
		v2#=VertexV(surf,TriangleVertex(surf,tria,2))-v0

		x0#=VertexX(surf,TriangleVertex(surf,tria,0))
		x1#=VertexX(surf,TriangleVertex(surf,tria,1))
		x2#=VertexX(surf,TriangleVertex(surf,tria,2))
		
		y0#=VertexY(surf,TriangleVertex(surf,tria,0))
		y1#=VertexY(surf,TriangleVertex(surf,tria,1))
		y2#=VertexY(surf,TriangleVertex(surf,tria,2))
		
		z0#=VertexZ(surf,TriangleVertex(surf,tria,0))
		z1#=VertexZ(surf,TriangleVertex(surf,tria,1))
		z2#=VertexZ(surf,TriangleVertex(surf,tria,2))


		TFormPoint x0,y0,z0,ent,0
		CameraProject cam,TFormedX(),TFormedY(),TFormedZ()
		sx0#=ProjectedX()
		sy0#=ProjectedY()
		
		TFormPoint x1,y1,z1,ent,0
		CameraProject cam,TFormedX(),TFormedY(),TFormedZ()
		sx1#=ProjectedX()-sx0
		sy1#=ProjectedY()-sy0

		TFormPoint x2,y2,z2,ent,0
		CameraProject cam,TFormedX(),TFormedY(),TFormedZ()
		sx2#=ProjectedX()-sx0
		sy2#=ProjectedY()-sy0
		
;		TFormPoint PickedX(),PickedY(),PickedZ(),ent,0
;		CameraProject cam,TFormedX(),TFormedY(),TFormedZ()
		CameraProject cam,PickedX(),PickedY(),PickedZ()
		sxp#=ProjectedX()-sx0
		syp#=ProjectedY()-sy0

		sx0=0
		sy0=0
		

;		If u1<>0 Then us1#=u1/sx1 Else us1#=0
;		If v1<>0 Then vs1#=v1/sy1 Else vs1#=0
;		If u2<>0 Then us2#=u2/sx2 Else us2#=0
;		If v2<>0 Then vs2#=v2/sy2 Else vs2#=0
		us1#=u1/sx1
		vs1#=v1/sy1
		us2#=u2/sx2
		vs2#=v2/sy2
	
		un0#=((sxp*us1))
		vn0#=((syp*vs1))
		un1#=((sxp*us2))
		vn1#=((syp*vs2))
		
		If un0=0 Or vn0=0 Then
			Paint_Blood(tex,(u0+un1)*TextureWidth(tex),(v0+vn1)*TextureHeight(tex))
		Else
			paint_Blood(tex,(u0+un0)*TextureWidth(tex),(v0+vn0)*TextureHeight(tex))
		End If
	End If
	
End Function

Function Paint_Blood(tex,x,y)
	SetBuffer TextureBuffer(tex)
;	LockBuffer TextureBuffer(tex)
;	Col=255 Shl 16
;	WritePixelFast x Mod TextureWidth(tex),y Mod TextureHeight(tex),col,TextureBuffer(tex)
	Color 255,0,0
	s#=4
	Oval x-s/2,y-s/2,s,s
	Color 255,255,255
;	UnlockBuffer TextureBuffer(tex)
	SetBuffer BackBuffer()
End Function