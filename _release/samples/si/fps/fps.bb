; * FPS sample.
; * Program code by Simon Harrison (si@si-design.co.uk).
; * Gargoyle model by Adam Gore.

; Graphics values
Const width=640,height=480,depth=16								

; Collision type values
Const type_camera=1,type_blood=2	; Source 
Const type_scenery=3,type_ladders=4	; Destination

; Camera position, angle values
Global cam_x#,cam_z#,cam_pitch#,cam_yaw#						; Current
Global dest_cam_x#,dest_cam_z#,dest_cam_pitch#,dest_cam_yaw#	; Destination

; CameraPick value
Global picked

; Blood intensity, mouse shake values
Global blood_intensity=10,mouse_shake#							

; Gargoyle speed, yaw values
Global garg_speed#=0.2,garg_yaw#

Global node=2	; Node gargoyle aims for												

Dim node_x(10)
Dim node_y(10)
Dim node_z(10)
Dim node_n(10,3)
Dim node_v(10)

node_x(1)=390	; x position of first node
node_y(1)=160	; y position of first node
node_z(1)=50	; z position of first node
node_n(1,0)=1	; no of connected nodes
node_n(1,1)=2	; no of the first connected node
node_v(1)=1		; visits to first node by gargoyle

node_x(2)=390	; x position of second node
node_y(2)=160	; y position of second node
node_z(2)=190	; z position of second node
node_n(2,0)=2	; no of connected nodes
node_n(2,1)=3	; no of the first connected node
node_n(2,2)=1	; no of the second connected node

node_x(3)=210	; etc
node_y(3)=120	; etc
node_z(3)=190	; etc
node_n(3,0)=2	; etc
node_n(3,1)=4	; etc
node_n(3,2)=2	; etc

node_x(4)=210
node_y(4)=120
node_z(4)=10
node_n(4,0)=2
node_n(4,1)=5
node_n(4,2)=3

node_x(5)=390
node_y(5)=80
node_z(5)=10
node_n(5,0)=2
node_n(5,1)=6
node_n(5,2)=4

node_x(6)=390
node_y(6)=80
node_z(6)=190
node_n(6,0)=2
node_n(6,1)=7
node_n(6,2)=5

node_x(7)=210
node_y(7)=40
node_z(7)=190
node_n(7,0)=3
node_n(7,1)=8
node_n(7,2)=10
node_n(7,3)=6

node_x(8)=50
node_y(8)=40
node_z(8)=190
node_n(8,0)=2
node_n(8,1)=9
node_n(8,2)=7

node_x(9)=50
node_y(9)=40
node_z(9)=110
node_n(9,0)=1
node_n(9,1)=8

node_x(10)=210
node_y(10)=40
node_z(10)=10
node_n(10,0)=1
node_n(10,1)=7

; Bullet spark type
Type spark
	Field entity,alpha#
End Type

; Blood type (hehe)
Type blood
	Field entity,alpha#,y_speed#,stuck
End Type

; Set display mode
Graphics3D width,height,depth
SetBuffer BackBuffer()

; Set up camera
Global camera=CreateCamera()					
CameraRange camera,1,600
EntityRadius camera,7.5
EntityType camera,type_camera
PositionEntity camera,10,10,10

; Set up lights
AmbientLight 191,191,191						
Global light=CreateLight()
LightColor light,31,31,31
RotateEntity light,45,0,0

; Load level
Global level=LoadMesh("interior.x")	
ScaleEntity level,100,100,100
EntityType level,type_scenery
EntityPickMode level,2

; Load ladders collision mesh
Global ladders=LoadMesh("ladders.x")
ScaleEntity ladders,100,100,100
EntityType ladders,type_ladders
EntityAlpha ladders,0

; Load gun target
Global target=LoadSprite("target.bmp",1,camera)
MoveEntity target,0,0,75
EntityOrder target,-2

; Load bullet spark
Global spark=LoadSprite("bigspark.bmp")	
ScaleSprite spark,2,2
EntityOrder spark,-1
HideEntity spark

; Load gargoyle
Global gargoyle=LoadMD2("gargoyle/gargoyle.md2")
garg_tex=LoadTexture("gargoyle/gargoyle.bmp")
EntityTexture gargoyle,garg_tex
AnimateMD2 gargoyle,1,0.1,32,46
ScaleEntity gargoyle,0.2,0.2,0.2
TurnEntity gargoyle,0,180,0
PositionEntity gargoyle,390,160,50
EntityBox gargoyle,-5,0,-5,10,20,10
EntityPickMode gargoyle,3

; Create gargoyle-walk pivot
Global target_piv=CreatePivot()
PositionEntity target_piv,node_x(2),node_y(2),node_z(2)

; Load blood sprite
Global blood=LoadSprite("blood.bmp",3)
EntityRadius blood,1
EntityType blood,type_blood
HideEntity blood

; Load sounds
Global gunshot=LoadSound("gunshot.wav")
Global squish=LoadSound("squish.wav")

; Initialise collisions
Collisions type_camera,type_scenery,2,3
Collisions type_blood,type_scenery,2,1
Collisions type_blood,type_camera,1,1
Collisions type_camera,type_ladders,2,2


; Main loop
; ---------

Global old_time=MilliSecs()
While Not KeyHit(1)

	UpdateGame()
	UpdateWorld
	RenderWorld
	
	; FPS
	frames=frames+1
	If MilliSecs()-render_time=>1000 Then fps=frames : frames=0 : render_time=MilliSecs()	
	Text 0,0,fps
	
	Flip
	
Wend
End


; --------------------
; Update game function
; --------------------

Function UpdateGame()

	GameInput()
	UpdateGun()
	GargPath()
	SpurtBlood()
	
End Function


; -------------------
; Game input function
; -------------------

Function GameInput()


	; Shoot
	; -----
	
	If MouseHit(1)=True
		PlaySound gunshot
		picked=CameraPick(camera,width/2,height/2)
		s.spark=New spark
		s\entity=CopyEntity(spark)
		s\alpha=1
		PositionEntity s\entity,PickedX(),PickedY(),PickedZ()		
	EndIf


	; Mouse look
	; ----------

	; Mouse x and y speed
	mxs=MouseXSpeed()
	mys=MouseYSpeed()
	
	; Mouse shake (total mouse movement)
	mouse_shake=Abs(((mxs+mys)/2)/1000.0)

	; Destination camera angle x and y values
	dest_cam_yaw#=dest_cam_yaw#-mxs
	dest_cam_pitch#=dest_cam_pitch#+mys

	; Current camera angle x and y values
	cam_yaw=cam_yaw+((dest_cam_yaw-cam_yaw)/5)
	cam_pitch=cam_pitch+((dest_cam_pitch-cam_pitch)/5)
	
	RotateEntity camera,cam_pitch#,cam_yaw#,0
	
	; Rest mouse position to centre of screen
	MoveMouse width/2,height/2
	

	; Camera move
	; -----------
	
	; Forward/backwards - destination camera move z values
	If KeyDown(200)=True Or MouseDown(2)=True Then dest_cam_z=1
	If KeyDown(208)=True Then dest_cam_z#=-1

	; Strafe - destination camera move x values
	If KeyDown(205)=True Then dest_cam_x=1
	If KeyDown(203)=True Then dest_cam_x=-1
	
	; Current camera move x and z values
	cam_z=cam_z+((dest_cam_z-cam_z)/5)
	cam_x=cam_x+((dest_cam_x-cam_x)/5)

	; Move camera
	MoveEntity camera,cam_x,0,cam_z
	dest_cam_x=0 : dest_cam_z=0
	
	; Climb ladders
	If EntityCollided(camera,type_ladders)<>0 Then TranslateEntity camera,0,1,0
	
	; Gravity
	TranslateEntity camera,0,-1,0


End Function


; ----------
; Update gun
; ----------

Function UpdateGun()

	; Update each bullet spark
	For s.spark=Each spark
		EntityAlpha s\entity,s\alpha
		s\alpha=s\alpha-0.02
		If s\alpha<=0 Then FreeEntity s\entity : Delete s
	Next
	
	; If gargoyle is picked (shot) then play squish sound and call FreshBlood function
	If picked=gargoyle
		PlaySound squish
		FreshBlood()
		picked=0
	EndIf

End Function


; --------------------
; Fresh blood function
; --------------------

Function FreshBlood() 

	; New blood drops
	For i=1 To blood_intensity
		b.blood=New blood
		b\y_speed=0
		b\alpha=2
		b\entity=CopyEntity(blood)
		PositionEntity  b\entity,PickedX(),PickedY(),PickedZ()
		ResetEntity b\entity
		scale_randx#=Rnd(0.2,2.9)
		scale_randy#=scale_randx#+Rnd(-0.1,0.1)
		ScaleSprite b\entity,scale_randx#,scale_randy#
		RotateEntity b\entity,Rnd(360),Rnd(360),Rnd(360)
	Next
	
End Function


; --------------------
; Spurt blood function
; --------------------

Function SpurtBlood()

	; For each drop of blood...
	For b.blood=Each blood
		
	; If blood drop collides with camera then stick blood drop on camera
	If EntityCollided(b\entity,type_camera)<>0 Then EntityParent b\entity,camera : EntityType b\entity,0 : b\alpha=1 : b\stuck=True
	
		; Update blood drops
		If b\alpha>0
			EntityAlpha b\entity,b\alpha
			If b\stuck<>True Then MoveEntity b\entity,0,0,1
			If b\stuck<>True Then TranslateEntity b\entity,0,b\y_speed,0
			If b\stuck<>True Then b\alpha=b\alpha-0.01
			If b\stuck=True Then b\alpha=b\alpha-mouse_shake
			b\y_speed=b\y_speed-0.025
		Else
			FreeEntity b\entity
			Delete b
		EndIf
		
	Next

End Function


; ------------------
; Garg path function
; ------------------

Function GargPath()

	PointEntity gargoyle,target_piv

	dest_garg_yaw=EntityYaw(gargoyle)

	If dest_garg_yaw-garg_yaw>180 Then garg_yaw=garg_yaw+360
	If dest_garg_yaw-garg_yaw<-180 Then garg_yaw=garg_yaw-360

	garg_yaw=garg_yaw+((dest_garg_yaw-garg_yaw)/(5/garg_speed))

	MoveEntity gargoyle,0,0,garg_speed
	RotateEntity gargoyle,0,garg_yaw,0

	garg_x=EntityX(gargoyle)
	garg_y=EntityY(gargoyle)
	garg_z=EntityZ(gargoyle)

	If garg_x=node_x(node)
		If garg_y=node_y(node)
			If garg_z=node_z(node)
	
				least=1000000
				node_v(node)=(MilliSecs()-old_time)/1000
				For i=1 To node_n(node,0)
					nv=node_v(node_n(node,i))
					If nv<least Then least=nv : lnode=i
				Next
				node=node_n(node,lnode)

				PositionEntity target_piv,node_x(node),node_y(node),node_z(node)
				
			EndIf
		EndIf
	EndIf

End Function