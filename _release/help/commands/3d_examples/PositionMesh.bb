; PositionMesh Example
; --------------------
 
; In this example we will demonstrate the use of the PositionMesh command.

; Unlike PositionEntity, PositionMesh actually modifies the actual mesh structure.

; So whereas using PositionEntity 0,0,1 would only move an entity by one unit the first time it was 
; used, PositionMesh 0,0,1 will move the mesh by one unit every time it is used.

; This is because PositionEntity positions an entity based on a fixed mesh structure, whereas 
; PositionMesh actually modifies the mesh structure itself.

Graphics3D 640,480
SetBuffer BackBuffer()
 
camera=CreateCamera()
 
light=CreateLight()
 
; Create cube mesh
cube=CreateCube()

; Position cube in front of camera so we can see it 
PositionEntity cube,0,0,5
 
While Not KeyDown(1)

	; If space bar pressed then position mesh 1 unit along the z axis. Also set syntax$ text.
	If KeyHit(57)=True Then PositionMesh cube,0,0,1 : syntax$="PositionMesh 0,0,1"

	RenderWorld
	
	Text 0,0,"Press space to position the mesh 1 unit along the z axis"
	Text 0,20,syntax$
	
	Flip

Wend
 
End