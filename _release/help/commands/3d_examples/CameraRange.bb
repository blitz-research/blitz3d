; CameraRange Example
; -------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,0,1,0

light=CreateLight()
RotateEntity light,90,0,0

plane=CreatePlane()
grass_tex=LoadTexture("media/mossyground.bmp")
EntityTexture plane,grass_tex

; Set intial camera range value
cam_range=10

While Not KeyDown( 1 )

; If square brackets keys pressed then change camera range value
If KeyDown(26)=True Then cam_range=cam_range-1
If KeyDown(27)=True Then cam_range=cam_range+1

; Set camera range
CameraRange camera,1,cam_range

If KeyDown(205)=True Then TurnEntity camera,0,-1,0
If KeyDown(203)=True Then TurnEntity camera,0,1,0
If KeyDown(208)=True Then MoveEntity camera,0,0,-0.05
If KeyDown(200)=True Then MoveEntity camera,0,0,0.05

RenderWorld

Text 0,0,"Use cursor keys to move about the infinite plane"
Text 0,20,"Press [ or ] to change CameraRange value"
Text 0,40,"CameraRange camera,1,"+cam_range

Flip

Wend

End