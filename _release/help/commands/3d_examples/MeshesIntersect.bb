; MeshesIntersect Example
; -----------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()

light=CreateLight()
RotateEntity light,90,0,0

drum=LoadMesh("media/oil-drum/oildrum.3ds")
PositionEntity drum,-20,0,100

crate=LoadMesh("media/wood-crate/wcrate1.3ds")
PositionEntity crate,20,0,100

While Not KeyDown( 1 )

TurnEntity drum,1,1,1
TurnEntity crate,-1,-1,-1

RenderWorld

; Test to see if drum and crate meshes are intersecting; if so then display  message to confirm this
If MeshesIntersect(drum,crate)=True Then Text 0,0,"Meshes are intersecting!"

Flip

Wend

End