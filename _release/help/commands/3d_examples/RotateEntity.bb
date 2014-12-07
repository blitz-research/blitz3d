; RotateEntity Example
; --------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
light=CreateLight()

cone=CreateCone( 32 )
PositionEntity cone,0,0,5

While Not KeyDown( 1 )

; Change rotation values depending on the key pressed
If KeyDown( 208 )=True Then pitch#=pitch#-1
If KeyDown( 200 )=True Then pitch#=pitch#+1
If KeyDown( 203 )=True Then yaw#=yaw#-1
If KeyDown( 205 )=True Then yaw#=yaw#+1
If KeyDown( 45 )=True Then roll#=roll#-1
If KeyDown( 44 )=True Then roll#=roll#+1

; Rotate cone using rotation values
RotateEntity cone,pitch#,yaw#,roll#

RenderWorld

Text 0,0,"Use cursor/Z/X keys to change cone rotation"
Text 0,20,"Pitch: "+pitch#
Text 0,40,"Yaw : "+yaw# 
Text 0,60,"Roll : "+roll#

Flip

Wend

End