
;The castle demo!
Const FPS=30
Const n_trees=100

Global info1$="Castle Demo"
Global info2$="Featuring dynamic terrain, sliding collisions,"
Global info3$="transparency effects and an intelligent camera"
Global info4$="Arrow keys/A/Z to move, Space to jump, Left-Alt to fire"

Include "../start.bb"

Global shoot=Load3DSound( "sounds\shoot.wav" )
Global boom=Load3DSound( "sounds\boom.wav" )
SoundVolume boom,.5

Type Player
	Field entity,model
	Field anim_speed#,player_y#,roll#
End Type

Type ChaseCam
	Field entity,camera,target,heading,sky
End Type

Type Spark
	Field alpha#,sprite
End Type
	
Type Bullet
	Field rot#,sprite,time_out
End Type

Type Hole
	Field alpha#,sprite
End Type

Const TYPE_PLAYER=1,TYPE_BULLET=2,TYPE_TARGET=3

Const TYPE_SCENERY=10,TYPE_TERRAIN=11

Collisions TYPE_PLAYER,TYPE_TERRAIN,2,3
Collisions TYPE_PLAYER,TYPE_SCENERY,2,2

Collisions TYPE_BULLET,TYPE_TERRAIN,2,1
Collisions TYPE_BULLET,TYPE_SCENERY,2,1

Collisions TYPE_TARGET,TYPE_TERRAIN,2,2
Collisions TYPE_TARGET,TYPE_SCENERY,2,2

Global water_level=-98
Global light,castle,land,ground,water,sky
Global spark_sprite,bull_sprite,player_model,hole_sprite,tree_sprite
Global bull_x#=1.5

Setup()

ChangeDir "environ"
LoadEnviron( "terrain-1.jpg","water-2_mip.bmp","sky","heightmap_256.bmp" )
ChangeDir "..\"

player1.Player=CreatePlayer( 0,10,0 )
camera1.ChaseCam=CreateChaseCam( player1\entity )

period=1000/FPS
time=MilliSecs()-period

listener=CreateListener( player1\entity,.1,1,.2 )
piv=CreatePivot()
PositionEntity piv,0,10,40

While Not KeyHit(1)
	If KeyHit(17)
		wire=Not wire
		Wireframe wire
	EndIf
	Repeat
		elapsed=MilliSecs()-time
	Until elapsed

	;how many 'frames' have elapsed	
	ticks=elapsed/period
	
	;fractional remainder
	tween#=Float(elapsed Mod period)/Float(period)
	
	For k=1 To ticks
		time=time+period
		If k=ticks Then CaptureWorld

		TurnEntity piv,0,5,0
		PositionEntity piv,0,7,40;47+Sin(time*.1)*40,40
		
		UpdateGame()
		UpdateWorld
		PositionEntity water,Sin(time*.01)*10,water_level+Sin(time*.05)*.5,Cos(time*.02)*10
		For c.ChaseCam=Each ChaseCam
			UpdateChaseCam( c )
			PositionEntity sky,EntityX(c\camera),EntityY(c\camera),EntityZ(c\camera)
		Next
	Next
	
	RenderWorld tween
	Flip
	
Wend

End

Function UpdateGame()
	For h.Hole=Each Hole
		UpdateHole( h )
	Next
	For b.Bullet=Each Bullet
		UpdateBullet( b )
	Next
	For s.Spark=Each Spark
		UpdateSpark( s )
	Next
	For p.Player=Each Player
		UpdatePlayer( p )
	Next
End Function

Function UpdateHole( h.Hole )
	h\alpha=h\alpha-.005
	If h\alpha>0
		EntityAlpha h\sprite,h\alpha
	Else
		FreeEntity h\sprite
		Delete h
	EndIf
End Function

Function CreatePlayer.Player( x#,y#,z# )
	p.Player=New Player
	p\entity=CreatePivot()
	p\model=CopyEntity( player_model,p\entity )
	p\player_y=y
	PositionEntity p\entity,x,y,z
	EntityType p\entity,TYPE_PLAYER
	EntityRadius p\entity,1.5
	ResetEntity p\entity
	Return p
End Function

Function CreateBullet.Bullet( p.Player )
	bull_x=-bull_x
	b.Bullet=New Bullet
	b\time_out=150
	b\sprite=CopyEntity( bull_sprite,p\entity )
	TranslateEntity b\sprite,bull_x,1,.25
	EntityParent b\sprite,0
	EmitSound shoot,b\sprite
	Return b
End Function

Function UpdateBullet( b.Bullet )
	If CountCollisions( b\sprite )
		If EntityCollided( b\sprite,TYPE_TERRAIN )
			EmitSound boom,b\sprite
			ex#=EntityX(b\sprite)
			ey#=EntityY(b\sprite)
			ez#=EntityZ(b\sprite)
			TFormPoint( ex,ey,ez,0,land )
			hi#=TerrainHeight( land,TFormedX(),TFormedZ() )
			If hi>0
				hi=hi-.02:If hi<0 Then hi=0
				ModifyTerrain land,TFormedX(),TFormedZ(),hi,True
			EndIf
			CreateSpark( b )
			FreeEntity b\sprite
			Delete b
			Return
		EndIf
		If EntityCollided( b\sprite,TYPE_SCENERY )
			For k=1 To CountCollisions( b\sprite )
				If GetEntityType( CollisionEntity( b\sprite,k ) )=TYPE_SCENERY
					cx#=CollisionX( b\sprite,k )
					cy#=CollisionY( b\sprite,k )
					cz#=CollisionZ( b\sprite,k )
					nx#=CollisionNX( b\sprite,k )
					ny#=CollisionNY( b\sprite,k )
					nz#=CollisionNZ( b\sprite,k )
					th.Hole=New Hole
					th\alpha=1
					th\sprite=CopyEntity( hole_sprite )
					PositionEntity th\sprite,cx,cy,cz
					AlignToVector th\sprite,-nx,-ny,-nz,3
					MoveEntity th\sprite,0,0,-.1
					Exit
				EndIf
			Next
			EmitSound boom,b\sprite
			CreateSpark( b )
			FreeEntity b\sprite
			Delete b
			Return
		EndIf
	EndIf
	b\time_out=b\time_out-1
	If b\time_out=0
		FreeEntity b\sprite
		Delete b
		Return
	EndIf
	b\rot=b\rot+30
	RotateSprite b\sprite,b\rot
	MoveEntity b\sprite,0,0,2
End Function

Function CreateSpark.Spark( b.Bullet )
	s.Spark=New Spark
	s\alpha=-90
	s\sprite=CopyEntity( spark_sprite,b\sprite )
	EntityParent s\sprite,0
	Return s
End Function

Function UpdateSpark( s.Spark )
	If s\alpha<270
		sz#=Sin(s\alpha)*5+5
		ScaleSprite s\sprite,sz,sz
		RotateSprite s\sprite,Rnd(360)
		s\alpha=s\alpha+15
	Else
		FreeEntity s\sprite
		Delete s
	EndIf
End Function

Function UpdatePlayer( p.Player )
	If KeyHit(56)	;fire?
		CreateBullet( p )
	EndIf
	
	If KeyDown(203)	;left/right
		TurnEntity p\entity,0,6,0	;turn player left/right
	Else If KeyDown(205)
		TurnEntity p\entity,0,-6,0
	EndIf
	
	If KeyDown(30)		;forward
		If p\anim_speed<=0
			p\anim_speed=1.75
			Animate p\model,1,p\anim_speed
		EndIf
		MoveEntity p\entity,0,0,1
	Else If KeyDown(44)	;back
		If p\anim_speed>=0
			p\anim_speed=-1.75
			Animate p\model,1,p\anim_speed
		EndIf
		MoveEntity p\entity,0,0,-1
	Else If p\anim_speed	;stop animating
		p\anim_speed=0
		Animate p\model,0
	EndIf

	Goto skip	
	ex#=EntityX(p\entity):ez#=EntityZ(p\entity)
	PositionEntity p\entity,ex,TerrainY( land,ex,0,ez )+1.5,ez
	Return
	.skip
	
	ty#=EntityY(p\entity)
	y_vel#=(ty-p\player_y)
	p\player_y=ty
	
	If KeyHit(57)	;jump?
		y_vel=5	;2.4
	Else
		y_vel=y_vel-.5	;2
	EndIf
	TranslateEntity p\entity,0,y_vel,0
	
End Function

Function CreateChaseCam.ChaseCam( entity )
	c.ChaseCam=New ChaseCam
	c\entity=entity
	c\camera=CreateCamera()
	
	c\target=CreatePivot( entity )
	PositionEntity c\target,0,3,-10
	EntityType c\target,TYPE_TARGET
	
	c\heading=CreatePivot( entity )
	PositionEntity c\heading,0,0,20
	c\sky=CopyEntity( sky )
	Return c
End Function

Function UpdateChaseCam( c.ChaseCam )

	If KeyDown(200)
		TranslateEntity c\heading,0,-3,0
	Else If KeyDown(208)
		TranslateEntity c\heading,0,+3,0
	EndIf
	
	dx#=EntityX(c\target,True)-EntityX(c\camera,True)
	dy#=EntityY(c\target,True)-EntityY(c\camera,True)
	dz#=EntityZ(c\target,True)-EntityZ(c\camera,True)
	
	TranslateEntity c\camera,dx*.1,dy*.1,dz*.1
	
	PointEntity c\camera,c\heading
		
	PositionEntity c\target,0,0,0
	ResetEntity c\target
	PositionEntity c\target,0,3,-10

	PositionEntity c\sky,EntityX(c\camera),EntityY(c\camera),EntityZ(c\camera)
End Function

Function LoadEnviron( land_tex$,water_tex$,sky_tex$,height_map$ )

	light=CreateLight()
	TurnEntity light,45,45,0
	
	land_tex=LoadTexture( land_tex$,1 )
	ScaleTexture land_tex,10,10

	land=LoadTerrain( height_map$ )
	EntityTexture land,land_tex
	
	TerrainShading land,True
	PositionEntity land,-1000,-100,-1000
	ScaleEntity land,2000.0/256,100,2000.0/256
	EntityType land,TYPE_TERRAIN
	TerrainDetail land,750,True
	
	For k=1 To n_trees
		Repeat
			tx#=Rnd(-70,70)-150
			tz#=Rnd(-70,70)+400
			ty#=TerrainY( land,tx,0,tz )
		Until ty>water_level
		t=CopyEntity( tree_sprite )
		PositionEntity t,tx,ty,tz
		ScaleSprite t,Rand(2,3),Rand(4,6)
	Next
	
	ground=CreatePlane()
	EntityTexture ground,land_tex
	PositionEntity ground,0,-100,0
	EntityOrder ground,9
	
	water_tex=LoadTexture( water_tex$,3 )
	ScaleTexture water_tex,20,20
	
	water=CreatePlane()
	EntityTexture water,water_tex
	PositionEntity water,0,water_level,0
		
	sky=LoadSkyBox( sky_tex$ )
	EntityOrder sky,10
	HideEntity sky

End Function

Function LoadSkyBox( file$ )
	m=CreateMesh()
	;front face
	b=LoadBrush( file$+"_FR.jpg",49 )
	s=CreateSurface( m,b )
	AddVertex s,-1,+1,-1,0,0:AddVertex s,+1,+1,-1,1,0
	AddVertex s,+1,-1,-1,1,1:AddVertex s,-1,-1,-1,0,1
	AddTriangle s,0,1,2:AddTriangle s,0,2,3
	FreeBrush b
	;right face
	b=LoadBrush( file$+"_LF.jpg",49 )
	s=CreateSurface( m,b )
	AddVertex s,+1,+1,-1,0,0:AddVertex s,+1,+1,+1,1,0
	AddVertex s,+1,-1,+1,1,1:AddVertex s,+1,-1,-1,0,1
	AddTriangle s,0,1,2:AddTriangle s,0,2,3
	FreeBrush b
	;back face
	b=LoadBrush( file$+"_BK.jpg",49 )
	s=CreateSurface( m,b )
	AddVertex s,+1,+1,+1,0,0:AddVertex s,-1,+1,+1,1,0
	AddVertex s,-1,-1,+1,1,1:AddVertex s,+1,-1,+1,0,1
	AddTriangle s,0,1,2:AddTriangle s,0,2,3
	FreeBrush b
	;left face
	b=LoadBrush( file$+"_RT.jpg",49 )
	s=CreateSurface( m,b )
	AddVertex s,-1,+1,+1,0,0:AddVertex s,-1,+1,-1,1,0
	AddVertex s,-1,-1,-1,1,1:AddVertex s,-1,-1,+1,0,1
	AddTriangle s,0,1,2:AddTriangle s,0,2,3
	FreeBrush b
	;top face
	b=LoadBrush( file$+"_UP.jpg",49 )
	s=CreateSurface( m,b )
	AddVertex s,-1,+1,+1,0,1:AddVertex s,+1,+1,+1,0,0
	AddVertex s,+1,+1,-1,1,0:AddVertex s,-1,+1,-1,1,1
	AddTriangle s,0,1,2:AddTriangle s,0,2,3
	FreeBrush b
	;bottom face	
	b=LoadBrush( file$+"_DN.jpg",49 )
	s=CreateSurface( m,b )
	AddVertex s,-1,-1,-1,1,0:AddVertex s,+1,-1,-1,1,1
	AddVertex s,+1,-1,+1,0,1:AddVertex s,-1,-1,+1,0,0
	AddTriangle s,0,1,2:AddTriangle s,0,2,3
	FreeBrush b
	ScaleMesh m,100,100,100
	FlipMesh m
	EntityFX m,1
	Return m
End Function

Function Setup()

	castle=LoadMesh( "castle\castle1.x" )
	ScaleEntity castle,.15,.15,.15
	EntityType castle,TYPE_SCENERY

	player_model=LoadAnimMesh( "markio\mariorun.x" )
	ScaleEntity player_model,.2,.2,.2
	TranslateEntity player_model,0,-1.25,0
	HideEntity player_model

	spark_sprite=LoadSprite( "sprites\bigspark.bmp" )
	HideEntity spark_sprite

	bull_sprite=LoadSprite( "sprites\bluspark.bmp" )
	ScaleSprite bull_sprite,3,3
	EntityRadius bull_sprite,1.5
	EntityType bull_sprite,TYPE_BULLET
	HideEntity bull_sprite
	
	hole_sprite=LoadSprite( "sprites\bullet_hole.bmp",1 )
	EntityBlend hole_sprite,2
	SpriteViewMode hole_sprite,2
	HideEntity hole_sprite
	
	tree_sprite=LoadSprite( "sprites\tree.bmp",7 )
	HandleSprite tree_sprite,0,-1
	ScaleSprite tree_sprite,2,4
	PositionEntity tree_sprite,0,0,-100
	SpriteViewMode tree_sprite,3
	EntityAutoFade tree_sprite,120,150
End Function