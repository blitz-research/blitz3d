; ProjectedZ Example
; ------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,0,2,-10

light=CreateLight()
RotateEntity light,90,0,0

plane=CreatePlane()
ground_tex=LoadTexture("media/Chorme-2.bmp")
EntityTexture plane,ground_tex

cube=CreateCube()
cube_tex=LoadTexture("media/b3dlogo.jpg")
EntityTexture cube,cube_tex
PositionEntity cube,0,1,0

While Not KeyDown( 1 )

If KeyDown( 205 )=True Then TurnEntity camera,0,-1,0
If KeyDown( 203 )=True Then TurnEntity camera,0,1,0
If KeyDown( 208 )=True Then MoveEntity camera,0,0,-0.05
If KeyDown( 200 )=True Then MoveEntity camera,0,0,0.05

; Use camera project to get 2D coordinates from 3D coordinates of cube
CameraProject(camera,EntityX(cube),EntityY(cube),EntityZ(cube))

RenderWorld

; If cube is in view then draw text, if not then draw nothing otherwise text  will be drawn at 0,0
If EntityInView(cube,camera)=True

; Use ProjectedX() and ProjectedY() to get 2D coordinates from when CameraProject  was used.
; Use these coordinates to draw text at a 2D position, on top of a 3D scene.
Text ProjectedX#(),ProjectedY#(),"Cube" 

EndIf

Text 0,0,"Use cursor keys to move about"
Text 0,20,"ProjectedX: "+ProjectedX#()
Text 0,40,"ProjectedY: "+ProjectedY#()
Text 0,60,"ProjectedZ: "+ProjectedZ#()
Text 0,80,"EntityInView: "+EntityInView(cube,camera)

Flip

Wend

End