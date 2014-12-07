; CameraViewport Example
; ----------------------

Graphics3D 640,480
SetBuffer BackBuffer()

; Create first camera
cam1=CreateCamera()

; Set the first camera's viewport so that it fills the top half of the  camera
CameraViewport cam1,0,0,GraphicsWidth(),GraphicsHeight()/2

; Create second camera
cam2=CreateCamera()

; Set the second camera's viewport so that it fills the bottom half of the  camera
CameraViewport cam2,0,GraphicsHeight()/2,GraphicsWidth(),GraphicsHeight()/2

light=CreateLight()
RotateEntity light,90,0,0

plane=CreatePlane()
grass_tex=LoadTexture( "media/mossyground.bmp" )
EntityTexture plane,grass_tex
PositionEntity plane,0,-1,0

While Not KeyDown( 1 )

If KeyDown( 205 )=True Then TurnEntity cam1,0,-1,0
If KeyDown( 203 )=True Then TurnEntity cam1,0,1,0
If KeyDown( 208 )=True Then MoveEntity cam1,0,0,-0.05
If KeyDown( 200 )=True Then MoveEntity cam1,0,0,0.05

RenderWorld

Text 0,0,"Use cursor keys to move the first camera about the infinite plane"

Flip

Wend

End