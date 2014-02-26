; LoadAnimTexture Example
; -----------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
light=CreateLight()

cone=CreateCube()
PositionEntity cone,0,0,5

kaboom=LoadAnimTexture("media/kaboom.bmp",1,60,48,2,2)

While Not KeyDown( 1 )

	EntityTexture cone,kaboom,1
	
	RenderWorld
	Flip

Wend

End