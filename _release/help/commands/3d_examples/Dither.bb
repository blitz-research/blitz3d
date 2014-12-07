; Dither Example
; --------------

Graphics3D 640,480,16
SetBuffer BackBuffer()

camera=CreateCamera()
light=CreateLight()

; Rotate light so that it creates maximum shading effect on sphere
RotateEntity light,90,0,0

sphere=CreateSphere( 32 )
PositionEntity sphere,0,0,2

While Not KeyDown( 1 )

; Toggle dither enable value between true and false when spacebar is pressed
If KeyHit( 57 )=True Then enable=1-enable

; Enable/disable hardware dithering
Dither enable

RenderWorld

Text 0,0,"Press spacebar to toggle between Dither True/False"
If enable=True Then Text 0,20,"Dither True" Else Text 0,20,"Dither False"

Flip

Wend

End