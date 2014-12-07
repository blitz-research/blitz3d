; CopyEntity Example
; This example creates an entity and
; allows you to make copies of it.

Graphics3D 640,480
AppTitle "CopyEntity Example"

Cam = CreateCamera()
Lit = CreateLight()

PositionEntity Lit,-5,-5,0  
PositionEntity Cam,0,0,-5   

AnEntity = CreateCube()    ; This is our Test Entity
ScaleMesh anEntity,0.4,0.4,0.4
While Not KeyDown(1) ; Until we press ESC

If KeyHit(57) Then 

; When we hit Space, a new Entity is created
; These share the same internal mesh structure though!
; Hence although we are only Rotating the original MESH
; Linked to the original Entity, since it is a Mesh command,
; all the Entity Copies are linked to it..

NewEntity = CopyEntity(AnEntity) ; Hit Space to Copy!

; Change the Color of the Entity. Since this is an entity
; Property, it doesn't effect the other copies.
EntityColor NewEntity,Rand(255),Rand(255),Rand(255)
PositionEntity NewEntity,Rand(4)-2,Rand(4)-2,0
EndIf

SeedRnd MilliSecs()

RotateMesh AnEntity,.25,.35,.45

RenderWorld ; Draw the Scene
Flip ; Flip it into View
Wend

End