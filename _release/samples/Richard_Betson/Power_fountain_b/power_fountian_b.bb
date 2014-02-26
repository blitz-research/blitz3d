;-----------------------------------------------------------------------
; Power Fountain 1.b
; 07/28/01
;
; Written By Richard R Betson
; Copyright, 2001 Richard R Betson
; www.redeyeware.50megs.com
; vidiot@getgoin.net
; Spite movement based on code by Simon Harris (config2000@hotmail.com)
;-----------------------------------------------------------------------
AppTitle "Power Fountain"
Include "../start.bb"
;Graphics3D 640,480,16
SetBuffer BackBuffer()
AmbientLight 10,10,10


Dim sparks(80)
Global time=8000+MilliSecs()
Const total_sprites=80		;Total number of sprites
Global fi#=.2


Type sprite_type
	Field x#,y#		 ;Position
	Field vx#,vy#	 ; Velocity 
	Field delay_time ; Delay 
End Type

Global sprite.sprite_type


For d=1 To total_sprites
 	sprite.sprite_type=New sprite_type
	sprite_pos()
	sprite\delay_time=Rnd(50)
Next



;----------------------------------
; Create Camera
;----------------------------------
c_pivot=CreatePivot()			;Camera Pivot
camera=CreateCamera(c_pivot)	;Create Camera
PositionEntity camera,0,-10,7.5	;Position camera
;CameraZoom camera,1.1			;Zoom in jus a bit
CameraRange camera,.1,100		;Limit camera range
CameraFogMode camera,1 			;Fog on
CameraFogColor camera,16,0,128	;Color fog
CameraFogRange camera,60,90		;Visible fog range


;----------------------------------
; Create Sparks
;----------------------------------
;spark_pivot=CreatePivot()
spark=LoadSprite("spark.bmp",1)  ;Load bitmap for sprite "1" colored
ScaleSprite spark,.7,.7			 ;Scale the sprite a little smaller
If spark=0 Then End				 ;If the sprite did not load function error


For i=1 To 80
	sparks(i)=CopyEntity(spark)	 ;Make a copy of the sprite and color it
	EntityColor sparks(i),Rnd(255),Rnd(255),Rnd(255)
Next

FreeEntity spark



;----------------------------------
; Create Fountain
;----------------------------------
brick=LoadTexture("brick1.bmp",1) ;Load texture "1" colored
ScaleTexture brick,.5,.5		  ;Scale texture
pad1_pivot=CreatePivot()		  ;create a pivot for fountain (not really needed)
pad1=CreateCube(pad1_pivot)		  ;Create a cube with it's parent a pivot
EntityTexture pad1,brick		  ;Apply texture
ScaleEntity pad1,.55,1.5,.55      ;Scale the cube
PositionEntity pad1,0,0,1.55      ;Position the cube
UpdateNormals pad1				  ;Update texture
TurnEntity pad1,90,0,0            ;Turn the cube so it will match mirror plane

pad2=CreateCube(pad1_pivot)
EntityTexture pad2,brick
ScaleEntity pad2,1,.2,1
PositionEntity pad2,0,0,0;1.80
TurnEntity pad2,90,0,0


;----------------------------------
; Create Plane
;----------------------------------
stone=LoadTexture("stone.bmp",2) ;Load texture "2" Alpha
ScaleTexture stone,2,2			 ;Scale texture
plane=CreatePlane(10,pad1)		 ;Create plane with segments of 10
EntityTexture plane,stone	     ;Texture plane
PositionEntity plane,0,-1.1,0

;----------------------------------
; Create Mirror
;----------------------------------

mirror=CreateMirror(pad1)	;Create mirror
PositionEntity mirror,0,-1.2,0



;----------------------------------
; Create Light
;----------------------------------

light1=CreateLight(3)		   ;Create Light
LightConeAngles light1,1,90    ;Adjust Light Cone Angle
PositionEntity light1,0,-10,50 ;Position light
LightColor light1,255,255,255  ;Full brightt white light
LightRange light1,100	       ;Set light range
EntityParent light1,c_pivot	   ;Make the lights parent camera's pivot





PointEntity camera,pad1  ;Point to fountain
PointEntity light1,pad1


HideEntity plane	;Hide plane



;----------------------------------
; Main Loop
;----------------------------------
While Not KeyHit(1)

	For sprite.sprite_type = Each sprite_type   ;Loop through each sprite type
	n=n+1    ;counter for sprites
	If sprite\delay_time=0  ;Check condition of delay until 0
		sprite\x#=sprite\x#-sprite\vx#	;Subtract velocity from position
		sprite\vy#=sprite\vy#+.005      ;Velocity y +.005
		sprite\y#=sprite\y#-sprite\vy#  ;Subtract velocity from position
		If sprite\y<-.5 Then sprite_pos() ;Collision detection
			zz#=((Sin((n*2)*Pi)*sprite\x)) ;Add 3rd dimension to sparks based on x 
			PositionEntity sparks(n),sprite\x,zz,sprite\y ;Position sprite
		Else
			sprite\delay_time=sprite\delay_time-1 ;Subtract 1 from delay time
		EndIf
	Next
	zz=0 ;reset
	n=0
	

	TurnEntity c_pivot,0,0,.5 ;Orbit camera and light


	If KeyHit(2) ;key 1 for plane toggle
	If on_off=0
			ShowEntity plane ;show plane
			on_off=1
		Else
			HideEntity plane ;hide plane
			on_off=0
		EndIf
	EndIf

	If KeyHit(3) ;key 2 for spark color toggle
	If on_off1=0
			For i=1 To 80
			EntityColor sparks(i),255,128,32 ;Set sparks color
			Next
			on_off1=1
		Else
			For i=1 To 80
			EntityColor sparks(i),Rnd(255),Rnd(255),Rnd(255)
			Next
			on_off1=0
		EndIf
	EndIf



	UpdateWorld
	RenderWorld
		If time>MilliSecs()
		Color 255,0,0
		Text 10,10,"Power Fountain * Written By Richard Betson"
		Text 10,25,"www.redeyeware.50megs.com"
		Color 64,255,32
		Text 10,40,"Press 1 to toggle Plane reflection on/off."
		Text 10,55,"Press 2 to toggle Spark Color."
	EndIf
	Flip

Wend


Function sprite_pos()
	sprite\x# =0    ;Set x width
	sprite\y# =3.55 ;Ser y height
	sprite\vx#=Rnd(.09)-Rnd(.09) ;Set velocity amount
	sprite\vy#=-.2+Rnd(.15)      ;Set velocity amount
End Function