;	Tunnel Run
;	(c)2001 David Bird
;	dave@birdie72.freeserve.co.uk
;	www.birdie72.freeserve.co.uk
;	Main Program

;
Global Cheater=False

Graphics3D 640,480
SetBuffer BackBuffer()
Include "globs.bb"
Include "functions.bb"

ChangeDir "Media"
;
;Set Up collision Constants here.
;
Const TYPE_CAM=1
Const TYPE_PLAYER=2
Const TYPE_SHOT1=3
Const TYPE_TUNNEL=50
Const TYPE_ASTER=51
Const TYPE_ASTERPOLY=52
Const TYPE_CLAW=53


;
;
Load_Media()
Get_GFX_Modes()
Load_Highscores()
Set_GFXMode()
Resize_Images(GraphicsWidth(),GraphicsHeight())

;
Goto MainMenu
End

.Game_Start_Loop
;
FlushMouse

Create_Tunnel()
lit=CreateLight()
TurnEntity lit,45,0,0

;
Select Gametype
	Case 1			;One Player Game
		this.play=Add_Player(player1type,255,255,255,False,0,"Player1")
		Num_Players=1
	Case 2			;Two Player Game
		this.play=Add_Player(player1type,255,255,255,True,0,"")
		other.play=Add_Player(player2type,255,255,0,True,1,"")
		Num_Players=2
		If Player2type>=5 Then Num_Players=2
End Select

Exited=False
Go1_timer=0
Go2_timer=0
Game_Counter=0

dummy=CreatePivot()
Listen=CreateListener(dummy,.001,10,5)
While Exited=False
Game_Counter=Game_Counter+1
	Cls
	TurnEntity lit,1,1,0
	Update_Flash()
	Do_Tunnel_Scroll(Tunnel_tex,1)
	Do_Tunnel_Scroll(tex0,-2)
	Update_Shield_Texture()
	Do_Control()
	
	Update_Players()
	Update_Shots()
	Update_Objects()
	Update_Smoke()
	Change_Tunnel_Colors()
	If (Game_Counter Mod 100)=0 Then Alter_Tunnel_Colors()
;
	If cnt=0 Then
		cnt=10+(10*(3-G_Diff))
		Add_Object(Rnd(-300,300),Rnd(-300,300),9000,-1,Rnd(10,70),Rnd(50,150),Rand(1,3))
;		Add_Object(ppop,0,10000,-1,50,4,1000)
		ppop=ppop+20
	End If
	;
	If KeyHit(1) Then Num_Players=0:Exited=True
	;
	cnt=cnt-1
;
	UpdateWorld
	RenderWorld
	
	lastplayer=Do_GameOver_Overlay()
	If lastplayer>=5 Then ppp=100
	If ppp>0 Then 
		ppp=ppp-1
		If ppp=2 Then Exited=True
	End If
	Update_Players_Views()
	Flip
Wend
	FreeEntity dummy
	;Check High Scores
	Select gametype
		Case 1			;One player Game
			Check_Highs(score(0),1)
		Case 2			;two player Game
		If player1type<=4 Then 
			Check_Highs(score(0),1)
		End If
		If player2type<=4 Then 
			Check_Highs(score(1),2)
		End If
	End Select
	FreeEntity lit
	FreeEntity Tunnel_Mesh
	FreeEntity Background
	Free_Smoke()
	Free_Shots()
	Delete_Objects()
	Delete_Players()
	FlushMouse
	VWait 50
Goto MainMenu
;
;
Function Do_GameOver_Overlay()
	If Go1_Timer>0 Then
		Go1_Timer=Go1_timer-1
		If Go1_Timer>2 Then
			If Num_Players=1 Then pos=0 Else pos=GraphicsHeight()/4
			If Go2_Timer>2 Then pos=0
			DrawImage gover_image,0,pos
		Else
			If Num_Players=0 Then 
				Exited=True
			Else
				lastplayer=Resize_Last_Player()
			End If
		End If
	End If
	If Go2_Timer>0 Then
		Go2_Timer=Go2_timer-1
		If Go2_Timer>2 Then
			If Num_Players=1 Then pos=GraphicsHeight()/2 Else pos=GraphicsHeight()/4
			If Go1_Timer>2 Then pos=GraphicsHeight()/2
			DrawImage gover_image,0,pos
		Else
			If Num_Players=0 Then 
				Exited=True
			Else
				lastplayer=Resize_Last_Player()
			End If
		End If
	End If
	Return lastplayer
End Function
;
;
Function Update_Shield_Texture()
	shield_tex_pos=shield_tex_pos+.01
	If shield_tex_pos>1 Then shield_tex_pos=shield_tex_pos-1
	PositionTexture shield_tex,shield_tex_pos,shield_tex_pos
End Function
;	Create Scrolling tunnel No entity movement done
;	at all. Texture scrolling screates the effect
;	Makes controling bad guys a whole lot easier..
;
Function Create_Tunnel()
	Tunnel_mesh=CreateCylinder(12)
	FlipMesh Tunnel_mesh
	ScaleMesh Tunnel_mesh,500,12000,500
	RotateEntity Tunnel_mesh,90,0,0
	EntityTexture Tunnel_mesh,Tunnel_tex,0,0
	EntityType tunnel_mesh,TYPE_TUNNEL
	PositionEntity tunnel_mesh,0,0,11400
	EntityColor tunnel_mesh,255,100,0
	t_r1=255:ta_r1=t_r1
	t_g1=100:ta_g1=t_g1
	t_b1=0:ta_b1=t_b1

	BackGround=CreateCylinder(12)
	FlipMesh BackGround
	ScaleMesh BackGround,510,12000,510
	RotateEntity BackGround,90,0,0
	EntityTexture BackGround,tex0,0,0
	PositionEntity BackGround,0,0,11400
	EntityColor BackGround,0,255,255
	t_r2=0:ta_r2=t_r2
	t_g2=255:ta_g2=t_g2
	t_b2=255:ta_b2=t_b2
End Function

Function Change_Tunnel_Colors()
	If t_r1>ta_r1 Then t_r1=t_r1-1
	If t_g1>ta_g1 Then t_g1=t_g1-1
	If t_b1>ta_b1 Then t_b1=t_b1-1
	If t_r2>ta_r2 Then t_r2=t_r2-1
	If t_g2>ta_g2 Then t_g2=t_g2-1
	If t_b2>ta_b2 Then t_b2=t_b2-1

	If t_r1<ta_r1 Then t_r1=t_r1+1
	If t_g1<ta_g1 Then t_g1=t_g1+1
	If t_b1<ta_b1 Then t_b1=t_b1+1
	If t_r2<ta_r2 Then t_r2=t_r2+1
	If t_g2<ta_g2 Then t_g2=t_g2+1
	If t_b2<ta_b2 Then t_b2=t_b2+1
	EntityColor Tunnel_mesh,t_r1,t_g1,t_b1
	EntityColor Background,t_r2,t_g2,t_b2
End Function

Function Alter_Tunnel_Colors()
	ta_r1=Rand(10,255):ta_g1=Rand(10,255):ta_b1=Rand(10,255)
	ta_r2=Rand(10,255):ta_g2=Rand(10,255):ta_b2=Rand(10,255)
End Function

Function Resize_Images(Width,Height)
	ResizeImage TitleScreen1,Width,Height
	ResizeImage gover_image,Width,Height/2
End Function
;
;
;
Function Load_media()
	Tunnel_tex=LoadTexture("tex0.bmp",6):ScaleTexture Tunnel_tex,1,2
	tex0=LoadTexture("tex0.bmp"):ScaleTexture tex0,.5,.5
	Shield_tex=LoadTexture("tex1.bmp",3)
	Rock_Tex=LoadTexture("stone04.bmp")
	;
	;	Global Meshes
	;
	Aster_mesh=LoadMesh("asteroid.3ds"):EntityTexture Aster_Mesh,Rock_tex
	ScaleMesh Aster_mesh,.005,.005,.005
	HideEntity Aster_Mesh
	;
	player_mesh=LoadMesh("hfighter.3ds")
	ScaleEntity player_mesh,8,9,11
	HideEntity player_mesh
	;
	shield_mesh=CreateSphere()
	ScaleEntity shield_mesh,55,35,60
	EntityTexture shield_mesh,shield_tex
	HideEntity shield_mesh
	;
	missile1_mesh=LoadMesh("missile1.3ds")
	RotateMesh missile1_mesh,Pi,0,0
	ScaleMesh missile1_mesh,2,2,2
	temptex=LoadTexture("tex2.bmp")
	EntityTexture missile1_mesh,temptex
	HideEntity missile1_mesh
	FreeTexture temptex
	;
	Bad01_mesh=CreateSphere()
	HideEntity bad01_mesh
	
	claw_anim_mesh=LoadAnimMesh("claw.3ds")
	HideEntity claw_anim_mesh
	;
	;
	; Sprites
	;
	shot1_spr=LoadSprite("shot1.bmp")
	HideEntity shot1_spr
	ScaleSprite shot1_spr,32,16
	;
	For a=1 To 4
		b$="smoke"+a+".bmp"
		smoke_spr(a)=LoadSprite(b$)
		HideEntity smoke_spr(a)
	Next
	target_spr=LoadSprite("target.bmp"):HideEntity target_spr
	

	;
	; 2DImages
	Titlescreen1=LoadImage("title.bmp")
	Mousepointer=LoadImage("mouse.bmp"):MaskImage MousePointer,255,255,0
	Lives_Image=LoadImage("lives.bmp")
	Gover_Image=LoadImage("go.bmp")
	;
	
	;
	;Sounds
	Shoot1_wave=LoadSound("shoot.wav")
	Explode_Wave=LoadSound("boom.wav")
End Function

Function Free_media()
	If Tunnel_tex<>0 Then FreeTexture tunnel_tex:tunnel_tex=0
	If tex0<>0 Then FreeTexture tex0:tex0=0
	If Shield_tex<>0 Then FreeTexture Shield_Tex:Shield_Tex=0
	If Rock_Tex<>0 Then FreeTexture Rock_Tex:Rock_Tex=0
	If temptex<>0 Then FreeTexture temptex:temptex=0

	;
	;	Global Meshes
	;
	If Aster_mesh<>0 Then FreeEntity Aster_mesh:Aster_Mesh=0
	If player_mesh<>0 Then FreeEntity player_mesh:player_mesh=0
	If shield_mesh<>0 Then FreeEntity shield_mesh:shield_mesh=0
	If missile1_mesh<>0 Then FreeEntity missile1_mesh:missile1_mesh=0
	;
	If Bad01_mesh<>0 Then FreeEntity bad01_mesh:Bad01_mesh=0
	If Claw_Anim_Mesh<>0 Then FreeEntity Claw_Anim_Mesh:Claw_Anim_Mesh=0
	;
	;
	; Sprites
	;
	If shot1_spr<>0 Then FreeEntity shot1_spr:shot1_spr=0
	;
	For a=1 To 4
		If smoke_spr(a)<>0 Then FreeEntity smoke_spr(a):smoke_spr(a)=0
	Next
	If target_spr<>0 Then FreeEntity target_spr:Target_Spr=0
	;
	; 2DImages
	If Titlescreen1<>0 Then FreeImage Titlescreen1:Titlescreen1=0
	If Mousepointe<>0 Then FreeImage MousePointer:MousePointer=0
	If Lives_Image<>0 Then FreeImage Lives_image:Lives_image=0
	If Gover_Image<>0 Then FreeImage Gover_Image:Gover_Image=0
	;
	;Sounds
	If Shoot1_wave<>0 Then FreeSound Shoot1_wave:Shoot1_wave=0
	If Explode_Wave<>0 Then FreeSound Explode_Wave:Explode_Wave=0
End Function


;
;	Trick scrolling
;	Move the texture
Function Do_Tunnel_Scroll(texture,div#)
	Tunnel_Texture_Coord=Tunnel_Texture_Coord+Scroll_speed
	If Tunnel_Texture_Coord>1 Then Tunnel_Texture_Coord=Tunnel_Texture_Coord-1
	PositionTexture texture,Tunnel_Texture_Coord/div,Tunnel_Texture_Coord
End Function

;
;	Control an entity 
;	
;	Player functions
;	typ = 	1 Mouse controled player
;			2 ana Joy Controlled player
;			3 dig Joy controlled player
;			4 keyboard controlled player
;			5 CPU Controlled player
;			6 internet player   ** TO DO **
Function Do_Control()
	For a.play=Each play
		If a\dead=False Then
			Select a\con
				Case 1		;Mouse Controlled
					Do_MouseMove(a)
				Case 2		;analog controlled
					Do_JoyMove(a)
				Case 3		;directional joy controlled
					Do_JoyDirMove(a)
				Case 4		;keyboard controlled
					Do_KeyMove(a)
				Case 5		;CPU Controlled
					CPU_Move(a)
				Case 6		;Internet controlled
			End Select
		End If
	Next
End Function

Function Do_MouseMove(a.play)
	dx#=MouseXSpeed()/2
	dy#=MouseYSpeed()/2
	If dx>0 Then a\roll=a\roll-4
	If dx<0 Then a\roll=a\roll+4
	If dy>0 Then a\pich=a\pich-4
	If dy<0 Then a\pich=a\pich+4
	If a\pich<>0 Then a\pich=a\pich*.85
	If a\roll<>0 Then a\roll=a\roll*.85
	RotateEntity a\piv,a\pich,0,a\roll
	TranslateEntity a\piv,dx,dy,0
	MoveMouse GraphicsWidth()/2,GraphicsHeight()/2
	If MouseDown(1) Then Add_Shot(a\piv,1,10,a)
End Function

Function Do_JoyMove(a.play)
	dx#=JoyX()
	dy#=JoyY()
	If dx>-Dead_BandX And dx<Dead_BandX Then dx=0
	If dy>-Dead_BandY And dy<Dead_BandY Then dy=0
	dx=dx*5
	dy=dy*5
	If dx>0 Then a\roll=a\roll-4
	If dx<0 Then a\roll=a\roll+4
	If dy>0 Then a\pich=a\pich-4
	If dy<0 Then a\pich=a\pich+4
	If a\pich<>0 Then a\pich=a\pich*.85
	If a\roll<>0 Then a\roll=a\roll*.85
	RotateEntity a\piv,a\pich,0,a\roll
	TranslateEntity a\piv,dx,dy,0
	If JoyDown(1) Then Add_Shot(a\piv,1,10,a)
End Function

Function Do_JoyDirMove(a.play)
	dx#=JoyXDir()*5 
	dy#=JoyYDir()*5
	If dx>0 Then a\roll=a\roll-4
	If dx<0 Then a\roll=a\roll+4
	If dy>0 Then a\pich=a\pich-4
	If dy<0 Then a\pich=a\pich+4
	If a\pich<>0 Then a\pich=a\pich*.85
	If a\roll<>0 Then a\roll=a\roll*.85
	RotateEntity a\piv,a\pich,0,a\roll
	TranslateEntity a\piv,dx,dy,0
	If JoyDown(1) Then Add_Shot(a\piv,1,10,a)
End Function

Function Do_KeyMove(a.play)
	If KeyDown(Keys(1)) Then dx#=-5
	If KeyDown(Keys(2)) Then dx#=5
	If KeyDown(Keys(3)) Then dy#=-5
	If KeyDown(Keys(4)) Then dy#=5
	If dx>0 Then a\roll=a\roll-4
	If dx<0 Then a\roll=a\roll+4
	If dy>0 Then a\pich=a\pich-4
	If dy<0 Then a\pich=a\pich+4
	If a\pich<>0 Then a\pich=a\pich*.85
	If a\roll<>0 Then a\roll=a\roll*.85
	RotateEntity a\piv,a\pich,0,a\roll
	TranslateEntity a\piv,dx,dy,0
	If KeyDown(keys(5)) Then Add_Shot(a\piv,1,10,a)
End Function

Function CPU_Move(a.play)
	b.Obj=Find_Closest_Target()
	If b<>Null Then
		dx#=(EntityX(b\piv)-EntityX(a\piv))/60
		dy#=(EntityY(b\piv)-EntityY(a\piv))/60
		If dx>0 Then a\roll=a\roll-2
		If dx<0 Then a\roll=a\roll+2
		If dy>0 Then a\pich=a\pich-2
		If dy<0 Then a\pich=a\pich+2
		If a\pich<>0 Then a\pich=a\pich*.85
		If a\roll<>0 Then a\roll=a\roll*.85
		RotateEntity a\piv,a\pich,0,a\roll
		TranslateEntity a\piv,dx,dy,0
		ent=LinePick(EntityX(a\piv),EntityY(a\piv),EntityZ(a\piv)+50,0,0,5000)
		If dx>-2.5 And dx<2.5 Then 
			If dy>-2.5 And dy<2.5 Then
				Add_Shot(a\piv,1,10,a)
			End If
		End If
	End If
End Function

Function Find_Obj_From_Ent.Obj(ent)
	For a.obj=Each obj
		If a\piv=ent Or a\ent=ent Then Return a
	Next
	Return Null
End Function

Function Find_Closest_Target.Obj()
	z#=1000000
	minz#=100
	For a.Obj=Each Obj
		If EntityZ(a\piv,True)>0 Then
			If EntityZ(a\piv,True)<z And EntityZ(a\piv,True)>minz Then target.Obj=a:z=EntityZ(a\piv,True)
		End If
	Next
	Return target
End Function
;
Type play
	Field name$
	Field rate
	Field ox#
	Field cam
	Field con
	Field piv
	Field ent
	Field shield_ent
	Field glow#
	Field dead
	Field pich#
	Field roll#
	Field shield#
	Field score
	Field waiting
	Field id
	;
	Field tlx
	Field tly
	Field brx
	Field bry
	Field lives
End Type

;
;
;
Type shot
	Field typ
	Field ent
	Field life#
	Field dx#
	Field dy#
	Field dz#
	Field speed#
	Field ang#
	Field firer.play
	Field target.Obj
End Type

;
;	Object
;
;	1-3 are asteroids
Type Obj
	Field typ
	Field piv
	Field ent
	Field val
	Field col
	Field cd
	Field speed#
	Field dx#
	Field dy#
	Field ds#
	Field fx
	Field fy
	Field dir
	Field st
	Field cnt
End Type

;
;	Smoke Effect
;
Type smoke
	Field alpha#
	Field reduct#
	Field ent
	Field size#
	Field rot#
	Field disp#
End Type

.MainMenu
SetBuffer BackBuffer()
Cls
FlushMouse:FlushKeys

Game_Start=False
Exit_Game=False
Options=False

While Not Game_Start=True
	cntr=cntr+1
	If MouseXSpeed()+MouseYSpeed()<>0 Then cntr=0

	Cls
	DrawImage TitleScreen1,0,0

	sel=0
	If cntr<500 Then
		Draw_Menu(True,0,0)
		sel=Draw_Menu(False,-2,-2)
		If MouseHit(1) Then
			Select sel
				Case 1
					 If gametype=1 Then gametype=2 Else gametype=1
				Case 2
					player1type=player1type+1
					If player1type>6 Then player1type=1
					If player2type=player1type Then player1type=player1type+1
				Case 3
					player2type=player2type+1
					If player2type>6 Then player2type=1
					If player2type=player1type Then player2type=player2type+1
				Case 4
					Game_Start=True
					Options=True
				Case 5
				Case 6
					If player1type<>6 And player2type<>6 Then 
						Game_Start=True
					End If
				Case 7
					Exit_Game=True
					Game_Start=True
			End Select
		End If
	Else
		If cntr>1000 Then cntr=0
		Draw_Highs(True,0,0)
		Draw_Highs(False,-2,-2)		
	End If
	DrawImage MousePointer,MouseX(),MouseY()
	Flip
Wend
cntr=0
If Options=True Then Goto Options
If Exit_Game=True Then Goto Exit_Game_Sub
If Game_Start=True Then Goto Game_Start_Loop
RuntimeError "Game Ended Abnormally"
EndGraphics
End


;
;	Exit Game / Save Highs etc
;	Do Credits
.Exit_Game_Sub
Save_Highscores()
ChangeDir ".."
EndGraphics
End
;
;
;Save Highscores and options
Function Load_Highscores()
	p=0
	file=ReadFile("../Highs/Highs.Hsr")
	For a=1 To 10
		Hn$(a)=ReadLine$(file)
		Hs(a)=ReadInt(file)
	Next
	G_Brightness=ReadInt(file)
	G_StartLives=ReadInt(file)
	G_Diff=ReadInt(file)
	G_CurrentGFX=ReadInt(file)
End Function

Function Save_Highscores()
	p=0
	file=WriteFile("../Highs/Highs.Hsr")
	For a=1 To 10
		WriteLine file,Hn$(a)
		WriteInt file,Hs(a)
	Next
	WriteInt file,G_Brightness
	WriteInt file,G_StartLives
	WriteInt file,G_Diff
	WriteInt file,G_CurrentGFX
End Function

Function Draw_Menu(col,ox,oy)
	sel=0
	For a=1 To 7
		b$=""
		If col=True Then Color 0,0,0 Else Color 255,255,255
		p=0
		Select a
			Case 1
				b$=Menu$(a)+GM_TYPE(gametype)
			Case 2
				b$=Menu$(a)+CN_TYPE(player1type)
			Case 3
				If gametype=2 Then 
					b$=Menu$(a)+CN_TYPE(player2type)
				End If
			Default
				b$=Menu(a)
		End Select
		c#=(Float(a)*32)/2
		w=Len(b$)*8
		y=GraphicsHeight()/2
		y=((y-c)+a*32)-oy
		x=(GraphicsWidth()/2)-ox-(w/2)
		h=16
		If RectsOverlap(x,y,w,h,MouseX(),MouseY(),1,1) Then sel=a:p=1
		If p<>0 And col=False Then Color Rnd(255),Rnd(255),Rnd(255)
		Text x,y,b$
	Next	
	Return sel
End Function


;
;Draw High's
Function Draw_Highs(col,ox,oy)
	For a=1 To 10
		If col=True Then Color 0,0,0 Else Color 255,255,255
		b$=a+" "+hn$(a)+"  "+hs(a)
		c#=(Float(a)*32)/2
		w=Len(b$)*8
		y=GraphicsHeight()/2
		y=((y-c)+a*32)-oy
		x=(GraphicsWidth()/2)-ox-(w/2)
		h=16
		Text x,y,b$
	Next
End Function

Function Check_Highs(score,id)
	If score>hs(10) Then     ; Got a high Score
		ps=0
		For a=10 To 1 Step -1
			If score>hs(a) Then 
				ps=a
				hs(a+1)=hs(a)
				hn(a+1)=hn(a)
			End If
		Next
		If ps<>0 Then
			hs(ps)=score
			SetBuffer FrontBuffer()
			Cls
			DrawImage TitleScreen1,0,0
			Color 0,0,0
			Text 0,GraphicsHeight()/2,"Well Done Player "+id+". Your On the High Score Table",0,1
			Color 255,255,255
			Text 2,2+GraphicsHeight()/2,"Well Done Player "+id+". Your On the High Score Table",0,1
			Locate GraphicsWidth/2,GraphicsHeight()/2+16
			hn(ps)=Input$("Enter Your Name ?")
			SetBuffer BackBuffer()
			cntr=1000
		End If
	End If
End Function

.dats
;Game Type
Data "One Player Game"
Data "Two Player Game"

;Control Type
Data "Mouse Control"
Data "Analog Joystick"
Data "Digital Joystick"
Data "Keyboard (Not Recommended)"
Data "CPU"
Data "Network Game(Not Availible)"

;Menus
Data "Game Type:"
Data "Player One Control:"
Data "Player Two Control:"
Data "Options"
Data ""
Data "START GAME"
Data "QUIT GAME"

.highs
Data "David Bird",100000
Data "Made With",90000
Data "The Great",80000
Data "Blitz Basic3D",70000
Data "(c)2001 David Bird",60000
Data "Donna Bird",50000
Data "Jessica Bird",40000
Data "Rebecca Bird",30000
Data "Daniel Bird",20000
Data "David Bird",10000
Data "",0

.Options
Create_Tunnel()
OptTunnel=CopyEntity(Tunnel_Mesh):FreeEntity Tunnel_Mesh
OptBackground=CopyEntity(Background):FreeEntity Background

Optlight=CreateLight()
TurnEntity OptLight,45,0,0
OptCamera=CreateCamera()
CameraRange Optcamera,1,25000

While Not KeyDown(1)


	AmbientLight G_Brightness,G_Brightness,G_Brightness
	UpdateWorld
	RenderWorld
	G_Brightness=Draw_slider("Brightness",50,50,300,16,G_Brightness,255,5,50)
	G_StartLives=Draw_slider("Number of Lives",50,75,300,16,G_StartLives,5,1,150)
	If G_StartLives<1 Then G_StartLives=1
	G_Diff=Draw_slider("Difficulty",50,100,300,16,G_Diff,3,1,150)
	G_CurrentGFX=Draw_GFX_Selector(G_CurrentGFX,50,130)
	DrawImage MousePointer,MouseX(),MouseY()
	
	Text GraphicsWidth()/2,GraphicsHeight()-16,"Press Escape to Return to Main Menu",1,0
	Flip
Wend

If OptLight<>0 Then FreeEntity OptLight:OptLight=0
If OptTunnel<>0 Then FreeEntity OptTunnel:OptTunnel=0
If OptBackground<>0 Then FreeEntity OptBackground:OptBackground=0
If OptCamera<>0 Then FreeEntity OptCamera:OptCamera=0
Goto MainMenu

Function Draw_slider(txt$,x,y,Ln#,hg,pos#,scale#,Stp,dely)
	sel=0
	Rect x,y,ln,hg,0
	Rect x+ln,y,16,hg,0:Text x+ln+8,y+(hg/2)-1,"-",1,1
	If RectsOverlap(x+ln,y,16,16,MouseX(),MouseY(),1,1) Then sel=1
	Rect x+ln+16,y,16,hg,0:Text x+ln+24,y+(hg/2)-1,"+",1,1
	If RectsOverlap(x+ln+16,y+(hg/2)-1,16,16,MouseX(),MouseY(),1,1) Then sel=2
	ps=(ln/scale)*pos
	Rect ps+x,y,2,hg,1
	Text x+ln+38,y,txt+" "+Int(Pos)
	newval=pos
	If MouseDown(1) And sel<>0 Then
		Delay dely
		If sel=1 And newval>0 Then newval=newval-stp
		If sel=2 And newval<scale Then newval=newval+stp
	End If
	If newval<0 Then newval=0
	If newval>scale Then newval=scale
	Return newval
End Function

Function Draw_GFX_Selector(current,x,y)
	Rect x-4,y-4,150,24,0
	sel=0
	Rect x+150,y-4,16,24,0:Text x+158,y+6,"-",1,1
	If RectsOverlap(x+150,y-4,16,24,MouseX(),MouseY(),1,1) Then sel=1
	Rect x+166,y-4,16,24,0:Text x+172,y+6,"+",1,1
	If RectsOverlap(x+166,y-4,16,24,MouseX(),MouseY(),1,1) Then sel=2
	Rect x+182,y-4,24,24,0:Text x+194,y+6,"GO",1,1
	If RectsOverlap(x+182,y-4,24,24,MouseX(),MouseY(),1,1) Then sel=3

	p.GfxMode=First GfxMode
	For r=1 To current
		p=After p
	Next
	Text x,y,p\t$
	
	If MouseDown(1) And sel<>0 Then
		Delay 100
		If sel=1 And current>1 Then current=current-1
		If sel=2 And current<Max_GFXModes Then current=current+1
		If sel=3 Then ;Reset GFXMODE
			If OptLight<>0 Then FreeEntity OptLight:OptLight=0
			If OptTunnel<>0 Then FreeEntity OptTunnel:OptTunnel=0
			If OptBackground<>0 Then FreeEntity OptBackground:OptBackground=0
			If OptCamera<>0 Then FreeEntity OptCamera:OptCamera=0
			Set_GFXMode()
			Create_Tunnel()
			OptTunnel=CopyEntity(Tunnel_Mesh):FreeEntity Tunnel_Mesh
			OptBackground=CopyEntity(Background):FreeEntity Background

			Optlight=CreateLight()
			TurnEntity OptLight,45,0,0
			OptCamera=CreateCamera()
			CameraRange Optcamera,1,25000
		End If
	End If
	Return current
End Function

Type GfxMode
	Field width,height,depth
	Field t$
End Type

Function Get_GFX_Modes()
	cnt=0
	For k=1 To CountGfxModes()
		If GfxMode3D(k)
			t.GfxMode=New GfxMode
			t\width=GfxModeWidth(k)
			t\height=GfxModeHeight(k)
			t\depth=GfxModeDepth(k)
			t\t$=t\width+","+t\height+","+t\Depth
			cnt=cnt+1
		EndIf
	Next
	Max_GFXModes=cnt-1
End Function

Function Set_GFXMode()
	m.GfxMode=First GfxMode
	For r=1 To G_CurrentGFX
		m=After m
	Next
	Free_Media()
	ClearCollisions
	Graphics3D m\width,m\height,m\depth,1
	SetBuffer BackBuffer()
	Load_Media()
	Text GraphicsWidth()/2,GraphicsHeight()/2,"Please Wait Initialising Graphics",1,1
	Flip
	Resize_Images(GraphicsWidth(),GraphicsHeight())
	;Do Collision Table Here
	Collisions TYPE_CAM,TYPE_TUNNEL,2,2
	Collisions TYPE_PLAYER,TYPE_TUNNEL,2,2
	Collisions TYPE_ASTER,TYPE_PLAYER,1,2

	Collisions TYPE_PLAYER,TYPE_ASTER,1,2

	Collisions TYPE_SHOT1,TYPE_ASTER,1,1
	Collisions TYPE_PLAYER,TYPE_PLAYER,1,1
	Collisions TYPE_PLAYER,TYPE_CLAW,2,2

End Function