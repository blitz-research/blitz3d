; TextureBlend Example
; --------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()

; Choose a background colour which isn't the same colour as anything else, to  avoid confusion
CameraClsColor camera,255,0,0

light=CreateLight()
RotateEntity light,90,0,0

cube=CreateCube()
PositionEntity cube,0,0,5

; Load textures
tex0=LoadTexture( "media/b3dlogo.jpg" )
tex1=LoadTexture( "media/chorme-2.bmp" )

; Texture cube with textures
EntityTexture cube,tex0,0,0
EntityTexture cube,tex1,0,1

tex0_blend_info$="no texture"
tex1_blend_info$="no texture"

While Not KeyDown( 1 )

; Change texture 0 blending mode
If KeyHit( 11 )=True
tex0_blend=tex0_blend+1
If tex0_blend=4 Then tex0_blend=0
If tex0_blend=0 Then tex0_blend_info$="no texture"
If tex0_blend=1 Then tex0_blend_info$="no blend"
If tex0_blend=2 Then tex0_blend_info$="multiply"
If tex0_blend=3 Then tex0_blend_info$="add"
EndIf

; Change texture 1 blending mode
If KeyHit( 2 )=True
tex1_blend=tex1_blend+1
If tex1_blend=4 Then tex1_blend=0
If tex1_blend=0 Then tex1_blend_info$="no texture"
If tex1_blend=1 Then tex1_blend_info$="no blend"
If tex1_blend=2 Then tex1_blend_info$="multiply"
If tex1_blend=3 Then tex1_blend_info$="add"
EndIf

; Set texture blend modes
TextureBlend tex0,tex0_blend 
TextureBlend tex1,tex1_blend 

TurnEntity cube,0.1,0.1,0.1

RenderWorld

Text 0,0,"Press 0 to change texture 0's blending mode"
Text 0,20,"Press 1 to change texture 1's blending mode"
Text 0,40,"TextureBlend tex0,"+tex0_blend+" ("+tex0_blend_info$+")"
Text 0,60,"TextureBlend tex1,"+tex1_blend+" ("+tex1_blend_info$+")"

Flip

Wend

End