; ------------------------------------
; Name:		Wing Ring
; Version:	V1.0
; Author:	Simon Harrison
; Email:	si@si-design.co.uk
; Website:	http://www.si-design.co.uk
; Date:		18/09/01
; ------------------------------------

; Player 1 - Keyboard. A = Forward. S = Shoot. Cursor keys = Move.
; Player 2 - Joypad.


AppTitle "Wing Ring V1.0"

Global width=640,height=480,depth=16,mode=1
Graphics3D width,height,depth,mode
SetBuffer BackBuffer()

SeedRnd MilliSecs()


; --------------
; Menu variables
; --------------

; Menu background
Global logo,logo_width,logo_height,tile_y 	; Logo-related variables
Global cr#,cg#,cb# 							; Current red, green, blue values
Global dr#,dg#,db# 							; Destination red, green, blue values
Global sr#,sg#,sb# 							; Step red, green, blue values
Global col,ct,fade=100 						; Current colour, time, fade

; Menu text spacing
Global y_space#=height/18,option=4,screen
Global x_space=width/71,x_nos=((width/4)-((9*3)/2)),x_names=((width/2)-((9*5)/2)),x_scores=((width/1.33)-((9*5)/2))

; Graphics modes
Global count_gfx_modes=CountGfxModes(),gfx_mode_option,mode_option=1

; Arrays
Dim greyscale(0,0)
Dim gfx_modes(0,2)
Dim names$(11),scores(11) : scores(11)=0

; High score rank
Global rank


; ----------------------
; General game variables
; ----------------------

; Game-timing stuff
Const fps=60
Global elapsed,time,tween#,period=1000/fps

; Collision types
Const type_none=0,type_plane1=1,type_plane1_bullet=2,type_plane2=3,type_plane2_bullet=4,type_ufo=5,type_ufo_bullet=6,type_statue=7

; No of players
Global no_players

; Score values
Global pot1,bank1	; Player 1
Global pot2,bank2	; Player 2

; Game over values
Global game_over1=False,game_over1_delay	; Player 1
Global game_over2,game_over2_delay			; Player 2


; -----------------------------
; Lighting and camera variables
; -----------------------------

; Light and camera entities
Global light
Global main_cam1,top_cam1	; Player 1
Global main_cam2,top_cam2	; Player 2

; Camera 1 position values
Global cam1_x#,cam1_y#=1000,cam1_z#,cam1_ay#=180		; Current values
Global cam1_dx#,cam1_dy#=1000,cam1_dz#,cam1_day#=180	; Destination values

; Camera 2 position values
Global cam2_x#,cam2_y#=1000,cam2_z#,cam2_ay#=180		; Current values
Global cam2_dx#,cam2_dy#=1000,cam2_dz#,cam2_day#=180	; Destination values


; -----------------
; Scenery variables
; -----------------

; Scenery entities
Global sphinx,liberty,ground,ring,sky,sun,moon

; Senery textures
Global sand_tex,lights_tex,lights_multi_tex,sky_tex,night_sky_tex

; Pointers to scenery entities (sort of)
Global statue,solar,solar_y,solar_z,solar_scale,solar_scale_shake

; Scenery value
Global scenery

; Ring values
Global red_ring_x#=0,red_ring_z#=0,red_ring_radius#=625
Global orange_ring_x#=0,orange_ring_z#=2773.4375,orange_ring_radius#=1406.25
Global yellow_ring_x#=2578.125,yellow_ring_z#=898.4375,yellow_ring_radius#=1250
Global green_ring_x#=-2578.125,green_ring_z#=898.4375,green_ring_radius#=1093.75
Global blue_ring_x#=1601.5625,blue_ring_z#=-2187.5,blue_ring_radius#=937.5
Global pink_ring_x#=-1601.5625,pink_ring_z#=-2187.5,pink_ring_radius#=781.25


; ---------------
; Plane variables
; ---------------

; Plane entities
Global plane1,shield1	; Player 1
Global plane2,shield2	; Player 2

; Player 1 position values
Global plane1_x#,plane1_y#=1000,plane1_z#,plane1_ay#=180		; Current values
Global plane1_dx#,plane1_dy#=1000,plane1_dz#,plane1_day#=180	; Destination values

; Player 2 position values
Global plane2_x#,plane2_y#=1000,plane2_z#,plane2_ay#=180		; Current values
Global plane2_dx#,plane2_dy#=1000,plane2_dz#,plane2_day#=180	; Destination values

; Plane + camera curve values
Global curve_plane#=10,curve_cam#=15


; ---------------
; Enemy variables
; ---------------

; Enemy entities
Global mother_pivot,mother
Global orange_ufo,blue_ufo,green_ufo,pink_ufo,yellow_ufo
Global orange_shadow,blue_shadow,green_shadow,pink_shadow,yellow_shadow

; New ufos
Global mother_timer=MilliSecs(),ufo_count,new_ufo,no_ufos,max_ufos=15

; Ufo trail
Global trail_timer=MilliSecs(),trail_timer_reset=False


; -------------------
; Explosion variables
; -------------------

; Explosion entities
Global bullet,orange_spark,blue_spark,green_spark,pink_spark,yellow_spark,debris

; Points entities
Global points_200,points_200x2,points_200x4,points_200x6,points_200x8,points_200x10
Global points_400,points_400x2,points_400x4,points_400x6,points_400x8,points_400x10
Global points_600,points_600x2,points_600x4,points_600x6,points_600x8,points_600x10
Global points_800,points_800x2,points_800x4,points_800x6,points_800x8,points_800x10
Global points_1000,points_1000x2,points_1000x4,points_1000x6,points_1000x8,points_1000x10
Global points_bust

; Intensity of explosions
Global plane_intensity=100,ufo_intensity=50


; ----------------------
; Bonus camels variables
; ----------------------

; Camel entities
Global orange_camel,yellow_camel,green_camel,blue_camel,pink_camel

; Player 1 bonus values
Global plane1_bonus													; Variable containing value of bonus collected
Global orange_bonus1_time,orange_bonus1_status
Global yellow_bonus1_time,yellow_bonus1_status,yellow_bonus1_pause
Global green_bonus1_time,green_bonus1_status
Global blue_bonus1_time,blue_bonus1_status
Global pink_bonus1_time,pink_bonus1_status

; Player 2 bonus values
Global plane2_bonus													; Variable containing value of bonus collected
Global orange_bonus2_time,orange_bonus2_status
Global yellow_bonus2_time,yellow_bonus2_status,yellow_bonus2_pause
Global green_bonus2_time,green_bonus2_status
Global blue_bonus2_time,blue_bonus2_status
Global pink_bonus2_time,pink_bonus2_status


; -------------------------------
; Fonts + music + sound variables
; -------------------------------

; Fonts
Global small_font,large_font

; Music
Global music

; Sounds
Global explode


; -----
; Types
; -----

Type plane1_bullet
	Field entity,alpha#
End Type

Type plane2_bullet
	Field entity,alpha#
End Type

Type ufo
	Field entity,shadow,colour,target,x#,y#,z#,dx#,dy#,dz#,count
End Type

Type trail
	Field entity,alpha#,count
End Type

Type ufo_bullet
	Field entity,alpha#
End Type

Type bullet_trail
	Field entity,alpha#
End Type

Type shadow
	Field entity,scale#,alpha#
End Type

Type spark
	Field entity,alpha#,y_speed#
End Type

Type points
	Field entity,scale#,alpha#
End Type

Type camel
	Field entity,colour,alpha#
End Type


; -------------
; Pre-main loop
; -------------

Include "menus.bb"
Include "scenery.bb"
Include "player2.bb"

InitMenus()
LoadMedia()
CreateEntities()
InitCollisions()


; ---------
; Main loop
; ---------

While Not KeyHit(1)

	Background()

	; Screen functions
	Select screen
		Case 4 : no_players=1 : Game()
		Case 5 : no_players=2 : Game()
		Case 6 : HighScores()
		Case 7 : Options()
		Default : Credits()
	End Select
	
	Flip
	
Wend

End ; End of program.


; -------------
; Game Function
; -------------

Function Game()

	; Use random scenery
	scenery=1-scenery
	If scenery=1 Then Egypt()
	If scenery=0 Then USA()

	StartGame()

	While KeyHit(1)<>True And (game_over1_delay<=120 Or game_over2_delay<=120)

		Repeat
			elapsed=MilliSecs()-time
		Until elapsed
	
		tween#=Float(elapsed)/Float(period)
	
		While tween>=1
			tween=tween-1
			time=time+period
			If tween<1 Then CaptureWorld
			UpdateGame()
			UpdateWorld
		Wend
	
		RenderWorld ;tween		
		Render2D()
		Flip
		
	Wend

	EndGame()
	
	If screen<>6 Then screen=0

End Function


; --------------------
; Update game function
; --------------------

Function UpdateGame()

	If game_over1=False
		GameInput1()
		UpdatePlane1()
		UpdateCamels1()
		CheckCollisions1()
	Else
		game_over1_delay=game_over1_delay+1
	EndIf
	
	; 2 player only
	If no_players=2
		If game_over2=False
			GameInput2()
			UpdatePlane2()
			UpdateCamels2()
			CheckCollisions2()
		Else
			game_over2_delay=game_over2_delay+1
		EndIf
	EndIf

	UpdateMother()
	UpdateUFO()
	UpdateExplosions()
	UpdateExtras()

End Function


; -------------------
; Start game function
; -------------------

Function StartGame()

	no_ufos=0
	ufo_count=0
	
	game_over2_delay=1000 ; Make sure it's game over for player 2 if we are having a one-player game!

	game_over1=False
	game_over1_delay=0

	plane1_x#=0
	plane1_z#=0
	plane1_ay#=180
	
	plane1_dx#=0
	plane1_dz#=0
	plane1_day#=180
	
	cam1_x#=0
	cam1_z#=0
	cam1_ay#=180
	
	cam1_dx#=0
	cam1_dz#=0
	cam1_day#=180
	
	pot1=0
	bank1=0
	
	orange_bonus1_time=0
	yellow_bonus1_time=0
	green_bonus1_time=0
	blue_bonus1_time=0
	pink_bonus1_time=0
	
	CameraViewport main_cam1,0,0,width,height

	ShowEntity plane1 : PositionEntity plane1,0,plane1_y#,0 : ResetEntity plane1
	HideEntity plane2
	HideEntity main_cam2
	HideEntity top_cam2
	
	If no_players=2
	
		game_over2=False
		game_over2_delay=0

		plane2_x#=0
		plane2_z#=0
		plane2_ay#=180
	
		plane2_dx#=0
		plane2_dz#=0
		plane2_day#=180
	
		cam2_x#=0
		cam2_z#=0
		cam2_ay#=180
	
		cam2_dx#=0
		cam2_dz#=0
		cam2_day#=180
		
		pot2=0
		bank2=0
	
		orange_bonus2_time=0
		yellow_bonus2_time=0
		green_bonus2_time=0
		blue_bonus2_time=0
		pink_bonus2_time=0
	
		; Resize the already active player 1 camera viewport for 2-player game
		CameraViewport main_cam1,0,0,width,(height/2)

		ShowEntity main_cam2
		ShowEntity top_cam2
		ShowEntity plane2 : PositionEntity plane2,0,plane2_y#,0 : ResetEntity plane2
	
	EndIf

	SetFont large_font
	StopChannel music
	music=PlayMusic("tune2.mid")
	
	time=MilliSecs()-period

End Function


; -----------------
; End game function
; -----------------

Function EndGame()

	For p1.plane1_bullet=Each plane1_bullet
		FreeEntity p1\entity
		Delete p1
	Next

	For p2.plane2_bullet=Each plane2_bullet
		FreeEntity p2\entity
		Delete p2
	Next

	For u.ufo=Each ufo
		FreeEntity u\entity
		FreeEntity u\shadow
		Delete u
	Next

	For t.trail=Each trail
		FreeEntity t\entity
		Delete t
	Next

	For ub.ufo_bullet=Each ufo_bullet
		FreeEntity ub\entity
		Delete ub
	Next

	For b.bullet_trail=Each bullet_trail
		FreeEntity b\entity
		Delete b
	Next

	For s.shadow=Each shadow
		FreeEntity s\entity
		Delete s
	Next

	For sp.spark=Each spark
		FreeEntity sp\entity
		Delete sp
	Next

	For po.points=Each points
		FreeEntity po\entity
		Delete po
	Next

	For c.camel=Each camel
		FreeEntity c\entity
		Delete c
	Next

	SetFont small_font
	StopChannel music
	music=PlayMusic("tune1.mid")

	FlushKeys

End Function


; -------------------
; Load media function
; -------------------

Function LoadMedia()

	ChangeDir "media"
	explode=LoadSound("explode.wav")
	small_font=LoadFont("courier",16)
	large_font=LoadFont("arial black",(width+height)/23.3)

End Function


; ------------------------
; Create entities function
; ------------------------

Function CreateEntities()

	; Create light
	light=CreateLight()
	RotateEntity light,45,0,0

	; Create main cam 1
	main_cam1=CreateCamera()
	CameraRange main_cam1,1,9995
	CameraFogRange main_cam1,5000,9995
	CameraFogMode main_cam1,False

	; Create top cam 1
	top_cam1=CreateCamera()
	CameraClsColor top_cam1,96,160,248
	CameraViewport top_cam1,(width+height)/140,(width+height)/140,(width+height)/8.75,(width+height)/8.75
	CameraRange top_cam1,1,1100
	MoveEntity top_cam1,0,50,0
	TurnEntity top_cam1,90,0,0

	; Create main cam 2
	main_cam2=CreateCamera()
	CameraViewport main_cam2,0,(height/2),width,(height/2)
	CameraRange main_cam2,1,9995
	CameraFogRange main_cam2,5000,9995
	CameraFogMode main_cam2,1
	HideEntity main_cam2

	; Create top cam 2
	top_cam2=CreateCamera()
	CameraClsColor top_cam2,96,160,248 
	CameraViewport top_cam2,(width+height)/140,(height/2)+((width+height)/140),(width+height)/8.75,(width+height)/8.75
	CameraRange top_cam2,1,1100
	MoveEntity top_cam2,0,50,0
	TurnEntity top_cam2,90,0,0
	HideEntity top_cam2

	; Create ground mesh
	ground=CreatePlane()
	
	; Load sand texture
	sand_tex=LoadTexture("sand.bmp")
	ScaleTexture sand_tex,100,100
	
	; Load lights texture
	lights_tex=LoadTexture("lights.bmp")
	ScaleTexture lights_tex,1000,1000
	
	; Load lights texture again for multi-texturing purposes
	lights_multi_tex=LoadTexture("lights.bmp")
	ScaleTexture lights_multi_tex,1000,1000
	
	; Create ring patterns mesh
	ring=CreateMesh()
	ring_surf=CreateSurface(ring)
	AddVertex(ring_surf,-5000,1,-5000,0,0)
	AddVertex(ring_surf,-5000,1,5000,0,1)
	AddVertex(ring_surf,5000,1,5000,1,1)
	AddVertex(ring_surf,5000,1,-5000,1,0)
	AddTriangle(ring_surf,0,1,2)
	AddTriangle(ring_surf,0,2,3)
	ring_tex=LoadTexture("ring.bmp",3)
	EntityTexture ring,ring_tex
	EntityFX ring,1

	; Create sky mesh
	sky=CreateSphere()
	ScaleMesh sky,5000,2750,5000
	FlipMesh sky
	EntityFX sky,1
	EntityOrder sky,1
	
	; Load sky texture
	sky_tex=LoadTexture("clouds.bmp")

	; Load night sky texture
	night_sky_tex=LoadTexture("clouds_night.bmp")

	; Load Sphinx mesh
	sphinx=LoadMesh("sphinx.x")
	EntityShininess sphinx,0.5
	EntityType sphinx,type_statue
	PositionEntity sphinx,0,90,0

	; Load Statue of Liberty mesh
	liberty=LoadMesh("stat_lib.x")
	ScaleMesh liberty,10,10,10
	EntityType liberty,type_statue
	PositionEntity liberty,0,365,0

	; Load plane 1 mesh
	plane1=LoadMesh("plane.x")
	EntityShininess plane1,1
	EntityRadius plane1,2
	EntityType plane1,type_plane1
	PositionEntity plane1,0,250,0
	ResetEntity plane1

	; Create plane 2 mesh - create before shield1 so that shield1 is not copied too
	plane2=CopyEntity(plane1)
	EntityType plane2,type_plane2
	
	; Create plane 1 shield mesh
	shield1=CreateSphere(16,plane1)
	ScaleEntity shield1,10,10,10
	EntityColor shield1,0,0,255
	EntityShininess shield1,1

	; Create plane 2 shield mesh
	shield2=CopyEntity(shield1,plane2)
	EntityColor shield2,255,0,0

	; Create debris mesh
	debris=CreateMesh()
	debris_surf=CreateSurface(debris)
	AddVertex(debris_surf,Sin(240)*5,Cos(240)*5,0)
	AddVertex(debris_surf,Sin(0)*5,Cos(0)*5,0)
	AddVertex(debris_surf,Sin(120)*5,Cos(120)*5,0)
	AddVertex(debris_surf,Sin(240)*5,Cos(240)*5,0.001)
	AddVertex(debris_surf,Sin(0)*5,Cos(0)*5,0.001)
	AddVertex(debris_surf,Sin(120)*5,Cos(120)*5,0.001)
	AddTriangle(debris_surf,0,1,2)
	AddTriangle(debris_surf,3,5,4)
	HideEntity debris

	; Create mother pivot + mesh
	mother_pivot=CreatePivot()
	PositionEntity mother_pivot,0,1040,0
	mother=LoadMesh("mother.x",mother_pivot)
	ScaleMesh mother,0.1,0.1,0.1
	MoveEntity mother,4880,0,0

	; Load orange ufo mesh
	orange_ufo=LoadMesh("orange_ufo.x")
	ScaleMesh orange_ufo,20,20,20
	EntityShininess orange_ufo,1
	EntityRadius orange_ufo,50
	EntityType orange_ufo,type_ufo
	HideEntity orange_ufo

	; Load yellow ufo mesh
	yellow_ufo=LoadMesh("yellow_ufo.x")
	ScaleMesh yellow_ufo,20,20,20
	EntityShininess yellow_ufo,1
	EntityRadius yellow_ufo,50
	EntityType yellow_ufo,type_ufo
	HideEntity yellow_ufo
	
	; Load green ufo mesh
	green_ufo=LoadMesh("green_ufo.x")
	ScaleMesh green_ufo,20,20,20
	EntityShininess green_ufo,1
	EntityRadius green_ufo,50
	EntityType green_ufo,type_ufo
	HideEntity green_ufo

	; Load blue ufo mesh
	blue_ufo=LoadMesh("blue_ufo.x")
	ScaleMesh blue_ufo,20,20,20
	EntityShininess blue_ufo,1
	EntityRadius blue_ufo,50
	EntityType blue_ufo,type_ufo
	HideEntity blue_ufo

	; Load pink ufo mesh
	pink_ufo=LoadMesh("pink_ufo.x")
	ScaleMesh pink_ufo,20,20,20
	EntityShininess pink_ufo,1
	EntityRadius pink_ufo,50
	EntityType pink_ufo,type_ufo
	HideEntity pink_ufo

	; Create orange shadow mesh
	orange_shadow=CreateMesh()
	orange_shadow_surf=CreateSurface(orange_shadow)
	AddVertex(orange_shadow_surf,-50,0,-50,0,0)
	AddVertex(orange_shadow_surf,-50,0,50,0,1)
	AddVertex(orange_shadow_surf,50,0,50,1,1)
	AddVertex(orange_shadow_surf,50,0,-50,1,0)
	AddTriangle(orange_shadow_surf,0,1,2)
	AddTriangle(orange_shadow_surf,0,2,3)
	orange_spark_tex=LoadTexture("orange_spark.bmp")
	EntityTexture orange_shadow,orange_spark_tex
	EntityBlend orange_shadow,3
	HideEntity orange_shadow

	; Create yellow shadow mesh
	yellow_shadow=CopyEntity(orange_shadow)
	yellow_spark_tex=LoadTexture("yellow_spark.bmp")
	EntityTexture yellow_shadow,yellow_spark_tex
	HideEntity yellow_shadow
	
	; Create green shadow mesh
	green_shadow=CopyEntity(orange_shadow)
	green_spark_tex=LoadTexture("green_spark.bmp")
	EntityTexture green_shadow,green_spark_tex
	HideEntity green_shadow

	; Create blue shadow mesh
	blue_shadow=CopyEntity(orange_shadow)
	blue_spark_tex=LoadTexture("blue_spark.bmp")
	EntityTexture blue_shadow,blue_spark_tex
	HideEntity blue_shadow

	; Create pink shadow mesh
	pink_shadow=CopyEntity(orange_shadow)
	pink_spark_tex=LoadTexture("pink_spark.bmp")
	EntityTexture pink_shadow,pink_spark_tex
	HideEntity pink_shadow

	; Create orange camel mesh
	orange_camel=LoadMesh("camel.x")
	ScaleMesh orange_camel,30,30,30
	PaintMesh orange_camel,CreateBrush()
	EntityColor orange_camel,255,102,0
	HideEntity orange_camel

	; Create yellow camel mesh
	yellow_camel=CopyEntity(orange_camel)
	EntityColor yellow_camel,255,255,0
	HideEntity yellow_camel
	
	; Create green camel mesh
	green_camel=CopyEntity(orange_camel)
	EntityColor green_camel,0,255,0
	HideEntity green_camel

	; Create blue camel mesh
	blue_camel=CopyEntity(orange_camel)
	EntityColor blue_camel,0,255,255
	HideEntity blue_camel
	
	; Create pink camel mesh
	pink_camel=CopyEntity(orange_camel)
	EntityColor pink_camel,255,0,255
	HideEntity pink_camel

	; Load sun sprite
	sun=LoadSprite("big_spark.bmp")

	; Load moon sprite
	moon=LoadSprite("moon.bmp",3)

	; Load bullet sprite
	bullet=LoadSprite("orange_spark.bmp")
	ScaleSprite bullet,10,10
	EntityRadius bullet,5
	EntityType bullet,type_plane1_bullet
	HideEntity bullet

	; Load orange spark sprite - acts as ufo bullet and ufo explosion spark
	orange_spark=LoadSprite("orange_spark.bmp")
	ScaleSprite orange_spark,10,10
	HideEntity orange_spark

	; Load yellow spark sprite - acts as ufo bullet and ufo explosion spark
	yellow_spark=LoadSprite("yellow_spark.bmp")
	ScaleSprite yellow_spark,10,10
	HideEntity yellow_spark

	; Load green spark sprite - acts as ufo bullet and ufo explosion spark
	green_spark=LoadSprite("green_spark.bmp")
	ScaleSprite green_spark,10,10
	HideEntity green_spark

	; Load blue spark sprite - acts as ufo bullet and ufo explosion spark
	blue_spark=LoadSprite("blue_spark.bmp")
	ScaleSprite blue_spark,10,10
	HideEntity blue_spark

	; Load pink spark sprite - acts as ufo bullet and ufo explosion spark
	pink_spark=LoadSprite("pink_spark.bmp")
	ScaleSprite pink_spark,10,10
	HideEntity pink_spark

	; Load 200 points sprite
	points_200=LoadSprite("200.bmp",3)
	EntityBlend points_200,1
	HideEntity points_200

	; Create 200x2 points sprite
	points_200x2=LoadSprite("200x2.bmp",3)
	EntityBlend points_200x2,1
	HideEntity points_200x2

	; Create 200x4 points sprite
	points_200x4=LoadSprite("200x4.bmp",3)
	EntityBlend points_200x4,1
	HideEntity points_200x4

	; Create 200x6 points sprite
	points_200x6=LoadSprite("200x6.bmp",3)
	EntityBlend points_200x6,1
	HideEntity points_200x6

	; Create 200x8 points sprite
	points_200x8=LoadSprite("200x8.bmp",3)
	EntityBlend points_200x8,1
	HideEntity points_200x8

	; Create 200x10 points sprite
	points_200x10=LoadSprite("200x10.bmp",3)
	EntityBlend points_200x10,1
	HideEntity points_200x10

	; Create 400 points sprite
	points_400=LoadSprite("400.bmp",3)
	EntityBlend points_400,1
	HideEntity points_400

	; Create 400x2 points sprite
	points_400x2=LoadSprite("400x2.bmp",3)
	EntityBlend points_400x2,1
	HideEntity points_400x2

	; Create 400x4 points sprite
	points_400x4=LoadSprite("400x4.bmp",3)
	EntityBlend points_400x4,1
	HideEntity points_400x4

	; Create 400x6 points sprite
	points_400x6=LoadSprite("400x6.bmp",3)
	EntityBlend points_400x6,1
	HideEntity points_400x6

	; Create 400x8 points sprite
	points_400x8=LoadSprite("400x8.bmp",3)
	EntityBlend points_400x8,1
	HideEntity points_400x8

	; Create 400x10 points sprite
	points_400x10=LoadSprite("400x10.bmp",3)
	EntityBlend points_400x10,1
	HideEntity points_400x10

	; Create 600 points sprite
	points_600=LoadSprite("600.bmp",3)
	EntityBlend points_600,1
	HideEntity points_600

	; Create 600x2 points sprite
	points_600x2=LoadSprite("600x2.bmp",3)
	EntityBlend points_600x2,1
	HideEntity points_600x2

	; Create 600x4 points sprite
	points_600x4=LoadSprite("600x4.bmp",3)
	EntityBlend points_600x4,1
	HideEntity points_600x4

	; Create 600x6 points sprite
	points_600x6=LoadSprite("600x6.bmp",3)
	EntityBlend points_600x6,1
	HideEntity points_600x6

	; Create 600x8 points sprite
	points_600x8=LoadSprite("600x8.bmp",3)
	EntityBlend points_600x8,1
	HideEntity points_600x8

	; Create 600x10 points sprite
	points_600x10=LoadSprite("600x10.bmp",3)
	EntityBlend points_600x10,1
	HideEntity points_600x10

	; Create 800 points sprite
	points_800=LoadSprite("800.bmp",3)
	EntityBlend points_800,1
	HideEntity points_800

	; Create 800x2 points sprite
	points_800x2=LoadSprite("800x2.bmp",3)
	EntityBlend points_800x2,1
	HideEntity points_800x2

	; Create 800x4 points sprite
	points_800x4=LoadSprite("800x4.bmp",3)
	EntityBlend points_800x4,1
	HideEntity points_800x4

	; Create 800x6 points sprite
	points_800x6=LoadSprite("800x6.bmp",3)
	EntityBlend points_800x6,1
	HideEntity points_800x6

	; Create 800x8 points sprite
	points_800x8=LoadSprite("800x8.bmp",3)
	EntityBlend points_800x8,1
	HideEntity points_800x8

	; Create 800x10 points sprite
	points_800x10=LoadSprite("800x10.bmp",3)
	EntityBlend points_800x10,1
	HideEntity points_800x10

	; Create 1000 points sprite
	points_1000=LoadSprite("1000.bmp",3)
	EntityBlend points_1000,1
	HideEntity points_1000

	; Create 1000x2 points sprite
	points_1000x2=LoadSprite("1000x2.bmp",3)
	EntityBlend points_1000x2,1
	HideEntity points_1000x2

	; Create 1000x4 points sprite
	points_1000x4=LoadSprite("1000x4.bmp",3)
	EntityBlend points_1000x4,1
	HideEntity points_1000x4

	; Create 1000x6 points sprite
	points_1000x6=LoadSprite("1000x6.bmp",3)
	EntityBlend points_1000x6,1
	HideEntity points_1000x6

	; Create 1000x8 points sprite
	points_1000x8=LoadSprite("1000x8.bmp",3)
	EntityBlend points_1000x8,1
	HideEntity points_1000x8

	; Create 1000x10 points sprite
	points_1000x10=LoadSprite("1000x10.bmp",3)
	EntityBlend points_1000x10,1
	HideEntity points_1000x10

	; Create bust points sprite
	points_bust=LoadSprite("bust.bmp",3)
	EntityBlend points_bust,1
	HideEntity points_bust

End Function


; ------------------------
; Init collisions function
; ------------------------

Function InitCollisions()

	Collisions type_plane1_bullet,type_ufo,1,1
	Collisions type_ufo_bullet,type_plane1,1,1
	Collisions type_plane1,type_statue,2,1

	Collisions type_plane2_bullet,type_ufo,1,1
	Collisions type_ufo_bullet,type_plane2,1,1
	Collisions type_plane2,type_statue,2,1
	
	Collisions type_spark,type_ufo,1,1

End Function


; --------------------
; Game input1 function
; --------------------

Function GameInput1()

	; Move up/down
	If KeyDown(200)=True Then plane1_dy=plane1_dy+2 : cam1_dy=cam1_dy+2
	If KeyDown(208)=True Then plane1_dy=plane1_dy-2 : cam1_dy=cam1_dy-2
	
	; Turn left/right
	If KeyDown(203)=True Then plane1_day=plane1_day+2 : cam1_day=cam1_day+2
	If KeyDown(205)=True Then plane1_day=plane1_day-2 : cam1_day=cam1_day-2

	; Move forward - normal
	If KeyDown(30)=True And blue_bonus1_status=False
		plane1_dx=plane1_dx-Sin(plane1_ay)*8
		plane1_dz=plane1_dz+Cos(plane1_ay)*8
		cam1_dx=cam1_dx-Sin(cam1_ay)*8
		cam1_dz=cam1_dz+Cos(cam1_ay)*8
	EndIf
	
	; Move forward - blue bonus - extra speed
	If KeyDown(30)=True And blue_bonus1_status=True
		plane1_dx=plane1_dx-Sin(plane1_ay)*16
		plane1_dz=plane1_dz+Cos(plane1_ay)*16
		cam1_dx=cam1_dx-Sin(cam1_ay)*16
		cam1_dz=cam1_dz+Cos(cam1_ay)*16
	EndIf

	; Fire bullet - normal
	If KeyHit(31)=True And yellow_bonus1_status=False
		p1.plane1_bullet=New plane1_bullet
		p1\entity=CopyEntity(bullet,plane1)
		EntityParent p1\entity,0
		TurnEntity p1\entity,0,270,0
		MoveEntity p1\entity,0,0,10
		p1\alpha=1
	EndIf
	
	; Fire bullet - yellow bonus - rapid fire
	yellow_bonus1_pause=yellow_bonus1_pause+1
	If KeyDown(31)=True And yellow_bonus1_status=True And yellow_bonus1_pause>=3
		yellow_bonus1_pause=0
		p1.plane1_bullet=New plane1_bullet
		p1\entity=CopyEntity(bullet,plane1)
		EntityParent p1\entity,0
		TurnEntity p1\entity,0,270,0
		MoveEntity p1\entity,0,0,10
		p1\alpha=1
	EndIf

End Function


; ----------------------
; Update plane1 function
; ----------------------

Function UpdatePlane1()

	; Prevent plane1 and main cam from going too low/high
	If plane1_dy<2 Then plane1_dy=2
	If plane1_dy>1000 Then plane1_dy=1000
	If cam1_dy<2 Then cam1_dy=2
	If cam1_dy>1000 Then cam1_dy=1000

	; Update plane1 position/angle values
	plane1_x=CurveValue#(plane1_x,plane1_dx,curve_plane)
	plane1_y=CurveValue#(plane1_y,plane1_dy,curve_plane)
	plane1_z=CurveValue#(plane1_z,plane1_dz,curve_plane)
	plane1_ay=CurveValue#(plane1_ay,plane1_day,curve_plane)

	PositionEntity plane1,plane1_x,plane1_y,plane1_z
	RotateEntity plane1,0,plane1_ay+90,0

	; Update camera1 position values
	cam1_x=CurveValue#(cam1_x,cam1_dx,curve_cam)
	cam1_y=CurveValue#(cam1_y,cam1_dy,curve_cam)
	cam1_z=CurveValue#(cam1_z,cam1_dz,curve_cam)
	cam1_ay=CurveValue#(cam1_ay,cam1_day,curve_cam)
	
	PositionEntity main_cam1,plane1_x+Sin(cam1_ay)*50,cam1_y,plane1_z-Cos(cam1_ay)*50
	RotateEntity main_cam1,0,cam1_ay,0
	
	PositionEntity top_cam1,plane1_x,plane1_y#+75,plane1_z
	RotateEntity top_cam1,90,plane1_ay,0
	
	; Update plane1 bullets
	For p1.plane1_bullet=Each plane1_bullet
		MoveEntity p1\entity,0,0,100
		If EntityDistance(p1\entity,plane1)>5000 Then FreeEntity p1\entity : Delete p1
	Next

	; Check to see if plane1 is inside bank zone and if so bank pot
	temp_pivot=CreatePivot()
	PositionEntity temp_pivot,red_ring_x,plane1_y,red_ring_z
	If EntityDistance(temp_pivot,plane1)<=625 And pot1>0 Then bank1=bank1+pot1 : pot1=0
	FreeEntity temp_pivot

End Function


; -----------------------
; Update camels1 function
; -----------------------

Function UpdateCamels1()

	For c.camel=Each camel
		
		; Turn camels, fade them
		TurnEntity c\entity,0,2,0
		EntityAlpha c\entity,c\alpha : c\alpha=c\alpha-0.0005
		
		; Use EntityDistance command to check for collision between camel and plane
		If EntityDistance(c\entity,plane1)<=40
			plane1_bonus=c\colour
			c\alpha=0
		EndIf
		
		; Delete camel if invisible
		If c\alpha<=0 Then FreeEntity c\entity : Delete c
			
	Next
	
	; Orange bonus - shield
	
	; Time
	If orange_bonus1_time>300 Then EntityAlpha shield1,0.5 Else EntityAlpha shield1,orange_bonus1_time/600.0
	If orange_bonus1_time>0 Then orange_bonus1_time=orange_bonus1_time-1
	
	; Activate
	If plane1_bonus=1
		orange_bonus1_status=True
		orange_bonus1_time=orange_bonus1_time+600
		EntityType plane1,type_none
		plane1_bonus=0
	EndIf
	
	; Deactivate
	If orange_bonus1_time=0 And orange_bonus1_status=True
		orange_bonus1_status=False
		EntityType plane1,type_plane1
	EndIf
	
	; Yellow bonus - rapid fire
	If yellow_bonus1_time>0 Then yellow_bonus1_time=yellow_bonus1_time-1 ; time
	If plane1_bonus=2 Then yellow_bonus1_status=True : yellow_bonus1_time=yellow_bonus1_time+600 : plane1_bonus=0 ; activate
	If yellow_bonus1_time=0 And yellow_bonus1_status=True Then yellow_bonus1_status=False ; deactivate
	
	; Green bonus - homing missiles
	If green_bonus1_time>0 Then green_bonus1_time=green_bonus1_time-1 ; time
	If plane1_bonus=3 Then green_bonus1_status=True : green_bonus1_time=green_bonus1_time+600 : plane1_bonus=0 ; activate
	If green_bonus1_time=0 And green_bonus1_status=True Then green_bonus1_status=False ; deactivate
	
	; blue bonus - extra speed
	If blue_bonus1_time>0 Then blue_bonus1_time=blue_bonus1_time-1 ; time
	If plane1_bonus=4 Then blue_bonus1_status=True : blue_bonus1_time=blue_bonus1_time+600 : plane1_bonus=0 ; activate
	If blue_bonus1_time=0 And blue_bonus1_status=True Then blue_bonus1_status=False ; deactivate
	
	; Pink bonus - UFO to rings
	If pink_bonus1_time>0 Then pink_bonus1_time=pink_bonus1_time-1 ; time
	If plane1_bonus=5 Then pink_bonus1_status=True : pink_bonus1_time=pink_bonus1_time+600 : plane1_bonus=0 ; activate
	If pink_bonus1_time=0 And pink_bonus1_status=True Then pink_bonus1_status=False ; deactivate

End Function


; --------------------------
; Check collisions1 function
; --------------------------

Function CheckCollisions1()

	; Ufo and plane1 bullet
	For u.ufo=Each ufo
		entity_col=EntityCollided(u\entity,type_plane1_bullet)
		If entity_col<>0
			DeleteBullet1(entity_col)
			pot1=pot1+Points(u)
			CreateShadow(u) ; Create a new shadow that will reflect explosion sparks after old ufo + shadow have been nuked
			ExplodeUFO(u,1)
		EndIf
	Next

	; Ufo bullet and plane1
	If orange_bonus1_status=False
		For ub.ufo_bullet=Each ufo_bullet
			If EntityCollided(ub\entity,type_plane1)<>0
				If pot1>0
					pot1=0
					FreeEntity ub\entity
					Delete ub
					po.points=New points
					po\entity=CopyEntity(points_bust,plane1)
					ShowEntity po\entity
					EntityParent po\entity,0
					po\alpha=1
				Else
					game_over1=True
					ExplodePlane(plane1)
					If no_players=1
						HighScore()
					EndIf
				EndIf
			EndIf
		Next
	EndIf

	; Plane1 and statue
	If EntityCollided(plane1,type_statue)<>0 Then game_over1=True : ExplodePlane(plane1) : If no_players=1 Then HighScore()
	
End Function


; ----------------------
; Update mother function
; ----------------------

Function UpdateMother()

	If MilliSecs()-mother_timer=>1000
		If no_ufos<max_ufos ; No. ufos limit
			ufo_count=ufo_count+1
			If ufo_count<=5 Then new_ufo=1 : mother_timer=MilliSecs()
			If ufo_count>=6 And ufo_count<=9 Then new_ufo=2 : mother_timer=MilliSecs()
			If ufo_count>=10 And ufo_count<=12 Then new_ufo=3 : mother_timer=MilliSecs()
			If ufo_count>=13 And ufo_count<=14 Then new_ufo=4 : mother_timer=MilliSecs()
			If ufo_count=15 Then new_ufo=5 : ufo_count=0 : mother_timer=MilliSecs()
		EndIf
	EndIf

	TurnEntity mother_pivot,0,0.1,0

End Function


; -------------------
; Update ufo function
; -------------------

Function UpdateUFO()

	If new_ufo<>0 Then CreateUFO(new_ufo) : new_ufo=0

	For u.ufo=Each ufo
		MoveUFO(u)
		UFOFire(u)
		If MilliSecs()-trail_timer=>500 And u\colour=5 Then CreateTrail(u) : reset_trail_timer=True
	Next
	
	If reset_trail_timer=True Then trail_timer=MilliSecs() : reset_trail_timer=False
	UFOBullets()
	
End Function	


; --------------------------
; Update explosions function	
; --------------------------

Function UpdateExplosions()

	; Explosion sparks
	For sp.spark=Each spark
		If sp\alpha>0 And EntityY(sp\entity)>-10
			EntityAlpha sp\entity,sp\alpha
			MoveEntity sp\entity,0,0,4
			TranslateEntity sp\entity,0,sp\y_speed,0
			sp\alpha=sp\alpha-0.01
			sp\y_speed=sp\y_speed-0.1
		Else
			FreeEntity sp\entity
			Delete sp
		EndIf
	Next
	
	; Shadows that reflect the sparks
	For s.shadow=Each shadow
		ScaleEntity s\entity,s\scale,1,s\scale
		EntityAlpha s\entity,s\alpha
		s\scale=s\scale+0.1
		s\alpha=s\alpha-0.01
		If s\alpha=<0 Then HideEntity s\entity : FreeEntity s\entity : Delete s
	Next

	; Points sprites eg. "200x2"
	For po.points=Each points
		ScaleSprite po\entity,po\scale,po\scale
		EntityAlpha po\entity,po\alpha
		po\scale=po\scale+10
		po\alpha=po\alpha-0.04
		If po\alpha=<0 Then HideEntity po\entity : FreeEntity po\entity : Delete po
	Next

End Function


; ----------------------
; Update extras function
; ----------------------

Function UpdateExtras()

	If no_players=1
		PositionEntity sky,EntityX(main_cam1),0,EntityZ(main_cam1)
		PositionEntity solar,EntityX(main_cam1),EntityY(main_cam1)+solar_y,EntityZ(main_cam1)-solar_z
		ScaleSprite solar,Rnd(solar_scale,solar_scale+solar_scale_shake),Rnd(solar_scale,solar_scale+solar_scale_shake)
	EndIf

End Function


; ------------------
; Render 2D function
; ------------------

Function Render2D()

	If no_players=1
	
		Color 255,0,0
		Text (width+height)/140,height-((width+height)/14.5),"Pot: "+pot1+" "

		Color 255,255,255
		Text (width+height)/140,height-((width+height)/24.75),"Bank: "+bank1+" "
		
		If orange_bonus1_time>0
			Color 255,102,0
			Text width-((width+height)/22.4),0,(orange_bonus1_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
		If yellow_bonus1_time>0
			Color 255,255,0
			Text width-((width+height)/22.4),time_y,(yellow_bonus1_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
		If green_bonus1_time>0
			Color 0,255,0
			Text width-((width+height)/22.4),time_y,(green_bonus1_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
		If blue_bonus1_time>0
			Color 0,255,255
			Text width-((width+height)/22.4),time_y,(blue_bonus1_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
		If pink_bonus1_time>0
			Color 255,0,255
			Text width-((width+height)/22.4),time_y,(pink_bonus1_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
	EndIf
	
	If no_players=2
		
		Color 255,0,0
		Text (width+height)/140,(height/2)-((width+height)/14.5),"Pot: "+pot1+" "
		Text (width+height)/140,height-((width+height)/14.5),"Pot: "+pot2+" "
		
		Color 255,255,255
		Text (width+height)/140,(height/2)-((width+height)/24.75),"Bank: "+bank1+" "
		Text (width+height)/140,height-((width+height)/24.75),"Bank: "+bank2+" "
		
		; Player 1 bonus times
		
		If orange_bonus1_time>0
			Color 255,102,0
			Text width-((width+height)/22.4),time_y,(orange_bonus1_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
		If yellow_bonus1_time>0
			Color 255,255,0
			Text width-((width+height)/22.4),time_y,(yellow_bonus1_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
		If green_bonus1_time>0
			Color 0,255,0
			Text width-((width+height)/22.4),time_y,(green_bonus1_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
		If blue_bonus1_time>0
			Color 0,255,255
			Text width-((width+height)/22.4),time_y,(blue_bonus1_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
		If pink_bonus1_time>0
			Color 255,0,255
			Text width-((width+height)/22.4),time_y,(pink_bonus1_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
		; Player 2 bonus times
		
		time_y=height/2
		If orange_bonus2_time>0
			Color 255,102,0
			Text width-((width+height)/22.4),time_y,(orange_bonus2_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
		If yellow_bonus2_time>0
			Color 255,255,0
			Text width-((width+height)/22.4),time_y,(yellow_bonus2_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
		If green_bonus2_time>0
			Color 0,255,0
			Text width-((width+height)/22.4),time_y,(green_bonus2_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
		If blue_bonus2_time>0
			Color 0,255,255
			Text width-((width+height)/22.4),time_y,(blue_bonus2_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
		If pink_bonus2_time>0
			Color 255,0,255
			Text width-((width+height)/22.4),time_y,(pink_bonus2_time/60)+1
			time_y=time_y+(width+height)/31.98
		EndIf
		
	EndIf
	
End Function


; -----------------------
; Delete bullet1 function
; -----------------------

Function DeleteBullet1(entity_col)

	; Delete bullet that hits ufo
	For p1.plane1_bullet=Each plane1_bullet
		If p1\entity=entity_col Then FreeEntity p1\entity : Delete p1 : Exit
	Next

End Function


; ----------------------
; Explode plane function
; ----------------------

Function ExplodePlane(plane) 

	; Create a new set of sparks
	For t=1 To plane_intensity
		sp.spark=New spark
		sp\y_speed=0
		sp\alpha=2
		sp\entity=CopyEntity(debris,plane)
		EntityParent sp\entity,0
		RotateEntity sp\entity,Rnd(360),Rnd(360),Rnd(360)
		ShowEntity sp\entity
	Next

	; Hide plane
	HideEntity plane

	; "BOOM!"
	PlaySound explode
	
End Function


; -------------------
; Create ufo function
; -------------------

Function CreateUfo(new_ufo)

	u.ufo=New ufo
		
	Select new_ufo
		Case 1 : u\entity=CopyEntity(orange_ufo,mother) : u\shadow=CopyEntity(orange_shadow) : u\colour=1
		Case 2 : u\entity=CopyEntity(yellow_ufo,mother) : u\shadow=CopyEntity(yellow_shadow) : u\colour=2
		Case 3 : u\entity=CopyEntity(green_ufo,mother) : u\shadow=CopyEntity(green_shadow) : u\colour=3
		Case 4 : u\entity=CopyEntity(blue_ufo,mother) : u\shadow=CopyEntity(blue_shadow) : u\colour=4
		Case 5 : u\entity=CopyEntity(pink_ufo,mother) : u\shadow=CopyEntity(pink_shadow) : u\colour=5
	End Select

	EntityParent u\entity,0
	MoveEntity u\entity,0,0,-100

	If no_players=1 Then u\target=plane1

	If no_players=2
		If game_over1<>True And game_over2<>True
			d1#=EntityDistance(u\entity,plane1)
			d2#=EntityDistance(u\entity,plane2)
			If d1#<=d2# Then u\target=plane1 Else u\target=plane2
		Else
			If game_over1=True Then u\target=plane2
			If game_over2=True Then u\target=plane1
		EndIf
	EndIf

	u\x=EntityX(u\entity)
	u\y=EntityY(u\entity)
	u\z=EntityZ(u\entity)
	u\count=179
	no_ufos=no_ufos+1

End Function


; ---------------------
; Create trail function
; ---------------------

Function CreateTrail(u.ufo)

	; New pink ufo trail
	t.trail=New trail
	t\entity=CopyEntity(u\entity)
	EntityType t\entity,type_none
	t\alpha=1

End Function


; -----------------
; Move ufo function
; -----------------

Function MoveUFO(u.ufo)

	; Move UFO towards destination values
	
	u\count=u\count+1
	u\x=u\x+((u\dx-u\x)/100.0)
	u\y=u\y+((u\dy-u\y)/100.0)
	u\z=u\z+((u\dz-u\z)/100.0)
	
	PositionEntity u\entity,u\x,u\y,u\z
	PointEntity u\entity,u\target
	TurnEntity u\entity,0,180,0
	RotateEntity u\entity,EntityPitch(u\entity)/2,EntityYaw(u\entity),0

	; Reset ufo destination values every 180 updates (3 seconds)
	
	If u\count>=180
		u\count=0
		
		If pink_bonus1_status=False

			; Normal
			If u\target=plane1
				u\dx=plane1_x+Rnd(-1500,1500)
				u\dz=plane1_z+Rnd(-1500,1500)
				If plane1_y>=150 Then u\dy=plane1_y+Rnd(-100,100) Else u\dy=plane1_y+Rnd(50,150)
			Else
				u\dx=plane2_x+Rnd(-1500,1500)
				u\dz=plane2_z+Rnd(-1500,1500)
				If plane2_y>=150 Then u\dy=plane2_y+Rnd(-100,100) Else u\dy=plane2_y+Rnd(50,150)
			EndIf
			
		Else
		
			; Pink bonus
			Select u\colour
				Case 1
				u\dx=orange_ring_x+Rnd(-orange_ring_radius,orange_ring_radius)
				u\dz=orange_ring_z+Rnd(-orange_ring_radius,orange_ring_radius)
				Case 2
				u\dx=yellow_ring_x+Rnd(-yellow_ring_radius,yellow_ring_radius)
				u\dz=yellow_ring_z+Rnd(-yellow_ring_radius,yellow_ring_radius)
				Case 3
				u\dx=green_ring_x+Rnd(-green_ring_radius,green_ring_radius)
				u\dz=green_ring_z+Rnd(-green_ring_radius,green_ring_radius)
				Case 4
				u\dx=blue_ring_x+Rnd(-blue_ring_radius,blue_ring_radius)
				u\dz=blue_ring_z+Rnd(-blue_ring_radius,blue_ring_radius)
				Case 5
				u\dx=pink_ring_x+Rnd(-pink_ring_radius,pink_ring_radius)
				u\dz=pink_ring_z+Rnd(-pink_ring_radius,pink_ring_radius)
			End Select

		EndIf
		
	EndIf

	; Position shadow
	PositionEntity u\shadow,u\x,1,u\z

End Function


; -----------------
; Ufo fire function
; -----------------

Function UFOFire(u.ufo)

	; Random ufo fire
	
	If Int(Rnd(0,900))=450
			ub.ufo_bullet=New ufo_bullet

			Select u\colour
				Case 1 : ub\entity=CopyEntity(orange_spark,u\entity)
				Case 2 : ub\entity=CopyEntity(yellow_spark,u\entity)
				Case 3 : ub\entity=CopyEntity(green_spark,u\entity)
				Case 4 : ub\entity=CopyEntity(blue_spark,u\entity)
				Case 5 : ub\entity=CopyEntity(pink_spark,u\entity)
			End Select

			piv=CreatePivot(u\target)
			PointEntity ub\entity,piv
			TurnEntity ub\entity,0,180,0
			FreeEntity piv

			EntityParent ub\entity,0
			EntityType ub\entity,type_ufo_bullet
			EntityRadius ub\entity,5
			ShowEntity ub\entity
	EndIf

End Function


; --------------------
; Ufo bullets function
; --------------------

Function UFOBullets()

	; Update ufo trail
	For t.trail=Each trail
		EntityAlpha t\entity,t\alpha
		t\alpha=t\alpha-0.01
		If t\alpha<=0
			FreeEntity t\entity
			Delete t		
		EndIf
	Next

	; Update ufo bullets
	For ub.ufo_bullet=Each ufo_bullet
		b.bullet_trail=New bullet_trail : b\entity=CopyEntity(ub\entity) : EntityType b\entity,0 : b\alpha=1
		MoveEntity ub\entity,0,0,-20
		If EntityDistance(ub\entity,plane1)>5000 Or EntityY(ub\entity)<=-10 Then FreeEntity ub\entity : Delete ub
	Next

	; Update ufo bullets trail
	For b.bullet_trail=Each bullet_trail
		b\alpha=b\alpha-0.1
		EntityAlpha b\entity,b\alpha
		If b\alpha<=0 Then FreeEntity b\entity : Delete b
	Next

End Function


; --------------------
; Explode ufo function
; --------------------

Function ExplodeUFO(u.ufo,player)

	; Create a new set of sparks
	
	For t=1 To ufo_intensity
		sp.spark=New spark
		sp\y_speed=0
		sp\alpha=2
		If u\colour=1 Then sp\entity=CopyEntity(orange_spark,u\entity)
		If u\colour=2 Then sp\entity=CopyEntity(yellow_spark,u\entity)
		If u\colour=3 Then sp\entity=CopyEntity(green_spark,u\entity)
		If u\colour=4 Then sp\entity=CopyEntity(blue_spark,u\entity)
		If u\colour=5 Then sp\entity=CopyEntity(pink_spark,u\entity)
		EntityParent sp\entity,0
		RotateEntity sp\entity,Rnd(360),Rnd(360),Rnd(360)

		If green_bonus1_status=True And player=1
			EntityRadius sp\entity,500
			EntityType sp\entity,type_plane1_bullet
		EndIf

		If green_bonus2_status=True And player=2
			EntityRadius sp\entity,500
			EntityType sp\entity,type_plane2_bullet
		EndIf
	Next

	; Get rid of ufo
	HideEntity u\entity
	FreeEntity u\entity
	Delete u
	no_ufos=no_ufos-1

	; "BOOM!"
	PlaySound explode
	
End Function


; ----------------------
; Create shadow function
; ----------------------

Function CreateShadow(u.ufo)

	s.shadow=New shadow
	s\entity=CopyEntity(u\shadow)
	s\scale=1
	s\alpha=1

	HideEntity u\shadow
	FreeEntity u\shadow

End Function


; ---------------
; Points function
; ---------------

Function Points(u.ufo)

	Select u\colour
		Case 1 : single=200
		Case 2 : single=400
		Case 3 : single=600
		Case 4 : single=800
		Case 5 : single=1000
	End Select
			
	temp_pivot=CreatePivot()
	multi=1
	PositionEntity temp_pivot,red_ring_x,u\y,red_ring_z : If EntityDistance(temp_pivot,u\entity)<=red_ring_radius Then multi=0
	PositionEntity temp_pivot,orange_ring_x,u\y,orange_ring_z : If EntityDistance(temp_pivot,u\entity)<=orange_ring_radius Then multi=2
	PositionEntity temp_pivot,yellow_ring_x,u\y,yellow_ring_z : If EntityDistance(temp_pivot,u\entity)<=yellow_ring_radius Then multi=4
	PositionEntity temp_pivot,green_ring_x,u\y,green_ring_z : If EntityDistance(temp_pivot,u\entity)<=green_ring_radius Then multi=6
	PositionEntity temp_pivot,blue_ring_x,u\y,blue_ring_z : If EntityDistance(temp_pivot,u\entity)<=blue_ring_radius Then multi=8
	PositionEntity temp_pivot,pink_ring_x,u\y,pink_ring_z : If EntityDistance(temp_pivot,u\entity)<=pink_ring_radius Then multi=10

	points=single*multi
			
	If multi=0 Then points=0 : po.points=New points : po\entity=CopyEntity(points_bust,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=200 And multi=1 Then po.points=New points : po\entity=CopyEntity(points_200,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=200 And multi=2 Then po.points=New points : po\entity=CopyEntity(points_200x2,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2 : c.camel=New camel : c\entity=CopyEntity(orange_camel,u\entity) : EntityParent c\entity,0 : c\colour=1 : c\alpha=1 
	If single=200 And multi=4 Then po.points=New points : po\entity=CopyEntity(points_200x4,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=200 And multi=6 Then po.points=New points : po\entity=CopyEntity(points_200x6,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=200 And multi=8 Then po.points=New points : po\entity=CopyEntity(points_200x8,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=200 And multi=10 Then po.points=New points : po\entity=CopyEntity(points_200x10,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=400 And multi=1 Then po.points=New points : po\entity=CopyEntity(points_400,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=400 And multi=2 Then po.points=New points : po\entity=CopyEntity(points_400x2,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=400 And multi=4 Then po.points=New points : po\entity=CopyEntity(points_400x4,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2 : c.camel=New camel : c\entity=CopyEntity(yellow_camel,u\entity) : EntityParent c\entity,0 : c\colour=2 : c\alpha=1
	If single=400 And multi=6 Then po.points=New points : po\entity=CopyEntity(points_400x6,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=400 And multi=8 Then po.points=New points : po\entity=CopyEntity(points_400x8,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=400 And multi=10 Then po.points=New points : po\entity=CopyEntity(points_400x10,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=600 And multi=1 Then po.points=New points : po\entity=CopyEntity(points_600,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=600 And multi=2 Then po.points=New points : po\entity=CopyEntity(points_600x2,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=600 And multi=4 Then po.points=New points : po\entity=CopyEntity(points_600x4,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=600 And multi=6 Then po.points=New points : po\entity=CopyEntity(points_600x6,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2 : c.camel=New camel : c\entity=CopyEntity(green_camel,u\entity) : EntityParent c\entity,0 : c\colour=3 : c\alpha=1
	If single=600 And multi=8 Then po.points=New points : po\entity=CopyEntity(points_600x8,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=600 And multi=10 Then po.points=New points : po\entity=CopyEntity(points_600x10,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=800 And multi=1 Then po.points=New points : po\entity=CopyEntity(points_800,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=800 And multi=2 Then po.points=New points : po\entity=CopyEntity(points_800x2,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=800 And multi=4 Then po.points=New points : po\entity=CopyEntity(points_800x4,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=800 And multi=6 Then po.points=New points : po\entity=CopyEntity(points_800x6,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=800 And multi=8 Then po.points=New points : po\entity=CopyEntity(points_800x8,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2 : c.camel=New camel : c\entity=CopyEntity(blue_camel,u\entity) : EntityParent c\entity,0 : c\colour=4 : c\alpha=1
	If single=800 And multi=10 Then po.points=New points : po\entity=CopyEntity(points_800x10,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=1000 And multi=1 Then po.points=New points : po\entity=CopyEntity(points_1000,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=1000 And multi=2 Then po.points=New points : po\entity=CopyEntity(points_1000x2,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=1000 And multi=4 Then po.points=New points : po\entity=CopyEntity(points_1000x4,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=1000 And multi=6 Then po.points=New points : po\entity=CopyEntity(points_1000x6,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=1000 And multi=8 Then po.points=New points : po\entity=CopyEntity(points_1000x8,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2
	If single=1000 And multi=10 Then po.points=New points : po\entity=CopyEntity(points_1000x10,u\entity) : ShowEntity po\entity : EntityParent po\entity,0 : po\alpha=2 : c.camel=New camel : c\entity=CopyEntity(pink_camel,u\entity) : EntityParent c\entity,0 : c\colour=5 : c\alpha=1

	FreeEntity temp_pivot

	Return points

End Function


; -------------------
; High score function
; -------------------

Function HighScore()

	; Load high score data
	load_file=ReadFile("high_scores.sav")
	For s=1 To 10
		names$(s)=ReadLine(load_file)
		scores(s)=ReadLine(load_file)
	Next
	CloseFile load_file

	; Insert high score
	
	player_name$=""

	rank=1
	While bank1<scores(rank) ; scores(11) = zero value so score will never be lower than that, so rank = 11 max
		rank=rank+1
	Wend

	If rank<=10 ; If score is high enough to get on high score table	
		For s=10 To rank Step -1
			scores(s)=scores(s-1)
			names$(s)=names$(s-1)
		Next
		names$(rank)=player_name$
		scores(rank)=bank1
		screen=6
		option=1
	EndIf
	
	; Save high score data
	save_file=WriteFile("high_scores.sav")
	For s=1 To 10
		WriteLine save_file,names$(s)
		WriteLine save_file,scores(s)
	Next
	CloseFile save_file

End Function


;---------------------
; Curve value function
;---------------------

Function CurveValue#(current#,destination#,curve)

	current#=current#+((destination#-current#)/curve)
	
	Return current#

End Function