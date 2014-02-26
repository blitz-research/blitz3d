;
;
;

Graphics3D 640,480
SetBuffer BackBuffer()

lit=CreateLight()
piv=CreatePivot()
cam=CreateCamera(piv)
CameraRange cam,1,5000
PositionEntity cam,0,0,-5
Global lightning_brush=LoadBrush("tex0.bmp",2)
Const Lightning_Count=4

While Not KeyDown(1)
	TurnEntity piv,0,1,0

	If KeyDown(203) TurnEntity cam,0,+1,0
	If KeyDown(205) TurnEntity cam,0,-1,0
	If KeyDown(200) TurnEntity cam,+1,0,0
	If KeyDown(208) TurnEntity cam,-1,0,0

	If KeyDown(44) MoveEntity cam,0,0,-.1
	If KeyDown(30) MoveEntity cam,0,0,.1
	If KeyHit(2) Then Create_lightning(0,0,0,300+Rand(20),30-Rand(20))
	Update_Lightning(cam)
	UpdateWorld
	RenderWorld
	Flip
Wend
Free_lightning()
FreeEntity cam
EndGraphics
End

Type lightning
	Field ent
	Field surf
	Field alpha#
End Type

Function Update_Lightning(cam)
	For a.lightning=Each lightning
		a\alpha=a\alpha-.03
		EntityAlpha a\ent,a\alpha
		EntityColor a\ent,255*a\alpha,255*a\alpha,255
		PointEntity a\ent,cam,0
		If a\alpha<0 Then FreeEntity a\ent:Delete a	
	Next
End Function

Function Free_Lightning()
	For a.lightning=Each lightning
		If a\ent Then FreeEntity a\ent
		Delete a
	Next
End Function

Function Create_lightning(gx#,gy#,gz#,high#,seg)
	pseg=seg
	For cnt=0 To Lightning_Count
		seg=Rand(pseg)
		a.lightning=New lightning
		a\alpha=1
		a\ent=CreateMesh()
		EntityFX a\ent,4
		a\surf=CreateSurface(a\ent,lightning_brush)
		x#=0
		y#=0
		z#=0
		stp#=high/Float(seg)
		s2p#=stp/2
		For b=0 To seg
			AddVertex a\surf,x,y,z,0,0
			x=x-3
			AddVertex a\surf,x,y,z,1,0
			x=x+Rnd(-8,8)
			y=y-s2p
			AddVertex a\surf,x,y,z,1,1
			x=x+3
			AddVertex a\surf,x,y,z,0,1
		Next
		For b=0 To (seg) Step 4
			AddTriangle(a\surf,b,b+1,b+2)
			AddTriangle(a\surf,b+1,b+3,b+2)
			AddTriangle(a\surf,b,b+2,b+1)
			AddTriangle(a\surf,b+1,b+2,b+3)
		Next
	Next
	PositionEntity a\ent,gx,gy,gz
End Function