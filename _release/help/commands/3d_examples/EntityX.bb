; EntityX Example
; ---------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
light=CreateLight()

cone=CreateCone( 32 )
PositionEntity cone,0,0,10

While Not KeyDown( 1 )

	x#=0
	y#=0
	z#=0

	If KeyDown(203)=True Then x#=-0.1
	If KeyDown(205)=True Then x#=0.1
	If KeyDown(208)=True Then y#=-0.1
	If KeyDown(200)=True Then y#=0.1
	If KeyDown(44)=True Then z#=-0.1
	If KeyDown(30)=True Then z#=0.1
	
	MoveEntity cone,x#,y#,z#
			
	RenderWorld

	Text 0,0,"Use cursor/A/Z keys to move cone"
	
	; Return entity x position of cone
	Text 0,20,"X Position: "+EntityX#( cone )

	Flip

Wend

End