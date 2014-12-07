; ClearCollisions Example
; -----------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
light=CreateLight()

sphere=CreateSphere( 32 )
PositionEntity sphere,-2,0,5

cone=CreateCone( 32 )
EntityType cone,type_cone
PositionEntity cone,2,0,5

; Set collision type values
type_sphere=1
type_cone=2

; Set up sphere collision data
EntityRadius sphere,1
EntityType sphere,type_sphere

; Set up cone collision data
EntityType cone,type_cone

; Enable collisions between type_sphere and type_cone, with sphere->polygon  method and slide response
Collisions type_sphere,type_cone,2,2

While Not KeyDown( 1 )

x#=0
y#=0
z#=0

If KeyDown( 203 )=True Then x#=-0.1
If KeyDown( 205 )=True Then x#=0.1
If KeyDown( 208 )=True Then y#=-0.1
If KeyDown( 200 )=True Then y#=0.1
If KeyDown( 44 )=True Then z#=-0.1
If KeyDown( 30 )=True Then z#=0.1

MoveEntity sphere,x#,y#,z#

; If spacebar pressed then clear collisions 
If KeyHit( 57 )=True Then ClearCollisions

; Perform collision checking
UpdateWorld

RenderWorld

Text 0,0,"Use cursor/A/Z keys to move sphere"
Text 0,20,"Press spacebar to use ClearCollisions command"

Flip

Wend

End