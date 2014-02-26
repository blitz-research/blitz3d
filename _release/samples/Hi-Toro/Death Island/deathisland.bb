
; ---------------------------------------------------------------
; An exercise in atmosphere...
; ---------------------------------------------------------------

; Run it at night with the lights off (NOT during the day!).

; Use MOUSE and CURSORS, QIII-style...

; DO NOT try to learn anything from this program, apart from the effects
; of tweaking various parameters, perhaps. This is a serious hack-job!

; ---------------------------------------------------------------
; It's ugly down there, men...
; ---------------------------------------------------------------

Type Timer
	Field start
	Field timeOut
End Type

Function SetTimer.Timer (timeOut)
	t.Timer = New Timer
	t\start   = MilliSecs ()
	t\timeOut = t\start + timeOut
	Return t
End Function

Function TimeOut (test.Timer)
	If test <> Null
		If test\timeOut < MilliSecs ()
			Delete test
			Return 1
		EndIf
	EndIf
End Function

; ---------------------------------------------------------------
; Lens stuff...
; ---------------------------------------------------------------

Global lensDiameter = 25
Include "incs/lensIncs.bb"

; ---------------------------------------------------------------
; Graphics/light...
; ---------------------------------------------------------------

Global dWidth  = 640
Global dHeight = 480

Graphics3D dWidth, dHeight
SetBuffer BackBuffer ()

AmbientLight 0, 0, 0

wind 	 = LoadSound ("snd/seawind2.wav")
sea  	 = LoadSound ("snd/wavdance.wav")
rain 	 = LoadSound ("snd/rainroof.wav")
thunder1 = LoadSound ("snd/thunder.wav")
thunder2 = LoadSound ("snd/txstorm.wav")
run	 	 = LoadSound ("snd/gravel.wav")
wade	 = LoadSound ("snd/water2.wav")

; ---------------------------------------------------------------
; 747...
; ---------------------------------------------------------------

Function CenterPivot (model)
	FitMesh model, 0, 0, 0, MeshWidth (model), MeshHeight (model), MeshDepth (model)
	modelPivot = CreatePivot ()
	PositionEntity modelPivot, MeshWidth (model) / 2, MeshHeight (model) / 2, MeshDepth (model) / 2
	EntityParent model, modelPivot
	Return modelPivot
End Function

gimme747 = 1;0

; ---------------------------------------------------------------
; Don't ask...
; ---------------------------------------------------------------

If gimme747
	b747Model = LoadMesh ("msh/747.x")
	EntityShininess b747Model, 0.1
	b747 = CenterPivot (b747Model)
	ScaleEntity b747, 600, 600, 600 ; 500
	PositionEntity b747, 3750, 400, 1300
	RotateEntity b747, 3, 45, -1
	scaler = 2
EndIf

; ---------------------------------------------------------------
; Terrain...
; ---------------------------------------------------------------

terrain = LoadTerrain ("gfx/height.bmp")
ScaleEntity terrain, 5, 150, 5
TerrainShading terrain, True
TerrainDetail terrain, 2500, True
grass = LoadTexture ("gfx/greygrass.bmp")
ScaleTexture grass, 20, 20
EntityTexture terrain, grass, 0, 1
MoveEntity terrain, 0, -4, 0

seabed = CreatePlane ()
EntityTexture seabed, grass
MoveEntity seabed, 0, -3.9, 0

; ---------------------------------------------------------------
; Water...
; ---------------------------------------------------------------

water = CreatePlane ()
h20 = LoadTexture ("gfx/greywater.bmp")
EntityAlpha water, 0.75
PositionEntity water, 0, 2.5, 0
ScaleTexture h20, 200, 200
EntityTexture water, h20
EntityColor water, 64, 64, 64
EntityShininess water, 0.05

; ---------------------------------------------------------------
; Camera and fog...
; ---------------------------------------------------------------

cam = CreatePivot ()
EntityRadius cam, 2

camera = CreateCamera (cam)
CameraViewport camera, 0, 0, dWidth, dHeight
CameraFogMode camera, 1
CameraFogRange camera, 1, 1600; * scaler
CameraFogColor camera, 0, 0, 0;75, 75, 75
CameraRange camera, 1, 1600 * scaler
PositionEntity cam, 2340, 0, 2390 ; 1394, 0, 4660 ;1298, 0, 4653 ; 3750, 45, 1370

dome = CreateSphere (12)
clouds = LoadTexture ("gfx/realsky.bmp")
;ScaleEntity dome, 1300 * scaler, 1300 * scaler, 1300 * scaler
ScaleEntity dome, 1600 * scaler, 1600 * scaler, 1600 * scaler
EntityTexture dome, clouds
ScaleTexture clouds, 0.25, 0.25
EntityOrder dome, 1
FlipMesh dome
EntityAlpha dome, 0.25
EntityFX dome, 8

flash = CreateLight ()
LightColor flash, 0, 0, 0
PositionEntity flash, 1900, 100, 0
RotateEntity flash, 90, 0, 0

; ---------------------------------------------------------------
; Collisions...
; ---------------------------------------------------------------
	
Const ENTITY_TERRAIN = 1
Const ENTITY_CAM	 = 2
Const ENTITY_PLANE	 = 3

EntityType terrain, ENTITY_TERRAIN
EntityType cam, ENTITY_CAM

If gimme747
	EntityType b747Model, ENTITY_PLANE
EndIf

Collisions ENTITY_CAM, ENTITY_TERRAIN, 2, 2
Collisions ENTITY_CAM, ENTITY_PLANE, 2, 2

Function CurveValue#(current#,destination#,curve)
	current#=current#+((destination#-current#)/curve)
	Return current#
End Function

; ---------------------------------------------------------------
; Main loop...
; ---------------------------------------------------------------

FPS = 50

period = 1000 / FPS
time = MilliSecs () - period

SoundVolume wind, 0.25
SoundVolume sea, 0.65
SoundVolume rain, 0.75
SoundVolume run, 0
SoundVolume wade, 0

LoopSound wind
LoopSound sea
LoopSound rain
LoopSound run
LoopSound wade

PlaySound wind
PlaySound sea
PlaySound rain
runChannel = PlaySound (run)
wadeChannel = PlaySound (wade)

; ---------------------------------------------------------------
; Lens stuff...
; ---------------------------------------------------------------

diameter = lensDiameter
magnification = 5

CreateLens (diameter, magnification)

dropx# = 0
dropy# = 0

dropx2# = 0
dropy2# = 0

sky# = 0

startlite# = 0
foglite# = 0
sunlite# = 0

;CameraFogColor camera, 36, 36, 36

; Titles...
horror = LoadSprite ("gfx/di.bmp", 48, camera)
ScaleSprite horror, 9, 5
PositionEntity horror, 0, 0, 10
horrorAlpha# = 0
EntityAlpha horror, horrorAlpha
showHorror = 1

steps# = 1.5

HidePointer

;chapel = LoadMesh ("G:\My Documents\Temp\chapel.x")
;RotateEntity chapel, -90, 0, 0
;PositionEntity chapel, 0, 400, 0
;EntityShininess chapel, 0

disclaimer$ = "Run it at night or you won't see a thing :)"
SetFont LoadFont ("arial")

Repeat

	If startlite < 40
		startlite = startlite + 0.5
		AmbientLight startlite, startlite, startlite
	EndIf

	If makesun = 0
		sun = CreateLight ()
		LightColor sun, 0, 0, 0
		PositionEntity sun, 3000, 500, 3000
		RotateEntity sun, 45, 0, 0
		makesun = 1
	Else
		; Titles... requesting the services of Major Hack... Major Hack, you're needed for some titles...
		If Not noMoreHorror
			EntityAlpha horror, horrorAlpha
			If Not reduce
				If horrorAlpha <= 1
					horrorAlpha = horrorAlpha + 0.05
				Else
					reduce = 1
				EndIf
			Else
				If horrorAlpha => 0
					horrorAlpha = horrorAlpha - 0.005
				EndIf
			EndIf	
			If horrorAlpha <= 0
				FreeEntity horror
				noMoreHorror = 1
			EndIf
		EndIf
		
	EndIf
	
	If sunlite < 70
		sunlite = sunlite + 0.5
		If sun
			LightColor sun, sunlite, sunlite, sunlite
		EndIf
	EndIf

	If foglite < 20
		foglite = foglite + 0.5
		CameraFogColor camera, foglite, foglite, foglite
		CameraClsColor camera, foglite, foglite, foglite
	EndIf

	Repeat
		elapsed = MilliSecs () - time
	Until elapsed

	ticks = elapsed / period
	
	tween# = Float (elapsed Mod period) / Float (period)
	
	For framelimit = 1 To ticks
	
		If framelimit = ticks Then CaptureWorld
		time = time + period

		PositionEntity cam, EntityX (cam), TerrainY (terrain, EntityX (cam), EntityY (cam), EntityZ (cam)) + 25, EntityZ (cam)
	
		mxs = MouseXSpeed()
		mys = MouseYSpeed()
		
		dest_xang# = dest_xang + mys
		dest_yang# = dest_yang - mxs
	
		xang# = CurveValue (xang, dest_xang, 5)
		yang# = CurveValue (yang, dest_yang, 5)
	
		RotateEntity camera, xang, 0, 0
		RotateEntity cam, 0, yang, 0
		
		MoveMouse GraphicsWidth()/2,GraphicsHeight()/2
		
		running# = 0
		
		If KeyDown (200)
			MoveEntity cam, 0, 0, steps
			running = Rnd (0.35, 0.75)
		EndIf
	
		If KeyDown (208)
			MoveEntity cam, 0, 0, -steps
			running = Rnd (0.55, 0.95)
		EndIf
	
		If running > 0
			ChannelPitch runChannel, Rnd (8000, 14000)
			ChannelPitch wadeChannel, Rnd (7000, 9000)
		EndIf
		
		wading# = 0
		
		If running
			If EntityY (cam) < EntityY (water) + 24
				wading = Rnd (0.5, 1)
				running = 0
			EndIf
		EndIf
		
		ChannelVolume wadeChannel, wading
		ChannelVolume runChannel, running
		
		PositionEntity dome, EntityX (cam), EntityY (cam), EntityZ (cam)
		TurnEntity dome, 0.005, 0.025, 0.005
		
		UpdateWorld
		
		; Mark's code, hacked cluelessly...
		PositionEntity water, Sin (time * 0.01) * 10, (EntityY (water) + (Sin (time * 0.05) * 0.2) * 0.25), Cos (time * 0.02) * 10

		If (Rnd (1000) > 998.8) Or (KeyDown (28))
			startlite = 0
			sunlite = 40;255;10
			sky = 255
			foglite = 255
			thunderGo = 1
			thunderTimer.Timer = SetTimer (Rnd (500, 1500))
		EndIf

		If thunderGo
			If TimeOut (thunderTimer)
				If Rnd (0, 2) > 1
					thunder = thunder1
				Else thunder = thunder2
				EndIf
				SoundPitch thunder, Rnd (9000, 14000)
				PlaySound thunder
				thunderGo = 0
				Delete thunderTimer
			EndIf
		EndIf
				
		If sky > 10
			sky = sky - Rnd (5, 20)
		EndIf
		LightColor flash, sky, sky, sky

		If foglite > 20
			foglite = foglite - 10
			If foglite < 20 Then foglite = 20
		EndIf
		CameraClsColor camera, foglite, foglite, foglite					
;		CameraFogColor camera, foglite, foglite, foglite					

	Next
	
	; W is for Wireframe...
	If KeyHit (17)
		w = 1 - w
		WireFrame w
	EndIf
	
	RenderWorld tween

	If Not nomorehorror
		Text (GraphicsWidth () / 2) - (StringWidth (disclaimer$) / 2), GraphicsHeight () - (StringHeight (disclaimer$) * 2), disclaimer$
	EndIf
	
	; Raindrops on lens...
	If dropy <= GraphicsHeight () - diameter - 2
		DrawLens (dropx, dropy, diameter)
		dropy = dropy + 2
	Else
		dropx = Rnd (0, GraphicsWidth () - diameter)
		dropy = 0
	EndIf

	If dropy2 <= GraphicsHeight () - diameter - 7
		DrawLens (dropx2, dropy2, diameter)
		dropy2 = dropy2 + 7
	Else
		dropx2 = Rnd (0, GraphicsWidth () - diameter)
		dropy2 = 0
	EndIf

;	Text 20, 20, EntityX (cam)
;	Text 20, 40, EntityY (cam)
;	Text 20, 60, EntityZ (cam)
	
	Flip

;	If KeyDown (57)
;		SaveBuffer FrontBuffer (), "grab" + grab + ".bmp"
;		grab = grab + 1
;	EndIf
		
Until KeyDown (1)

End