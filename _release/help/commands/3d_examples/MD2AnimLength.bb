; MD2AnimLength Example
; ---------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()

light=CreateLight()
RotateEntity light,90,0,0

; Load md2
gargoyle=LoadMD2( "media/gargoyle/gargoyle.md2")

; Load md2 texture
garg_tex=LoadTexture( "media/gargoyle/gargoyle.bmp" )

; Apply md2 texture to md2
EntityTexture gargoyle,garg_tex

PositionEntity gargoyle,0,-45,100
RotateEntity gargoyle,0,180,0

While Not KeyDown( 1 )

RenderWorld

; Output animation length to screen
Text 0,0,"MD2AnimLength: "+MD2AnimLength( gargoyle )

Flip

Wend

End