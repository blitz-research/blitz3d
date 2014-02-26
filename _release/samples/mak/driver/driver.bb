
Global info1$="Driver"

Include "../start.bb"

Const GRAVITY#=-.01

Const BODY=1,WHEEL=2,SCENE=3

Collisions BODY,SCENE,2,3
Collisions WHEEL,SCENE,2,3

terr=LoadTerrain( "heightmap_256.bmp" )
ScaleEntity terr,1000/TerrainSize(terr),70,1000/TerrainSize(terr)
TerrainDetail terr,1000,True
TerrainShading terr,True
PositionEntity terr,-500,0,-500
tex=LoadTexture( "terrain-1.jpg" )
ScaleTexture tex,50,50
EntityTexture terr,tex
EntityType terr,SCENE

car=LoadMesh( "car.x" )
ScaleMesh car,1,1,-1
FlipMesh car
FitMesh car,-1.5,-1,-3,3,2,6
PositionEntity car,0,70,0
EntityShininess car,1
EntityType car,BODY

Global wheels[4]

cnt=1
For z#=1.5 To -1.5 Step -3
For x#=-1 To 1 Step 2
	wheels[cnt]=CreateSphere( 8,car )
	EntityAlpha wheels[cnt],.5
	ScaleEntity wheels[cnt],.5,.5,.5
	EntityRadius wheels[cnt],.5
	PositionEntity wheels[cnt],x,0,z
	EntityType wheels[cnt],WHEEL
	cnt=cnt+1
Next
Next

light=CreateLight()
TurnEntity light,45,45,0

target=CreatePivot( car )
PositionEntity target,0,5,-12

camera=CreateCamera()
CameraClsColor camera,0,128,255

speed#=0
x_vel#=0:prev_x#=EntityX( car )
y_vel#=0:prev_y#=EntityY( car )
z_vel#=0:prev_z#=EntityZ( car )

While Not KeyHit(1)

	;align car to wheels
	zx#=(EntityX( wheels[2],True )+EntityX( wheels[4],True ))/2
	zx=zx-(EntityX( wheels[1],True )+EntityX( wheels[3],True ))/2
	zy#=(EntityY( wheels[2],True )+EntityY( wheels[4],True ))/2
	zy=zy-(EntityY( wheels[1],True )+EntityY( wheels[3],True ))/2
	zz#=(EntityZ( wheels[2],True )+EntityZ( wheels[4],True ))/2
	zz=zz-(EntityZ( wheels[1],True )+EntityZ( wheels[3],True ))/2
	AlignToVector car,zx,zy,zz,1
	
	zx#=(EntityX( wheels[1],True )+EntityX( wheels[2],True ))/2
	zx=zx-(EntityX( wheels[3],True )+EntityX( wheels[4],True ))/2
	zy#=(EntityY( wheels[1],True )+EntityY( wheels[2],True ))/2
	zy=zy-(EntityY( wheels[3],True )+EntityY( wheels[4],True ))/2
	zz#=(EntityZ( wheels[1],True )+EntityZ( wheels[2],True ))/2
	zz=zz-(EntityZ( wheels[3],True )+EntityZ( wheels[4],True ))/2
	AlignToVector car,zx,zy,zz,3
	
	;calculate car velocities	
	cx#=EntityX( car ):x_vel=cx-prev_x:prev_x=cx
	cy#=EntityY( car ):y_vel=cy-prev_y:prev_y=cy
	cz#=EntityZ( car ):z_vel=cz-prev_z:prev_z=cz
	
	;resposition wheels
	cnt=1
	For z=1.5 To -1.5 Step -3
	For x=-1 To 1 Step 2
;		PositionEntity wheels[cnt],0,0,0
;		ResetEntity wheels[cnt]
		PositionEntity wheels[cnt],x,-1,z
		cnt=cnt+1
	Next
	Next

	;move car
	If KeyDown(203) TurnEntity car,0,3,0
	If KeyDown(205) TurnEntity car,0,-3,0
	If EntityCollided( car,SCENE )
		If KeyDown(200)
			speed=speed+.02
			If speed>.7 speed=.7
		Else If KeyDown(208)
			speed=speed-.02
			If speed<-.5 speed=-.5
		Else
			speed=speed*.9
		EndIf
		MoveEntity car,0,0,speed
		TranslateEntity car,0,GRAVITY,0
	Else
		TranslateEntity car,x_vel,y_vel+GRAVITY,z_vel
	EndIf

	;update camera
	If speed>=0	
		dx#=EntityX( target,True )-EntityX( camera )
		dy#=EntityY( target,True )-EntityY( camera )
		dz#=EntityZ( target,True )-EntityZ( camera )
		TranslateEntity camera,dx*.1,dy*.1,dz*.1
	EndIf
	PointEntity camera,car
	
	UpdateWorld
	RenderWorld
	Flip
Wend

End