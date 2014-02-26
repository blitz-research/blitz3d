
Global use_fog=False
If Lower$( Left$( Input$( "Use fog? :" ),1 ) )="y" use_fog=True

;game FPS
Const FPS=30

Global info1$="Terrain demo"
Global info2$="Arrows/A/Z move, F1-F4 change camera"
Global info3$="W toggle wireframe, M toggle morphing"
Global info4$="[ and ] alter detail level"

Include "../start.bb"

;*******************
;grrr..ATI - FIX IT!
; HWMultiTex False ;
;*******************
	
Type Player
	Field entity,camera
	Field ctrl_mode,cam_mode,ignition
	Field pitch#,yaw#,pitch_speed#,yaw_speed#,roll#,thrust#
End Type
 
Collisions 1,10,2,2	;sphere-to-polygon, colliding collisions

Global terr

CreateScene()

plane=LoadAirPlane( "biplane.x" )

player1.Player=CreatePlayer( plane,0,0,GraphicsWidth(),GraphicsHeight(),1 )
;player1.Player=CreatePlayer( plane,0,0,640,239,1 )
;player2.Player=CreatePlayer( plane,0,240,640,239,2 )

period=1000/FPS
time=MilliSecs()-period

detail=2000:morph=True
TerrainDetail terr,detail,morph

While Not KeyHit(1)
	Repeat
		elapsed=MilliSecs()-time
	Until elapsed

	;how many 'frames' have elapsed	
	ticks=elapsed/period
	
	;fractional remainder
	tween#=Float(elapsed Mod period)/Float(period)
	
	For k=1 To ticks
		If k=ticks Then CaptureWorld
		time=time+period
		UpdateGame()
		UpdateWorld
	Next
	
	If KeyHit(17)
		wire=Not wire
		WireFrame wire
	EndIf
	If KeyHit(26)
		detail=detail-100
		If detail<100 Then detail=100
		TerrainDetail terr,detail,morph
	Else If KeyHit(27)
		detail=detail+100
		If detail>10000 Then detail=10000
		TerrainDetail terr,detail,morph
	EndIf
	If KeyHit(50)
		morph=Not morph
		TerrainDetail terr,detail,morph
	EndIf
			
	RenderWorld tween
	
	Color 255,0,0
	If morph t$="Y" Else t$="N"
	Text 0,0,"Detail:"+detail+" Morph:"+t$
	Flip
Wend

End

Function UpdateGame()
	For p.Player=Each Player
		UpdatePlayer(p)
	Next
End Function

Function UpdatePlayer( p.Player )

	Local x_dir,y_dir,z_dir

	Select p\ctrl_mode
	Case 1:
		If KeyDown(203) x_dir=-1
		If KeyDown(205) x_dir=1
		
		If KeyDown(200) y_dir=-1
		If KeyDown(208) y_dir=1
		
		If KeyDown(30) z_dir=1
		If KeyDown(44) z_dir=-1
		
		If KeyHit(59) p\cam_mode=1
		If KeyHit(60) p\cam_mode=2
		If KeyHit(61) p\cam_mode=3
		If KeyHit(62) p\cam_mode=4
		
	Case 2:
		x_dir=JoyXDir()
		y_dir=JoyYDir()
		If JoyDown(1) z_dir=1
		If JoyDown(2) z_dir=-1
		
		If KeyHit(63) p\cam_mode=1
		If KeyHit(64) p\cam_mode=2
		If KeyHit(65) p\cam_mode=3
		If KeyHit(66) p\cam_mode=4
		
	End Select
	
	If x_dir<0
		p\yaw_speed=p\yaw_speed + (4-p\yaw_speed)*.04
	Else If x_dir>0
		p\yaw_speed=p\yaw_speed + (-4-p\yaw_speed)*.04
	Else
		p\yaw_speed=p\yaw_speed + (-p\yaw_speed)*.02
	EndIf
		
	If y_dir<0
		p\pitch_speed=p\pitch_speed + (2-p\pitch_speed)*.2
	Else If y_dir>0
		p\pitch_speed=p\pitch_speed + (-2-p\pitch_speed)*.2
	Else
		p\pitch_speed=p\pitch_speed + (-p\pitch_speed)*.1
	EndIf
		
	p\yaw=p\yaw+p\yaw_speed
	If p\yaw<-180 Then p\yaw=p\yaw+360
	If p\yaw>=180 Then p\yaw=p\yaw-360
	
	p\pitch=p\pitch+p\pitch_speed
	If p\pitch<-180 Then p\pitch=p\pitch+360
	If p\pitch>=180 Then p\pitch=p\pitch-360
		
	p\roll=p\yaw_speed*30
	RotateEntity p\entity,p\pitch,p\yaw,p\roll
	
	;see if y/p/r funcs are working...
	t_p#=EntityPitch( p\entity )
	t_y#=EntityYaw( p\entity )
	t_r#=EntityRoll( p\entity )
	RotateEntity p\entity,t_p,t_y,t_r
	
	If p\ignition
		If z_dir>0			;faster?
			p\thrust=p\thrust + (1.5-p\thrust)*.04	;1.5
		Else If z_dir<0		;slower?
			p\thrust=p\thrust + (-p\thrust)*.04
		EndIf
		MoveEntity p\entity,0,0,p\thrust
	Else If z_dir>0
		p\ignition=True
	EndIf
	
	If p\camera
		Select p\cam_mode
		Case 1:
			EntityParent p\camera,p\entity
			RotateEntity p\camera,0,p\yaw,0,True
			PositionEntity p\camera,EntityX(p\entity),EntityY(p\entity),EntityZ(p\entity),True
			MoveEntity p\camera,0,1,-5
			PointEntity p\camera,p\entity,p\roll/2
		Case 2:
			EntityParent p\camera,0
			PositionEntity p\camera,EntityX(p\entity),EntityY(p\entity),EntityZ(p\entity)
			TranslateEntity p\camera,0,1,-5
			PointEntity p\camera,p\entity,0
		Case 3:
			EntityParent p\camera,p\entity
			PositionEntity p\camera,0,.25,0
			RotateEntity p\camera,0,0,0
		Case 4:
			EntityParent p\camera,0
			PointEntity p\camera,p\entity,0
		End Select
	EndIf
	
End Function

Function LoadAirPlane( file$ )
	pivot=CreatePivot()
	plane=LoadMesh( file$,pivot )
	ScaleMesh plane,.125,.25,.125	;make it more spherical!
	RotateEntity plane,0,180,0	;and align to z axis
	EntityRadius pivot,1
	EntityType pivot,1
	HideEntity pivot
	Return pivot
End Function

Function CreatePlayer.Player( plane,vp_x,vp_y,vp_w,vp_h,ctrl_mode )
	p.Player=New Player
	p\ctrl_mode=ctrl_mode
	p\cam_mode=1
	x#=ctrl_mode*10
	z#=ctrl_mode*10-2500
	p\entity=CopyEntity( plane )
	PositionEntity p\entity,x,TerrainY( terr,x,0,z )+50,z
	RotateEntity p\entity,0,180,0
	ResetEntity p\entity
	p\camera=CreateCamera( p\entity )
	PositionEntity p\camera,0,3,-10
	CameraViewport p\camera,vp_x,vp_y,vp_w,vp_h
	CameraClsColor p\camera,0,192,255
	CameraFogColor p\camera,0,192,255
	CameraFogRange p\camera,1000,3000
	CameraRange p\camera,1,3000
;	CameraZoom p\camera,1.5
	If use_fog Then CameraFogMode p\camera,1
	Return p
End Function

Function CreateScene()

	;setup lighting
	l=CreateLight()
	RotateEntity l,45,45,0
	AmbientLight 32,32,32
	
	;Load terrain
	terr=LoadTerrain( "hmap_1024.bmp" )
	ScaleEntity terr,20,800,20
	PositionEntity terr,-20*512,0,-20*512
	EntityFX terr,1
	EntityType terr,10

	;apply textures to terrain	
	tex1=LoadTexture( "coolgrass2.bmp",1 )
	ScaleTexture tex1,10,10
	tex2=LoadTexture( "lmap_256.bmp" )
	ScaleTexture tex2,TerrainSize(terr),TerrainSize(terr)
	EntityTexture terr,tex1,0,0
	EntityTexture terr,tex2,0,1
	
	;and ground plane
	plane=CreatePlane()
	ScaleEntity plane,20,1,20
	PositionEntity plane,-20*512,0,-20*512
	EntityTexture plane,tex1,0,0
	EntityOrder plane,3
	EntityFX plane,1
	EntityType plane,10
	
	;create cloud planes
	tex=LoadTexture( "cloud_2.bmp",3 )
	ScaleTexture tex,1000,1000
	p=CreatePlane()
	EntityTexture p,tex
	EntityFX p,1
	PositionEntity p,0,450,0
	p=CopyEntity( p )
	RotateEntity p,0,0,180
	
	;create water plane
	tex=LoadTexture( "water-2_mip.bmp",3 )
	ScaleTexture tex,10,10
	p=CreatePlane()
	EntityTexture p,tex
	EntityBlend p,1
	EntityAlpha p,.75
	PositionEntity p,0,10,0
	EntityFX p,1
	
End Function