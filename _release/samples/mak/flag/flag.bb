
Global info1$="Flag demo"
Global info2$="Features mesh deformation"

Include "../start.bb"

Const segs=128,width#=4,depth#=.125

mesh=CreateMesh()
surf=CreateSurface( mesh )

For k=0 To segs
	x#=Float(k)*width/segs-width/2
	u#=Float(k)/segs
	AddVertex surf,x,1,0,u,0
	AddVertex surf,x,-1,0,u,1
Next

For k=0 To segs-1
	AddTriangle surf,k*2,k*2+2,k*2+3
	AddTriangle surf,k*2,k*2+3,k*2+1
Next

b=LoadBrush( "b3dlogo.jpg" )
PaintSurface surf,b

camera=CreateCamera()
PositionEntity camera,0,0,-5

light=CreateLight()
TurnEntity light,45,45,0

While Not KeyHit(1)

	ph#=MilliSecs()/4
	cnt=CountVertices(surf)-1
	For k=0 To cnt
		x#=VertexX(surf,k)
		y#=VertexY(surf,k)
		z#=Sin(ph+x*300)*depth
		VertexCoords surf,k,x,y,z
	Next
	UpdateNormals mesh
	
	If KeyDown(26) TurnEntity camera,0,1,0
	If KeyDown(27) TurnEntity camera,0,-1,0
	If KeyDown(30) MoveEntity camera,0,0,.1
	If KeyDown(44) MoveEntity camera,0,0,-.1
	
	If KeyDown(203) TurnEntity mesh,0,1,0,True
	If KeyDown(205) TurnEntity mesh,0,-1,0,True
	If KeyDown(200) TurnEntity mesh,1,0,0,True
	If KeyDown(208) TurnEntity mesh,-1,0,0,True
	
	UpdateWorld
	RenderWorld
	Flip
Wend
End