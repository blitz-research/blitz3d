; EntityPitch Example
; -------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
light=CreateLight()

cone=CreateCone( 32 )
PositionEntity cone,0,0,5

While Not KeyDown( 1 )

pitch#=0
yaw#=0
roll#=0

If KeyDown( 208 )=True Then pitch#=-1
If KeyDown( 200 )=True Then pitch#=1
If KeyDown( 203 )=True Then yaw#=-1
If KeyDown( 205 )=True Then yaw#=1
If KeyDown( 45 )=True Then roll#=-1
If KeyDown( 44 )=True Then roll#=1

TurnEntity cone,pitch#,yaw#,roll#

RenderWorld

Text 0,0,"Use cursor/Z/X keys to turn cone"

; Return entity pitch angle of cone
Text 0,20,"Pitch: "+EntityPitch#( cone )

Flip

Wend

End