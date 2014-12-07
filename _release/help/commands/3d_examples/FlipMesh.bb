; FlipMesh Example
; ----------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
light=CreateLight()

; Create sphere
sphere=CreateSphere()

; Scale sphere
ScaleEntity sphere,100,100,100

; Texture sphere with sky texture
sky_tex=LoadTexture("media/sky.bmp")
EntityTexture sphere,sky_tex

; Flip mesh so we can see the inside of it
FlipMesh sphere

Color 0,0,0

While Not KeyDown( 1 )
RenderWorld
Text 0,0,"You are viewing a flipped sphere mesh - makes a great sky!"
Flip
Wend

End