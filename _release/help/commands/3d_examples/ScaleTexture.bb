; ScaleTexture Example
; --------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()

light=CreateLight()
RotateEntity light,90,0,0

cube=CreateCube()
PositionEntity cube,0,0,5

; Load texture
tex=LoadTexture( "media/b3dlogo.jpg" )

; Texture cube
EntityTexture cube,tex

; Set initial uv scale values
u_scale#=1
v_scale#=1

While Not KeyDown( 1 )

; Change uv scale values depending on key pressed
If KeyDown( 208 )=True Then u_scale#=u_scale#-0.01 
If KeyDown( 200 )=True Then u_scale#=u_scale#+0.01 
If KeyDown( 203 )=True Then v_scale#=v_scale#-0.01 
If KeyDown( 205 )=True Then v_scale#=v_scale#+0.01 

; Scale texture
ScaleTexture tex,u_scale#,v_scale# 

TurnEntity cube,0.1,0.1,0.1

RenderWorld

Text 0,0,"Use cursor keys to change uv scale values"
Text 0,20,"u_scale#="+u_scale#
Text 0,40,"v_scale#="+v_scale#

Flip

Wend

End