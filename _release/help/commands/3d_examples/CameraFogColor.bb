; CameraFogColor Example
; ----------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,0,1,0

light=CreateLight()
RotateEntity light,90,0,0

plane=CreatePlane()
grass_tex=LoadTexture( "media/mossyground.bmp" )
EntityTexture plane,grass_tex

; Set camera fog to 1 (linear fog)
CameraFogMode camera,1

; Set camera fog range
CameraFogRange camera,1,10

; Set initial fog colour values
red#=0
green#=0
blue#=0

While Not KeyDown( 1 )

; Change red, green, blue values depending on key pressed
If KeyDown( 2 )=True And red#>0 Then red#=red#-1
If KeyDown( 3 )=True And red#<255 Then red#=red#+1
If KeyDown( 4 )=True And green#>0 Then green#=green#-1
If KeyDown( 5 )=True And green#<255 Then green#=green#+1
If KeyDown( 6 )=True And blue#>0 Then blue#=blue#-1
If KeyDown( 7 )=True And blue#<255 Then blue#=blue#+1

; Set camera fog color using red, green, blue values
CameraFogColor camera,red#,green#,blue#

If KeyDown( 205 )=True Then TurnEntity camera,0,-1,0
If KeyDown( 203 )=True Then TurnEntity camera,0,1,0
If KeyDown( 208 )=True Then MoveEntity camera,0,0,-0.05
If KeyDown( 200 )=True Then MoveEntity camera,0,0,0.05

RenderWorld

Text 0,0,"Use cursor keys to move about the infinite plane"
Text 0,20,"Press keys 1-6 to change CameraFogColor red#,green#,blue# values
Text 0,40,"Fog Red: "+red#
Text 0,60,"Fog Green: "+green#
Text 0,80,"Fog Blue: "+blue#

Flip

Wend

End