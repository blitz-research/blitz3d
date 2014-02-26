
Global info1$="Pick demo"
Global info2$="Hit A/Z To change camera zoom"

Include "../start.bb"

sphere=CreateSphere()
EntityPickMode sphere,1

For k=1 To 100
	model=CopyEntity( sphere )
	EntityColor model,Rnd(255),Rnd(255),Rnd(255)
	EntityShininess model,Rnd(1)
	
	rad#=Rnd(1,2)
	EntityRadius model,rad
	ScaleEntity model,rad,rad,rad
	TurnEntity model,Rnd(360),Rnd(360),0
	MoveEntity model,0,0,Rnd(20)+20
Next

FreeEntity sphere

light=CreateLight()
TurnEntity light,45,45,0

camera=CreateCamera()
CameraRange camera,.1,1000

entity=0

zoom#=1

While Not KeyHit(1)

	If KeyDown(30) zoom=zoom*1.1
	If KeyDown(44) zoom=zoom/1.1
	CameraZoom camera,zoom

	x=MouseX()
	y=MouseY()
	
	If y<32 TurnEntity camera,-2,0,0
	If y>480-32 TurnEntity camera,2,0,0
	
	If x<32 TurnEntity camera,0,2,0
	If x>640-32 TurnEntity camera,0,-2,0
	
	e=CameraPick( camera,x,y )
	If e<>entity
		If entity Then EntityAlpha entity,1
		entity=e
	EndIf
	
	If entity
		EntityAlpha entity,Sin( MilliSecs() )*.5+.5
	EndIf
	
	UpdateWorld
	RenderWorld
	
	Rect x,y-3,1,7	
	Rect x-3,y,7,1
	
	Flip

Wend