; Object Animation ***
; By Paul Gerfen (www.gamecoding.co.uk)

Graphics3D 800,600

SetBuffer BackBuffer()

camera=CreateCamera()
CameraViewport camera,0,0,800,600

light=CreateLight()

man=LoadMD2( "gargoyle.md2" )
PositionEntity man,0,-35,600
RotateEntity man,0,180,0

AnimateMD2 man,1,.1,32,46

While Not KeyHit(1)

	If dist<970 MoveEntity man,0,0,.5
	If dist=970 AnimateMD2 man,1,.05,0,31

	dist=dist+1
		
	UpdateWorld
	RenderWorld
	
	Text 320,500,"An Animated MD2 Demo"
	
	Flip

Wend
End