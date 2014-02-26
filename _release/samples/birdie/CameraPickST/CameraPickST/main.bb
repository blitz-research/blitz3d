; CameraPick/ pickedsurface()/ pickedtriange() Demo
; dave@birdie72.freeserve.co.uk
; http://www.birdie72.freeserve.co.uk
;

Graphics3D 640,480
SetBuffer BackBuffer()
piv=CreatePivot()
cam=CreateCamera(piv)
PositionEntity cam,0,0,-10

lit=CreateLight()
TurnEntity lit,25,55,0

sp=CreateSphere(10)
EntityPickMode sp,2
EntityFX sp,2
ScaleMesh sp,4,3,3

;Random paint of vertices
Color_Mesh(sp)

sp2=CreateSphere(10)
PositionEntity sp2,0,4,0
EntityPickMode sp2,2
EntityFX sp2,2
ScaleMesh sp2,2,1,2

;Random paint of vertices
Color_Mesh(sp2)

While Not KeyDown(1)
	TurnEntity sp,0,.2,0
	TurnEntity sp2,0,-.2,0
	ent=CameraPick(cam,MouseX(),MouseY())
	If KeyDown(203) TurnEntity piv,0,1,0
	If KeyDown(205) TurnEntity piv,0,-1,0
	If KeyDown(200) TurnEntity piv,1,0,0
	If KeyDown(208) TurnEntity piv,-1,0,0

	If KeyDown(44) MoveEntity cam,0,0,-.2
	If KeyDown(30) MoveEntity cam,0,0,.2
	If KeyHit(2) Then wire=1-wire
	WireFrame wire
	UpdateWorld
	RenderWorld
	If ent<>0 Then
		surf=PickedSurface()
		tria=PickedTriangle()
		If MouseDown(1) Then
			v0=TriangleVertex(surf,tria,0)
			v1=TriangleVertex(surf,tria,1)
			v2=TriangleVertex(surf,tria,2)
			x0#=VertexX(surf,v0)+(VertexNX(surf,v0)*.005)
			x1#=VertexX(surf,v1)+(VertexNX(surf,v1)*.005)
			x2#=VertexX(surf,v2)+(VertexNX(surf,v2)*.005)
			y0#=VertexY(surf,v0)+(VertexNY(surf,v0)*.005)
			y1#=VertexY(surf,v1)+(VertexNY(surf,v1)*.005)
			y2#=VertexY(surf,v2)+(VertexNY(surf,v2)*.005)
			z0#=VertexZ(surf,v0)+(VertexNZ(surf,v0)*.005)
			z1#=VertexZ(surf,v1)+(VertexNZ(surf,v1)*.005)
			z2#=VertexZ(surf,v2)+(VertexNZ(surf,v2)*.005)
			VertexCoords surf,v0,x0,y0,z0
			VertexCoords surf,v1,x1,y1,z1
			VertexCoords surf,v2,x2,y2,z2
		End If
		If MouseHit(2)
			v0=TriangleVertex(surf,tria,0)
			v1=TriangleVertex(surf,tria,1)
			v2=TriangleVertex(surf,tria,2)
			VertexColor surf,v0,Rnd(255),Rnd(255),Rnd(255)
			VertexColor surf,v1,Rnd(255),Rnd(255),Rnd(255)
			VertexColor surf,v2,Rnd(255),Rnd(255),Rnd(255)
		End If
	End If
	Plot MouseX(),MouseY()
	Text 0,0,"Click left mouse on an entity to swell it at that point"
	Text 0,15,"Click right mouse on an entity to paint the vertices of that triangle"
	Text 0,30,"Press 1 to toggle wireframe"
	Text 0,45,"Cursors to rotate camera"
	Text GraphicsWidth()/2,GraphicsHeight()-32,"Ent:"+ent+" Surf:"+surf+" Triangle:"+tria,1,0
	Flip
Wend

FreeEntity sp2
FreeEntity sp
EndGraphics
End

;Random paint of vertices
Function Color_Mesh(mesh)
	For s=1 To CountSurfaces(mesh)
		surf=GetSurface(mesh,s)
		For a=0 To CountVertices(surf)-1
			VertexColor surf,a,Rnd(255),Rnd(255),Rnd(255)
		Next
	Next
End Function