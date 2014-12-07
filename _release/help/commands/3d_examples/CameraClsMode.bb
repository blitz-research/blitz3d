; CameraClsMode Example
; ---------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,0,1,-10

light=CreateLight()
RotateEntity light,90,0,0

plane=CreatePlane()
ground_tex=LoadTexture("media/MossyGround.bmp")
EntityTexture plane,ground_tex

cube=CreateCube()
PositionEntity cube,0,1,0

; Load 2D background image
background=LoadImage("media/sky.bmp")

; Use red ink color so we can see text on top of black or light blue background
Color 255,0,0

While Not KeyDown(1)

If KeyDown(205)=True Then TurnEntity camera,0,-1,0
If KeyDown(203)=True Then TurnEntity camera,0,1,0
If KeyDown(208)=True Then MoveEntity camera,0,0,-0.05
If KeyDown(200)=True Then MoveEntity camera,0,0,0.05

; Toggle cls_color value between 0 and 1 when spacebar is pressed
If KeyHit(57)=True Then cls_color=1-cls_color

; Set the camera clear mode, using the cls_color value
CameraClsMode camera,cls_color,1

; Draw a 2D background. When cls_color is set to 0, the 2D graphics will show  behind the 3D graphics.
TileBlock background,0,0 

RenderWorld

Text 0,0,"Use cursor keys to move about"
Text 0,20,"Press space bar to enable/disable colour clearing"
Text 0,40,"CameraClsMode camera,"+cls_color+","+1

Flip

Wend

End