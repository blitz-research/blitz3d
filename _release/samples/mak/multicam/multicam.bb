
Graphics3D 640,480

tex=CreateTexture( 64,64 )
SetBuffer TextureBuffer( tex )
Color 255,0,0:Rect 0,0,32,32:Rect 32,32,32,32
Color 255,128,0:Rect 32,0,32,32:Rect 0,32,32,32
SetBuffer BackBuffer()
Color 255,255,255

cone=CreateCone(20)
EntityTexture cone,tex

sphere=CreateSphere(10)
PositionEntity sphere,2,0,0
EntityTexture sphere,tex

cylinder=CreateCylinder(20)
PositionEntity cylinder,-2,0,0
EntityTexture cylinder,tex

light=CreateLight()
TurnEntity light,45,45,0

pivot=CreatePivot()

z_cam=CreateCamera( pivot )
CameraViewport z_cam,0,0,320,240
PositionEntity z_cam,0,0,-5

y_cam=CreateCamera( pivot )
CameraViewport y_cam,320,0,320,240
PositionEntity y_cam,0,5,0
TurnEntity y_cam,90,0,0

x_cam=CreateCamera( pivot )
CameraViewport x_cam,0,240,320,240
TurnEntity x_cam,0,-90,0
PositionEntity x_cam,-5,0,0

While Not KeyHit(1)

	If KeyDown(203) MoveEntity pivot,-.1,0,0
	If KeyDown(205) MoveEntity pivot,.1,0,0
	If KeyDown(200) MoveEntity pivot,0,.1,0
	If KeyDown(208) MoveEntity pivot,0,-.1,0
	If KeyDown(30) MoveEntity pivot,0,0,.1
	If KeyDown(44) MoveEntity pivot,0,0,-.1

	UpdateWorld
	RenderWorld
	Text 0,0,"Front"
	Text 320,0,"Top"
	Text 0,240,"Left"
	Flip

Wend

End