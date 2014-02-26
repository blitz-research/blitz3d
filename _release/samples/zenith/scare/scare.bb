Graphics3D 800,600,16
SetBuffer BackBuffer()
AmbientLight 120,120,90

Global up=17,down=31,Right=32,Left=30,esc=1,mx,my
Global timer=CreateTimer(60)
Global init=MilliSecs(),frames#=0,fps=0,font=LoadFont("verdana")

Global grav#=.3,mxspd#=1.4,myspd#=1.4,friction#=.04

Type cam
Field x#,y#,z#,picked
Field ax#,ay#,az#,gun,gx#,gy#,gz#,g#,gzz#,gtime
Field ent,spd#,mmx,mmy,mlook,fall,pent,jump
Field mx#,my#,mz#,jstrength#,crouch,cy#,cy2#,mm,zo#,zs#
Field walk,head#,xspd#,yspd#,zspd#,move,gya#,light,s_step#
End Type

Global cursor=LoadAnimImage("textures/gui/cursors.png",31,31,0,17),c_id,m_id
Global c_gui=LoadImage("textures/gui/chat.png"),maxh#,maxl#

Type h
Field time#,ent,ji,spd#
End Type

Global PLYTYPE=1,WALLTYPE=2
Collisions PLYTYPE,WALLTYPE,2,3

Global level=LoadMesh("models/level/level.x")

ScaleEntity level,35,35,35
EntityType level,WALLTYPE
EntityPickMode level,2,1

c.cam=New cam

;EntityRadius c\ent,3
c\pent=CreateSphere(10)
c\ent=CreateCamera()
EntityRadius c\ent,10
EntityType c\ent,PLYTYPE

hw=True
HWMultiTex hw

CameraClsColor c\ent,0,0,155
c\gun=LoadMesh("models\pistol\pistol.x",c\ent)
Global gzz=EntityZ(c\gun)
c\gya=-180

RotateEntity c\gun,-180,180,0
PositionEntity c\gun,3,-3.5,4
PositionEntity c\pent,0,2,0
c\light=CreateLight(2,c\ent)
LightRange c\light,50


Dim snd(5)
snd(3)=LoadSound("sounds\jump1.wav")

te=LoadTexture("models\pistol\gun.png")
t2=LoadTexture("models\pistol\g1.png",67)
EntityTexture c\gun,te,0,0
EntityTexture c\gun,t2,0,1
UpdateNormals c\gun

TextureBlend te,1
TextureBlend t2,2

ScaleEntity c\pent,12,10,12
EntityType c\pent,PLYTYPE
EntityAlpha c\pent,0
FlipMesh c\pent
EntityRadius c\pent,10
c\jstrength=2/grav#

Global pcam=c\ent

Global sprite=InitSprite()

Global hit=CopyEntity(sprite)
Global sp=LoadAnimTexture("models/pistol/hit.png",1,1,1,0,6)
EntityFX hit,1
EntityTexture hit,sp
HideEntity hit

; MAIN LOOP
;#################################
While Not KeyDown(esc)

	ticks=WaitTimer(timer)
	For k=1 To ticks
		UpdateGame()
		UpdateWorld
	Next
	Cls
	RenderWorld
	RenderGame()
	fps = frames#/(MilliSecs()-init)*1000
	Flip : frames#=frames#+1
Wend

FreeFont font
FreeTimer(timer)
EndGraphics
End

Function UpdateGame()

	mx=MouseX()
	my=MouseY()
	mxs=MouseXSpeed()/(myspd)
	mys=MouseYSpeed()/(mxspd)

	For c.cam= Each cam
	
		ox#=EntityX(c\pent)
		oz#=EntityZ(c\pent)
		
		If c\zs<c\zo Then c\zs=c\zs+.1
		If c\zs>c\zo Then c\zs=c\zs-.1
		If c\zs<=0 Then c\zs=1
		If KeyDown(57) Then c\zo=2.5 Else c\zo=1
		CameraZoom c\ent,c\zs
	
		c\cy2=(EntityY(c\pent))-c\crouch*17
		c\cy= CurveValue(c\cy, c\cy2, 5)
		
		
		If c\gya<0
			RotateEntity c\gun,c\gya,180,c\gya
			c\gya=c\gya+2.5
		EndIf

		If KeyHit(4) Then c\gya=-180
		PositionEntity c\ent,EntityX(c\pent),c\cy+20,EntityZ(c\pent)
		If KeyDown(157) Or KeyDown(29) Then c\crouch=True Else c\crouch=False
		
		If KeyHit(2) Then c_id=c_id+1
		If c_id=>13 Then c_id=0
		
		If KeyHit(3) Then m_id=m_id+1
		If m_id=>4 Then m_id=0
		
		; ========== mlook
		
		If KeyHit(42) Or KeyHit(54) Then c\mmx=GraphicsWidth()/2:c\mmy=GraphicsHeight()/2:c\mlook=c\mlook+1
		If c\mlook=>2 Then c\mlook=0
		
		; ========== camera smooth	
		
		If c\mlook=1
		
			mx=GraphicsWidth()/2
			my=GraphicsHeight()/2
			
			c\ax = c\ax + mys;+(MouseDown(1)*Rand(-2,2))
			c\ay = c\ay - mxs
			
			If c\ax=>71 Then c\ax=70.99999999
			If c\ax=<-71 Then c\ax=-70.99999999
			
			MoveMouse c\mmx,c\mmy
			
			If MouseDown(2) And c\fall=False Then c\g=c\jstrength#:PlaySound snd(3)
			
			PositionEntity c\gun,EntityX(c\gun),EntityY(c\gun),c\gzz+3
			
			c\gtime=c\gtime+1
			
			If c\picked>0 And c\gtime=>3
				CreateShot(PickedX(),PickedY(),PickedZ(),1,2,True)
				CreateShot(PickedX(),PickedY(),PickedZ(),1,2,True)
				CreateShot(PickedX(),PickedY(),PickedZ(),1,2,True)
				CreateShot(PickedX(),PickedY(),PickedZ(),1,2,True)
				CreateShot(PickedX(),PickedY(),PickedZ(),3,0,False)
				c\picked=0
				c\gzz=Rnd(1)
				c\gtime=0
			Else
				c\gzz=gzz
				
			EndIf
			
			If MouseDown(1)
				c\picked=CameraPick(c\ent,mx+Rand(-5,5),my+Rand(-5,5))
			EndIf
				
		Else
			
			c\ax = c\ax - (KeyDown(up)-KeyDown(down))
			If c\ax=>71 Then c\ax=70.99999999
			If c\ax=<-71 Then c\ax=-70.99999999
			c\ay = c\ay - (KeyDown(Right)-KeyDown(Left))
			
		EndIf
		
		
		If KeyHit(2) Then ScaleEntity c\pent,12,30,12
		
		; 		===== Head bob =====
		; [variables]
		; 	c\type
		; 		g=gravity up/down
		; 		walk= which way your walking
		; 		head= which way your head should Zrotate
		; 		az= actual direction your head Zrotate
		; 		x,y,z=smoothed X,Y,Z
		;	gi=whether or not he rotates his head
		
		va=KeyDown(up)
		vb=KeyDown(down)
		vc=KeyDown(Left)
		vd=KeyDown(Right)
		If va Then c\walk=1:c\head=0:c\mm=1
		If vb Then c\walk=-1:c\head=0:c\mm=1
		If vc Then c\walk=0:c\head=2.9:c\mm=1
		If vd Then c\walk=0:c\head=-2.9:c\mm=1
		
		If va=0 And vb=0 And vc=0 And vd=0 Then c\walk=0:c\head=0:c\mm=0
		
		If c\g>2 Then c\walk=0
		
		c\az = 0
		
		If c\walk<>0
			c\az = c\az + ((Sin(MilliSecs())/16)*12)
		EndIf
		
		If c\head<>0
			c\az = c\az + ((Sin(MilliSecs())/16)*12)
		EndIf
		
		c\x = CurveValue(c\x + c\walk, c\ax, 5)
		c\y = CurveValue(c\y, c\ay, 5)
		
		gi=0
			
		c\z = CurveValue(c\z, c\head - (c\az), 5)
		
		;If c\head<>0 Then 
		RotateEntity c\ent, c\x, c\y, c\z
		
		;      ===== /Head bob
		
		; ========== /camera smooth
		
		c\fall=True
		
		For x=1 To CountCollisions(c\pent)
			If GetEntityType(CollisionEntity(c\pent,x))=WALLTYPE
				Select CollisionNY(c\pent,x)
					
					Case 1
					c\g=0
					c\fall=False
					
				End Select
				
				cye#=EntityY(c\ent)-CollisionY(c\pent,x)
				If cye>16 And cye<30
					MoveEntity c\pent,0,4,0
				EndIf
			EndIf
		Next
		
		c\move=True
		
		For x=1 To CountCollisions(c\ent)
			If GetEntityType(CollisionEntity(c\ent,x))=WALLTYPE
				c\move=False
				Select CollisionNY(c\ent,x)
					
					Case -1
					c\g=-9
					c\fall=True
					
				End Select
				
			EndIf
		Next

		If c\fall=True
			c\g=c\g-grav#
			c\spd=.5
		Else
			If Not c\crouch
				c\spd=5.4
			Else
				c\spd=2.0
			EndIf
			
			If c\mlook
				c\mx=(KeyDown(Right)-KeyDown(Left))*c\spd
				c\mz=(KeyDown(up)-KeyDown(down))*c\spd
			EndIf

			
		EndIf
		
	; Z Slide/Movement (up/down)

		If c\mz=0
			If c\zspd>0
				c\zspd=c\zspd - friction#
			ElseIf c\zspd<0
				c\zspd=c\zspd + friction#
			EndIf
			
			;If c\zspd>-1 And c\zspd<0.01 Then c\zspd=0
		EndIf
		
		If c\zspd>c\spd Then c\zspd=c\spd
		If c\zspd<-c\spd Then c\zspd=-c\spd
		
		c\zspd=c\zspd + c\mz
		zi#=CurveValue(zi,c\zspd,3)
		
	; X Slide/Movement (right/left)
	
		If c\mx=0
			If c\xspd>0
				c\xspd=c\xspd - friction#
			ElseIf c\xspd<0
				c\xspd=c\xspd + friction#
			EndIf
			
			If c\xspd>-1 And c\xspd<0.1 Then c\xspd=0
		EndIf
		
		If c\xspd>c\spd/2 Then c\xspd=c\spd/2
		If c\xspd<-c\spd/2 Then c\xspd=-c\spd/2	
		c\xspd=c\xspd + c\mx
		xi#=CurveValue(xi,c\xspd,3)
		
		RotateEntity c\pent, 0, c\y, 0
		RotateEntity c\light, EntityPitch(c\ent), EntityYaw(c\ent), 0
		;If Not c\move Then xi=0:zi=0
		MoveEntity c\pent,xi,c\g,zi
		

	Next
	
	For hi.h=Each h
		If hi\ji=1 And hi\spd>0 Then RotateEntity hi\ent,Rnd(360),Rnd(360),Rnd(360)
		If hi\time>1 Then PointEntity hi\ent,pcam
		hi\time=hi\time-.01
		EntityAlpha hi\ent,hi\time
		EntityTexture hi\ent,sp,Rnd(6)
		
		
		If hi\time>-1 And hi\time<1
			MoveEntity hi\ent,0,0,hi\spd
			hi\ji=hi\ji+1
			TranslateEntity hi\ent,0,hi\spd*.1,0
		EndIf
		
		If hi\time=<-1
			FreeEntity hi\ent
			Delete hi
		EndIf
	Next

End Function

Function RenderGame()

	Color 255,255,255
	c.cam=FindCam(pcam)
	
	Text 0,0,fps
	
	If c\mlook=0
		DrawImage c_gui,0,GraphicsHeight()-ImageHeight(c_gui)
		DrawImage cursor,mx-16,my-16,m_id+13
	Else
		DrawImage cursor,mx-16,my-16,c_id
	EndIf

End Function

Function Distance(scr,x2#,y2#,z2#)
	x1#=EntityX(scr)
	y1#=EntityY(scr)
	z1#=EntityZ(scr)
	Return Sqr((x1-x2)^2+(y1-y2)^2+(z1-z2)^2)
End Function

Function parse$(in$, delim$, position)
  out$ = ""
  max = Len(in$)
  count = 0
  If position = 0
    For x = 1 To max
      char$ = Mid$(in$, x, 1)
      If char$ <> delim$ 
        out$ = out$ + char$
      Else
        Exit
      EndIf
    Next
    Return out$
  Else
      For x = 1 To max
      char$ = Mid$(in$, x, 1)
      If count = position
        If char$ <> delim$ 
          out$ = out$ + char$
        Else
          Exit
        EndIf
      Else If char$ = delim$
        count = count + 1
      EndIf
    Next
    Return out$ 
  EndIf
End Function

Function CurveValue#(current#,destination#,curve)
	current#=current#+((destination#-current#)/curve)
	Return current#
End Function

Function FindCam.cam(ent)
	For c.cam= Each cam
		If c\ent=ent Then Return c.cam
	Next
End Function

Function nxv#(x#,d#,s#)
	Return -Sin(d#)*s#+x#
End Function

Function nyv#(y#,d#,s#)
	Return Sin(d#)*s#+y#
End Function

Function nzv#(z#,d#,s#)
	Return Cos(d#)*s#+z#
End Function

Function InitSprite()
	sprite=CreateMesh()
	he=CreateBrush(255,255,255)
	v=CreateSurface(sprite,he)
	FreeBrush he
	AddVertex ( v,-3,3,0,1,0)  ; top left 0,1;1,0
	AddVertex ( v,3,3,0,0,0)   ; top right 1,1;1,1
	AddVertex ( v,-3,-3,0,1,1) ; bottom left 0,0;,0,0
	AddVertex ( v,3,-3,0,0,1)  ; bottom right 1,0;0,1
	AddTriangle( v,0,1,2)
	;AddTriangle( v,3,2,1)
	FlipMesh(sprite)
	HideEntity(sprite)
	Return sprite
End Function

Function CreateShot(x#,y#,z#,time#,spd#,fx)
	hi.h= New h
	hi\spd=spd
	hi\ent=CopyEntity(hit)
	PositionEntity hi\ent,x,y,z
	PointEntity hi\ent,pcam
	EntityFX hi\ent,fx
	EntityAlpha hi\ent,.2
	If fx=0
		ScaleEntity hi\ent,.1,.1,1
	Else
		ScaleEntity hi\ent,.2,.2,1
	EndIf
		
	hi\time=time
End Function