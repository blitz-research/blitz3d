; PositionEntity Example
; ----------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
light=CreateLight()

cone=CreateCone( 32 )

; Set position values so that cone is positioned in front of camera, so we  can see it to begin with
x#=0
y#=0
z#=10

While Not KeyDown( 1 )

; Change position values depending on key pressed
If KeyDown( 203 )=True Then x#=x#-0.1
If KeyDown( 205 )=True Then x#=x#+0.1
If KeyDown( 208 )=True Then y#=y#-0.1
If KeyDown( 200 )=True Then y#=y#+0.1
If KeyDown( 44 )=True Then z#=z#-0.1
If KeyDown( 30 )=True Then z#=z#+0.1

; Position cone using position values
PositionEntity cone,x#,y#,z#

RenderWorld

Text 0,0,"Use cursor/A/Z keys to change cone position"
Text 0,20,"X Position: "+x#
Text 0,40,"Y Position: "+y# 
Text 0,60,"Z Position: "+z#

Flip

Wend

End