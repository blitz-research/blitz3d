; CreateTexture Example
; ---------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()

light=CreateLight()
RotateEntity light,90,0,0

cube=CreateCube()
PositionEntity cube,0,0,5

; Create texture of size 256x256
tex=CreateTexture( 256,256 )

; Set buffer - texture buffer
SetBuffer TextureBuffer( tex )

; Clear texture buffer with background white color
ClsColor 255,255,255
Cls

; Draw text on texture
font=LoadFont( "arial",24 )
SetFont font
Color 0,0,0
Text 0,0,"This texture"
Text 0,40,"was created using" : Color 0,0,255
Text 0,80,"CreateTexture()" : Color 0,0,0
Text 0,120,"and drawn to using" : Color 0,0,255
Text 0,160,"SetBuffer TextureBuffer()"

; Texture cube with texture
EntityTexture cube,tex

; Set buffer - backbuffer
SetBuffer BackBuffer()

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