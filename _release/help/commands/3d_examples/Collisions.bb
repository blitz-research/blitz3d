; Collisions Example
; ------------------

Graphics3D 640,480
SetBuffer BackBuffer()

; Set collision type values
type_ground=1
type_character=2
type_scenery=3

camera=CreateCamera()
RotateEntity camera,45,0,0
PositionEntity camera,0,15,-10

light=CreateLight()
RotateEntity light,45,0,0

; Create cube 'ground'
cube=CreateCube()
ScaleEntity cube,10,10,10
EntityColor cube,0,127,0
EntityType cube,type_ground
PositionEntity cube,0,-5,0

; Create sphere 'character'
sphere=CreateSphere( 32 )
EntityColor sphere,127,0,0
EntityRadius sphere,1
EntityType sphere,type_character
PositionEntity sphere,0,7,0

; Enable collisions between type_character and type_ground
Collisions type_character,type_ground,2,2

; Create cylinder 'scenery'
cylinder=CreateCylinder( 32 )
ScaleEntity cylinder,2,2,2
EntityColor cylinder,0,0,255
EntityRadius cylinder,2
EntityBox cylinder,-2,-2,-2,4,4,4
EntityType cylinder,type_scenery
PositionEntity cylinder,-4,7,-4

; Create cone 'scenery'
cone=CreateCone( 32 )
ScaleEntity cone,2,2,2
EntityColor cone,0,0,255
EntityRadius cone,2
EntityBox cone,-2,-2,-2,4,4,4
EntityType cone,type_scenery
PositionEntity cone,4,7,-4

; Create prism 'scenery'
prism=CreateCylinder( 3 )
ScaleEntity prism,2,2,2
EntityColor prism,0,0,255
EntityRadius prism,2
EntityBox prism,-2,-2,-2,4,4,4
EntityType prism,type_scenery
PositionEntity prism,-4,7,4
RotateEntity prism,0,180,0

; Create pyramid 'scenery'
pyramid=CreateCone( 4 )
ScaleEntity pyramid,2,2,2
EntityColor pyramid,0,0,255
EntityRadius pyramid,2
EntityBox pyramid,-2,-2,-2,4,4,4
EntityType pyramid,type_scenery
RotateEntity pyramid,0,45,0
PositionEntity pyramid,4,7,4

; Set collision method and response values
method=2
response=2

method_info$="ellipsoid-to-polygon"
response_info$="slide1"

While Not KeyDown( 1 )

x#=0
y#=0
z#=0

If KeyDown( 203 )=True Then x#=-0.1
If KeyDown( 205 )=True Then x#=0.1
If KeyDown( 208 )=True Then z#=-0.1
If KeyDown( 200 )=True Then z#=0.1

MoveEntity sphere,x#,y#,z#
MoveEntity sphere,0,-0.02,0 ; gravity

; Change collision method
If KeyHit( 50 )=True
method=method+1
If method=4 Then method=1
If method=1 Then method_info$="ellipsoid-to-sphere"
If method=2 Then method_info$="ellipsoid-to-polygon"
If method=3 Then method_info$="ellipsoid-to-box"
EndIf

; Change collision response
If KeyHit( 19 )=True
response=response+1
If response=4 Then response=1
If response=1 Then response_info$="stop"
If response=2 Then response_info$="slide1"
If response=3 Then response_info$="slide2"
EndIf

; Enable collisions between type_character and type_scenery
Collisions type_character,type_scenery,method,response

; Perform collision checking
UpdateWorld

RenderWorld

Text 0,0,"Use cursor keys to move sphere"
Text 0,20,"Press M to change collision Method (currently: "+method_info$+")"
Text 0,40,"Press R to change collision Response (currently: "+response_info$+")"
Text 0,60,"Collisions type_character,type_scenery,"+method+","+response

Flip

Wend

End