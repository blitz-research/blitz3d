; Texturing ***
; By Paul Gerfen (www.gamecoding.co.uk)

Graphics3D 800,600

SetBuffer BackBuffer()

camera=CreateCamera()
CameraViewport camera,0,0,800,600

light=CreateLight()

cube=CreateCube()
PositionEntity cube,0,0,5

texture=LoadTexture("blitztexture.bmp")
EntityTexture cube,texture

While Not KeyHit(1)

	TurnEntity cube,0.1,0.2,0.3
	
	UpdateWorld
	RenderWorld
	
	Text 340,500,"Texturing Demo"
	
	Flip

Wend
End