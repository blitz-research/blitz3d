; Setting up ***
; By Paul Gerfen (www.gamecoding.co.uk)

Graphics3D 800,600

SetBuffer BackBuffer()

camera=CreateCamera()
CameraViewport camera,0,0,800,600

light=CreateLight()

cube=CreateCube()
PositionEntity cube,0,0,5


While Not KeyHit(1)

	TurnEntity cube,.1,.2,.3

	UpdateWorld
	RenderWorld
	
	Text 320,500,"First Blitz3D Program"
	
	Flip

Wend
End