; Graphics3D Example
; ------------------

; Set 3D graphics mode
Graphics3D 640,480,16,0

SetBuffer BackBuffer()

camera=CreateCamera()
light=CreateLight()

cone=CreateCone( 32 )
PositionEntity cone,0,0,5

While Not KeyDown( 1 )
RenderWorld
Flip
Wend

End