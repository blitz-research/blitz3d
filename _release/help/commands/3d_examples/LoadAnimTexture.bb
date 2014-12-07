; LoadAnimTexture Example
; -----------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()

light=CreateLight()
RotateEntity light,90,0,0

cube=CreateCube()
PositionEntity cube,0,0,5

; Load anim texture
anim_tex=LoadAnimTexture( "media/boomstrip.bmp",49,64,64,0,39 )

While Not KeyDown( 1 )

; Cycle through anim frame values. 100 represents delay, 39 represents no. of  anim frames
frame=MilliSecs()/100 Mod 39

; Texture cube with anim texture frame
EntityTexture cube,anim_tex,frame

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