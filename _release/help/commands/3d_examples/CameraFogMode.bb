; CameraFogMode Example
; ---------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,0,1,0
CameraFogRange camera,1,10

light=CreateLight()
RotateEntity light,90,0,0

plane=CreatePlane()
grass_tex=LoadTexture( "media/mossyground.bmp" )
EntityTexture plane,grass_tex

While Not KeyDown( 1 )

; Toggle camera fog mode between 0 and 1 when spacebar is pressed
If KeyHit( 57 )=True Then fog_mode=1-fog_mode : CameraFogMode camera,fog_mode

If KeyDown( 205 )=True Then TurnEntity camera,0,-1,0
If KeyDown( 203 )=True Then TurnEntity camera,0,1,0
If KeyDown( 208 )=True Then MoveEntity camera,0,0,-0.05
If KeyDown( 200 )=True Then MoveEntity camera,0,0,0.05

RenderWorld

Text 0,0,"Use cursor keys to move about the infinite plane"
Text 0,20,"Press spacebar to toggle between CameraFogMode 0/1"
If fog_mode=False Then Text 0,40,"CameraFogMode 0" Else Text 0,40,"CameraFogMode  1"

Flip

Wend

End