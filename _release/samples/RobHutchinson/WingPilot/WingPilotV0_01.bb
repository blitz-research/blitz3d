;
; Wings Pilot V0.01
; Written and Copyright by Rob Hutchinson 2001
;
; CONTROLS:
; ESC    - Exit
; ARROWS - Move craft around.
; A      - Thrust
; Z      - Brake
; ALT    - Fire
; SPACE  - Shields
;

;
; To do next:
; - Add meters.
;
; Then:
; - Use INI functions to create a mini level script language with names of objects to load and positions.
; - Create ground (try some terrains) 
; 
;

;Graphics3D 1024,768,16
Graphics3D 640,480
SetBuffer BackBuffer()

; Variables..
Global dObjs$ = "objects\",dGfx$ = "graphics\",dSnd$ = "sounds\"

Global cCamera,mShip,pRing,mRing,sLamp,tRing,mCent,tCent
Global mCraft,sFlame1,sFlame2,pCraft,pCraftIn,mShields,mShields2,tShields
Global pWeaponPoint1,pWeaponPoint2

Global screenLeft = -20,screenRight = 20,screenTop = 15,screenBottom = -15

; Craft VARS
Global craftTopSpeed# = 0.5                   ; Craft's top speed.
Global craftInertiaX# = 0                     ; Craft X inertia.
Global craftInertiaY# = 0                     ; Craft Y inertia.
Global craftAccel#    = craftTopSpeed# / 50   ; Craft acceleration speed.
Global craftDecel#    = craftTopSpeed# / 100  ; Craft deceleration speed.
Global craftTilt#     = 100                   ; Amount to tilt the craft when manouvering
Global craftPitch#    = 0                     ; This is used by the program to store craft tilt.
Global craftRoll#     = 0                     ; -"-
Global craftRollMax#  = 60                    ; What angle to stop craft rotating at when moving.
Global craftPitchMax# = 70                    ; -"-
Global craftWobble#   = 0                     ; Used in prog for general craft wobble
Global craftX#        = 0                     ; Craft absolute x
Global craftY#        = 0                     ; Craft absolute y
Global craftZ#        = 15                    ; Craft absolute z

Global craftShieldAlphaMax#    = 0.3           ; Maximum shield alpha
Global craftShieldAlphaRunOff# = craftShieldAlphaMax# / 150  ; How fast shields fade back to 0
Global craftShieldAlpha#       = 0             ; Current alpha level of the shields.

Global craftTurbulance#       = 0             ; Current craft turbulance
Global craftTurbulanceRunOff# = 0.07           ; How fast turbulance wears off.

; Scene variables
Global sceneZ#
Global sceneSpeed#

; Sound variables
Global sThruRing,sRingBonus,sDamage

; Ring variables
Type tRing
	; Entities
	Field ePivot
	Field eRing
	Field eCenter
	Field eFlare1
	Field eFlare2	
	
	; Attributes
	Field x#,y#,z#
	Field pitch#,yaw#,roll#
	Field bonus
End Type
Global ringDamage# = 150               ; The amount of damage hitting a ring does.

; Flame variables.
Type tFlame
	Field obj
	Field speed#,count#,untildist#
	Field prescale#,scaledown#
	Field alphadown#,alphacn#
End Type
Global pFlamePoint
Global flameCountFrame     = 0
Global newflameAfterFrames = 0
Global flameNormalSize#    = 1.5
Global flameNewSize#       = 0

; Player variables
Global playerScore#      = 0      ; Current players' score.
Global playerThrust      = 1000   ; How much thrust does the player have left?
Global playerThrustMax   = 1000   ; How much thrust can the player have maximum.
Global playerBrake       = 250    ; How much brake does the player have left?
Global playerBrakeMax    = 250    ; How much brake can the player have maximum.
Global playerThrustZ#    = 0      ; The current amount of thrust
Global playerBrakeZ#     = 0      ; The current amount of brake
Global playerThrustZMax# = 0.8    ; How much to add to scene coords when thrust is at max.
Global playerBrakeZMax#  = 0.2    ; How much to sub from scene coords when thrust is at max.
Global playerFrameToMax# = 60     ; Number of updates before thrust of brake is at full capacity.

Global playerFSThrstMax# = 1.5    ; FLAME SCALE: Addition to make when thrust is at full.
Global playerFSBrakeMax# = 0.5    ; FLAME SCALE: Subtraction to make when brake is at full.

Global playerShields#    = 1000   ; The current amount of shields the player has.
Global playerShieldsMax# = 1000   ; The max shields amount
Global playerEnergy#     = 1000   ; The current amount of energy the player has.
Global playerEnergyMax#  = 1000   ; The max energy amount

; Weapons
Type tWeapon
	Field name$
	Field ent
	Field typeid
	Field sound
	Field strength#
	Field speed#
	Field vanishdist#
	Field ammomax
	Field ammo
	Field rotspeed#
End Type

Const WEAPON_TYPEID_RECHARGE    = 1
Const WEAPON_TYPEID_UNLIMITED   = 2
Const WEAPON_TYPEID_DEMINISHING = 3

Dim weaponList.tWeapon(100)
Global weaponCurrent = 0

Type tBullet
	Field weapon
	Field ent1
	Field rot#
End Type

; Floor
Global mFloor1,tFloor1,mFloor2,tFloor2
Global fRot1#

; sky
Global mSky1,tSky1,mSky2,tSky2

; Constants..
Const T_RING=1,T_CRAFT=2,T_CENTER=3
Const KEY_UP    = 200
Const KEY_LEFT  = 203
Const KEY_RIGHT = 205
Const KEY_DOWN  = 208
Const KEY_A     = 30
Const KEY_Z     = 44
Const KEY_X     = 45

Collisions T_CRAFT,T_RING,2,0
Collisions T_CRAFT,T_CENTER,2,0

; Set everything up..
Setup()

lt# = 0
For a=0 To 50
	CreateNewRing(Sin(lt#)*25,Cos(lt#)*25,a*60,Rnd(-4.0,4.0),Rnd(-4.0,4.0),Rnd(-12.0,12.0),Rnd(0,255),Rnd(0,255),Rnd(0,255))
	lt = lt + 30
Next

Global temp

; Main loop!
Repeat
	; Stuff
	If KeyDown(KEY_LEFT)
		craftInertiaX# = craftInertiaX# - craftAccel#
		ikeyHitX = True
	EndIf
	If KeyDown(KEY_RIGHT)
		craftInertiaX# = craftInertiaX# + craftAccel#
		ikeyHitX = True
	EndIf
	If KeyDown(KEY_DOWN)
		craftInertiaY# = craftInertiaY# + craftAccel#
		ikeyHitY = True
	EndIf
	If KeyDown(KEY_UP)
		craftInertiaY# = craftInertiaY# - craftAccel#
		ikeyHitY = True
	EndIf
	
	; Fire button
	If KeyHit(KEY_X)
		AddWeaponFire(weaponCurrent)
	EndIf
	
	; Do thrust calc
	If KeyDown(KEY_A)
		playerThrust = playerThrust - 1
		If playerThrust < 0 Then playerThrust = 0
		If playerThrust > 0
			playerThrustZ# = playerThrustZ# + (playerThrustZMax#/playerFrameToMax#)
			If playerThrustZ# >= playerThrustZMax# Then	playerThrustZ# = playerThrustZMax#
		Else
			playerThrustZ# = playerThrustZ# - (playerThrustZMax#/playerFrameToMax#)
			If playerThrustZ# < 0 Then playerThrustZ# = 0
		EndIf
	Else
		playerThrustZ# = playerThrustZ# - (playerThrustZMax#/playerFrameToMax#)
		If playerThrustZ# < 0 Then playerThrustZ# = 0
	EndIf

	; Do brake calc
	If KeyDown(KEY_Z)
		playerBrake = playerBrake - 1
		If playerBrake < 0 Then playerBrake = 0
		If playerBrake > 0
			playerBrakeZ# = playerBrakeZ# + (playerBrakeZMax#/playerFrameToMax#)
			If playerBrakeZ# >= playerBrakeZMax# Then playerBrakeZ# = playerBrakeZMax#
		Else
			playerBrakeZ# = playerBrakeZ# - (playerBrakeZMax#/playerFrameToMax#)
			If playerBrakeZ# < 0 Then playerBrakeZ# = 0
		EndIf
	Else
		playerBrakeZ# = playerBrakeZ# - (playerBrakeZMax#/playerFrameToMax#)
		If playerBrakeZ# < 0 Then playerBrakeZ# = 0
	EndIf

	; Start slowing down if no key is hit.
	If ikeyHitX = False
		If craftInertiaX# <> 0
			If craftInertiaX# < 0
				craftInertiaX# = craftInertiaX# + craftDecel#
				If craftInertiaX >= craftDecel# Then craftInertiaX# = 0
			Else
				craftInertiaX# = craftInertiaX# - craftDecel#
				If craftInertiaX <= craftDecel# Then craftInertiaX# = 0
			EndIf
		EndIf
	EndIf

	If ikeyHitY = False
		If craftInertiaY# <> 0
			If craftInertiaY# < 0
				craftInertiaY# = craftInertiaY# + craftDecel#
				If craftInertiaY >= craftDecel# Then craftInertiaY# = 0
			Else
				craftInertiaY# = craftInertiaY# - craftDecel#
				If craftInertiaY <= craftDecel# Then craftInertiaY# = 0
			EndIf
		EndIf
	EndIf
	
	; Craft Turbulance calculations.
	craftTurbulance# = craftTurbulance# - craftTurbulanceRunOff#
	If craftTurbulance# < 0 Then craftTurbulance# = 0

	; Do shield stuff.
	craftShieldAlpha# = craftShieldAlpha# - craftShieldAlphaRunOff#
	If craftShieldAlpha# < 0 Then craftShieldAlpha# = 0
	DoShields(craftShieldAlpha#,4)

	; Move scenery. calculate speed here.
	sceneSpeed# = ((0.4 + playerThrustZ#) - playerBrakeZ#)
	UpdateScene(sceneSpeed#)

;	PointEntity cCamera,mCraft
	
	MoveMouse 200,200
	
	UpdateWorld
;	If EntityCollided(mRing,T_CRAFT)
;		Text 0,0,"collided"
;	EndIf

	ent = EntityCollided(pCraft,T_RING)
	; Collisions with the outer ring here.
	If ent
		EntityType ent,0
		ResetEntity ent

		; Work damage taken for hitting ring....
		If playerShields# <= 0
			craftTurbulance# = 6
		Else
			craftTurbulance# = 4
			craftShieldAlpha# = craftShieldAlphaMax#
		EndIf
		; Work out shield damage.
		If playerShields# <> 0
			playerShields# = playerShields# - ringDamage#
			If playerShields# < 0
				; Take it off energy cos shields are down.
				playerEnergy# = playerEnergy# - Abs(playerShields#)
				playerShields# = 0
			EndIf
		Else
			playerEnergy# = playerEnergy# - ringDamage#
			PlaySound sDamage
			If playerEnergy# <= 0 
				; Terminate Game
			EndIf
		EndIf	
	EndIf


	ent = EntityCollided(pCraft,T_CENTER)
	; Collisions with the inner part of the ring here.
	If ent
		EntityType ent,0
		ResetEntity ent

		; Work out score for passing thru the ring...
		dist# = EntityDistance(mCraft,ent)
		If dist# < 2
			; Give big score bonus for hitting the ring very close to the center.
			playerScore = playerScore + 500
			PlaySound sRingBonus
		Else
			playerScore = playerScore + (100.0 / dist)
			PlaySound sThruRing
		EndIf

		; Reposition and re-update.
		PositionEntity pCraft,craftX#,craftY#,sceneZ# + craftZ#
		UpdateWorld
	EndIf

	RenderWorld
	
	
	mph# = (sceneSpeed# * 300.0)+(Cos(deg#)*(sceneSpeed#*20)) - Abs(craftPitch#/5) - Abs(craftRoll#/5)
	deg#=deg#+1.2
	
	Text 0,0,"SCORE: " + Int(playerScore)
	Text 0,15,"DISTANCE FROM CENTER OF RING: " + dist
	Text 0,40,"THRUST: " + playerThrust + "/" + playerThrustMax
	Text 0,55,"BRAKE: " + playerBrake + "/" + playerBrakeMax
	Text 0,70,"SHIELDS: " + Int(playerShields#) + "/" + Int(playerShieldsMax#)
	Text 0,95,"ENERGY: " + Int(playerEnergy#) + "/" + Int(playerEnergyMax#)
	Text 0,110,"SPEED: " + Int(mph#) + " mph"
	Text 0,200,"BULLETS ON SCREEN: " + temp
	temp = 0

	ikeyHitX = False
	ikeyHitY = False
	Flip
Until KeyDown(1)
End

Function UpdateScene(amount#)

	; Move the floor
	fRot1# = fRot1# + 3
	PositionEntity mFloor1,(Sin(fRot1#)*1.2)+craftX#,-21,(Cos(fRot1#)*2.9)+craftZ#
	PositionEntity mFloor2,-(Sin(fRot1#)*3.0)+craftX#,-22,-(Cos(fRot1#)*1.1)+craftZ#

	; Move the sky
	PositionEntity mSky1,(Sin(fRot1#)*1.2)+craftX#,21,(Cos(fRot1#)*2.9)+craftZ#
	PositionEntity mSky2,-(Sin(fRot1#)*3.0)+craftX#,22,-(Cos(fRot1#)*1.1)+craftZ#


	; Update scene position
	sceneZ = sceneZ + amount#

	; Calculate craft tilt.
	craftRoll#  = -(craftInertiaX#*100)
	craftPitch# = -(craftInertiaY#*100)
	If craftRoll# > craftRollMax#
		craftRoll# = craftRollMax#
	ElseIf craftRoll# < -craftRollMax#
		craftRoll# = -craftRollMax#
	EndIf
	If craftPitch# > craftPitchMax#
		craftPitch# = craftPitchMax#
	ElseIf craftPitch# < -craftPitchMax#
		craftPitch# = -craftPitchMax#
	EndIf
	turbPitch# = Rnd(-craftTurbulance#,craftTurbulance#)
	turbYaw#   = Rnd(-craftTurbulance#,craftTurbulance#)
	turbRoll#  = Rnd(-craftTurbulance#,craftTurbulance#)

    ; Tilt it!
	newCPitch# = craftPitch#+(Sin(craftWobble#)*3)+turbPitch#
	newCYaw#   = craftRoll#+turbYaw#
	newCRoll#  = craftRoll#+(Cos(craftWobble#)*15)+turbRoll#
	RotateEntity pCraftIn,newCPitch#,newCYaw#,newCRoll#
	craftWobble# = craftWobble# + 1

	; Move the craft.
	craftX# = craftX# + craftInertiaX#
	craftY# = craftY# + craftInertiaY#
	PositionEntity pCraft,craftX#,craftY#,sceneZ# + craftZ#
	
	; Move the camera.
	PositionEntity cCamera,EntityX(pCraft)/1.5,EntityY(pCraft)/1.5,sceneZ
	RotateEntity cCamera,turbPitch#/6,turbYaw#/6,turbRoll#/6

	; Do stuff.
	DoFlame()
	DoRings()
	DoBullets()

	; Update the flame.
	flameCountFrame = flameCountFrame + 1
	If flameCountFrame > newflameAfterFrames
		j = Rnd(100)
		; Calculate the size of the flame, larger if there is thrust, smaller if there is brake.
		flameNewSize# = (flameNormalSize# + ((playerThrustZ# / playerThrustZMax#) * playerFSThrstMax#)) - ((playerBrakeZ# / playerBrakeZMax#) * playerFSBrakeMax#)
		If j > 50
			CreateNewFlame(sFlame1,0.0000001,.000008,flameNewSize#)
		Else
			CreateNewFlame(sFlame2,0.0000001,.000008,flameNewSize#)
		EndIf
		flameCountFrame = 0
	EndIf
End Function

Function Setup()
	cCamera = CreateCamera()
	CameraRange cCamera,1,300
	CameraFogMode cCamera,1
	CameraFogRange cCamera,1,300
	CameraFogColor cCamera,0,0,0
	
	light   = CreateLight()

	; Create the ring.....
	pRing = CreatePivot()
	mRing = LoadMesh(dObjs$ + "mainring.x",pRing)
	mCent = LoadMesh(dObjs$ + "maincent.x",pRing)
	NameEntity mRing,"ring"
	NameEntity mCent,"center"
	EntityAlpha mCent,0
	EntityType mCent,T_CENTER
	EntityType mRing,T_RING
	tRing = LoadTexture(dGfx$ + "ring.bmp")
	EntityTexture mRing,tRing
;	tCent = LoadTexture(dGfx$ + "central.bmp")
;	EntityTexture mCent,tCent
;	TextureBlend  tCent,1
	sLamp = LoadSprite(dGfx$ + "lamp.bmp",1,pRing)
	ScaleSprite sLamp,3,3
	sLam2 = CopyEntity(sLamp,pRing)
	NameEntity sLamp,"lamp1"
	NameEntity sLam2,"lamp2"
	MoveEntity sLamp,14.5,0,0
	MoveEntity sLam2,-14.5,0,0
	MoveEntity pRing,0,0,50
	HideEntity pRing
	
	; Create ship
	pCraft = CreatePivot()
	pCraftIn = CreatePivot(pCraft)
	mCraft = LoadMesh("craft1.x",pCraftIn)
	ScaleEntity mCraft,.08,.08,.08
	EntityType pCraft,T_CRAFT
	pWeaponPoint1 = CreatePivot(pCraftIn)
	MoveEntity pWeaponPoint1,-2,0,20
	pWeaponPoint2 = CreatePivot(pCraftIn)
	MoveEntity pWeaponPoint2,2,0,20

;	MoveEntity pCraft,0,0,15

	; Create Ships' Shields.
	mShields = CreateSphere(7,pCraftIn)
	ScaleEntity mShields,3.5,1.5,3.5
	tShields = LoadTexture(dGfx$ + "shields.bmp")
	EntityTexture mShields,tShields
	EntityAlpha mShields,.3
	MoveEntity mShields,0,-.3,.5

	mShields2 = CreateSphere(7,pCraftIn)
	ScaleEntity mShields2,3.4,1.4,3.4
	EntityTexture mShields2,tShields
	MoveEntity mShields2,0,-.3,.5
	
	; Setup the ship flame
	pFlamePoint = CreatePivot(pCraftIn)
	MoveEntity pFlamePoint,0,0,-4
	sFlame1 = LoadSprite(dGfx$ + "flame1.bmp",1,pCraftIn)
	sFlame2 = LoadSprite(dGfx$ + "flame2.bmp",1,pCraftIn)
	MoveEntity sFlame1,0,0,-2.2
	MoveEntity sFlame2,0,0,-2.2
	HideEntity sFlame1
	HideEntity sFlame2
	
	; Load sound FX
	sThruRing  = LoadSound(dSnd$ + "toot.wav")
	sRingBonus = LoadSound(dSnd$ + "bonus.wav")
	sDamage    = LoadSound(dSnd$ + "damage.wav")

	; Setup the weapons
	weaponList(0)             = New tWeapon
	weaponList(0)\name$       = "Sparktronic-Protoblogterfier"
	weaponList(0)\sound       = LoadSound(dSnd$ + "bullet1.wav")
	weaponList(0)\ent         = LoadSprite(dGfx$ + "bullet1.bmp",1,pCraftIn)
	weaponList(0)\vanishdist# = 500
	ScaleSprite weaponList(0)\ent,1.4,1.4
	MoveEntity weaponList(0)\ent,-2,0,4
	HideEntity weaponList(0)\ent
	weaponList(0)\strength#   = 5
	weaponList(0)\ammomax     = 200
	weaponList(0)\ammo        = 200
	weaponList(0)\speed#      = 4
	weaponList(0)\rotspeed#   = 10
	
	; Setup the ground:
	mFloor1 = CreatePlane(3)
	tFloor1 = LoadTexture(dGfx$ + "lava3.bmp")
	ScaleTexture tFloor1,30,30
	EntityTexture mFloor1,tFloor1
	EntityAlpha mFloor1,.3
	mFloor2 = CreatePlane(3)
	EntityTexture mFloor2,tFloor1
	EntityAlpha mFloor2,.6
	TurnEntity mFloor2,0,90,0
	EntityOrder mFloor2,2
	EntityOrder mFloor1,1
	
	; Setup the sky:
	mSky1 = CreatePlane(3)
	tSky1 = LoadTexture(dGfx$ + "marblate1.bmp")
	tSky2 = LoadTexture(dGfx$ + "marblate1.bmp")
	ScaleTexture tSky1,20,20
	ScaleTexture tSky2,20,20
	EntityTexture mSky1,tSky1
	EntityAlpha mSky1,.3
	mSky2 = CreatePlane(3)
	EntityTexture mSky2,tSky2
	EntityAlpha mSky2,.9
	RotateEntity mSky1,0,0,180
	RotateEntity mSky2,0,90,180
	EntityOrder mSky2,2
	EntityOrder mSky1,1
End Function

Function CreateNewFlame(flameent,speed#,untildist#,scale#)
	Local tempFlame.tFlame = New tFlame
	Local rn1#,rn2#
	
	tempFlame\obj = CopyEntity(flameent)
	RotateEntity tempFlame\obj,Rnd(0,359),Rnd(0,359),Rnd(0,359)
	PositionEntity tempFlame\obj,EntityX(pCraft),EntityY(pCraft),EntityZ(pCraft)
	PointEntity  tempFlame\obj,pFlamePoint
	rn1# = Rnd(-.2,.2)
	rn2# = Rnd(-.2,.2)
	MoveEntity tempFlame\obj,rn1,rn2,2.2       ; jiggle it a little.
	ShowEntity tempFlame\obj
	If scale <> 1 Then ScaleSprite tempFlame\obj,scale,scale
	tempFlame\speed     = speed
	tempFlame\untildist = untildist
	tempFlame\prescale  = (tempFlame\speed / tempFlame\untildist)
	tempFlame\scaledown = scale
	tempFlame\alphacn   = 1.0 / (tempFlame\untildist / tempFlame\speed)
	tempFlame\alphadown = 1
End Function

Function DoFlame()
	Local tempFlame.tFlame
	For tempFlame = Each tFlame
		MoveEntity  tempFlame\obj,0,0,tempFlame\speed
		tempFlame\scaledown = tempFlame\scaledown - (tempFlame\prescale*4)
		ScaleSprite tempFlame\obj,tempFlame\scaledown#,tempFlame\scaledown#
		EntityAlpha tempFlame\obj,tempFlame\scaledown#
;		RotateSprite tempFlame\obj,Rnd(0,360)
		If tempFlame\scaledown# < 0.05
			FreeEntity tempFlame\obj
			Delete tempFlame
		Else
			tempFlame\alphadown = tempFlame\alphadown - tempFlame\alphacn
			EntityAlpha tempFlame\obj,tempFlame\alphadown
			
			tempFlame\count = tempFlame\count + tempFlame\speed
			If tempFlame\count > tempFlame\untildist
				FreeEntity tempFlame\obj
				Delete tempFlame
			EndIf
		EndIf
	Next
End Function

Function CreateNewRing.tRing(x#,y#,z#,pitch#,yaw#,roll#,r,g,b)
	Local tempRing.tRing = New tRing
	tempRing\x = x
	tempRing\y = y
	tempRing\z = z
	tempRing\pitch = pitch
	tempRing\yaw   = yaw
	tempRing\roll  = roll
	tempRing\ePivot  = CopyEntity(pRing)
	tempRing\eRing   = GetChild(tempRing\ePivot,1)
	tempRing\eCenter = GetChild(tempRing\ePivot,2)
	tempRing\eFlare1 = GetChild(tempRing\ePivot,3)
	tempRing\eFlare2 = GetChild(tempRing\ePivot,4)
	MoveEntity tempRing\ePivot,x,y,z
	ShowEntity tempRing\ePivot
	If (r Or g Or b) <> -1 Then EntityColor tempRing\eRing,r,g,b
	
	Return tempRing
End Function

Function DoRings()
	Local tempRing.tRing
	For tempRing = Each tRing
		TurnEntity tempRing\ePivot,tempRing\pitch,tempRing\yaw,tempRing\roll
	Next
End Function

Function DoShields(alph#,turnspeed#)
	EntityAlpha mShields,alph#
	EntityAlpha mShields2,alph#
	TurnEntity  mShields,0,turnspeed#,0
	TurnEntity  mShields2,0,-turnspeed#,0
End Function

Function AddWeaponFire(weapon)
	tempBullet1.tBullet = CreateBullet(weapon,EntityX(pCraft)-2,EntityY(pCraft),EntityZ(pCraft)+4,pWeaponPoint1)
	tempBullet2.tBullet = CreateBullet(weapon,EntityX(pCraft)+2,EntityY(pCraft),EntityZ(pCraft)+4,pWeaponPoint2)
	PlaySound weaponList(weapon)\sound
End Function

Function CreateBullet.tBullet(weapon,x#,y#,z#,weaponpoint)
	tempBullet.tBullet = New tBullet
	tempBullet\weapon  = weapon
	tempBullet\ent1    = CopyEntity(weaponList(weapon)\ent)

	PositionEntity tempBullet\ent1,x#,y#,z#
	PointEntity    tempBullet\ent1,weaponpoint
	EntityAutoFade tempBullet\ent1,1,weaponList(weapon)\vanishdist#
	Return tempBullet
End Function

Function DoBullets()
	For tempBullet.tBullet = Each tBullet
		MoveEntity tempBullet\ent1,0,0,weaponList(tempBullet\weapon)\speed#
		RotateSprite tempBullet\ent1,tempBullet\rot#
		tempBullet\rot# = tempBullet\rot# + weaponList(tempBullet\weapon)\rotspeed#
		If EntityDistance(pCraft,tempBullet\ent1) > weaponList(tempBullet\weapon)\vanishdist#
			FreeEntity tempBullet\ent1
			Delete tempBullet
		EndIf
		temp = temp + 1 
	Next
End Function