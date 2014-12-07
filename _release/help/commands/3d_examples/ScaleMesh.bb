; ScaleMesh Example
; -----------------
 
; In this example we will demonstrate the use of the ScaleMesh command.

; Unlike ScaleEntity, ScaleMesh actually modifies the actual mesh structure.

; So whereas using ScaleEntity 2,2,2 would only double the size of an entity the first time it was 
; used, ScaleMesh 2,2,2 will double the size of the mesh every time it is used.

; This is because ScaleEntity scales an entity based on a fixed mesh structure, whereas ScaleMesh
; actually modifies the mesh structure itself.

Graphics3D 640,480
SetBuffer BackBuffer()
 
camera=CreateCamera()
 
light=CreateLight()
 
; Create cube mesh
cube=CreateCube()

; Position cube in front of camera so we can see it 
PositionEntity cube,0,0,5
 
While Not KeyDown(1)

	; If space bar pressed then scale cube mesh by 1%. Also set syntax$ text.
	If KeyHit(57)=True Then ScaleMesh cube,1.01,1.01,1.01 : syntax$="ScaleMesh 1.01,1.01,1.01"

	RenderWorld
	
	Text 0,0,"Press space to scale mesh by 1%"
	Text 0,20,syntax$
	
	Flip

Wend
 
End