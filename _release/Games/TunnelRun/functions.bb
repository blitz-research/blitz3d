;
;
; Game Functions

;
;	EFX functions smoke first
;

Function Update_Smoke()
	For a.smoke=Each smoke
		TranslateEntity a\ent,0,0,-EFX_SPEED
		ScaleSprite a\ent,a\size,a\size
		EntityAlpha a\ent,a\alpha
		a\size=a\size+a\disp
		a\alpha=a\alpha-a\reduct
		a\rot=a\rot+1
		RotateSprite a\ent,a\rot
		If a\alpha<.1 Or EntityZ(a\ent)<CameraZpos Then
			FreeEntity a\ent
			Delete a
			smk=smk-1
		End If		
	Next
End Function

Function Add_Smoke_puff(size#,dis#,rduct#,x#,y#,z#,r,g,b)
	smk=smk+1
	a.smoke=New smoke
	a\size=size
	a\disp=dis
	a\alpha=.6
	a\reduct=rduct
	a\ent=CopyEntity(smoke_spr(Rand(1,4)))
	EntityColor a\ent,r,g,b
	ScaleSprite a\ent,size,size
	PositionEntity a\ent,x,y,z
	Return a\ent
End Function

Function Add_White_Smoke(size#,x#,y#,z#)
	ent=Add_Smoke_puff(size,.2,.04,x,y,z,255,255,255)
	Return ent
End Function

Function Add_Color_Smoke(size,x#,y#,z#,r,g,b)
	ent=Add_Smoke_puff(size,.2,.04,x,y,z,r,g,b)
	Return ent
End Function

;
;Delete all smoke
Function Free_Smoke()
	For a.smoke=Each smoke
		If a\ent FreeEntity a\ent
	Next
	Delete Each smoke
End Function

;
;Asteroid functions
Function Add_Object(x#,y#,z#,dir,speed#,size#,typ)
	a.Obj=New Obj
	a\speed=speed
	a\dir=dir
	a\typ=typ
	a\st=False
	a\piv=CreatePivot()

	Select a\typ
		Case 1		;Normal asteroid
			a\ent=CopyEntity(Aster_mesh,a\piv)
			ScaleEntity a\ent,size,size,size
			RotateEntity a\ent,Rnd(360),Rnd(360),Rnd(360)
			PositionEntity a\piv,x,y,z
			EntityType a\piv,TYPE_ASTER
			EntityPickMode a\ent,2
			EntityRadius a\piv,size
			a\val=100
		Case 2		;Double Points
			a\ent=CopyEntity(Aster_mesh,a\piv)
			ScaleEntity a\ent,size,size,size
			RotateEntity a\ent,Rnd(360),Rnd(360),Rnd(360)
			PositionEntity a\piv,x,y,z
			EntityType a\piv,TYPE_ASTER
			EntityPickMode a\ent,2
			EntityRadius a\piv,size
			a\val=200
			EntityColor a\ent,255,255,0
		Case 3
			a\ent=CopyEntity(Aster_mesh,a\piv)
			ScaleEntity a\ent,size,size,size
			RotateEntity a\ent,Rnd(360),Rnd(360),Rnd(360)
			PositionEntity a\piv,x,y,z
			EntityType a\piv,TYPE_ASTER
			EntityPickMode a\ent,2
			EntityRadius a\piv,size
			a\val=500
			EntityColor a\ent,255,0,0
			a\col=240
			a\cd=10
		Case 10
			a\ent=CopyEntity(Bad01_mesh,a\piv)
			ScaleEntity a\ent,size,size,size
			PositionEntity a\piv,x,y,z
			a\dx=0
			a\dy=0
			EntityType a\piv,TYPE_ASTER
			EntityPickMode a\ent,2
			EntityRadius a\piv,size
			a\val=1500
			EntityColor a\ent,255,0,0
			a\ds=100
			a\fx=x
			a\fy=y
		Case 1000	;Claw
			a\ent=CopyEntity(Claw_Anim_Mesh,a\piv)
			ScaleEntity a\ent,size,size,size
			PositionEntity a\piv,800*Cos(x),800*Sin(x),z
			RotateEntity a\piv,0,0,x-180
			a\dx=0
			a\dy=0
			EntityType GetChild(a\ent,1),TYPE_CLAW
			a\ds=100
			a\fx=x
			a\fy=y
			Animate a\ent,3,Rnd(.3,3)
	End Select
End Function

;Update Asteroids
Function Update_Objects()
	For a.Obj=Each Obj
		Select a\typ
			Case 1
				Control_Asteroid(a,1)
			Case 2
				Control_Asteroid(a,2)
			Case 3
				Control_Asteroid(a,3)
			Case 1000
				If EntityZ(a\piv)>CameraZpos Then
					TranslateEntity a\piv,0,0,a\speed*a\dir
				Else
					FreeEntity a\ent
					FreeEntity a\piv
					Delete a
				End If
		End Select

	Next
End Function

Function Control_Asteroid(a.Obj,typ)
	Select a\typ
		Case 3
			EntityColor a\ent,a\col,255-a\col,0
			a\col=a\col-a\cd
			If a\col<140 Or a\col>250 Then a\cd=-a\cd
	End Select
	If EntityZ(a\piv)>CameraZpos Then
		TurnEntity a\piv,2,0,0.5
		TranslateEntity a\piv,-a\dx,-a\dy,a\speed*a\dir
	Else
		FreeEntity a\ent
		FreeEntity a\piv
		Delete a
	End If
End Function

;Delete all objects
Function Delete_Objects()
	For a.Obj=Each Obj
		If a\ent FreeEntity a\ent
		If a\piv FreeEntity a\piv
		Delete a
	Next
End Function

;
;	Shooting funcitons
;
Function Add_Shot.shot(ent,typ,dir,firer.play)
	If firer<>Null Then
		If firer\rate>0 Then Return
	End If
	a.shot=New shot
	a\typ=typ
	a\firer=firer
	Select typ
		Case 1
			If firer<>Null Then
				a\firer\rate=6
			End If
			a\speed=8
			a\dx=0
			a\dy=0
			a\dz=dir
			a\life=125
			a\ent=CopyEntity(shot1_spr,ent)
			If firer<>Null Then
				PositionEntity a\ent,a\firer\ox,0,10
				a\firer\ox=-a\firer\ox
			End If
			EntityParent a\ent,0
			EntityRadius a\ent,8
			EntityType a\ent,TYPE_SHOT1
			EmitSound Shoot1_wave,a\ent
	End Select
	Return a
End Function

;
;Update all players shots
Function Update_Shots()
	For a.shot=Each shot
		a\life=a\life-1
		Select a\typ	
			Case 1
				TranslateEntity a\ent,a\dx,a\dy,a\dz*a\speed
				a\ang=a\ang+5
				RotateSprite a\ent,a\ang
		End Select
		If a\life<100 Then
			EntityAlpha a\ent,a\life/10
		End If
		If a\life<1 Then 
			FreeEntity a\ent
			Delete a
		Else
			If CountCollisions(a\ent)>0
				For b=1 To CountCollisions(a\ent)
					ent=CollisionEntity(a\ent,b)
					x#=EntityX(ent)
					y#=EntityY(ent)
					z#=EntityZ(ent)
					Set_Flash()
					For c=1 To 2
						add_color_smoke(275,x+Rnd(-2,2),y+Rnd(-2,2),z,255,10,0)
						add_color_smoke(170,x+Rnd(-2,2),y+Rnd(-2,2),z,255,200,0)
						ent2=add_white_smoke(140,x+Rnd(-2,2),y+Rnd(-2,2),z)
					Next
					PositionEntity ent,EntityX(ent),EntityY(ent),-1000
					EmitSound Explode_wave,ent2
				Next
				FreeEntity a\ent
				If a\firer<>Null Then
					a\firer\score=a\firer\score+150
				End If
				Delete a
			End If
		End If
	Next
End Function

;Delete all shots
Function Free_Shots()
	For a.shot=Each shot
		If a\ent FreeEntity a\ent
		Delete a
	Next
End Function
;
;	Player functions
;	typ = 	1 Mouse controled player
;			2 ana Joy Controlled player
;			3 dig Joy controlled player
;			4 keyboard controlled player
;			5 CPU Controlled player
;			6 internet player   ** TO DO **
;
Function Add_Player.play(typ,r,g,b,split,id,n$)
	a.play=New play
	a\dead=False
	a\con=typ
	a\id=id
	a\name=n
	a\lives=G_StartLives
	a\tlx=0
	a\tly=0
	a\score=0
	a\ox=15
	a\brx=GraphicsWidth()
	a\bry=GraphicsHeight()
	a\piv=CreatePivot()
	EntityRadius a\piv,40
	EntityType a\piv,TYPE_PLAYER
	a\ent=CopyEntity(player_mesh,a\piv)
	EntityColor a\ent,r,g,b
	a\shield=100
	a\shield_ent=CopyEntity(shield_mesh,a\piv)
	;
	a\cam=CreateCamera()
	CameraRange a\cam,1,24000
	CameraFogRange a\cam,12000,24000
	CameraFogMode a\cam,1
	PositionEntity a\cam,0,0,CameraZpos
	EntityType a\cam,TYPE_CAM
	EntityRadius a\cam,40
	If split=True Then
		Select id
			Case 0
				CameraViewport a\cam,0,0,GraphicsWidth(),GraphicsHeight()/2
				a\tlx=0
				a\tly=0
				a\brx=GraphicsWidth()
				a\bry=GraphicsHeight()/2
			Case 1
				CameraViewport a\cam,0,GraphicsHeight()/2,GraphicsWidth(),GraphicsHeight()/2
				a\tlx=0
				a\tly=GraphicsHeight()/2
				a\brx=GraphicsWidth()
				a\bry=GraphicsHeight()
		End Select
	End If
	;
	Return a
End Function

;
;Main players update
Function Update_Players()
	For a.play=Each play

		;
		;
		If a\rate>0 Then a\rate=a\rate-1
		score(a\id)=a\score
		EntityAlpha a\shield_ent,a\glow
		If a\dead=False
			If a\glow>0 Then a\glow=a\glow-.05
			PositionEntity a\piv,EntityX(a\piv),EntityY(a\piv),0
			PositionEntity a\cam,EntityX(a\cam),EntityY(a\cam),CameraZpos
			If Cheater=False Then				;Cheat Mode
				cnt=CountCollisions(a\piv)
				If cnt>0 Then
					For b=1 To cnt
						te=CollisionEntity(a\piv,cnt)
						add_color_Smoke(25,CollisionX(a\piv,b),CollisionY(a\piv,b),CollisionZ(a\piv,b),255,Rnd(100,200),Rnd(100,200))
						If te<>Tunnel_Mesh Then
							a\shield=a\shield-10
						Else
							a\shield=a\shield-.5
						End If
						a\glow=1
					Next
				End If
			End If
			;
			; Do Camera Code
			;
			dx#=EntityX(a\piv,True)-EntityX(a\cam,True)
			dy#=EntityY(a\piv,True)-(EntityY(a\cam,True)-30)
			TranslateEntity a\cam,dx*.05,dy*.05,0
			;
			If a\shield<20 And a\shield>0 Then
				add_color_Smoke(15,EntityX(a\piv)+Rnd(-1,1),EntityY(a\piv),EntityZ(a\piv),255,Rnd(50,140),Rnd(50,140))
			End If
			If a\shield<100 And a\shield>0 Then
				a\shield=a\shield+.01
			End If
			If a\shield<0 Then
				a\dead=True
				a\lives=a\lives-1
			End If
		
			If a\dead=True Then
				PositionEntity a\piv,0,0,CameraZpos-100
				a\waiting=(CameraZpos-100)
				a\shield=100
			End If
		Else	;If dead the do dead routene
			If a\waiting<0 Then
				ResetEntity a\piv
				PositionEntity a\piv,EntityX(a\piv),EntityY(a\piv),a\waiting
				a\waiting=a\waiting+1
				ResetEntity a\piv
				Update_PLCam(a)
				RotateEntity a\piv,0,0,0
			Else
				a\dead=False
			End If
		End If
		If a\lives=0 Then 
			score(a\id)=a\score
						Cls
			Num_Players=Num_Players-1
			Select a\id
				Case 0
					Go1_timer=300
				Case 1
					Go2_timer=300	
			End Select
			Free_Player(a)
		End If
	Next
End Function

Function Resize_Last_Player()
	For a.play=Each play
		CameraViewport a\cam,0,0,GraphicsWidth(),GraphicsHeight()
		a\tlx=0
		a\tly=0
		a\brx=GraphicsWidth()
		a\bry=GraphicsHeight()
		cont=a\con
	Next
	Return cont
End Function

Function Free_Player(a.play)
		If a\shield_ent FreeEntity a\shield_ent
		If a\ent FreeEntity a\ent
		If a\piv FreeEntity a\piv
		If a\cam FreeEntity a\cam
		Delete a
End Function

;
;Manual update of players camera
Function Update_PLCam(a.play)
			;
			; Do Camera Code
			;
			dx#=EntityX(a\piv,True)-EntityX(a\cam,True)
			dy#=EntityY(a\piv,True)-(EntityY(a\cam,True)-30)
			TranslateEntity a\cam,dx*.05,dy*.05,0
End Function

;Delete all players in memory
Function delete_Players()
	For a.play=Each play
		If a\shield_ent FreeEntity a\shield_ent
		If a\ent FreeEntity a\ent
		If a\piv FreeEntity a\piv
		If a\cam FreeEntity a\cam
		Delete a
	Next
End Function

;View Update (players)
Function Update_Players_Views()
	For a.play=Each play
		Color 255,255,255
		Text a\tlx,a\tly,a\score
		If a\shield<40 Then
			Color 255,255,0
		End If
		If a\shield<20 Then
			Color 255,0,0
		End If
		Rect a\tlx,a\tly+16,a\shield,10
		Color 255,255,255
		Text a\brx-100,a\bry-16,a\name
		Text a\brx-176,a\tly,"Lives:"
		For x=0 To a\lives-1
			DrawImage Lives_image,a\brx-80+(x*16),a\tly
		Next
	Next
End Function

;Find a player from id
Function Get_Player_id.play(id)
	For a.play=Each play
		If a\id=id Then Return a
	Next
	Return Null
End Function

;
;Update explosion Flash
Function Update_Flash()
	AmbientLight Flash_Cnt,Flash_Cnt,Flash_Cnt
	If Flash_Cnt>G_Brightness Then Flash_Cnt=Flash_Cnt-5
End Function

Function Set_Flash()
	Flash_Cnt=255
End Function
;
;
;
;
;
;