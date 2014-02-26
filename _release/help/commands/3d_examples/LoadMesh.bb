; LoadMesh Example
; ----------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()

light=CreateLight()
RotateEntity light,90,0,0

; Load mesh
drum=LoadMesh("media/oil-drum/oildrum.3ds")

PositionEntity drum,0,0,MeshDepth(drum)*2

While Not KeyDown( 1 )
	RenderWorld
	Flip
Wend

End