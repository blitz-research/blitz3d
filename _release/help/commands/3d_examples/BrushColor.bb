; BrushColor Example
; ------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()

light=CreateLight()
RotateEntity light,90,0,0

cube=CreateCube()
PositionEntity cube,0,0,5

; Create brush
brush=CreateBrush()

; Set brush color
BrushColor brush,0,0,255

; Paint mesh with brush
PaintMesh cube,brush

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

TurnEntity cube,pitch#,yaw#,roll#

RenderWorld
Flip

Wend

End