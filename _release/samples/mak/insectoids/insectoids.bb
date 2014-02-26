
Global info1$="Insectoids!"
Global info2$="Features texturing of 2D graphics"

Include "../start.bb"

image=CreateImage( 256,256 )

texture=CreateTexture( 256,256 )

cube=CreateCube()
EntityTexture cube,texture
EntityFX cube,1

cube2=CopyEntity( cube )
PositionEntity cube2,-2,0,0
ScaleEntity cube2,.5,.5,.5

cube3=CopyEntity( cube )
PositionEntity cube3,2,0,0
ScaleEntity cube3,.5,.5,.5

camera=CreateCamera()
PositionEntity camera,0,0,-3
CameraClsColor camera,0,0,64

AmbientLight 255,255,255

Global buffer=ImageBuffer( image )

Gosub SetupInsectoids

While Not KeyHit(1)

	Gosub UpdateInsectoids

	SetBuffer buffer
	Gosub RenderInsectoids
	SetBuffer BackBuffer()
	
	CopyRect 0,0,256,256,0,0,ImageBuffer( image ),TextureBuffer( texture )

	If KeyDown(26) TurnEntity cube,0,-2,0
	If KeyDown(27) TurnEntity cube,0,+2,0
	
	If KeyDown(30) MoveEntity camera,0,0,.1
	If KeyDown(44) MoveEntity camera,0,0,-.1	
	
	p.Player=First Player
	If p<>Null
		RotateEntity cube,0,(p\x-128)/10.0,0
	Else
		RotateEntity cube,0,0,0
	EndIf
	TurnEntity cube2,-1,0,.5
	TurnEntity cube3,1,0,-.5
	
	UpdateWorld
	RenderWorld
	Flip
Wend
End

.SetupInsectoids

Const width=256,height=256,num_rots=64

Dim alien_rots(num_rots)

Global game_state,game_timer,level_name$,alien_speed
Global num_aliens,num_flying,fly_timer,num_bulls,num_players
Global c_x#,c_y#,c_xs#,c_ys#,c_dir#,c_phase#,rev_dir,c_speed#,c_xsize#,c_ysize#
Global player_image,stars_image,bomb_image,bull_image,stars_scroll,boom_anim
Global mini_ship,tmp_image,insectoids_image

Global boom_sound=LoadSound( "sounds\boom.wav" )
Global cool_sound=LoadSound( "sounds\cool.wav" )
Global kazap_sound=LoadSound( "sounds\kazap.wav" )
Global shoot_sound=LoadSound( "sounds\shoot.wav" )

Type Alien
	Field x#,y#,rot,state
	Field f_x#,f_y#
	Field dest_y#,dest_rot,rot_step,bomb_cnt
End Type

Type Player
	Field x,y,state,lives,ctrl,bang,score
End Type

Type Bomb
	Field x#,y#,xs#,ys#
End Type

Type Bullet
	Field x,y
End Type

Type Explosion
	Field x,y,frame
End Type

SetFont LoadFont( "arial",12,True )

tmp_image=CreateImage( width,FontHeight() )

LoadGraphics()

game_state=0
game_timer=0

Return

.UpdateInsectoids
UpdateGame()
Return

.RenderInsectoids
RenderGame()
Return

Function CreatePlayer( ctrl )
	p.Player=New Player
	p\lives=3
	p\ctrl=ctrl
	ResetPlayer( p )
	num_players=num_players+1
End Function

Function ResetPlayer( p.Player )
	p\x=width/2
	p\y=height-20
	p\state=1
End Function

Function UpdatePlayer( p.Player )

	If p\state<>1 Then Return

	Local l,r,f
	Select p\ctrl
	Case 1
		l=KeyDown( 203 )
		r=KeyDown( 205 )
		f=KeyHit( 57 )
	Case 2
;		jx=JoyXDir()
		If jx<0 Then l=1 Else If jx>0 Then r=1
;		f=JoyHit(1)
	End Select
	If l
		If p\x>16 Then p\x=p\x-4
	Else If r
		If p\x<width-16 Then p\x=p\x+4
	EndIf
	
	If game_state<>2 Then Return
	
	If f And num_bulls<3
		PlaySound shoot_sound
		b.Bullet=New Bullet
		b\x=p\x:b\y=p\y-16
		num_bulls=num_bulls+1
	EndIf

	dead=False	
	For a.Alien=Each Alien
		If ImagesOverlap( player_image,p\x,p\y,alien_rots( a\rot*num_rots/360 ),a\x,a\y )
			dead=True
			Exit
		EndIf
	Next
	For bb.Bomb=Each Bomb
		If ImagesOverlap( player_image,p\x,p\y,bomb_image,bb\x,bb\y )
			dead=True
			Exit
		EndIf
	Next
	
	If Not dead Then Return
	
	PlaySound boom_sound
	
	For k=-105 To 0 Step 15
		CreateExplosion( p\x+Rnd(-10,10),p\y+Rnd(-10,10),k )
	Next
	
	p\bang=1
	p\state=2
	p\lives=p\lives-1
	game_state=3
	
End Function

Function RenderPlayer( p.Player )

	Color 255,255,255
	Text width/2,4,p\score,1

	For k=1 To p\lives
		DrawImage mini_ship,k*12+4,14
	Next
	
	If p\state=1
		DrawImage player_image,p\x,p\y
		Return
	EndIf
	
	p\bang=p\bang+8
	
	r#=p\bang
	For i=255 To 1 Step -15
		Color i,i,i
		For an=0 To 359 Step 6
			x=p\x+Cos(an)*r
			y=p\y+Sin(an)*r
			Rect x,y,3,3
		Next
		r=r-6:If r<=0 Then Exit
	Next
	
End Function

Function AddPoints( p.Player,pnts )
	t=p\score/5000
	p\score=p\score+pnts
	If p\score/5000<>t Then p\lives=p\lives+1
End Function

Function UpdateBullet( b.Bullet )
	b\y=b\y-5
		
	For a.Alien=Each Alien
		If ImagesOverlap( bull_image,b\x,b\y,alien_rots( a\rot*num_rots/360 ),a\x,a\y )
			PlaySound kazap_sound
			If a\state=1
				pnts=25
			Else If a\state=2
				pnts=50
			Else
				pnts=100
			EndIf
			AddPoints( First Player,pnts )
			CreateExplosion( a\x,a\y )
			Delete b:num_bulls=num_bulls-1
			If a\state<>1 num_flying=num_flying-1
			num_aliens=num_aliens-1
			Delete a
			Return
		EndIf
	Next
	
	If b\y>0 Then Return
	Delete b:num_bulls=num_bulls-1
	
End Function

Function RenderBullet( b.Bullet )
	DrawImage bull_image,b\x,b\y
End Function

Function UpdateBomb( b.Bomb )
	b\x=b\x+b\xs
	b\y=b\y+b\ys
	If b\y>height Then Delete b
End Function

Function RenderBomb( b.Bomb )
	DrawImage bomb_image,b\x,b\y
End Function

Function UpdateAlien( a.Alien )

	Select a\state
	Case 1
		If a\rot<>0
			If a\rot>180 a\rot=a\rot+6 Else a\rot=a\rot-6
			If a\rot<0 Or a\rot>=360 Then a\rot=0
		EndIf
		dx=c_x+a\f_x*c_xs - a\x
		dy=c_y+a\f_y*c_ys - a\y
		If dx<-alien_speed Then dx=-alien_speed Else If dx>alien_speed Then dx=alien_speed
		If dy<-alien_speed Then dy=-alien_speed Else If dy>alien_speed Then dy=alien_speed
		a\x=a\x+dx:a\y=a\y+dy
		If c_dir<0 And a\x<16 Then rev_dir=True
		If c_dir>0 And a\x>width-16 Then rev_dir=True
	Case 2
		a\rot=a\rot+a\rot_step
		If a\rot<0 Then a\rot=a\rot+360 Else If a\rot>=360 Then a\rot=a\rot-360
		If a\rot<90 Or a\rot>270
			a\dest_rot=Rnd(180-40,180+40)
			a\dest_y=a\dest_y+Rnd( 100,300 )
			a\state=3
		EndIf
		a\x=a\x+Cos( a\rot-90 )*alien_speed
		a\y=a\y+Sin( a\rot-90 )*alien_speed
		DropBomb( a )
	Case 3		
		dr=a\rot-a\dest_rot
		If Abs(dr)>Abs(a\rot_step)
			a\rot=a\rot+a\rot_step
			If a\rot<0 Then a\rot=a\rot+360 Else If a\rot>=360 Then a\rot=a\rot-360
		EndIf
		a\x=a\x+Cos( a\rot-90 )*alien_speed
		a\y=a\y+Sin( a\rot-90 )*alien_speed
		If a\y>height
			a\x=Rnd(width/2)+width/4:a\y=0
			num_flying=num_flying-1
			a\state=1
		Else If a\y>a\dest_y
			a\rot_step=-a\rot_step
			a\state=2
		EndIf
		DropBomb( a )
	End Select
End Function

Function RenderAlien( a.Alien )
	DrawImage alien_rots( a\rot*num_rots/360 ),a\x,a\y
End Function

Function UpdateExplosion( e.Explosion )
	e\frame=e\frame+1
	If e\frame=18 Then Delete e
End Function

Function RenderExplosion( e.Explosion )
	If e\frame<0 Then Return
	DrawImage boom_anim,e\x,e\y,e\frame/3
End Function

Function CreateExplosion( x,y,frame=0 )
	e.Explosion=New Explosion
	e\x=x:e\y=y:e\frame=frame
End Function

Function DropBomb( a.Alien )
	If a\bomb_cnt=0 Then a\bomb_cnt=Rnd(50,100)
	a\bomb_cnt=a\bomb_cnt-1
	If a\bomb_cnt>0 Then Return
	p.Player=First Player
	If p=Null Then Return
	b.Bomb=New Bomb
	b\x=a\x
	b\y=a\y
	If a\x<p\x Then b\xs=1 Else b\xs=-1
	b\ys=4
End Function

Function UpdateFormation()

	c_phase=(c_phase+c_speed)Mod 360
	t#=Sin( c_phase )*.5+.5
	c_xs=t*c_xsize+1:c_ys=t*c_ysize+1
	
	If game_state<>1 Then c_x=c_x+c_dir
	
End Function

Function UpdateFlyTimer()

	If num_aliens>3
		If fly_timer=0 Then fly_timer=600
		fly_timer=fly_timer-1
		If fly_timer>120 Then Return
		If fly_timer Mod 30<>0 Then Return
	EndIf
	
	n=Rnd( num_aliens-num_flying )
	
	For a.Alien=Each Alien
		If a\state=1
			If n=0
				a\dest_y=a\y
				a\rot_step=3:If Rnd(1)<.5 Then a\rot_step=-3
				num_flying=num_flying+1
				a\state=2
				Return
			EndIf
			n=n-1
		EndIf
	Next
End Function

Function UpdateGame()

	Select game_state
	Case 0
		game_timer=game_timer+1
		If KeyHit(57) Then BeginGame()
	Case 1
		game_timer=game_timer+1
		If game_timer=150 Then game_state=2
		UpdateFormation()
	Case 2
		UpdateFlyTimer()
		UpdateFormation()
		If num_aliens=0 Then BeginLevel()
	Case 3
		UpdateFormation()
		If num_flying=0 And First Explosion=Null
			p.Player=First Player
			If p\lives>0
				ResetPlayer( p )
				game_state=2
				FlushKeys
			Else
				game_state=4
				game_timer=0
			EndIf
		EndIf
	Case 4
		UpdateFlyTimer()
		UpdateFormation()
		game_timer=game_timer+1
		If game_timer=150 Then EndGame()
	End Select
	
	rev_dir=False
	For a.Alien=Each Alien
		UpdateAlien( a )
	Next
	If rev_dir Then c_dir=-c_dir
	
	For bb.Bomb=Each Bomb
		UpdateBomb( bb )
	Next
	
	For p.Player=Each Player
		UpdatePlayer( p )
	Next
	
	For b.Bullet=Each Bullet
		UpdateBullet( b )
	Next
	
	For e.Explosion=Each Explosion
		UpdateExplosion( e )
	Next
End Function

Function RenderGame()

	TileBlock stars_image,0,stars_scroll
	TileImage stars_image,7,stars_scroll*2
	TileImage stars_image,23,stars_scroll*3
	stars_scroll=(stars_scroll+1) Mod ImageHeight( stars_image )
	
	For a.Alien=Each Alien
		RenderAlien( a )
	Next
	
	For bb.Bomb=Each Bomb
		RenderBomb( bb )
	Next
	
	For p.Player=Each Player
		RenderPlayer( p )
	Next
	
	For b.Bullet=Each Bullet
		RenderBullet( b )
	Next
	
	For e.Explosion=Each Explosion
		RenderExplosion( e )
	Next
	
	Select game_state
	Case 0
		DrawImage insectoids_image,width/2,height/3
		If game_timer<150 Or (game_timer-150) Mod 80<40
			Color 255,255,255:TitleText( "PRESS SPACE TO START",width/2,height-FontHeight()*2,game_timer )
		EndIf
	Case 1
		Rainbow( game_timer*5 )
		TitleText( level_name$,width/2,height/2,game_timer*2 )
	Case 4
		Color 255,255,255
		Text width/2,height/2,"GAME OVER",1,1
	End Select
	
End Function

Function TitleText( txt$,x,y,time )
	n=0
	If time<100 Then n=100-time
	If n=1
		Text x,y,txt$,1,1
	Else
		ExplodeText( x,y,txt$,n*.5+1,n+1 )
	EndIf
End Function

Function LoadGraphics()

	AutoMidHandle True
	
	stars_image=LoadImage( "graphics\stars.bmp" )
	player_image=LoadImage( "graphics\player.bmp" ):ScaleImage player_image,.5,.5
	mini_ship=CopyImage( player_image ):ScaleImage mini_ship,.4,.4
	bull_image=LoadImage( "graphics\bullet.bmp" ):ScaleImage bull_image,.25,.5
	bomb_image=LoadImage( "graphics\bbomb.bmp" ):ScaleImage bomb_image,.3,.3
	boom_anim=LoadAnimImage( "graphics\kaboom.bmp",60,48,0,6 )
	insectoids_image=LoadImage( "graphics\insectoids_logo.bmp" )
	ResizeImage insectoids_image,256,64
	ResizeImage boom_anim,8,8:ResizeImage boom_anim,16,16
	i=LoadImage( "graphics\alien.bmp" ):ScaleImage i,.3,.3
	For k=0 To num_rots-1
		alien_rots(k)=CopyImage( i )
		RotateImage alien_rots(k),k*360/num_rots
	Next
End Function

Function BeginLevel()

	PlaySound cool_sound
	
	Read level_name$
	If level_name$=""
		Restore .levels
		Read level_name$
		alien_speed=alien_speed+1
		If alien_speed>6 Then alien_speed=6
	EndIf
	
	c_x=width/2:c_y=height/4:c_phase=0:c_dir=1
	
	Read c_speed,c_xsize,c_ysize
	
	Repeat
		Read x
		If x=999 Then Exit
		Read y,cnt
		For k=1 To cnt
			a.Alien=New Alien
			a\x=c_x
			a\y=c_y
			a\rot=0
			a\state=1
			a\f_x=x*16
			a\f_y=y*12
			x=x+1
		Next
		num_aliens=num_aliens+cnt
	Forever

	game_state=1
	game_timer=0
	
End Function

Function BeginGame()
	
	level=0
	num_bulls=0
	num_aliens=0
	num_flying=0
	game_state=0
	num_players=0
	alien_speed=3
	
	CreatePlayer( 1 )
	
	Restore .levels
	
	BeginLevel()
	
	FlushKeys
	
End Function

Function EndGame()

	Delete Each Player
	Delete Each Bullet
	Delete Each Alien
	Delete Each Bomb

	game_state=0
	game_timer=0

	FlushKeys

End Function

Function Rainbow( time )
	r=time Mod 768:If r>255 Then r=511-r
	g=(time+256)Mod 768:If g>255 Then g=511-g
	b=(time+512) Mod 768:If b>255 Then b=511-b
	If r<0 Then r=0
	If g<0 Then g=0
	If b<0 Then b=0
	Color r,g,b
End Function

Function ExplodeText( x,y,txt$,xn#,yn# )
	w=StringWidth( txt$ )
	h=FontHeight()
	SetBuffer ImageBuffer( tmp_image )
	Cls:Text 0,0,txt$
	SetBuffer buffer
	For ty=0 To h-1 Step 4
		For tx=0 To w-1 Step 4
			xo#=(tx-w/2)*xn
			yo#=(ty-h/2)*yn
			DrawImageRect tmp_image,x+xo,y+yo,tx,ty,4,4
		Next
	Next
End Function

.levels

Data "LEVEL 1"
Data 1,.25,0
Data -2,-2,5
Data -3,-1,7
Data -3,0,7
Data -3,1,7
Data -3,2,7
Data 999

Data "LEVEL 2"
Data 1,.25,.25
Data 0,-2,1
Data -1,-1,3
Data -2,0,5
Data -3,1,7
Data -4,2,9
Data -5,3,11
Data 999

Data "LEVEL 3"
Data 3,.25,.5
Data -5,-2,11
Data -4,-1,9
Data -3,0,7
Data -4,1,9
Data -5,2,11
Data 999

Data "LEVEL 4"
Data 2,0,1
Data -5,-1,11
Data -5,0,11
Data -5,1,11
Data -5,2,11
Data -5,3,11
Data -5,4,11
Data -5,5,11
Data 999

Data "LEVEL 5"
Data 1,.25,.125
Data -3,-2,7
Data -4,-1,9
Data -5,0,11
Data -5,1,11
Data -5,2,11
Data -5,3,11
Data -5,4,11
Data -5,5,11
Data -5,6,11
Data -5,7,11
Data 999

Data "LEVEL 6"
Data 1,.25,.125
Data -7,-2,15
Data -7,-1,15
Data -7,0,15
Data -7,1,15
Data -7,2,15
Data -7,3,15
Data -7,4,15
Data -7,5,15
Data -7,6,15
Data -7,7,15
Data -7,8,15
Data -7,9,15
Data -7,10,15
Data -7,11,15
Data 999

Data ""