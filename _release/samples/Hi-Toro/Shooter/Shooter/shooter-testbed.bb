
; ---------------------------------------------------------------
; Testbed thing -- HIGHLY UNFINISHED AND MESSY!
; ---------------------------------------------------------------

; Features

; Tracking gun emplacements (they aim ahead, depending on player's speed)
; Updating loadsa stuff via For... Each loops:
;	- Enemy shots
;	- Player shots
;	- Fading of all flames
;	- Enemies only fire within certain range
; Sprites-for-shots 'n' flames
; Blitz terrain
; Awful controls

; If you have a M$ control pad, you might need to turn off the tilt mode
; cos it makes things go all loopy!

noAliens = 50; 25;10

; ---------------------------------------------------------------
; Graphics/light...
; ---------------------------------------------------------------

Global dWidth  =1024
Global dHeight = 768

Graphics3D dWidth, dHeight
SetBuffer BackBuffer ()

;AntiAlias True

AmbientLight 200, 200, 200
sun = CreateLight ()
LightColor sun, 255, 220, 180
RotateEntity sun, 0, 45, 0

; ---------------------------------------------------------------
; Terrain...
; ---------------------------------------------------------------

terrain = LoadTerrain ("gfx/height.bmp")
ScaleEntity terrain, 5, 300, 5
TerrainShading terrain, True
TerrainDetail terrain, 2500, True
EntityPickMode terrain, 2, True
grass = LoadTexture ("gfx/grass.bmp")
ScaleTexture grass, 20, 20
EntityTexture terrain, grass, 0, 1

; ---------------------------------------------------------------
; Hack me an alien gun thing... ball with cylinder sticking out
; ---------------------------------------------------------------

Type Alien
	Field entity
End Type

ballAlien = CreateSphere ()
ScaleEntity ballAlien, 4, 4, 4
EntityColor ballAlien, 64, 64, 64
EntityShininess ballAlien, 1
ballAlienGun = CreateCylinder (12, 1, ballAlien)
ScaleEntity ballAlienGun, 0.25, 2.5, 0.25
MoveEntity ballAlienGun, 0, 0, 2
RotateEntity ballAlienGun, 90, 0, 0
EntityColor ballAlienGun, 32, 32, 32
EntityShininess ballAlienGun, 1
EntityPickMode ballAlien, 1, True
EntityRadius ballAlien, 9
Const ENTITY_ALIEN = 5
EntityType ballAlien, ENTITY_ALIEN
HideEntity ballAlien

; ---------------------------------------------------------------
; Copy alien x times...
; ---------------------------------------------------------------

For a = 1 To noAliens
	ball.Alien = New Alien
	ball\entity = CopyEntity (ballAlien): ShowEntity ball\entity
	bx = Rnd (TerrainSize (terrain) * 5)
	bz = Rnd (TerrainSize (terrain) * 5)
	PositionEntity ball\entity, bx, TerrainY (terrain, bx, 0, bz) + 1, bz
Next

; ---------------------------------------------------------------
; Water...
; ---------------------------------------------------------------

water = CreatePlane ()
h20 = LoadTexture ("gfx/water.bmp")
ScaleTexture h20, 200, 200
EntityTexture water, h20
EntityAlpha water, 0.4
PositionEntity water, 0, 20, 0
EntityShininess water, 1

; ---------------------------------------------------------------
; Camera and fog...
; ---------------------------------------------------------------

Global cam = CreatePivot ()
Global camera = CreateCamera (cam)
CameraViewport camera, 0, 0, dWidth, dHeight
CameraFogMode camera, 1
CameraFogColor camera, 200, 220, 255
range = 2500
CameraRange camera, 1, range
CameraFogRange camera, 1, range - 10
CameraClsColor camera, 200, 220, 255

; ---------------------------------------------------------------
; Player...
; ---------------------------------------------------------------

Global ship = CreatePivot ()
Global shipMesh = LoadMesh ("msh/ship.x", ship)
MoveEntity shipMesh, -2.5, 0, -1
ScaleEntity shipMesh, 0.5, 0.5, 0.5
EntityRadius ship, 1.6
EntityShininess shipMesh, 1

Global aliensTarget = CreatePivot (ship) ; Sphere (5, ship)
MoveEntity aliensTarget, 0, 0, 200

; ---------------------------------------------------------------
; Attach camera to player...
; ---------------------------------------------------------------

PositionEntity cam, 0, EntityY (ship) + 0.85, -4
EntityParent cam, ship
PositionEntity ship, 10, 50, 10

; ---------------------------------------------------------------
; 2D stuff...
; ---------------------------------------------------------------

; Target...
target = LoadImage ("gfx/target.bmp")
MaskImage target, 255, 0, 255
MidHandle target

; Flame...
Global fire = LoadSprite ("gfx/fire.bmp")
HandleSprite fire, 0, -1
ScaleSprite fire, 5, 5
EntityAlpha fire, 0.25
EntityFX fire, 8
HideEntity fire

; Splash...
Global splash = LoadSprite ("gfx/splash.bmp")
HandleSprite splash, 0, -1
ScaleSprite splash, 5, 5
EntityAlpha splash, 0.25
EntityFX splash, 8
HideEntity splash

; Alien shot...
Global afire = LoadSprite ("gfx/ashot.bmp")
HandleSprite afire, 0, -1
ScaleSprite afire, 3, 3
EntityFX afire, 8
EntityRadius afire, 4
HideEntity afire

Global hits

; ---------------------------------------------------------------
; Collisions...
; ---------------------------------------------------------------
	
Const ENTITY_TERRAIN = 1
Const ENTITY_SHIP	 = 2
Const ENTITY_WATER	 = 3
Const ENTITY_AFIRE	 = 4

EntityType terrain, ENTITY_TERRAIN
EntityType ship, ENTITY_SHIP
EntityType water, ENTITY_WATER
EntityType afire, ENTITY_AFIRE

Collisions ENTITY_SHIP, ENTITY_TERRAIN, 2, 2
Collisions ENTITY_SHIP, ENTITY_WATER, 2, 2
Collisions ENTITY_AFIRE, ENTITY_SHIP, 1, 1

; ---------------------------------------------------------------
; Auto-updated entity functions...
; ---------------------------------------------------------------

Type Flame
	Field entity
	Field alpha#
End Type

Function CreateFlame (x#, y#, z#, image)
	flame.Flame = New Flame
	flame\entity = CopyEntity (image): ShowEntity flame\entity
	PositionEntity flame\entity, x, y, z
	flame\alpha = 1
End Function

Function UpdateFlames ()
	For a.Flame = Each Flame
		a\alpha = a\alpha - 0.001
		EntityAlpha a\entity, a\alpha
		If a\alpha < 0.01 Then FreeEntity a\entity: Delete a
	Next
End Function

Type AlienShot
	Field entity ; 'afire' sprite
	Field alpha#
End Type

Function AlienShoot (a.Alien, image)
	aShot.AlienShot = New AlienShot
	aShot\entity = CopyEntity (image): ShowEntity aShot\entity
	RotateEntity aShot\entity, EntityPitch (a\entity) + Rnd (-1, 1), EntityYaw (a\entity) + Rnd (-1, 1), 0;EntityRoll (a\entity) + Rnd (-1, 1)
	PositionEntity aShot\entity, EntityX (a\entity), EntityY (a\entity), EntityZ (a\entity)
	aShot\alpha = 1
End Function

Function UpdateAlienShots ()
	For a.AlienShot = Each AlienShot
		MoveEntity a\entity, 0, 0, 5
		If EntityCollided (a\entity, ENTITY_SHIP)
;			CreateFlame (EntityX (shipMesh), EntityY (shipMesh), EntityZ (shipMesh), fire)
			hits = hits + 1
		EndIf
		a\alpha = a\alpha - 0.0025
		EntityAlpha a\entity, a\alpha
		If a\alpha < 0.01 Then FreeEntity a\entity: Delete a
	Next
End Function

; ---------------------------------------------------------------
; Reduce roll rate to zero...
; ---------------------------------------------------------------

Function Diminish# (value#, amount#)
	If Abs (value) <= Abs (amount) Then Return 0
	value = value - (amount * Sgn (value))
	Return value
End Function

; ---------------------------------------------------------------
; Player control variables...
; ---------------------------------------------------------------

Global zAcc# = 0
Global shipRoll#  = 0
Global shipPitch# = 0

; ---------------------------------------------------------------
; Main loop...
; ---------------------------------------------------------------

MoveMouse GraphicsWidth () / 2, GraphicsHeight () / 2

Global topSpeed = 3
FPS = 50

period = 1000 / FPS
time = MilliSecs () - period

Global kills
Global damage

;WireFrame 1

Repeat
	
	Repeat
		elapsed = MilliSecs () - time
	Until elapsed

	ticks = elapsed / period
	
	tween# = Float (elapsed Mod period) / Float (period)
	
	For framelimit = 1 To ticks
		If framelimit = ticks Then CaptureWorld
		time = time + period
		UpdateGame ()
		UpdateWorld
		PositionEntity water, Sin (time * 0.01) * 10, EntityY (water) + (Sin (time * 0.05) * 0.5) * 0.2, Cos (time * 0.02) * 10
	Next
	
	If KeyHit (17)
		w = 1 - w
		WireFrame w
	EndIf
	
	RenderWorld tween

	; -----------------------------------------------------------
	; Draw target...
	; -----------------------------------------------------------

	x = (MouseX () - x) / 3 + x
	y = (MouseY () - y) / 3 + y
		
	DrawImage target, x, y

	; -----------------------------------------------------------
	; Instructions...
	; -----------------------------------------------------------

	Text 20, GraphicsHeight () - 60, "Cursors to move"
	Text 20, GraphicsHeight () - 40, "Right mouse button to accelerate"
	Text 20, GraphicsHeight () - 20, "Mouse + left button to fire"

;	Text 20, 20, EntityX (shipMesh)
;	Text 20, 40, EntityY (shipMesh)
;	Text 20, 60, EntityZ (shipMesh)

	Text 20, 80,  "Kills: "  + kills + " dead alien scum"
	Text 20, 100, "Hits: "   + "Hit by aliens " + hits  + " times"
	Text 20, 120, "Ground damage: " + damage
	
	Flip
	
Until KeyDown (1)

End

Function UpdateGame ()

	; -----------------------------------------------------------
	; Left/right/up/down...
	; -----------------------------------------------------------

	If KeyDown (203) Or JoyXDir () = -1
		If shipRoll < 2 Then shipRoll = shipRoll + 0.125
	EndIf
	
	If KeyDown (205) Or JoyXDir () = 1
		If shipRoll > -2 Then shipRoll = shipRoll - 0.125
	EndIf
	
	If KeyDown (200) Or JoyYDir () = -1
		If shipPitch < 1 Then shipPitch = shipPitch + 0.02
	EndIf

	If KeyDown (208) Or JoyYDir () = 1
		If shipPitch > -3 Then shipPitch = shipPitch - 0.023
	EndIf

	; -----------------------------------------------------------
	; Speed up/slow down...
	; -----------------------------------------------------------
	
	If MouseDown (2) Or JoyDown (8)
		If zAcc < topSpeed Then zAcc = zAcc + 0.05
	Else
		If zAcc > 0.025 Then zAcc = zAcc - (0.025 * zAcc / 10)
	EndIf

	; -----------------------------------------------------------
	; Ship "inertia"...
	; -----------------------------------------------------------

	shipRoll = Diminish (shipRoll, 0.05)
	shipRoll = shipRoll + Rnd (-0.075, 0.075)
	shipPitch = Diminish (shipPitch, 0.0125)
	
	; -----------------------------------------------------------
	; Position player and camera...
	; -----------------------------------------------------------

	TurnEntity ship, shipPitch, 0, shipRoll
;	TurnEntity ship, 0, ((Sin (EntityRoll (ship))) / topSpeed) * zAcc, 0, 1
	TurnEntity ship, 0, 2 * (((Sin (EntityRoll (ship))) / topSpeed) * zAcc), 0, 1
	TurnEntity cam, 0, EntityRoll (ship) / 90, 0, 1
	MoveEntity ship, 0, 0, zAcc
	PointEntity cam, ship
	
	PositionEntity aliensTarget, 0, 0, 5 + zAcc * 50
	
	; -----------------------------------------------------------
	; Fire button...
	; -----------------------------------------------------------

	If MouseDown (1) Or JoyDown (1)
		pickedEntity = CameraPick (camera, MouseX (), MouseY ())
		If pickedEntity
			For a.Alien = Each Alien
				If pickedEntity = a\entity Then kills = kills + 1: FreeEntity a\entity: Delete a: Exit
			Next
			CreateFlame (PickedX (), PickedY (), PickedZ (), fire)
		EndIf
	EndIf
	
	; -----------------------------------------------------------
	; Player-to-terrain collision check...
	; -----------------------------------------------------------
	
	If EntityCollided (ship, ENTITY_TERRAIN)
		CreateFlame (EntityX (ship), EntityY (ship), EntityZ (ship), fire)
		damage = damage + 1
	EndIf

	If EntityCollided (ship, ENTITY_WATER)
		CreateFlame (EntityX (ship), EntityY (ship), EntityZ (ship), splash)
	EndIf

	; -----------------------------------------------------------
	; Aliens fire at player... sorta :)
	; -----------------------------------------------------------

	For gun.Alien = Each Alien
		PointEntity gun\entity, aliensTarget;ship
		If Rnd (1000) > 950 And EntityDistance (gun\entity, ship) < 750
			AlienShoot (gun, afire)
		EndIf
	Next

	; -----------------------------------------------------------
	; Auto-update stuff...
	; -----------------------------------------------------------

	UpdateFlames ()
	UpdateAlienShots ()
	
End Function