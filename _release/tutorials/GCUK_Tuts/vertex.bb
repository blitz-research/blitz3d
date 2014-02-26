; Vertex Control ***
; By Paul Gerfen (www.gamecoding.co.uk)

Graphics3D 800,600

SetBuffer BackBuffer()

camera=CreateCamera()
CameraViewport camera,0,0,800,600

light=CreateLight()

plane=LoadMesh("plane.3ds")
PositionEntity plane,0,0,25
EntityFX plane,2

surface=GetSurface(plane,CountSurfaces(plane))

VertexColor surface,0,255,0,0
VertexColor surface,1,0,255,0
VertexColor surface,2,0,0,255
VertexColor surface,3,255,0,255

While Not KeyHit(1)

	TurnEntity plane,0,0,.3
	
	counter=counter+1
	
	If counter<500 Then 
	x1#=-.01
	y1#=-.01
	x2#=+.01
	EndIf
	
	If counter>499 Then
	x1#=+.01
	y1#=+.01
	x2#=-.01
	EndIf
	
	xx#=VertexX(surface,0)
	yy#=VertexY(surface,0)
	zz#=VertexZ(surface,0)

	VertexCoords surface,0,xx+x1,yy+y1,zz
	
	xx#=VertexX(surface,2)
	yy#=VertexY(surface,2)
	zz#=VertexZ(surface,2)

	VertexCoords surface,2,xx+x2,yy+y1,zz

	If counter=1000 Then
	counter=0
	VertexColor surface,0,Rnd#(0,255),Rnd#(0,255),Rnd#(0,255)
	VertexColor surface,1,Rnd#(0,255),Rnd#(0,255),Rnd#(0,255)
	VertexColor surface,2,Rnd#(0,255),Rnd#(0,255),Rnd#(0,255)
	VertexColor surface,3,Rnd#(0,255),Rnd#(0,255),Rnd#(0,255)
	EndIf

	UpdateWorld
	RenderWorld
	
	Text 350,500,"Vertex Control"
	
	Flip

Wend
End