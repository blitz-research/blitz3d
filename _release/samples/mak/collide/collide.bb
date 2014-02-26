
Repeat
	n_dudes=Input$( "Number of dudes (1-50):" )
Until n_dudes>=1 And n_dudes<=100

Global info1$="Collision demo"
Global info2$="Arrow keys/A/Z To drive, Tab To repel"

Include "../start.bb"

Type Dude
	Field entity,speed#
End Type

Const T_DUDE=1,T_WALLS=2

Collisions T_DUDE,T_DUDE,1,2	;sphere-to-sphere, sliding collisions
Collisions T_DUDE,T_WALLS,2,2	;sphere-to-polygon, sliding collisions

walls=CreateCube()
EntityColor walls,0,32,192
FitMesh walls,-40,0,-40,80,80,80
FlipMesh walls
EntityType walls,T_WALLS

col=CreateCube()
FitMesh col,-1,0,-1,2,40,2
EntityColor col,255,0,0
EntityAlpha col,.75
EntityType col,T_WALLS
For k=30 To 359+30 Step 60
	t=CopyEntity( col )
	RotateEntity t,0,k,0
	MoveEntity t,0,0,34
Next
FreeEntity col

camera=CreateCamera()
PositionEntity camera,0,50,-46
TurnEntity camera,45,0,0

light=CreateLight()
TurnEntity light,45,45,0

player=CreateCube()
EntityColor player,0,255,0
PositionEntity player,0,3,0
EntityRadius player,2
EntityType player,T_DUDE

nose=CreateCube( player )
EntityColor nose,0,255,0
ScaleEntity nose,.5,.5,.5
PositionEntity nose,0,0,1.5

sphere=CreateSphere()
EntityShininess sphere,.5
EntityType sphere,T_DUDE

an#=0
an_step#=360.0/n_dudes

For k=1 To n_dudes
	d.Dude=New Dude
	d\entity=CopyEntity( sphere )
	EntityColor d\entity,Rnd(255),Rnd(255),Rnd(255)
	TurnEntity d\entity,0,an,0
	MoveEntity d\entity,0,2,37
	ResetEntity d\entity
	d\speed=Rnd( .4,.49 )
	an=an+an_step
Next

FreeEntity sphere

ok=True	

While Not KeyHit(1)

	If KeyDown(203) TurnEntity player,0,5,0
	If KeyDown(205) TurnEntity player,0,-5,0
	If KeyDown(200) MoveEntity player,0,0,.5
	If KeyDown(208) MoveEntity player,0,0,-.5
	If KeyDown(30) TranslateEntity player,0,.2,0
	If KeyDown(44) TranslateEntity player,0,-.2,0
	
	For d.Dude=Each Dude
		If EntityDistance( player,d\entity )>2
			PointEntity d\entity,player
			If KeyDown(15) TurnEntity d\entity,0,180,0
		EndIf
		MoveEntity d\entity,0,0,d\speed
	Next

	UpdateWorld
	RenderWorld

	Goto skip
	If ok
		;
		;sanity check!
		;make sure nothings gone through anything else...
		;
		For d=Each Dude
			If EntityY( d\entity )<.9
				ok=False
				bad$="Bad Dude Y: "+EntityY( d\entity )
			EndIf
			For d2.Dude=Each Dude
				If d=d2 Then Exit
				If EntityDistance( d\entity,d2\entity )<.9
					ok=False
					bad$="Dude overlap!"
				EndIf
			Next
		Next
	EndIf
	
	If ok
		Text 0,0,"Dudes OK"
	Else
		CameraClsColor camera,255,0,0
		Text 0,0,bad$
	EndIf
	.skip
	
	Flip
Wend