;
; SKY PLATEAU V1.01
; --------------------
; Simple 3D Demo, shows lots of simple 3D concepts.
; Written by Rob Hutchinson 2001 for Blitz 3D
; All models, textures and sounds by Rob Hutchinson.
; Email: loki.sd@cableinet.co.uk / ICQ: 27181384
;
; Created with BB3D Beta 1.60
; Last Modified: 18/08/2001
;

; Change this if you like.....
Global maxFlies = 5
Global skiesUnreal = 1

; Application title
AppTitle "Sky Plateau V1.01 - By Rob Hutchinson 2001"

; A type to hold each totem pole..
Type totem
	Field pivot,totem,flare,light
End Type

Type plant
	Field pivot,plant1,plant2,plant3,plant4
End Type

; Startup Include for BBLauncher!
;---------------------------------
; you can format the "Program Command Line" from the options like this:
; "/TYPE<Screen> /WIDTH<Width> /HEIGHT<Height> /BPP<Depth> /ADDIT<Additional>"
; Where <Screen> is either:
;  0 = FULLSCREEN
;  2 = WINDOWED or
;  1 = SCALED
;
; <Width> = display width, <Height> = display height, <Depth> = display depth.
;
; If you do not specify a command line, the default resolution of: 640x480x16 
; (fullscreen always), will be assumed.
;
Include "launcher3d.bb"
If iAdditional = -1 Or iAdditional = 0
	skiesUnreal = 0
Else 
	skiesUnreal = 1
EndIf

If skiesUnreal = 1
	iPlaneSegs = 15
Else
	iPlaneSegs = 1
EndIf

; Build the camera grouping...
Global campiv = CreatePivot()
Global camgrp = CreatePivot(campiv)   ; Camera Grip
Global camera = CreateCamera(camgrp)
MoveEntity campiv,0,5,0
MoveEntity camgrp,0,25,-40
CameraFogMode camera,1
CameraFogRange camera,1,2
PointEntity camgrp,campiv
If skiesUnreal Then AmbientLight 0,0,0
Global cammoving = 1

; Create some totem poles.
Dim totems.totem(4)
totems(0) = stCreateTotem("Models\totem.x","Graphics\flare.bmp",20,0,20,0,45*5,0,255,0,0)
totems(1) = stCopyTotem(totems(0),-20,0,-20,0,45,0,0,255,0)
totems(2) = stCopyTotem(totems(0),20,0,-20,0,45*3,0,0,0,255)
totems(3) = stCopyTotem(totems(0),-20,0,20,0,45*7,0,0,255,255)

; Load up the plateau.
Global ground = LoadAnimMesh("Models\Ground.x")
MoveEntity ground,0,-2,0

; Sky and lower
Global skyplane1  = CreatePlane(iPlaneSegs)
Global skyplane1t = LoadTexture("Graphics\sky.bmp")
ScaleTexture skyplane1t,70,70
EntityTexture skyplane1,skyplane1t
TurnEntity skyplane1,0,0,180
MoveEntity skyplane1,0,-65,0
EntityAlpha skyplane1,.8
EntityBlend skyplane1,3

Global skyplane2  = CreatePlane()
Global skyplane2t = LoadTexture("Graphics\stars.bmp")
ScaleTexture skyplane2t,22,22
EntityTexture skyplane2,skyplane2t
TurnEntity skyplane2,0,0,180
MoveEntity skyplane2,0,-65.2,0

Global skyplane3  = CreatePlane(4)
Global skyplane3t = LoadTexture("Graphics\lower.bmp")
ScaleTexture skyplane3t,70,70
EntityTexture skyplane3,skyplane3t
MoveEntity skyplane3,0,-10,0

; Logo
logosp = LoadSprite("Graphics\logo.bmp",1,camgrp)
EntityOrder logosp,-1

; SETUP SYSTEM
Const DEMO_STATE_FOGOPEN     = 1
Const DEMO_STATE_FOGLOGOMOVE = 2
Const DEMO_STATE_FADEOPTIONS = 3
Const DEMO_STATE_STREAM           = 99
Const DEMO_STATE_TERMINATESPIN    = 1000
Const DEMO_STATE_TERMINATEFOGFADE = 1001

demostate = DEMO_STATE_FOGOPEN
fr# = 2

; Logo movement
LogoStartX# = 0
LogoStartY# = 0
LogoStartZ# = 3.3
LogoEndX#   = -4.8
LogoEndY#   = 3.4
LogoEndZ#   = 6
MoveSpd#    = 0.1
SlowSpd#    = 1.015

PositionEntity logosp,LogoStartX#,LogoStartY#,LogoStartZ#
movetopiv = CreatePivot(camgrp)
PositionEntity movetopiv,LogoEndX#,LogoEndY#,LogoEndZ#

; Menu Setup
Const MENU_STATE_IDLE   = 0
Const MENU_STATE_FADEDN = 1
Const MENU_STATE_FADEUP = 2
Const MENU_STATE_ENTERU = 3
Const MENU_STATE_ENTERD = 4

Global menustate    = MENU_STATE_IDLE
Global menufademax# = .5
Global menufade#    = menufademax#
Global menufadespd# = .02

optssp = LoadSprite("Graphics\options.bmp",1,camgrp)
EntityOrder optssp,-1
ScaleSprite optssp,1.5,.7

optsbarsp = LoadSprite("Graphics\menubar.bmp",1,camgrp)
EntityOrder optsbarsp,-1
ScaleSprite optsbarsp,1.6,.19
EntityAlpha optsbarsp,menufade#

PointEntity logosp,movetopiv
PositionEntity optssp,-LogoEndX#-.5,LogoEndY#+.3,LogoEndZ#

Type mnuitem
	Field x#,y#,z#
End Type

Dim MenuItems.mnuitem(4)
MenuItems(0) = New mnuitem
MenuItems(0)\x = 4.12
MenuItems(0)\y = 4.07
MenuItems(0)\z = 5.8

MenuItems(1) = New mnuitem
MenuItems(1)\x = 4.12
MenuItems(1)\y = 3.74
MenuItems(1)\z = 5.8

MenuItems(2) = New mnuitem
MenuItems(2)\x = 4.12
MenuItems(2)\y = 3.42
MenuItems(2)\z = 5.8

MenuItems(3) = New mnuitem
MenuItems(3)\x = 4.12
MenuItems(3)\y = 3.09
MenuItems(3)\z = 5.8

Global CurrentOption = 3
PositionEntity optsbarsp,MenuItems(CurrentOption)\x,MenuItems(CurrentOption)\y,MenuItems(CurrentOption)\z

Global FadeBias# = 0.1

; Fireflies........
Type firefly
	Field obj
	Field posx#,posy#,posz#
	Field rotx#,roty#,rotz#
	Field radx#,rady#,radz#
	Field spdx#,spdy#,spdz#
	Field spnx#,spny#,spnz#
End Type

; Setup the fireflies
Global TopSpeed# = 2.5
flyOrig = LoadSprite("Graphics\hyperflare.bmp",1)
PositionEntity flyOrig,0,20,0
ScaleSprite flyOrig,1.75,1.75
HideEntity flyOrig

; Create the fireflies
Dim flies.firefly(maxFlies+1)

For a=0 To maxFlies							; Create a pile of flies
	flies(a)     = New firefly
	flies(a)\obj = CopyEntity(flyOrig)
	EntityAlpha flies(a)\obj,Rnd(0.1,1.0)
	ShowEntity  flies(a)\obj

	flies(a)\rotx = Rnd(360)
	flies(a)\roty = Rnd(360)
	flies(a)\rotz = Rnd(360)
	flies(a)\radx = Rnd(-10,10)
	flies(a)\rady = Rnd(-10,10)
	flies(a)\radz = Rnd(-10,10)
	flies(a)\spdx = Rnd(-TopSpeed#,TopSpeed#)	
	flies(a)\spdy = Rnd(-TopSpeed#,TopSpeed#)
	flies(a)\spdz = Rnd(-TopSpeed#,TopSpeed#)
	flies(a)\spnx = Rnd(-TopSpeed#,TopSpeed#)
	flies(a)\spny = Rnd(-TopSpeed#,TopSpeed#)
	flies(a)\spnz = Rnd(-TopSpeed#,TopSpeed#)
Next

; the environment map.
steela = LoadTexture("Graphics\env.bmp",64)
surrounda = LoadTexture("Graphics\env2.bmp",64)

; the center tubing computer thing-e-me-bob.. + the computer screens and up lighter...
EntityAlpha GetChild(ground,6),.7
EntityAlpha GetChild(ground,7),.7
centertube = GetChild(ground,5)
EntityTexture centertube,surrounda
EntityBlend GetChild(ground,9),3

Global iMouseLook = 0

; icon thing..
iconmesh  = LoadAnimMesh("Models\icon.x")
icon      = GetChild(iconmesh,1)
container = GetChild(iconmesh,2)
ScaleEntity iconmesh,.2,.2,.2
MoveEntity iconmesh,0,20,0
EntityAlpha container,.2
shieldsa = LoadTexture("Graphics\shields.bmp")
EntityTexture icon,steela
EntityTexture container,shieldsa

; MD2 stuff.....
myplant.plant = stCreateFourPlant("Models\Plantlife.md2","Graphics\Plantlife.bmp",11.8,-2.1,-10.2,0,0,0)
planter1.plant = stCopyFourPlant(myplant,-40.6,1.4,7.0,-17.2,36.79,-7.4,4)
planter2.plant = stCopyFourPlant(myplant,29,1.6,-49.2,4,0,9.9,4)
planter3.plant = stCopyFourPlant(myplant,-11,-2.4,3.82,-6.39,-28.4,-2,1)
planter4.plant = stCopyFourPlant(myplant,30.8,2.7,46.76,-1.19,-108.3,-21.1,1)

; Final lighting...
;piva = CreatePivot()
;cona = CreateCone(3,1,piva)
;toplight = CreateLight(2,piva)
;LightColor toplight,50,50,50
;LightRange toplight,100
;RotateEntity toplight,90,0,0
;MoveEntity piva,0,30,0

If skiesUnreal
	skylightpiv = CreatePivot()
	;skylightcone = CreateCone(3,1,skylightpiv)
	skylight = CreateLight(2,skylightpiv)
	LightColor skylight,255,0,0
	LightRange skylight,90
	RotateEntity skylight,-90,0,0
	MoveEntity skylightpiv,80,30,80
	
	skylightpiv1 = CreatePivot()
	;skylightcone1 = CreateCone(3,1,skylightpiv1)
	skylight1 = CreateLight(2,skylightpiv1)
	LightColor skylight1,0,0,255
	LightRange skylight1,90
	RotateEntity skylight1,-90,0,0
	MoveEntity skylightpiv1,-80,30,80
	
	skylightpiv2 = CreatePivot()
	;skylightcone2 = CreateCone(3,1,skylightpiv2)
	skylight2 = CreateLight(2,skylightpiv2)
	LightColor skylight2,255,255,0
	LightRange skylight2,90
	RotateEntity skylight2,-90,0,0
	MoveEntity skylightpiv2,80,30,-80
	
	skylightpiv3 = CreatePivot()
	;skylightcone3 = CreateCone(3,1,skylightpiv3)
	skylight3 = CreateLight(2,skylightpiv3)
	LightColor skylight3,0,255,255
	LightRange skylight3,90
	RotateEntity skylight3,-90,0,0
	MoveEntity skylightpiv3,-80,30,-80
EndIf	

; Start the music, boy-o!
Global musicchan
musicchan = StartMusic()

Repeat
	If iMouseLook
		; Thanks to Si for this little block of code I pinched :)....
		mxs=MouseXSpeed()/2
		mys=MouseYSpeed()/2
		
		; Update destination camera angle x and y values
		dest_xang#=dest_xang#+mys
		dest_yang#=dest_yang#-mxs
	
		; Curve camera angle values towards destination values
		xang#=CurveValue#(xang#,dest_xang#,5)
		yang#=CurveValue#(yang#,dest_yang#,5)
	
		; Rotate camera
		RotateEntity camgrp,xang#,yang#,0
		MoveMouse GraphicsWidth()/2,GraphicsHeight()/2
	EndIf
	
	; Do music repeat
	If ChannelPlaying(musicchan) = False Then musicchan = StartMusic()

	; Do the fire flies.
	For a=0 To maxFlies
		TurnEntity flies(a)\obj,flies(a)\spnx,flies(a)\spny,flies(a)\spnz
		flies(a)\posx=Sin(flies(a)\rotx)*flies(a)\radx
		flies(a)\posy=Cos(flies(a)\roty)*flies(a)\rady
		flies(a)\posz=Sin(flies(a)\rotz)*flies(a)\radz
		flies(a)\rotx=flies(a)\rotx+flies(a)\spdx
		flies(a)\roty=flies(a)\roty+flies(a)\spdy
		flies(a)\rotz=flies(a)\rotz+flies(a)\spdz
		flies(a)\rotx=QWrap(flies(a)\rotx,0,360)
		flies(a)\roty=QWrap(flies(a)\roty,0,360)
		flies(a)\rotz=QWrap(flies(a)\rotz,0,360)
		PositionEntity flies(a)\obj,flies(a)\posx,flies(a)\posy+20,flies(a)\posz
	Next	

	; Demo states...
	Select demostate
		Case DEMO_STATE_FOGOPEN
			CameraFogRange camera,1,fr#
			fr# = fr# + 0.4
			If fr# >= 250 Then demostate = DEMO_STATE_LOGOMOVE
			
		Case DEMO_STATE_LOGOMOVE
			MoveEntity logosp,0,0,MoveSpd
			MoveSpd = MoveSpd / SlowSpd
			If EntityZ(logosp) >= LogoEndZ# Then demostate = DEMO_STATE_FADEOPTIONS
			
		Case DEMO_STATE_FADEOPTIONS
		
		Case DEMO_STATE_STREAM

		Case DEMO_STATE_TERMINATESPIN
			MoveEntity camgrp,0,0,-FadeBias#
			If fadebw = 0
				FadeBias# = FadeBias# * 1.02
			Else
				FadeBias# = FadeBias# / 1.02
			EndIf
			If FadeBias# >= 2.00 Then fadebw = 1
			If fadebw = 1
				If FadeBias# < 0.2 
					demostate = DEMO_STATE_TERMINATEFOGFADE
				EndIf
			EndIf
		
		Case DEMO_STATE_TERMINATEFOGFADE
			fr# = fr# - 1.7
			If fr# <= 2.0 Then End
			CameraFogRange camera,1,fr#

	End Select

	; Menu Controls.
	If menuonline = 0
		Select menustate
			Case MENU_STATE_IDLE
				If KeyHit(208)  ; Down
					CurrentOption = CurrentOption + 1
					If CurrentOption > 3
						CurrentOption = 3
					Else
						menustate = MENU_STATE_FADEDN
					EndIf
				Else
					If KeyHit(200)  ; Up
						CurrentOption = CurrentOption - 1
						If CurrentOption < 0
							CurrentOption = 0
						Else
							menustate = MENU_STATE_FADEDN
						EndIf
					Else
						If KeyHit(28) ; Enter
							menustate = MENU_STATE_ENTERU
						EndIf
					EndIf
				EndIf
				
			Case MENU_STATE_FADEDN
				menufade# = menufade# - menufadespd#
				If menufade# <= 0
					menufade# = 0
					menustate = MENU_STATE_FADEUP
					PositionEntity optsbarsp,MenuItems(CurrentOption)\x,MenuItems(CurrentOption)\y,MenuItems(CurrentOption)\z
				EndIf
				EntityAlpha optsbarsp,menufade#
			
			Case MENU_STATE_FADEUP
				menufade# = menufade# + menufadespd#
				If menufade# >= menufademax#
					menufade# = menufademax#
					menustate = MENU_STATE_IDLE
				EndIf
				EntityAlpha optsbarsp,menufade#
				
			Case MENU_STATE_ENTERU
				menufade# = menufade# + menufadespd#
				If menufade# >= 1
					menufade# = 1
					menustate = MENU_STATE_ENTERD
					
					; Menu commands:
					Select CurrentOption
						Case 0
							EndGraphics
							ExecFile "Launcher.exe"
							End
						Case 1
							iMouseLook = Not iMouseLook
						Case 2
							cammoving = Not cammoving
						Case 3
							menuonline = 1
							demostate = DEMO_STATE_TERMINATESPIN
					End Select
				EndIf
				EntityAlpha optsbarsp,menufade#
			
			Case MENU_STATE_ENTERD
				menufade# = menufade# - menufadespd#
				If menufade# <= menufademax#
					menufade# = menufademax#
					menustate = MENU_STATE_IDLE
				EndIf
				EntityAlpha optsbarsp,menufade#
				
		End Select
	EndIf			

	; Move stuff around a lil

	MoveEntity skyplane1,0,0,.5
	MoveEntity skyplane2,0,0,.2
	MoveEntity skyplane3,0,0,.3

	TurnEntity container,0,1,2
	TurnEntity icon,0,-2,0
	
	If cammoving
		RotateEntity campiv,Sin(ll#)*20,ll#,0
		ll# = ll# + 0.7
	EndIf
	
	UpdateWorld
	RenderWorld
;	DEVPosRotEntityWM(planter4\pivot,campiv)
	Flip
Until KeyDown(1)
End

Function stCreateTotem.totem(filename$,flarename$,x#,y#,z#,p#,ya#,r#,lr,lg,lb)
	Local tTotem.totem = New totem
	
	tTotem\pivot = CreatePivot()
	tTotem\totem = LoadMesh(filename$,tTotem\pivot)
	tTotem\flare = LoadSprite(flarename$,1,tTotem\pivot)
	If skiesUnreal=0 Then
		tTotem\light = CreateLight(1,tTotem\pivot)
		LightColor tTotem\light,lr,lg,lb
		PointEntity tTotem\light,campiv
		MoveEntity tTotem\light,2.2,26.65,0
	EndIf
	
	ScaleSprite tTotem\flare,5,5
	MoveEntity tTotem\flare,2.2,26.65,0
	EntityOrder tTotem\flare,-1
	PositionEntity tTotem\pivot,x#,y#,z#
	RotateEntity tTotem\pivot,p#,ya#,r#
	
	Return tTotem
End Function

Function stCopyTotem.totem(tot.totem,x#,y#,z#,p#,ya#,r#,lr,lg,lb)
	Local tTotem.totem = New totem
	
	tTotem\pivot = CreatePivot()
	tTotem\totem = CopyEntity(tot\totem,tTotem\pivot)
	tTotem\flare = CopyEntity(tot\flare,tTotem\pivot)
	If skiesUnreal=0 Then
		tTotem\light = CreateLight(1,tTotem\pivot)
		MoveEntity tTotem\light,2.2,26.65,0
		LightColor tTotem\light,lr,lg,lb
		PointEntity tTotem\light,campiv
	EndIf
	
	PositionEntity tTotem\pivot,x,y,z
	RotateEntity tTotem\pivot,p,ya,r
	
	Return tTotem
End Function

Function QWrap(Value#,Low#,High#)			; Remember this? where is the Blitz
	Return Low#+(Value# Mod High#)			; PC version when you need it? :))
End Function 

Function StartMusic()
	Return PlayMusic("Music\atmos.mp3")
End Function

Function CurveValue#(current#,destination#,curve)
	current#=current#+((destination#-current#)/curve)
	Return current#
End Function

Function stCreateFourPlant.plant(filename$,texturepa$,x#,y#,z#,p#,ya#,r#)
	Local tPlant.plant = New plant

	tPlant\pivot  = CreatePivot()
	tPlant\plant1 = LoadMD2(filename$,tPlant\pivot)
	EntityTexture tPlant\plant1,LoadTexture(texturepa$)
	ScaleEntity tPlant\plant1,.2,.1,.1
	PositionEntity tPlant\plant1,-.5,0,0

	tPlant\plant2 = CopyEntity(tPlant\plant1,tPlant\pivot)
	tPlant\plant3 = CopyEntity(tPlant\plant1,tPlant\pivot)
	tPlant\plant4 = CopyEntity(tPlant\plant1,tPlant\pivot)
	RotateEntity tPlant\plant2,0,90*1,0
	RotateEntity tPlant\plant3,0,90*2,0
	RotateEntity tPlant\plant4,0,90*3,0
	
	PositionEntity tPlant\pivot,x#,y#,z#
	RotateEntity tPlant\pivot,p#,ya#,r#
	
	AnimateMD2 tPlant\plant1,2,Rnd(0.02,0.05),0,2
	AnimateMD2 tPlant\plant2,2,Rnd(0.02,0.05),0,2
	AnimateMD2 tPlant\plant3,2,Rnd(0.02,0.05),0,2
	AnimateMD2 tPlant\plant4,2,Rnd(0.02,0.05),0,2

	Return tPlant
End Function

Function stCopyFourPlant.plant(planty.plant,x#,y#,z#,p#,ya#,r#,copyamt)
	Local tPlant.plant = New plant

	tPlant\pivot  = CreatePivot()
	If copyamt > 0
		tPlant\plant1 = CopyEntity(planty\plant1,tPlant\pivot)
		AnimateMD2 tPlant\plant1,2,Rnd(0.02,0.05),0,2
	EndIf
	If copyamt > 1
		tPlant\plant3 = CopyEntity(planty\plant3,tPlant\pivot)
		AnimateMD2 tPlant\plant3,2,Rnd(0.02,0.05),0,2
	EndIf
	If copyamt > 2
		tPlant\plant2 = CopyEntity(planty\plant2,tPlant\pivot)
		AnimateMD2 tPlant\plant2,2,Rnd(0.02,0.05),0,2
	EndIf
	If copyamt > 3
		tPlant\plant4 = CopyEntity(planty\plant4,tPlant\pivot)
		AnimateMD2 tPlant\plant4,2,Rnd(0.02,0.05),0,2
	EndIf

	PositionEntity tPlant\pivot,x#,y#,z#
	RotateEntity tPlant\pivot,p#,ya#,r#

	Return tPlant
End Function

; DEVPosRotEntityWM(entityobject,cameraobject,[speed#])
; -----------------------------------------------------
; AUTHOR: Rob Hutchinson (loki.sd@cableinet.co.uk)
; BB3DV:  v1.60 Beta.
; DESC:   Simple function to help you position and rotate an entity with the mouse.
; PARAMS: entityobject - the entity you want to fiddle with.
;         speed#       - the speed to move with. Recommend around 0.1 (default)
; NOTES:  Will lock the mouse in the center of the screen in order to achieve more
;         smooth movement with MXS() and MYS(). You should comment out any mouse
;         command stuff your program is doing before using this function.
; USAGE:  Call this function straight after RenderWorld(), cos it needs to output text.
;
;           CONTROL:     NORMAL MODE  + ALT MODE     + ROT MODE    + ROT+ALT MODE
; CONTROLS: Up/Down    = +/- Z axis   | +/- Y axis   | +/- Roll    | +/- Roll    
;           Left/Right = +/- X axis   | +/- X axis   | +/- Pitch   | +/- Yaw
;           ---------------------------------------------------------------------------           
;           SWITCHES:
;           LMB        = Hold down to switch to alt mode.
;           RMB        = Hold down to switch to rotation mode.
;           MMB        = Hold down to switch to camera mode. (this will allow you to
;                        simply see whats going on)
;           F1         = Reset all positions
;
;           HAHA! its a bitch to start with, but it gets easier :) (unless your MMB is
;           in some ungodly place on the mouse :)).
;           
Global entitX#,entitY#,entitZ#
Global entitPi#,entitYa#,entitRo#
Global cammyX#,cammyY#,cammyZ#
Global cammyPi#,cammyYa#,cammyRo#
Function DEVPosRotEntityWM(ent,cammy,spd# = .1)
	msX# = MouseXSpeed() * spd#
	msY# = MouseYSpeed() * spd#

	If MouseDown(3) = False
		; We are working with the entity..
		If MouseDown(2) = False
			entitX# = entitX# + msX#
			If MouseDown(1) = False
				entitZ# = entitZ# - msY#
			Else
				entitY# = entitY# - msY#
			EndIf
		Else
			entitPi# = entitPi# - msY#
			If MouseDown(1) = False
				entitRo# = entitRo# - msX#
			Else
				entitYa# = entitYa# + msX#
			EndIf
		EndIf
	Else
		; We are working with the camera..
		If MouseDown(2) = False
			cammyX# = cammyX# + msX#
			If MouseDown(1) = False
				cammyZ# = cammyZ# - msY#
			Else
				cammyY# = cammyY# - msY#
			EndIf
		Else
			cammyPi# = cammyPi# - msY#
			If MouseDown(1) = False
				cammyRo# = cammyRo# - msX#
			Else
				cammyYa# = cammyYa# + msX#
			EndIf
		EndIf
	EndIf
	
	If KeyHit(59)
		; Reset positions....
		entitX# = 0 : entitY# = 0 : entitZ# = 0
		cammyX# = 0 : cammyY# = 0 : cammyZ# = 0
		entitPi# = 0 : entitYa# = 0 : entitRo# = 0
		cammyPi# = 0 : cammyYa# = 0 : cammyRo# = 0
	EndIf

	PositionEntity ent,entitX#,entitY#,entitZ#
	RotateEntity ent,entitPi#,entitYa#,entitRo#
	PositionEntity cammy,cammyX#,cammyY#,cammyZ#
	RotateEntity cammy,cammyPi#,cammyYa#,cammyRo#
	
	Text 0,FontHeight()*0,"PRWM() - Entity X# = " + entitX#
	Text 0,FontHeight()*1,"PRWM() - Entity Y# = " + entitY#
	Text 0,FontHeight()*2,"PRWM() - Entity Z# = " + entitZ#
	Text 0,FontHeight()*3,"PRWM() - Entity Pitch# = " + entitPi#
	Text 0,FontHeight()*4,"PRWM() - Entity Yaw# = " + entitYa#
	Text 0,FontHeight()*5,"PRWM() - Entity Roll# = " + entitRo#

	MoveMouse GraphicsWidth()/2,GraphicsHeight()/2
End Function