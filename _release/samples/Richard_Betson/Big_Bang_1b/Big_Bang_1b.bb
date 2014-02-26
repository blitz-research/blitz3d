;-----------------------------------------------------------------------
; Big Bang 1.b
; 07/31/01
;
; Written By Richard R Betson
; www.redeyeware.50megs.com
; vidiot@getgoin.net
; Copyright 2001, Richard Betson
;-----------------------------------------------------------------------
AppTitle "Big Bang 1.b"
Include "../start.bb"

AmbientLight 150,150,150

Dim sphere(2)


alpha#=1
inc#=0
os#=1
sx#=2
sy#=2


Global sky1
Global sky2
time=MilliSecs()+6000

;----------------------------------
; Create Camera
;----------------------------------
c_pivot=CreatePivot()			;Camera Pivot
camera=CreateCamera(c_pivot)	;Create Camera
PositionEntity camera,0,-8,13.5	;Position camera
CameraZoom camera,1.4			;Zoom in jus a bit
CameraRange camera,.1,150		;Limit camera range


;----------------------------------
; Create Textures
;----------------------------------
texture1=CreateTexture(64,64)
SetBuffer TextureBuffer(texture1)
Color 128,255,32
For y=0 To 64 Step 8
	For x=0 To 64
	Rect x,y,1,1,1
	Rect y,x,1,1,1
	Rect y-x,x,1,1
	Rect (64-y)+x,x,1,1
	Next
Next

SetBuffer BackBuffer()


;----------------------------------
; Create Pressure Waves
;----------------------------------
texture1=LoadTexture("gate4.bmp",9)
s_pivot=CreatePivot()
sphere1=CreateSphere(10,s_pivot)
EntityTexture sphere1,texture1
;EntityFX sphere1,1
EntityBlend sphere1,3
ScaleEntity sphere1,.1,.1,.1
UpdateNormals sphere1
TurnEntity sphere1,58,0,0
PointEntity camera,sphere1
For i=0 To 2
	sphere(i)=CopyEntity(sphere1)
Next


;----------------------------------
; Create Sun
;----------------------------------
sun=LoadSprite("bigspark.bmp",8)
ScaleSprite sun,.2,.2


;----------------------------------
; Create Ring
;----------------------------------
ring=LoadSprite("ring2.bmp",8)
ScaleSprite ring,.2,.2
SpriteViewMode ring,2
PositionEntity ring,0,0,-1
TurnEntity ring,0,120,0
HideEntity ring


make_sky()

FreeEntity sphere1



i=0
SetBuffer BackBuffer()

;----------------------------------
; Main Loop
;----------------------------------
While Not KeyHit(1)
sx=sx-.005
sy=sy-.005
os=os-.0025

	If os>0 And go=0 And stm2<MilliSecs()
		ScaleSprite sun,sx+Rnd#(os),sy+Rnd#(os)
		stm2=MilliSecs+1
		stm=MilliSecs()+200
	Else

		If stm>MilliSecs()
			ShowEntity ring
			ScaleSprite sun,20,20
		Else
			go=1
		EndIf
	EndIf

	If sky_tm<MilliSecs()
		TurnEntity sky1,0,.2,0
		TurnEntity sky2,0,.3,0
		sky_tm=MilliSecs()+40
	EndIf

	If go=1
		If tm<MilliSecs()
			ScaleSprite sun,20-(inc*2),20-(inc*2)
			EntityAlpha sun,alpha
				If inc#>3 Then inc#=inc+.12 Else inc=inc+.14
					swap#=swap+.1
					alpha#=alpha-.011
					tm=MilliSecs()+18
				EndIf
				If inc<16
					i=0
					ScaleEntity sphere(i),inc+inc2#,inc+inc2#,inc+inc2#
					EntityAlpha sphere(i),alpha
					TurnEntity sphere(i),0,0,-.5
					UpdateNormals sphere(i)
				EndIf
		
	
		If inc>1.5 And inc<18
			i=1
			ScaleEntity sphere(i),inc-1.5+inc2#,inc-1.5+inc2#,inc-1.5+inc2#
			EntityAlpha sphere(i),alpha+.2
			TurnEntity sphere(i),0,0,.5
			UpdateNormals sphere(i)
		EndIf
	

		If inc>3
			i=2
			ScaleEntity sphere(i),inc-3+inc2#,inc-3+inc2#,inc-3+inc2#
			EntityAlpha sphere(i),alpha+.4
			TurnEntity sphere(i),0,0,-1
			UpdateNormals sphere(i)
			ScaleSprite ring,(inc*2)-3,(inc*2)-3
			EntityAlpha ring,alpha+.4
		EndIf
	
	
		If inc>26
			inc=0
			alpha=1
			ScaleSprite sun,3,3
			EntityAlpha sun,1
			HideEntity ring
			go=0
			os#=1
			sx#=2
			sy#=2
			stm=MilliSecs()+200
		EndIf
EndIf
	
	
UpdateWorld
RenderWorld

	If time>MilliSecs()
		Color 255,0,0
		Text 10,10,"Big Bang * Written By Richard Betson"
		Text 10,25,"www.redeyeware.50megs.com"
		Color 64,255,32
		Text 10,40,"Press Escape to Exit."
	EndIf

Flip
	
Wend


;-------------------------------------------------------------------------

Function make_sky()
;---------------------------------
;Create Texture for Sky (STARS)
;---------------------------------
heaven=CreateTexture(128,128,1)
SetBuffer TextureBuffer(heaven)
	For i=0 To 150
		If Rnd(100)>80
			Color 32,32,255
		Else
			Color 255,255,255
		EndIf
		Plot Rnd(256),Rnd(256)
	Next
	
heaven2=CreateTexture(128,128,1)
SetBuffer TextureBuffer(heaven2)
	For i=0 To 80
		If Rnd(100)>80
			Color 255,32,32
		Else
			Color 255,255,255
		EndIf
		Plot Rnd(256),Rnd(256)
	Next


;---------------------------------
;Create Spheres for Sky
;Set Entity and Texture Flags
;---------------------------------

sky1=CreateSphere()
ScaleTexture heaven,.16,.16
EntityTexture sky1,heaven,0,0;Apply Multiple Textures to Sphere
FlipMesh sky1				;Flip Mesh (Texture) to face the inside of Sphere
ScaleEntity sky1,90,90,90	;Scale the Sphere
EntityFX sky1,1				;Set to FullBright


sky2=CreateSphere()
ScaleTexture heaven2,.16,.20
EntityTexture sky2,heaven2,0,0
FlipMesh sky2
ScaleEntity sky2,80,80,80
EntityAlpha sky2,.4
EntityFX sky2,1

End Function