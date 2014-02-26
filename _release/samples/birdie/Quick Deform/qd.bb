;Quick deform
;David Bird
;dave@birdie72.freeserve.co.uk
;15/6/01 (beta 2.24)
Graphics3D 640,480
SetBuffer BackBuffer()

piv=CreatePivot()

cam=CreateCamera(piv)
PositionEntity cam,0,0,-4
lit=CreateLight()

Global ox#,oy#,oz#,cnt#
mesh=CreateSphere(25)
tex=LoadTexture("bl3d_big.jpg")
ScaleTexture tex,.2,.2

EntityTexture mesh,tex
;WireFrame True

While Not KeyDown(1)
	TurnEntity mesh,1,1.5,0
	If KeyDown(2) Then Wave_Effect(mesh,.01,-1,2)
	If KeyDown(3) Then Wave_Effect(mesh,.01,1,2)
	
	If KeyHit(4) Then Add_Flats(mesh,.75,.75,.75,0,0)
	UpdateWorld
	RenderWorld
	Text 0,0,"1- wave effect +"
	Text 0,15,"2- wave effect -"
	Text 0,30,"3- Add Flats"
	Text 0,45,"dave@birdie72.freeserve.co.uk"
	Flip
Wend

FreeEntity mesh
FreeEntity lit
FreeEntity cam
FreeEntity piv
EndGraphics
End

Function Wave_Effect(mesh,ampl#,dir,act)
	surf=GetSurface(mesh,CountSurfaces(mesh))
	cnt=CountVertices(surf)
	For a=0 To cnt-1
		x#=VertexX(surf,a)
		y#=VertexY(surf,a)
		z#=VertexZ(surf,a)
		ang#=(180*y)
		Select act
			Case 0 ;	x axis
				x=x+(ampl*Cos(ang)*dir)
			Case 1 ;	z axis
				z=z+(ampl*Cos(ang)*dir)
			Case 2
				y=y+(ampl*Cos(ang)*dir)
		End Select
		VertexCoords surf,a,x,y,z
	Next
End Function

Function Add_Flats(mesh,width#,height#,depth#,ang1,ang2)
	RotateMesh mesh,ang1,ang2,0
	surf=GetSurface(mesh,CountSurfaces(mesh))
	cnt=CountVertices(surf)
	For a=0 To cnt-1
		x#=VertexX(surf,a)
		y#=VertexY(surf,a)
		z#=VertexZ(surf,a)
		If x>width Then x=width
		If x<-width Then x=-width

		If z>depth Then z=depth
		If z<-depth Then z=-depth

		If y>height Then y=height
		If y<-height Then y=-height

		VertexCoords surf,a,x,y,z
	Next
	RotateMesh mesh,-ang1,-ang2,0
End Function