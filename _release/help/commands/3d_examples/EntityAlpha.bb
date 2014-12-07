; EntityAlpha Example
; -------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()

light=CreateLight()
RotateEntity light,90,0,0

cube=CreateCube()
Back=CreateCube()

PositionEntity cube,0,0,5
PositionEntity back,0,0,15
ScaleEntity Back,10,2,1
EntityColor back,255,0,0
; Set initial entity color values
Alpha#=1

While Not KeyDown( 1 )

; Change alpha value depending on key pressed
If alpha#<0.01 Then alpha# = 0
If alpha#>1 Then alpha# = 1
If KeyDown( 2 )=True And Alpha#>0 Then Alpha#=Alpha#-0.01
If KeyDown( 3 )=True And alpha#<1 Then Alpha#=Alpha#+0.01
; Set entity alpha value
EntityAlpha cube,Alpha#


TurnEntity cube,0.1,0.1,0.1
TurnEntity back,1,0,0

RenderWorld

Text 0,0,"Press keys 1-2 to change EntityAlpha"
Text 0,20,"Entity Alpha: "+Alpha

Flip

Wend

End