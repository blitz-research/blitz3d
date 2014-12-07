; EntityRadius Example
; --------------------

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

; Set sphere radius value
sphere_radius#=1

; Set sphere and cone entity types
EntityType sphere,type_sphere
EntityType cone,type_cone

; Enable collisions between type_sphere and type_cone, with ellipsoid->polygon method and slide response
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

; If square brackets keys pressed then change sphere radius value
If KeyDown( 26 )=True Then sphere_radius#=sphere_radius#-0.1
If KeyDown( 27 )=True Then sphere_radius#=sphere_radius#+0.1

; Set entity radius of sphere
EntityRadius sphere,sphere_radius#

; Perform collision checking
UpdateWorld

RenderWorld

Text 0,0,"Use cursor/A/Z keys to move sphere"
Text 0,20,"Press [ or ] to change EntityRadius radius_x# value"
Text 0,40,"EntityRadius sphere,"+sphere_radius

Flip

Wend

End