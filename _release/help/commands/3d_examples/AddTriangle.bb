; AddTriangle Example
; ----------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera() : PositionEntity camera,0,0,-5
light=CreateLight()

mesh=CreateMesh() : DebugLog mesh
surface=CreateSurface(mesh) : DebugLog surface
v0=AddVertex(surface,-1,0,0) : DebugLog v0
v1=AddVertex(surface,0,2,0) : DebugLog v1
v2=AddVertex(surface,1,0,0) : DebugLog v2

t0=AddTriangle(surface,v0,v1,v2) : DebugLog t0

While Not KeyDown( 1 )
	
	RenderWorld
	Flip

Wend

End