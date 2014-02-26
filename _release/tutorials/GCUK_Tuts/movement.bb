; Movement & Rotation ***
; By Paul Gerfen (www.gamecoding.co.uk)

Graphics3D 800,600

SetBuffer BackBuffer()

camera=CreateCamera()
CameraViewport camera,0,0,800,600

light=CreateLight()

rocket=LoadMesh( "rocket.3ds" )
PositionEntity rocket,0,0,7


While Not KeyHit(1)

	
	If KeyDown(200)	Then
		MoveEntity rocket,0,0.05,0
	EndIf
	
	If KeyDown(203)	Then
		TurnEntity rocket,0,0,1.0
	EndIf
	
	If KeyDown(205)	Then
		TurnEntity rocket,0,0,-1.0
	EndIf
	
	UpdateWorld
	RenderWorld
	
	Text 320,500,"Movement & Rotation"
	
	Flip

Wend
End