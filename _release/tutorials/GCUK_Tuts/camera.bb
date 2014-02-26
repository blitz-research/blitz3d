; Camera Movement ***
; By Paul Gerfen (www.gamecoding.co.uk)

Graphics3D 800,600

SetBuffer BackBuffer()

camera=CreateCamera()
CameraViewport camera,0,0,800,600

light=CreateLight()

house=LoadMesh( "house.3ds" )
RotateEntity house,0,90,0

While Not KeyHit(1)

	
	If KeyDown(200)	Then
		MoveEntity camera,0,0,1
	EndIf
	
	If KeyDown(208)	Then
		MoveEntity camera,0,0,-1
	EndIf
	
	If KeyDown(203)	Then
		TurnEntity camera,0,1.0,0 
	EndIf
	
	If KeyDown(205)	Then
		TurnEntity camera,0,-1.0,0 
	EndIf
	
	UpdateWorld
	RenderWorld
	
	Text 335,500,"Camera Movement"
	
	Flip

Wend
End