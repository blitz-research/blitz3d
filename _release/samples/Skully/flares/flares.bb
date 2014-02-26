; Flares (C) 2001 Warren Skaalrud - SoftSkull Productions :)
; If you use this code email me at submissions@home.com so I know :)
; Unrestricted FREE use - just leave this text block in.

n_Flares=1

SeedRnd MilliSecs()

Global GxWidth=800
Global GxHeight=600
Global GxDepth=16
Global GxWindowed=0

;particle effects
Global particlecount
Global snaps$="FlareSnap"

Type particle		; not everything is used in this application
	Field x#		; x location of particle
	Field y#		; y location of particle
	Field z#		; z location of particle
	Field vx#		; x movement
	Field vy#		; y movement
	Field vz#		; z movement
	Field startlife ; For determining alpha levels
	Field life		; 0=death
	Field red		; red colour component
	Field green		; green colour component
	Field blue		; blue colour component
	Field gravity#	; Amount of gravity to exert on particle
	Field kind
	Field stage
	Field SEffect	;special effect
	Field flare
	Field alpha#
End Type

Type GfxMode
	Field width,height,depth,Windowed
End Type

; for use with normalizing etc.
Type vector	
	Field x#
	Field y#
	Field z#
	Field u#
	Field v#
End Type

; fire effects
Type Flare
	Field entity,speed#
	Field vx#,vy#,vz#
	Field life,lifespan
	Field kind
End Type

Type fpss	; fps history
	Field value
End Type

Type timer	; timers
	Field millisec
	Field time
End Type

Type ini	; stores the ini information
	Field info$
End Type

Global displayinfo=True
Global setpoint=140 ;fps
Global Bearing.vector=New vector

;Findscreen Globals
Global s3dx#	;3d upper left x,y in the 3D plane
Global s3dy#	;
Global Scale3DX#	; The ratio of screen location per 3D space
Global Scale3DY#
Global snap		;snapshot counter


;fps function
Global fpscount
Global fpstotal
Global fpstime

;mouse
Global mx=MouseX()
Global my=MouseY()

SoftStart3D()

AmbientLight 100,100,100
SetBuffer BackBuffer()

centre=CreateSphere()
PositionEntity centre,0,0,0
HideEntity centre

Global camera=CreateCamera()
PositionEntity camera,0,0,-10
PointEntity camera,centre
CameraRange camera,1,1000

MoveMouse GraphicsWidth()/2,GraphicsHeight()/2

;----------------------------------------
findscreen()

Global light=CreateLight(camera)
;TurnEntity light,45,45,0

Global sphere=SoftLoadSprite("graphics\redspark.bmp")
SpriteViewMode sphere,1
EntityShininess sphere,0.5
ScaleSprite sphere,12,0.4
;ScaleSprite sphere,12,0.1
HideEntity sphere

Global player=CreateSphere(8)
EntityColor player,100,255,255
EntityShininess player,0.5
EntityAlpha player,0.95
ScaleEntity player,1,1,2
HideEntity player
;PositionEntity player,0,0,-24

;flashlight=CreateLight(2,player)
;PositionEntity flashlight,0,0,0
;LightRange flashlight,100000
;LightColor	flashlight,100,255,255
;LightConeAngles flashlight,0,180

an#=0
an_step#=360.0/n_Flares

For k=1 To n_Flares
	;AddFlare(0)
Next
val#=0
rot#=0
speed#=0
While Not KeyHit(1)
	val=multiwrap(val+speed,0,359)
	fps=fps(20)			;get delayed fps
	mx=MouseX()
	my=MouseY()
	md1=MouseDown(1)
	md2=MouseDown(2)

	PositionEntity player,Screen3DX(mx),Screen3DY(my),-1

	If KeyDown(75) rot=multiwrap(rot-1,0,359)	; num 4
	If KeyDown(77) rot=multiwrap(rot+1,0,359)	; num 6

	If KeyDown(78) setpoint=setpoint+1; Num +
	If KeyDown(74) 	; Num -
		If setpoint>10
			 setpoint=setpoint-1
		EndIf
	EndIf
	If KeyDown(76)	; num 5
		PositionEntity camera,0,0,-10
		PointEntity camera,centre
	EndIf

	If KeyDown(72) Then speed=speed+0.1
	If KeyDown(80) Then speed=speed-0.1
	If KeyDown(200) Then MoveEntity camera,0,0,1
	If KeyDown(208) Then MoveEntity camera,0,0,-1
	If KeyDown(205) Then TurnEntity camera,0,-1,0
	If KeyDown(203) Then TurnEntity camera,0,1,0

	If particlecount<setpoint
		Addparticles(1,screen3dx(mx),screen3dy(my),0,Rnd(-0.5,0.5),Rnd(0,1),Rnd(-0.5,0.5),10000,0,0,1)
	Else
		If particlecount>setpoint
			p.particle=First particle
			FreeEntity p\flare
			Delete p
			particlecount=particlecount-1
		EndIf
	EndIf
	spiralize(val#,rot#)
	UpdateWorld
	RenderWorld

	If timer(200)
		If KeyDown(57)
			displayinfo=1-displayinfo
		EndIf
	EndIf

	If displayinfo
		Color 255,255,255
		Text 0,00,"FPS     : "+fps
		Color 100,100,100
		Text GraphicsWidth()/2,0,"Flares : "+particlecount,True
		Text GraphicsWidth()/2,20,"Space to hide/unhide",True
		Text 0,GraphicsHeight()-40,"Arrows to Navigate : Num 8/2 to Roll flares"
		Text 0,GraphicsHeight()-20,"SetPoint: "+setpoint+" Flares : Numpad +/- to change setpoint"
		Text GraphicsWidth()-StringWidth("(C) 2001 SoftSkull Productions"),GraphicsHeight()-20,"(C) 2001 SoftSkull Productions"
		Text GraphicsWidth()-StringWidth("Num 4/6 Rotate | PrntScrn = Snap"),0,"Num 4/6 Rotate | PrntScrn = Snap"
	EndIf
	Flip

	key=GetKey()
	If key
		show=key
	EndIf
	Select key
	Case 42 ;
		While fileexists(snaps+snap+".bmp")
			snap=snap+1
		Wend
		SaveBuffer (FrontBuffer(),snaps+snap+".bmp")
		snap=snap+1
	End Select
Wend
WriteINI("startup.ini")
EndGraphics
End

Function SoftStart3D()
	; Retreive default values
	LoadINI("startup.ini")
	GxWidth=GetINIVal("gxwidth",800)
	GxHeight=GetINIVal("gxheight",600)
	GxDepth=GetINIVal("gxdepth",16)
	GxWindowed=GetINIVal("gxwindowed",0)
	If GxWidth=0 Or GxHeight=0 Or (GxDepth=0 And GxWindowed=1) ; prevent errors
		GxWidth=800		;back to defaults - if this doesnt exist in the modes list
		GxHeight=600	;it will go to setup mode
		GxDepth=16
		GxWindowed=0
	EndIf
	win$="Full Screen"
	If GxWindowed
		win="Windowed"
	EndIf
	Print "Starting Mode="+gxwidth+"x"+gxheight+"-"+GxDepth+" Bit :"+win
	Print "<Spacebar> To configure Graphics Mode"
	setup=False
	time=MilliSecs()
	Repeat
		If KeyDown(57)	;spacebar
			Setup=True
		EndIf
	Until setup Or (MilliSecs()-time>3000)

	Cls
	Locate 0,0
	Print "Make sure your monitor can support your selection"
	
	If CountGfxDrivers()>1
		Print "Display drivers:"
		For k=1 To CountGfxDrivers()
			Print k+":"+GfxDriverName$(k)
		Next
		Repeat
			driver=Input$( "Display driver (1-"+CountGfxDrivers()+"):" )
		Until driver>=1 And driver<=CountGfxDrivers()
		SetGfxDriver driver
	EndIf
	
	Print "Display modes:"
	cnt=0
	column=0:ct=FontHeight()
	For k=1 To CountGfxModes()
		If GfxMode3D(k)
			
			t.GfxMode=New GfxMode
			t\width=GfxModeWidth(k)
			t\height=GfxModeHeight(k)
			t\depth=GfxModeDepth(k)
			
			; indicate currently selected mode
			char$=":"
			If t\width=GxWidth And t\height=GxHeight And t\depth=GxDepth
				char$=">" ; found the currently selected mode
				If Not setup	; if we are not changing that... GO!	
					Graphics3D GxWidth,GxHeight,GxDepth,GxWindowed
					Return
				EndIf
			EndIf
			
			
			b3d$="2D"
			If GfxMode3D(k)
				b3d$="3D"
			EndIf		
			
			cnt=cnt+1
			ct=ct+FontHeight()
			If ct>260
				ct=FontHeight()*2
				column=column+145
			EndIf
			Locate column,ct
			Print cnt+char+t\width+","+t\height+","+t\depth+":"+b3d$
		EndIf
	Next
	
	If Not cnt
		; Doom
		Print "No 3D Graphics modes available!"
		Print "Press any key..."
		WaitKey
		EndGraphics
		End
	EndIf
	
	Repeat
		mode=Input$( "Display Mode (1-"+cnt+"):" )
	Until mode>=1 And mode<=cnt
	
	t.GfxMode=First GfxMode
	
	While mode>1
		t=After t
		mode=mode-1
	Wend
	
	GxWidth=t\width
	GxHeight=t\height
	GxDepth=t\depth
	GxWindowed=0
	Graphics3D GxWidth,GxHeight,GxDepth,GxWindowed
	ChangeINI("gxWidth",GxWidth)
	ChangeINI("gxHeight",GxHeight)
	ChangeINI("gxDepth",GxDepth)
	ChangeINI("gxwindowed",GxWindowed)
	SetBuffer BackBuffer()
	Delete Each GfxMode
End Function

Function LoadINI(file$)
	; This is a one time call... It places the contents into the ini buffer
	If FileExists(file)
		ini=ReadFile(file)
		While Not Eof(ini)
			b.ini=New ini
			b\info=Lower$(ReadLine$(ini))
		Wend
		CloseFile INI
	EndIf
End Function

Function GetINIVal(search$,defvalue$)	; Extract a configuration item from the Search
	search=Lower(search)
	For i.ini=Each ini
		If Instr(i\info,search)=1
			spot=Instr(i\info,"=")	; check to see if theres a second half
			If spot>0 And spot<Len(i\info)	; otherwise there is no value (nan)
				value$=Right$(i\info,Len(i\info)-spot)
				Return value
			EndIf
		EndIf
	Next
	; value does not exist
	If defvalue
		ChangeINI(search$,defvalue$); - add the Default
		Return search+"="+defvalue
	EndIf
End Function

Function ChangeINI(search$,value$)
	;Changes Not permanent until WriteINI is called
	search=Lower(search)
	For i.ini=Each ini
		If Instr(i\info,search)>0
			i\info$=search+"="+value
			Return
		EndIf
	Next
	; if we are here its because no ini entry for this was found
	i.ini=New ini
	i\info$=search+"="+value
End Function

Function WriteINI(file$)
	If First ini = Null
		; No INI information to write
	Else
		If FileExists(file)
			DeleteFile file
		EndIf	
		ini=WriteFile(file)
		For i.ini=Each ini
			WriteLine ini,i\info
		Next	
		CloseFile ini
	EndIf
End Function

Function FileExists(file$)
	result=FileType(file$)
	If result
		Return True
	Else
		Return False
	EndIf
End Function

Function fps(buffer)
	;(c) 2001 SoftSkull Productions 
	;0 = averaged 1=instant (tends to be hard to read)
	fps#=MilliSecs()-fpstime
	If fps=0
		fps=1
	EndIf
	fps=1000.0/fps ;instantaneous fps (Buffer=0)

	fpstime=MilliSecs()

	If buffer>0
		f.fpss=New fpss
		f\value=fps
		fpstotal=0
		fpscount=0

		For l.fpss=Each fpss
			If fpscount>buffer		;
				Delete First fpss
			Else
				fpscount=fpscount+1 ;count the number of calls
				fpstotal=fpstotal+l\value;fps ;total amount
			EndIf
		Next
		fps=fpstotal/fpscount
	EndIf
	Return fps
End Function

Function timer(time)
	;(c) 2001 SoftSkull Productions 
	cond=False
	found=False
	For t.timer=Each timer
		If t\time=time
			found=True
			elapse=MilliSecs()-t\millisec
			If elapse>t\time
				cond=True
				t\millisec=t\millisec+t\time
			EndIf
		EndIf
	Next
	If found=False
		t.timer=New timer
		t\time=time
		t\MilliSec=MilliSecs()
	EndIf
	Return cond
End Function

Function findscreen()
		;(c) 2001 SoftSkull Productions 
		CameraProject(camera,0,0,0)
		sx#=ProjectedX()
		sy#=ProjectedY()
	
		CameraProject(camera,1,1,0)
		ex#=ProjectedX()
		ey#=ProjectedY()
	
		Scale3DX#=ex-sx			;ratio of x movement
		Scale3DX=1.0/Scale3DX	;inverted ratio of x movement
		Scale3DY#=ey-sy			;ratio of y movement
		Scale3DY=1.0/Scale3DY	;inverted ratio of y movement

		s3dx#=GraphicsWidth()/-2.0	; offset the pointer
		s3dx=s3dx*Scale3DX
		s3dy#=GraphicsHeight()/-2.0	; offset the pointer
		s3dy=s3dy*Scale3DY
		CameraProject(camera,s3dx,s3dy,0)
End Function

Function Screen3DX#(x#)		;used to move 3d objects to 2d coordinates
	;(c) 2001 SoftSkull Productions 
	ret#=S3dX+(x*Scale3dX)
	Return ret
End Function

Function Screen3DY#(y#)
	;(c) 2001 SoftSkull Productions 
	Ret#=S3dY+(y*Scale3dY)
	Return Ret
End Function

Function multiwrap#(x#,low#,high#)
	range=(high-low)+1
	If x>high
		x=low+((x-low) Mod range)	;high
	Else
		If x<low
			x=high-(Abs(x) Mod range)+1 ;low
		EndIf
	EndIf
	Return x
End Function

Function softLoadSprite(fname$)
	If FileType(fname$)
		file=LoadSprite(fname$)
		Return file
	Else
		RuntimeError "Sprite file <" + fname$ + "> not found."
	EndIf
End Function

Function addparticles(num,x#,y#,z#,vx#,vy#,vz#,life,life_spread,grvty#,kind)
	For t=1 To num
		particlecount=particlecount+1
		a.particle=New particle
		a\x=x	
		a\y=y
		a\z=z
		a\vx=vx+Rnd(-0.5,0.5)	
		a\vy=vy+Rnd(-0.5,0.5)
		a\vz=vz+Rnd(-0.5,0.5)
		a\life=life-Rnd(life_spread)
		a\startlife=a\life
		a\red=Rnd(150)+105
		a\green=Rnd(150)+105
		a\blue=Rnd(50)+100
		a\gravity=grvty
		a\kind=kind
		a\stage=Rnd(5)
		Select a\kind
		Case 1
			a\flare=CopyEntity(sphere)
			a\alpha=1.0
			PositionEntity a\flare,a\x,a\y,a\z
			EntityAlpha a\flare,0.9
			EntityColor a\flare,a\red,a\green,a\blue
		Default
			;
		End Select
	Next
End Function

;move the particles around
Function spiralize(start#,rot#)
	radius#=15.0
	prate#=14400.0/particlecount
	rrate#=radius/particlecount
	pos#=start
	poz#=0
	cx#=0
	cy#=0
	For p.particle=Each particle
		pos=multiwrap(pos+prate,0,359)
		pox#=cx+Sin(pos)*radius
		poy#=cy+Cos(pos)*radius
		poz#=poz#+1
		radius=radius-rrate
		PositionEntity p\flare,pox,poy,poz
		RotateSprite p\flare,-pos+rot
	Next
End Function

;My Thanks to: 
;=-===========================
;	Maths Functions
;
;	(c)David Bird
;	dave@birdie72.freeserve.co.uk

;
;	Types Universal


Function GetBearingVector(ent1,ent2)
	Bearing\x#=EntityX(ent2)-EntityX(ent1)
	Bearing\y#=EntityY(ent2)-EntityY(ent1)
	Bearing\z#=EntityZ(ent2)-EntityZ(ent1)
	Normalise(Bearing)
End Function

Function Mag#(x#,y#,z#)
	Return Sqr(x^2+y^2+z^2)
End Function

Function Normalise(a.vector)
	l# = Mag(a\x,a\y,a\z)
	a\x=a\x/l
	a\y=a\y/l
	a\z=a\z/l
	Return
End Function