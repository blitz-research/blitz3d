;
; BLOX&SPHERES V1.07 - Written for Blitz 3D.
; Written by Rob Hutchinson - loki.sd@cableinet.co.uk
;
; A VERY simple 3D (ha!), demo, hacked up from something I did a long time
; ago with the very first version of Blitz 3D Beta when all we had
; were cubes and spheres ;))
;
; Might turn this into something better later, though, will probably just
; work on something a little more fun, and well, better :)
;
; More comments later.
;
; SPACE - Switch camera to different (random) box.
; ESC   - Exit.
;

; Ok here we have a lot of menu stuff.
Print "[Blox&Spheres V1.07]"
Print "Written by Rob Hutchinson 2000 - 2001"

; Non program Vars
Global Profiles$   = "Profiles\"
Global ScreenFile$ = "_ScreenMode.def"
Global DriverFile$ = "_Driver.def"
Global TexDir$     = "Textures\"

; Inital values
Global ZoomFactor# = 1      ;
Global ZoomPanning = 0      ;
Global ZoomPanSpd# = 0.5    ;
Global TopSpeed#   = 1.5    ;
Global Brightness  = 30     ;
Global Spheres     = 1      ;
Global Segments    = 4      ;
Global Shiny#      = 0
Global EntityTex$  = "SDS.bmp"
Global OuterTex$   = "Stone1.bmp"
Global FogMode     = 1
Global FogColourR  = 0
Global FogColourG  = 0
Global FogColourB  = 0
Global FogRangeA#  = 1
Global FogRangeB#  = 2000
Global TextureFlags= 65
Global BAntiAlias  = True
Global BDithering  = True
Global bloxMin     = 5
Global bloxMax     = 5000
Global maxBlox     = 40
Global BackSeg     = 15
;Global BackTex$    = ""

Dim Files$(1000)

SaveProfile(Profiles$ + "_Default.pro")


; Ask the user some questions
If IAskQuestion("Profile","Do you want to use a preset profile?",1)
	UserDefined = 0
	; we are using a profile now
	; get the user to pick one...
	Print ""
	useprofile$ = ListDir$(Profiles$,"")
	If useprofile$ <> ""
		; Load in the profile and lets rock....
		LoadProfile Profiles$ + useprofile$
	Else
		; Terminate just in case
		RuntimeError "Couldn't read profile, terminating."
		End
	EndIf
Else
	UserDefined = 1
	Print ""
	PrintHeading "Attributes"
	BAntialias = IAskQuestion("","Use AntiAlias?",1,0,0,0,0)
	BDithering = IAskQuestion("","Use Dithering?",1,0,0,0,0)
	maxBlox    = IAskQuestion("","No. of objects (0=Single)?",0,500,0,20,0)
	TopSpeed   = FAskQuestion("","Object top speed?",10.0,0.001,1.5,0)
	TextureFlags= IAskQuestion("","Texture Flags (65=EnvMap)?",0,1000,0,0,0)

	UseSph     = IAskQuestion("","Use spheres instead of cubes?",1,0,0,0,0)
	If UseSph
		Spheres   = 1
		Segments  = IAskQuestion("","How many segments per sphere?",0,30,4,6,0)
	Else
		Spheres   = 0
		Segments  = 0
	EndIf
	ZoomFactor = FAskQuestion("","Zoom Factor?",10,-10,1,0)
	UsePan     = IAskQuestion("","Do you want to use 'Zoom Panning'?",1,0,0,0,0)
	If UsePan
		ZoomPanning = 1
		ZoomPanSpd  = FAskQuestion("","Zoom panning speed?",50,-50,0.5,0)
	Else
		ZoomPanning = 0
		ZoomPanSpd  = 0
	EndIf

	UseFog          = IAskQuestion("","Do you want to use fog?",1,0,0,0,0)
	If UseFog
		FogMode = 1
		FogColourR = IAskQuestion("","Fog red value?",0,255,0,0,0)
		FogColourG = IAskQuestion("","Fog green value?",0,255,0,0,0)
		FogColourB = IAskQuestion("","Fog blue value?",0,255,0,0,0)
		FogRangeA  = FAskQuestion("","Fog near?",3000,-3000,2000,0)
		FogRangeB  = FAskQuestion("","Fog far?",3000,-3000,1,0)
	Else
		FogMode = -1
		ZoomPanSpd  = 0
	EndIf
	
	Brightness = IAskQuestion("","What brightness to use?",0,255,0,30,0)
	Shiny#     = FAskQuestion("","How much shine?",1.0,0,0,0)
	BackSeg    = IAskQuestion("","Big sphere segments?",0,30,6,25,0)

	Dump$ = Input("Now pick a texture for the objects [ENTER]")
	Print ""
	EntityTex$ = ListDir$(TexDir$,"")
	
	Dump$ = Input("Pick a texture for the outer sphere [ENTER]")
	Print ""
	OuterTex$ = ListDir$(TexDir$,"")
	
EndIf

;----------------------------------------------------------------------------------------------

If FileType(ScreenFile$) = 1
	; Use default screen mode.
	ndf = ReadFile(ScreenFile$)
	If ndf
		sm = ReadLine(ndf)
		CloseFile ndf
	Else
		RuntimeError "Unable To Read ScreenFile, Please Delete it! (" + ScreenFile$ + ")"
		End
	EndIf
Else	
	Print ""
	PrintHeading "ScreenMode"
	For a=1 To CountGfxModes()
	    If GfxMode3D(a)
			Print Str(a) + " " + Str(GfxModeWidth(a)) + "x" + Str(GfxModeHeight(a)) + "x" + Str(GfxModeDepth(a))
		EndIf
	Next
	
	; Ok here we get some input from the user screenmode.
	sm = 0
	Repeat
		sm=Input("Enter a screen mode from the list above: ")
	Until sm > 0 And sm <= CountGfxModes()
	
	If IAskQuestion("Default Screenmode","Do you want to always use this mode?",1)
		ndf = WriteFile(ScreenFile$)
		If ndf
			WriteLine ndf,sm
			CloseFile ndf
		Else
			RuntimeError "Couldn't write preferences, terminating."
		EndIf
	EndIf
EndIf

Print ""
Print ""
PrintHeading("Controls")
Print "SPACE - Switch to another (random) box."
Print "W     - Toggle wireframe on/off."
Print "ESC   - Quit the program."
Print ""
Dump$ = Input("[ENTER]")
;----------------------------------------------------------------------------------------------

; Setup the graphics..
Graphics3D GfxModeWidth(sm),GfxModeHeight(sm),GfxModeDepth(sm)
SetBuffer BackBuffer()
SeedRnd MilliSecs()

; Setup some stuff
AmbientLight Brightness,Brightness,Brightness
AntiAlias BAntiAlias
Dither BDithering

; Create the lights....
lightgreen=CreateLight()
LightColor lightgreen,10,255,10
TurnEntity lightgreen,90,0,0
lightblue=CreateLight()
LightColor lightblue,10,10,255
TurnEntity lightblue,180,0,0
lightlello=CreateLight()
LightColor lightlello,255,255,10
TurnEntity lightlello,0,0,90
lightred=CreateLight()
LightColor lightred,255,10,10
TurnEntity lightred,90,0,0

; number wrapping function
Function QWrap(Value#,Low#,High#)			; Remember this? where is the Blitz
	Return Low#+(Value# Mod High#)			; PC version when you need it? :))
End Function 

Type blok									; A block type for each block.
	Field obj
	Field posx#,posy#,posz#
	Field rotx#,roty#,rotz#
	Field radx#,rady#,radz#
	Field spdx#,spdy#,spdz#
	Field spnx#,spny#,spnz#
End Type

Dim blox.blok(maxBlox)					; Dimension an array of blocks.

; Create a pile of boxes/spheres.
Global         bsh=CreateBrush()
BrushColor     bsh,255,255,255
BrushTexture   bsh,LoadTexture(TexDir$ + EntityTex$,TextureFlags)
BrushShininess bsh,Shiny

cratesize = 20

If Spheres = 0
	crate = CreateCube()
Else
	crate = CreateSphere(Segments)
EndIf
PaintEntity crate,bsh
HideEntity crate

FitMesh   crate,-cratesize,-cratesize,-cratesize,cratesize*2,cratesize*2,cratesize*2
LightMesh crate,0,0,0,2,255,255,255

For a=0 To maxBlox							; Create a pile of boxes.
	blox(a)      = New blok
	blox(a)\obj  = CopyEntity(crate)
	EntityAlpha blox(a)\obj,Rnd(0.2,1.0)
	ShowEntity  blox(a)\obj

	blox(a)\rotx = Rnd(360)
	blox(a)\roty = Rnd(360)
	blox(a)\rotz = Rnd(360)
	blox(a)\radx = Rnd(-500,500)
	blox(a)\rady = Rnd(-500,500)
	blox(a)\radz = Rnd(-500,500)
	blox(a)\spdx = Rnd(-TopSpeed#,TopSpeed#)	
	blox(a)\spdy = Rnd(-TopSpeed#,TopSpeed#)
	blox(a)\spdz = Rnd(-TopSpeed#,TopSpeed#)
	blox(a)\spnx = Rnd(-TopSpeed#,TopSpeed#)
	blox(a)\spny = Rnd(-TopSpeed#,TopSpeed#)
	blox(a)\spnz = Rnd(-TopSpeed#,TopSpeed#)
Next

; Create a material for the brick wall
Global         bsh2=CreateBrush()
tx = LoadTexture(TexDir$ + OuterTex$)
ScaleTexture tx,.1,.1
BrushTexture   bsh2,tx
BrushShininess bsh2,1
BrushColor     bsh2,100,100,100

backbox = CreateSphere(BackSeg)
PaintEntity backbox,bsh2

FitMesh    backbox,0,0,0,1500,1500,1500
FlipMesh   backbox
MoveEntity backbox,-750,-750,-750			;move it into the screen a bit


camera=CreateCamera()						; A camera for the scene.
CameraViewport camera,0,0,GfxModeWidth(sm),GfxModeHeight(sm)
CameraRange camera,1,2000
CameraZoom camera,ZoomFactor#
If FogMode <> -1 Then CameraFogMode camera,FogMode
If FogColourR <> -1 CameraFogColor camera,FogColourR,FogColourG,FogColourB
If FogMode <> -1 Then CameraFogRange camera,FogRangeA,FogRangeB

deg# = 1

While Not KeyDown(1)
	If KeyHit(17)
		wire=Not wire
		WireFrame wire
	EndIf

	Gosub UpdateAll
	RenderWorld
	Flip

Wend

EndGraphics
If UserDefined = 1
	; Ask them if they want to save the profile......
	UsePro     = IAskQuestion("","Do you want to save your profile?",1)
	If UsePro
		Print "Please enter a descriptive name:"
		savename$ = Input("> ")
		SaveProfile(Profiles$ + savename$ + ".pro")
	EndIf
	
EndIf
End

.UpdateAll
	If ZoomPanning <> 0
		deg# = deg# + ZoomPanSpd#
	 	CameraZoom camera,Cos(deg#)
	EndIf

	If KeyHit(17)
		wire=Not wire
		WireFrame wire
	EndIf
	
	TurnEntity lightgreen,1,0,1
	TurnEntity lightred,-1,0,-1
	TurnEntity lightblue,1,0,0
	TurnEntity lightlello,0,0,1

	; Spin the objects inside the box.
	For a=0 To maxBlox
		TurnEntity blox(a)\obj,blox(a)\spnx,blox(a)\spny,blox(a)\spnz
		blox(a)\posx=Sin(blox(a)\rotx)*blox(a)\radx
		blox(a)\posy=Cos(blox(a)\roty)*blox(a)\rady
		blox(a)\posz=Sin(blox(a)\rotz)*blox(a)\radz
		blox(a)\rotx=blox(a)\rotx+blox(a)\spdx
		blox(a)\roty=blox(a)\roty+blox(a)\spdy
		blox(a)\rotz=blox(a)\rotz+blox(a)\spdz
		blox(a)\rotx=QWrap(blox(a)\rotx,0,360)
		blox(a)\roty=QWrap(blox(a)\roty,0,360)
		blox(a)\rotz=QWrap(blox(a)\rotz,0,360)
		PositionEntity blox(a)\obj,blox(a)\posx,blox(a)\posy,blox(a)\posz
	Next

	; Put the camera inside the selected box, so it sees everything the box would.	
	PositionEntity camera,blox(camerafrom)\posx,blox(camerafrom)\posy,blox(camerafrom)\posz
	; Turn it the same way as the box.
	TurnEntity     camera,blox(camerafrom)\spnx,blox(camerafrom)\spny,blox(camerafrom)\spnz
	
	; Switch camera to a different box when spacebar is hit.
	If KeyHit(57)
		camerafrom = Rnd(0,maxBlox)
	EndIf
Return

Function FAskQuestion#(name$,question$,high#,low#,def#,prtblk=1)
	If prtblk = 1 Then Print ""
	PrintHeading name$
	outi# = 0
	Repeat
		outi# = Input(question$ + " " + TrimFloat$(low#) + "-" + TrimFloat$(high#) + " [Def=" + TrimFloat$(Def#) + "]: ")
	Until outi# >= low# And outi# <= high#
	Return outi#
End Function

Function IAskQuestion(name$,question$,mode,high=0,low=0,def=0,prtblk=1)
	If prtblk = 1 Then Print ""
	PrintHeading name$
	If mode = 1 ; yes/no
		out$ = ""
		Repeat
			out$ = Input(question$ + " (Y/N): ")
		Until Upper(out$) = "Y" Or Upper(out$) = "N"
		If Upper(out$) = "Y"
			Return True
		Else
			Return False
		EndIf
	Else ; other type
		outi = 0
		Repeat
			outi = Input(question$ + " (" + low + "-" + high + ") [Def=" + def + "]: ")
		Until outi >= low And outi <= high
		Return outi
	EndIf
End Function

Function PrintHeading(name$)
	If name$ <> "" Then Print "-[" + name$ + "]" + String("-",50-(Len(name$)+3))
End Function

Function ListDir$(dir$,only$)
	; Clear the file list.
	For a = 0 To 999
		Files$(a) = ""
	Next

;	Stop
	
	; Populate it..
	countfiles = -1
	dirhandle = ReadDir(dir$)
	outfile$ = ""
	countpage = 0
	If dirhandle
		nxfile$ = NextFile$(dirhandle)
		While nxfile$ <> ""
			If nxfile$ <> ".." And nxfile$ <> "."
				If only$ = ""
					countfiles = countfiles + 1
					Files$(countfiles) = nxfile$
				Else
		 			If Upper(Right$(nxfile$,Len(only$))) = Upper(only$)
						countfiles = countfiles + 1
						Files$(countfiles) = nxfile$
					EndIf
				EndIf
			EndIf
			nxfile$ = NextFile$(dirhandle)
		Wend
		
		For nxf = 0 To countfiles
			; output the files.
			
			countpage = countpage + 1
			Print nxf + ". " + Files$(nxf)
			If countpage = 22
				dump$ = Input("[ENTER]")
				countpage = 0
			EndIf
		Next
		outfile$ = Files(IAskQuestion("","Please select a file",0,countfiles,0,0))
		CloseDir dirhandle
	EndIf
	Return outfile$
End Function

Function SaveProfile(file$)
	flh=WriteFile(file$)
	If flh
;		WriteLine flh,
		WriteLine flh,ZoomFactor#
		WriteLine flh,ZoomPanning
		WriteLine flh,ZoomPanSpd#
		WriteLine flh,TopSpeed#
		WriteLine flh,Brightness
		WriteLine flh,Spheres
		WriteLine flh,Segments
		WriteLine flh,Shiny
		WriteLine flh,EntityTex$
		WriteLine flh,OuterTex$
		WriteLine flh,FogMode
		WriteLine flh,FogColourR
		WriteLine flh,FogColourG
		WriteLine flh,FogColourB
		WriteLine flh,FogRangeA
		WriteLine flh,TextureFlags
		WriteLine flh,BAntiAlias
		WriteLine flh,BDithering
        WriteLine flh,maxBlox
		WriteLine flh,FogRangeB
        WriteLine flh,BackSeg
		CloseFile flh
	EndIf
End Function

Function LoadProfile(file$)
	flh=ReadFile(file$)
	If flh
		ZoomFactor# = ReadLine$(flh)
		ZoomPanning = ReadLine$(flh)
		ZoomPanSpd# = ReadLine$(flh)
		TopSpeed#   = ReadLine$(flh)
		Brightness  = ReadLine$(flh)
		Spheres     = ReadLine$(flh)
		Segments    = ReadLine$(flh)
		Shiny       = ReadLine$(flh)
		EntityTex$  = ReadLine$(flh)
		OuterTex$   = ReadLine$(flh)
		FogMode     = ReadLine$(flh)
		FogColourR  = ReadLine$(flh)
		FogColourG  = ReadLine$(flh)
		FogColourB  = ReadLine$(flh)
		FogRangeA   = ReadLine$(flh)
		TextureFlags= ReadLine$(flh)
		BAntiAlias  = ReadLine$(flh)
		BDithering  = ReadLine$(flh)
        maxBlox     = ReadLine$(flh)
        FogRangeB   = ReadLine$(flh)
		BackSeg     = ReadLine$(flh)
		CloseFile flh
	EndIf
End Function

Function TrimFloat$(flt#,trimto = 3)
	fltstr$ = flt#
	decpnt = Instr(fltstr$,".")
	dec$ = Mid$(fltstr$,decpnt+1)
	start$ = Mid$(fltstr$,1,decpnt)
	Return start$ + Left$(dec$,trimto)
End Function








