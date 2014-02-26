;===================================================================
;					Orbit The Nebula Demo 1.0
;					Written by Richard Betson
;							07/16/01
;					www.redeyeware.50megs.com
;		Copyright 2001, Richard R Betson (if possible under NDA)
;===================================================================

AppTitle "Orbit The Nebula Demo 1.0"
Include "../start.bb"

;Graphics3D 640,480,16
;---------------------------------
;Variables
;---------------------------------
Dim rock_light(115)
Dim rock(115)
Dim spark(40)

;make_sky
Global sky1
Global sky2

;Make_sun
Global spark_pivot1
Global spark_pivot2
Global spark_pivot3
Global spark_pivot4

Global sun_light1
Global sun_light2
Global sun_light3

Global sun

Global spark_orbit_sprite1
Global spark_orbit_sprite2

;make_planet
Global ring
Global ring2
Global pv
Global planet_pivot
Global planet
Global planet_light

;make_moon
Global moon
Global moon_pivot

;make_rock
Global l1
Global rock_pivot

;lens_flare
Global flare1
Global flare2
Global flare3
Global flare4


sp1x_rate#=2.8
sp1y_rate#=-2.8
sp2x_rate#=-2.6
sp2y_rate#=2.6
sp3x_rate#=2.6
sp3y_rate#=2.6
sp4x_rate#=-.6
sp4y_rate#=-2.6


Const speed=4	;Speed of Mouse Movement
Global yaw1#=.1	;Speed of Sphere Rotation
Global yaw2#=.15
Const spitch#=.3
Const syaw#=.45

sw=GraphicsWidth()/2	;Mouse Center Point
sh=GraphicsHeight()/2

time=MilliSecs()+8000


orbit_t#=.1
Global oldpx1#
time1=MilliSecs()




signal=Load3DSound("signal.wav")
If signal=0 Then error()
LoopSound signal


;---------------------------------
;Set Lighting
;---------------------------------
AmbientLight 70,70,70


;---------------------------------
;Create Piviot and Camera Entities
;_________________________________
pivot=CreatePivot()
pv1=CreatePivot()
camera=CreateCamera(pivot)


;listen=CreateListener(sun,.1,.2,.2)
;EmitSound signal,sun


TurnEntity camera,80,0,0



make_sky()
make_sun()
make_planet()
make_moon()
make_rocks()
lens_flare()


;listen=CreateListener(camera,.1,.5,.5)
;EmitSound signal,sun

;--------------------------------
;Main Loop
;--------------------------------
While Not KeyHit(1)
;If time1<MilliSecs()

		
	fps=fps+1
	If fps_t<MilliSecs()
	;fps2=fps
	fp$=" "+Str$(fps)
	fps_t=1000+MilliSecs()
	fps=0
	EndIf
	
		
	fps2=fps2+1
	If fps_t2<MilliSecs()
	fps3#=fps2
	;fp$=" "+Str$(fps)
	fps_t2=30+MilliSecs()
	fps2=0
	EndIf


;If fps3>2
	
	;sp1x_rate#=2.8-(fps3-4.50)
	;sp1y_rate#=-2.8-(fps3-4.50)
	;sp2x_rate#=-2.6-(fps3-4.50)
	;sp2y_rate#=2.6-(fps3-4.50)
	;sp3x_rate#=0;(fps3)
	;sp3y_rate#=0;(fps3)
	;sp4x_rate#=0;-(diff/100)
	;sp4y_rate#=0;-(diff/8)
	;orbit_t=.075-(fps3/80)
	;yaw1=.1-(fps3-1.5)
	;yaw2=.2-(fps3-1)
;EndIf



	

TurnEntity sky1,0,yaw1,0,0		;Turn (Yaw) Sphere 1
TurnEntity sky2,0,yaw2,0,0		;Turn (Yaw) Sphere 2
	

TurnEntity planet_pivot,0,.9,0
TurnEntity pv,0,.9,0
TurnEntity moon_pivot,.8,.8,0



TurnEntity spark_pivot1,sp1x_rate,sp1y_rate,0
TurnEntity spark_pivot2,sp2x_rate,sp2y_rate,0
TurnEntity spark_pivot3,sp3x_rate,sp3y_rate,0
TurnEntity spark_pivot4,sp4x_rate,sp4y_rate,0



	si#=si#+.4+orbit_t
	If si>=114.25 Then si=0
	sx#=(5+(Sin(si*Pi)*20))
	sy#=(190+(Cos(si*Pi)*70))

	PositionEntity spark_orbit_sprite1,sx-(sx*2),sy,sx
	PositionEntity spark_orbit_sprite2,sx,sy,sx


	fi#=fi+orbit_t
		If fi>=114.50 Then fi=0

	px#=(2+(Sin(fi*Pi)*40))
	py#=(30 + (Cos(fi*Pi)*265))
	
	PositionEntity camera,px,py,0
	
	PositionEntity sky1,px,py,0
	PositionEntity sky2,px,py,0

	If (fi>100 And fi<110) Or (fi>8 And fi<14)
		flare_counter#=flare_counter#+.8
			If fi>100 And fi<110 Then f_offset=15
		ShowEntity flare1
		ShowEntity flare2
		ShowEntity flare3
		ShowEntity flare4
		PositionEntity flare1,EntityX#(sun)+10-f_offset,EntityY#(sun)+10-(flare_counter)-f_offset,EntityZ#(sun)+5+flare_counter
		PositionEntity flare2,EntityX#(sun)+10-f_offset,EntityY#(sun)+20-(flare_counter)-f_offset,EntityZ#(sun)+10+flare_counter
		PositionEntity flare3,EntityX#(sun)+10-f_offset,EntityY#(sun)+30-(flare_counter)-f_offset,EntityZ#(sun)+15+flare_counter
		PositionEntity flare4,EntityX#(sun)+10-f_offset,EntityY#(sun)+40-(flare_counter)-f_offset,EntityZ#(sun)+20+flare_counter

		Else
			f_offset=0
			flare_counter=0
			HideEntity flare1
			HideEntity flare2
			HideEntity flare3
			HideEntity flare4
	EndIf
	
		
	PointEntity camera,planet,90
	 

	time1=MilliSecs()+delay_time
	odlpy#=py

;EndIf


If KeyHit(2)
orbit_t=orbit_t+.025
EndIf

If KeyHit(3)
orbit_t=orbit_t-.025
If orbit_t<.025 Then orbit_t=.025
EndIf


UpdateNormals planet

UpdateWorld	;Update's
RenderWorld ;.90



If time>MilliSecs()
	Color 255,255,255
	Text 10,10,"Orbit The Nebula"
	Text 10,25,"Orbit The Nebula Demo 1.0 by Richard Betson"
	Color 255,0,0
	Text 10,40,"www.redeyeware.50megs.com"
	Color 128,255,0
	Text 10,70,"Hit ESCAPE KEY to EXIT."
	Text 10,80,"Hit 1 to speed up, 2 to slow down demo."
Else
Text 10,10,"FPS: "+fp$
EndIf	
;Text 10,10,px
;Text 10,25,py
;Text 10,40,fi

;Text 10,60,oldpx1-px1
;Text 10,80,sp1x_rate
;Text 10,95,orbit_t

Flip 		;Flip to FrontBuffer()



If KeyHit(68)=1
		kh=1
		xxp=CreateImage(sw*2,sh*2)
		GrabImage xxp,0,0
		SaveImage(xxp,"screen.bmp")
		FreeImage xxp
	EndIf
If GetKey()<>0 And kh=0
	exit_tdp=1
EndIf


oldpx1=px1
Wend





;-------------------------------------------------------------------------
Function make_sky()
;---------------------------------
;Create Texture for Sky (STARS)
;---------------------------------
heaven=CreateTexture(128,128,1)
SetBuffer TextureBuffer(heaven)
	For i=0 To 150
		Color 155+Rnd(100),255,255
		Plot Rnd(256),Rnd(256)
	Next
	
heaven2=CreateTexture(128,128,1)
SetBuffer TextureBuffer(heaven2)
	For i=0 To 80
		Color 155+Rnd(100),255,255
		Plot Rnd(256),Rnd(256)
	Next
	

;---------------------------------
;Load Nebula Textures
;---------------------------------
nebula=LoadTexture("nebula1.bmp")
belt=LoadTexture("belt.bmp")
If nebula=0 Then error()
If belt=0 Then error()
SetBuffer BackBuffer()

;---------------------------------
;Create Spheres for Sky
;Set Entity and Texture Flags
;---------------------------------

sky1=CreateSphere()
ScaleTexture heaven,.16,.16
EntityTexture sky1,heaven,0,0	;Apply Multiple Textures to Sphere
EntityTexture sky1,nebula,0,1
EntityTexture sky1,belt,0,2

TextureBlend heaven,1			;Set Texture Flags
TextureBlend nebula,3
TextureBlend belt,3

FlipMesh sky1					;Flip Mesh (Texture) to face the inside of Sphere
ScaleEntity sky1,1000,1000,1000	;Scale the Sphere
EntityFX sky1,1					;Set to FullBright


sky2=CreateSphere()
ScaleTexture heaven2,.16,.20
EntityTexture sky2,heaven2,0,0
TextureBlend heaven2,1
FlipMesh sky2
ScaleEntity sky2,900,900,900
EntityAlpha sky2,.4
EntityFX sky2,1

End Function


;-------------------------------------------------------------------------
Function make_sun()

spark_pivot1=CreatePivot()
spark_pivot2=CreatePivot()
spark_pivot3=CreatePivot()
spark_pivot4=CreatePivot()

PositionEntity spark_pivot1,0,190,0
PositionEntity spark_pivot2,0,190,0
PositionEntity spark_pivot3,0,190,0
PositionEntity spark_pivot4,0,190,0



sun=CreateSphere()
sun_brush=CreateBrush(255,32,0)
PaintEntity sun,sun_brush
PositionEntity sun,0,190,5
ScaleEntity sun,4,4,4


sun_light1=CreateLight(2)
sun_light2=CreateLight(2)
sun_light3=CreateLight(2)

LightColor sun_light1,255,255,255
LightColor sun_light2,255,255,255
LightColor sun_light3,255,255,255

LightRange sun_light1,150
LightRange sun_light2,150
LightRange sun_light3,150


PositionEntity sun_light1,120,120,-10
PositionEntity sun_light2,0,220,0
PositionEntity sun_light3,-60,120,10

PointEntity sun_light1,sun
PointEntity sun_light2,sun
PointEntity sun_light3,sun



spark_sprite=LoadSprite("spark.bmp",1)
sun_sprite=LoadSprite("spark.bmp",1)
spark_orbit_sprite1=CopyEntity(spark_sprite)
spark_orbit_sprite2=CopyEntity(spark_sprite)


ScaleSprite spark_orbit_sprite1,4,4
ScaleSprite spark_orbit_sprite2,4,4
ScaleSprite spark_sprite,2,2

EntityColor spark_orbit_sprite1,255,0,0
EntityColor spark_orbit_sprite2,255,0,0

EntityColor spark_sprite,220,255,64
ScaleSprite spark_sprite,3,3
SpriteViewMode spark_sprite,3



For i=1 To 40
	spark(i)=CopyEntity(spark_sprite)
	red=Rnd(255);+100
	green=Rnd(255);+100
	blue=Rnd(255);+100
	EntityColor spark(i),128,255,128;,red,green,blue
	
Next



For i=1 To 10
	EntityParent spark(i),spark_pivot1
	
	sx#=(Rnd(30)-15)
	sy#=(Rnd(30)-15)
	sz#=(Rnd(30)-15)
	
	PositionEntity spark(i),sx,sy,sz
 
Next


For i=11 To 20
	EntityParent spark(i),spark_pivot2
	PositionEntity spark(i),Rnd#(30)-15,Rnd#(30)-15,Rnd#(30)-15
Next


For i=21 To 30
	EntityParent spark(i),spark_pivot3
	PositionEntity spark(i),Rnd#(30)-15,Rnd#(30)-15,5+Rnd#(30)-15
Next

For i=31 To 40
	EntityParent spark(i),spark_pivot4
	PositionEntity spark(i),Rnd#(24)-12,Rnd#(24)-12,5+Rnd#(24)-12
Next

EntityColor sun_sprite,220,255,64
ScaleSprite sun_sprite,45,45
PositionEntity sun_sprite,0,188,5
SpriteViewMode sun_sprite,3
EntityAlpha sun_sprite,.5

FreeEntity spark_sprite

End Function




;-------------------------------------------------------------------------
Function make_planet()
;---------------------------------
;Planet
;---------------------------------
ring=LoadSprite("ring2.bmp",1)
PositionEntity ring,0,-180,0
ScaleSprite ring,15,15
TurnEntity ring,110,0,0
SpriteViewMode ring,2
EntityAlpha ring,.4
ring2=CopyEntity(ring)
TurnEntity ring2,-180,0,0
EntityAlpha ring2,.2


planet_pivot=CreatePivot()
EntityParent ring,planet_pivot
EntityParent ring2,planet_pivot


planet=CreateSphere()
planet_tex=LoadTexture("io.bmp",9)
EntityTexture planet,planet_tex
EntityColor planet,222,100,100
PositionEntity planet,0,-180,0
ScaleEntity planet,4,4,4

pv=CreatePivot()
EntityParent planet,pv
PositionEntity planet_pivot,10,0,-10
PositionEntity pv,10,0,-10

planet_light=CreateLight(3)
PointEntity planet_light,planet
;LightRange planet_light,2
PositionEntity planet_light,0,-160,0

End Function


;-------------------------------------------------------------------------
Function make_moon()

moon_texture=LoadTexture("moon.bmp")
moon=CreateSphere()
EntityTexture moon,moon_texture
PositionEntity moon,-10,-195,0
moon_pivot=CreatePivot()
PositionEntity moon_pivot,10,-180,-10
EntityParent moon,moon_pivot

End Function


;-------------------------------------------------------------------------
Function make_rocks()
;---------------------------------
;Rocks
;---------------------------------
rt=LoadTexture("rock.bmp",8)
rock1=LoadMesh("rock.3ds")
FitMesh rock1,-1,-1,-1,2,2,2,True
EntityTexture rock1,rt
ScaleEntity rock1,1,1,1
rock_pivot=CreatePivot()
PositionEntity rock_pivot,0,190,5


For i=1 To 114 Step 2
rock(i)=CopyEntity(rock1)

Next

For i=1 To 114 Step 2
ry#=(185+Sin(i*Pi)*100+Rnd(25))
rx#=(5+Cos(i*Pi)*100+Rnd(20))

PositionEntity rock(i),rx,ry,Rnd(20)
EntityParent rock(i),rock_pivot
Next
l1=CreateLight()
PositionEntity l1,-90,0,10



FreeEntity rock1

;rl=CreateLight()
;PositionEntity rl,0,55,0
;PositionEntity rock1,0,65,0
End Function


;-------------------------------------------------------------------------
Function lens_flare()

flare1=CreateSphere()
flare2=CreateSphere()
flare3=CreateSphere()
flare4=CreateSphere()

EntityColor flare1,255,0,0
EntityColor flare2,64,255,32
EntityColor flare3,32,255,64
EntityColor flare4,0,0,255

ScaleEntity flare1,25,25,25
ScaleEntity flare2,6,6,6
ScaleEntity flare3,10,10,10
ScaleEntity flare4,30,30,30

EntityAlpha flare1,.1
EntityAlpha flare2,.1
EntityAlpha flare3,.1
EntityAlpha flare4,.09

HideEntity flare1
HideEntity flare2
HideEntity flare3
HideEntity flare4

End Function



;-------------------------------------------------------------------------
Function error()
SetBuffer FrontBuffer()
Color 255,255,255
Text 20,20,"ERROR LOADING MEDIA FILES!!! PLEASE INSTALL AGAIN!!"
Delay 4000
End
End Function