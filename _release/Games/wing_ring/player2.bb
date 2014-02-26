; Exact replicas of player 1 functions but for player 2

; -----------
; Game Input2
; -----------

Function GameInput2()

	; Move up/down
	If JoyYDir()=-1 Then plane2_dy=plane2_dy+2 : cam2_dy=cam2_dy+2
	If JoyYDir()=1 Then plane2_dy=plane2_dy-2 : cam2_dy=cam2_dy-2
	
	; Turn left/right
	If JoyXDir()=-1 Then plane2_day=plane2_day+2 : cam2_day=cam2_day+2
	If JoyXDir()=1 Then plane2_day=plane2_day-2 : cam2_day=cam2_day-2
	
	; Move forward - normal
	If JoyDown(3)=True And blue_bonus2_status=False
		plane2_dx=plane2_dx-Sin(plane2_ay)*8
		plane2_dz=plane2_dz+Cos(plane2_ay)*8
		cam2_dx=cam2_dx-Sin(cam2_ay)*8
		cam2_dz=cam2_dz+Cos(cam2_ay)*8
	EndIf
	
	; Move forward - blue bonus - extra speed
	If JoyDown(3)=True And blue_bonus2_status=True
		plane2_dx=plane2_dx-Sin(plane2_ay)*16
		plane2_dz=plane2_dz+Cos(plane2_ay)*16
		cam2_dx=cam2_dx-Sin(cam2_ay)*16
		cam2_dz=cam2_dz+Cos(cam2_ay)*16
	EndIf

	; Fire bullet - normal
	If JoyHit(1)=True And rapid_fire_status2=False
		p2.plane2_bullet=New plane2_bullet
		p2\entity=CopyEntity(bullet,plane2)
		EntityType p2\entity,type_plane2_bullet
		EntityParent p2\entity,0
		TurnEntity p2\entity,0,270,0
		MoveEntity p2\entity,0,0,10
		p2\alpha=1
	EndIf
	
	; Fire bullet - yellow bonus - rapid fire
	yellow_bonus2_pause=yellow_bonus2_pause+1
	If JoyDown(1)=True And yellow_bonus2_status=True And yellow_bonus2_pause>=3
		yellow_bonus2_pause=0
		p2.plane2_bullet=New plane2_bullet
		p2\entity=CopyEntity(bullet,plane2)
		EntityType p2\entity,type_plane2_bullet
		EntityParent p2\entity,0
		TurnEntity p2\entity,0,270,0
		MoveEntity p2\entity,0,0,10
		p2\alpha=1
	EndIf

End Function


; ----------------------
; Update plane2 function
; ----------------------

Function UpdatePlane2()

	; Prevent plane2 and main cam from going too low/high
	If plane2_dy<2 Then plane2_dy=2
	If plane2_dy>1000 Then plane2_dy=1000
	If cam2_dy<2 Then cam2_dy=2
	If cam2_dy>1000 Then cam2_dy=1000

	; Update plane2 position/angle values
	plane2_x=plane2_x+((plane2_dx-plane2_x)/curve_plane)
	plane2_y=plane2_y+((plane2_dy-plane2_y)/curve_plane)
	plane2_z=plane2_z+((plane2_dz-plane2_z)/curve_plane)
	plane2_ay=plane2_ay+((plane2_day-plane2_ay)/curve_plane)
	
	PositionEntity plane2,plane2_x,plane2_y,plane2_z
	RotateEntity plane2,0,plane2_ay+90,0

	; Update camera2 position values
	cam2_x=cam2_x+((cam2_dx-cam2_x)/curve_cam)
	cam2_y=cam2_y+((cam2_dy-cam2_y)/curve_cam)
	cam2_z=cam2_z+((cam2_dz-cam2_z)/curve_cam)
	cam2_ay=cam2_ay+((cam2_day-cam2_ay)/curve_cam)
	
	PositionEntity main_cam2,plane2_x+Sin(cam2_ay)*50,cam2_y,plane2_z-Cos(cam2_ay)*50
	RotateEntity main_cam2,0,cam2_ay,0
	
	PositionEntity top_cam2,plane2_x,plane2_y#+75,plane2_z
	RotateEntity top_cam2,90,plane2_ay,0
			
	; Update plane2 bullets
	For p2.plane2_bullet=Each plane2_bullet
		MoveEntity p2\entity,0,0,100
		If EntityDistance(p2\entity,plane2)>5000 Then FreeEntity p2\entity : Delete p2
	Next

	; Check to see if plane2 is inside bank zone and if so bank pot
	temp_pivot=CreatePivot()
	PositionEntity temp_pivot,red_ring_x,plane2_y,red_ring_z
	If EntityDistance(temp_pivot,plane2)<=625 And pot2>0 Then bank2=bank2+pot2 : pot2=0
	FreeEntity temp_pivot

End Function


; -----------------------
; Update Camels2 function
; -----------------------

Function UpdateCamels2()

	For c.camel=Each camel

		; Turn camels, fade them
		TurnEntity c\entity,0,2,0
		EntityAlpha c\entity,c\alpha : c\alpha=c\alpha-0.0005

		; Use EntityDistance command to check for collision between camel and plane
		If EntityDistance(c\entity,plane2)<=40
			plane2_bonus=c\colour
			c\alpha=0
		EndIf

		; Delete camel if invisible
		If c\alpha<=0 Then FreeEntity c\entity : Delete c
			
	Next

	; Orange bonus - shield

	; Time
	If orange_bonus2_time>300 Then EntityAlpha shield2,0.5 Else EntityAlpha shield2,orange_bonus2_time/600.0
	If orange_bonus2_time>0 Then orange_bonus2_time=orange_bonus2_time-1

	; Activate
	If plane2_bonus=1
		orange_bonus2_status=True
		orange_bonus2_time=orange_bonus2_time+600
		EntityType plane2,type_none
		plane2_bonus=0
	EndIf
	
	; Deactivate
	If orange_bonus2_time=0 And orange_bonus2_status=True
		orange_bonus2_status=False
		EntityType plane2,type_plane2
	EndIf
	
	; Yellow bonus - rapid fire
	If yellow_bonus2_time>0 Then yellow_bonus2_time=yellow_bonus2_time-1 ; time
	If plane2_bonus=2 Then yellow_bonus2_status=True : yellow_bonus2_time=yellow_bonus2_time+600 : plane2_bonus=0 ; activate
	If yellow_bonus2_time=0 And yellow_bonus2_status=True Then yellow_bonus2_status=False ; deactivate
	
	; Green bonus - homing missiles
	If green_bonus2_time>0 Then green_bonus2_time=green_bonus2_time-1 ; time
	If plane2_bonus=3 Then green_bonus2_status=True : green_bonus2_time=green_bonus2_time+600 : plane2_bonus=0 ; activate
	If green_bonus2_time=0 And green_bonus2_status=True Then green_bonus2_status=False ; deactivate
	
	; Turquoise bonus - extra speed
	If blue_bonus2_time>0 Then blue_bonus2_time=blue_bonus2_time-1 ; time
	If plane2_bonus=4 Then blue_bonus2_status=True : blue_bonus2_time=blue_bonus2_time+600 : plane2_bonus=0 ; activate
	If blue_bonus2_time=0 And blue_bonus2_status=True Then blue_bonus2_status=False ; deactivate
	
	; Pink bonus - UFO to rings
	If pink_bonus2_time>0 Then pink_bonus2_time=pink_bonus2_time-1 ; time
	If plane2_bonus=5 Then pink_bonus2_status=True : pink_bonus2_time=pink_bonus2_time+600 : plane2_bonus=0 ; activate
	If pink_bonus2_time=0 And pink_bonus2_status=True Then pink_bonus2_status=False ; deactivate

End Function


; --------------------------
; Check Collisions2 function
; --------------------------

Function CheckCollisions2()

	; Ufo and plane2 bullet
	For u.ufo=Each ufo
		entity_col=EntityCollided(u\entity,type_plane2_bullet)
		If entity_col<>0
			DeleteBullet2(entity_col)
			pot2=pot2+Points(u)
			CreateShadow(u) ; Create a new shadow that will reflect explosion sparks after old ufo + shadow have been nuked
			ExplodeUfo(u,2)
		EndIf
	Next
	
	; Ufo bullet and plane2
	If orange_bonus2_status=False
		For ub.ufo_bullet=Each ufo_bullet
			If EntityCollided(ub\entity,type_plane2)<>0
				If pot2>0
					pot2=0
					FreeEntity ub\entity
					Delete ub
					po.points=New points
					po\entity=CopyEntity(points_bust,plane2)
					ShowEntity po\entity
					EntityParent po\entity,0
					po\alpha=1
				Else
					game_over2=True
					ExplodePlane(plane2)
				EndIf
			EndIf
		Next
	EndIf
	
	; Plane2 and statue
	If EntityCollided(plane2,type_statue)<>0 Then game_over2=True : ExplodePlane(plane2)
	
End Function


; -----------------------
; Delete Bullet2 function
; -----------------------

Function DeleteBullet2(entity_col)

	; Delete bullet that hits ufo
	For p2.plane2_bullet=Each plane2_bullet
		If p2\entity=entity_col Then FreeEntity p2\entity : Delete p2 : Exit
	Next

End Function