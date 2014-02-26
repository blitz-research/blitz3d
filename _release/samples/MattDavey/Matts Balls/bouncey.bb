;======================================================
;================== BY MATT DAVEY =====================
;============= www.fusionstudio.f2s.com ===============
;======================================================

;Wood texture used courtesy of Noctua Graphics
;www.noctua-graphics.de or www.hisoft.co.uk

Graphics3D 640 , 480 , 32
SetBuffer BackBuffer()

SeedRnd MilliSecs()

Const CT_BALL = 1
Const CT_PLANE = 2

Collisions CT_BALL , CT_PLANE , 2 , 0

;=================================== [ Data Structures ]

Type Ball
	Field Entity
	Field x# , y# , z#
	Field xs# , ys# , zs#
	Field r , g , b
End Type

Type Spark
	Field Sprite
	Field x# , y# , z#
	Field Alpha#
End Type

;============================================== [ Vars ]

Local sysFpsTimer1#
Local sysFpsTimer2#
Local sysFps

Ground = LoadMesh("Plane.3ds")

	PositionEntity(Ground , 0 , 0 , 0)
	EntityType(Ground , CT_PLANE)
	RotateMesh(Ground , 0 , 0 , 0)
	ScaleMesh(Ground , 2 , 2 , 2)
	EntityColor(Ground , 128 , 128 , 128)

GroundTexture = LoadTexture("planks02.jpg")
	EntityTexture(Ground , GroundTexture)

Global SparkTemplate = LoadSprite("spark.bmp")

	ScaleSprite(SparkTemplate , 15 , 15)
	SpriteViewMode(SparkTemplate , 2)
	RotateEntity(SparkTemplate , 90 , 0 , 0)
	PositionEntity(SparkTemplate , 0 , -1000 , 0)
	EntityBlend(SparkTemplate,3)

Local CamPivot = CreatePivot()

Local Cam = CreateCamera(CamPivot)

	PositionEntity(Cam , 0 , 30 , -100)
	PointEntity(Cam , Ground)

DirectionLight1 = CreateLight(1)
	LightColor(DirectionLight1 , -128 , -128 , -128)
	RotateEntity(DirectionLight1 , 0 , 180 , 0)

DirectionLight2 = CreateLight(1)

	FrameTimer = CreateTimer(60)

	StartTime = MilliSecs()

Repeat ;================================== [ Main Loop ]

	CurTime = MilliSecs()

	If CurTime > StartTime + (5000 - sysFps * 50)
		CreateBall()
		StartTime = MilliSecs()
	EndIf


	TurnEntity(CamPivot , 0 , 0.3 , 0)

	UpdateBalls(0.05)
	UpdateSparks()

	If KeyHit(17)
		WireMode = Not WireMode
		WireFrame WireMode
	EndIf

UpdateWorld()
RenderWorld()

	sysFpsTimer2# = MilliSecs()
	sysFps = 1.0 / ( (sysFpsTimer2 - sysFpsTimer1 ) / 1000.0)

	sysFpsTimer1 = sysFpsTimer2

	WaitTimer(FrameTimer)

	If KeyHit(57) Then SaveBuffer(FrontBuffer(),"scrn.bmp")

Flip True
Until KeyHit(1); Or MouseXSpeed()<>0 Or MouseYSpeed()<>0


End

;======================================== [ Functions ]

Function UpdateBalls(Gravity#)

	For instance.Ball = Each Ball

		If Not instance = Null

			instance\x# = instance\x# + instance\xs#
			instance\z# = instance\z# + instance\zs#

			instance\ys# = instance\ys# - Gravity#
			instance\y# = instance\y# + instance\ys#

			If EntityCollided(instance\Entity , CT_PLANE)

				CreateSpark(instance\x# , instance\z# , instance\r , instance\g , instance\b)

				instance\ys# = Abs(instance\ys# / 1.08)

			EndIf

			PositionEntity(instance\Entity , instance\x# , instance\y# , instance\z#)

			If instance\y# < -400
				FreeEntity instance\Entity
				Delete instance
			EndIf

		EndIf

	Next

End Function

;====================================================

Function CreateSpark(x# , z# , r , g , b)

	instance.Spark = New Spark

		instance\Sprite = CopyEntity(SparkTemplate)
		instance\x# = x#
		instance\z# = z#
		instance\Alpha = 1.0

		EntityColor(instance\Sprite , r , g , b)
		PositionEntity(instance\Sprite , x# , 0.07 , z#)

End Function

;====================================================

Function UpdateSparks()

	For instance.Spark = Each Spark

		If Not instance = Null

			instance\Alpha = instance\Alpha - 0.01
			EntityAlpha(instance\Sprite , instance\Alpha)

			If instance\Alpha <= 0

				FreeEntity instance\Sprite
				Delete instance

			EndIf

		EndIf

	Next

End Function

;====================================================

Function CreateBall()

	Ball.Ball = New Ball

		Ball\Entity = CreateSphere()
			EntityType(Ball\Entity , CT_BALL)
			ScaleEntity(Ball\Entity , 3 , 3 , 3)
			EntityRadius(Ball\Entity , 4)
			EntityShininess(Ball\Entity , 5.0)

		Ball\x# = 0
		Ball\y# = 80
		Ball\z# = 0

	Repeat

		Ball\xs# = Rnd(-0.3 , 0.3)
		Ball\ys# = 0
		Ball\zs# = Rnd(-0.3 , 0.3)

	Until (Ball\xs# > 0.2 Or Ball\xs# < -0.2) Or (Ball\zs# > 0.2 Or ball\zs# < -0.2)

		Ball\r = Rnd(255)
		Ball\g = Rnd(255)
		Ball\b = Rnd(255)

		EntityColor(Ball\Entity , Ball\r , Ball\g , Ball\b)

End Function

;====================================================