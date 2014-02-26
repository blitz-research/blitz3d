;Spherical landscapes
;David Bird
;dave@birdie72.freeserve.co.uk

Graphics3D 640,480
SetBuffer BackBuffer()
Include "functions.bb"
piv=CreatePivot()
cam=CreateCamera(piv)

CameraRange cam,.1,40
PositionEntity cam,0,0,-2
tex=LoadTexture("tex0.bmp")
lit=CreateLight()

mesh=CreateSphere(80);LoadMesh("geosphere.3ds")
ScaleEntity mesh,1,1,1
ScaleTexture tex,0.2,0.2

EntityTexture mesh,tex

WireFrame True

While Not KeyDown(1)
	TurnEntity mesh,0,0.1,0
	If KeyHit(57) Then ok=do_Landscaping(mesh,1,.02)


	If KeyDown(203) TurnEntity piv,0,1,0
	If KeyDown(205) TurnEntity piv,0,-1,0
	If KeyDown(200) TurnEntity piv,1,0,0
	If KeyDown(208) TurnEntity piv,-1,0,0

	If KeyDown(44) MoveEntity cam,0,0,-0.1
	If KeyDown(30) MoveEntity cam,0,0,0.1
	UpdateWorld
	RenderWorld
	Text 320,0,ok,1,0
	Text 0,0,"Hit <space> to terraform..."
	Flip
Wend

FreeEntity lit
FreeEntity cam
FreeEntity piv
EndGraphics
End

Function do_Landscaping(mesh,iterate,scale#)
	surf=GetSurface(mesh,CountSurfaces(mesh))
	n.vector=New vector
	p.vector=New vector
	v.vector=New vector
	For a=0 To iterate
		e#=Rnd(-scale,scale)
		Random_Vector(n)	;Create a random vector
		For b=0 To CountVertices(surf)-1
			v\x=VertexX(surf,b)
			v\y=VertexY(surf,b)
			v\z=VertexZ(surf,b)
			prod#=Vect_Product(n,v)
			If prod>0 Then
				VertexCoords surf,b,VertexX(surf,b)+(VertexNX(surf,b)*e),VertexY(surf,b)+(VertexNY(surf,b)*e),VertexZ(surf,b)+(VertexNZ(surf,b)*e)
			Else
				VertexCoords surf,b,VertexX(surf,b)-(VertexNX(surf,b)*e),VertexY(surf,b)-(VertexNY(surf,b)*e),VertexZ(surf,b)-(VertexNZ(surf,b)*e)
			End If		
		Next
		fst=False
	Next
	Delete n
	Delete p
	Delete v
	Return vcnt
End Function

Function Random_Vector(v.vector)
	v\x=Rnd(-1,1)
	v\y=Rnd(-1,1)
	v\z=Rnd(-1,1)
	Normalise(v)
End Function