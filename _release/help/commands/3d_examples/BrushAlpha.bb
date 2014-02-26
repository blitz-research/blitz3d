; BrushAlpha Example
; ------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
;PositionEntity camera,0,20,5
;RotateEntity camera,90,0,0

light=CreateLight()
RotateEntity light,90,0,0

cube=CreateCube()
PositionEntity cube,0,0,8

cube2=CreateCube()
PositionEntity cube2,6,0,8

; Create brush
brush=CreateBrush()

; Set brush color
BrushAlpha brush,0.5

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
	
	MoveEntity cube2,0,0,0.1
	TurnEntity cube2,0,1,0
	
	RenderWorld
	Flip

Wend

End