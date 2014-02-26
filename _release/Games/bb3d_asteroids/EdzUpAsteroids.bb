;EdzUp Asteroids
;Written by EdzUp of Stonegoat Software

Graphics3D 640,480,16
SetBuffer BackBuffer()

SeedRnd MilliSecs()      ;setup random numbers

Global ArialLarge=LoadFont("arial",72,1,0,0)
Global ArialMid=LoadFont("arial",48,1,0,0)
Global Arial=LoadFont("arial",14,0,0,0)

Global Explode = LoadSound("Explode.wav")      ;Asteroid Explosion
Global ShipExplode = LoadSound("Explode1.wav") ;ship Explosion
Global UFOSound = LoadSound("ufo.wav")      ;UFO Engine sound
Global Lazer = LoadSound("Lasergun.wav")       ;Lazer Sound
Global PlayerHit = LoadSound("hit.wav")      ;Player ship hit

Global as#=0

Global UfoSoundChannel=-1 ;ufo sound channel ;)
SetFont Arial

Type AsteroidInfo
 Field x,y,z     ;Asteroid position in 3d world
 Field Model     ;handle to Asteroid model
 Field speed     ;speed asteroid moving
End Type

Type Shots
 Field x,y,z     ;position in 3d world
 Field Model     ;Shot Model
 Field Timer     ;Shot Timer
 Field Speed
End Type

Type UFOShots
 Field x,y,z     ;position in 3d world
 Field Model     ;Shot Model
 Field Timer     ;Shot Timer
 Field Speed
End Type

Type UFOInfo
 Field x,y,z     ;position in 3d world
 Field Model     ;UFO Model
End Type

Type Fragment    ;Explosion Fragment
 Field x,y,z     ;position in 3d world
 Field speed     
 Field timer
 Field model
End Type

Type Explode
 Field Model     ;Entity attached to it
 Field Size      ;current size
 Field mode      ;1 it increases in size
 Field timer     ;explosion timer
End Type

;************ TRACERS FLAME CODE **************
Global flam = LoadSprite("flame2.bmp")
ScaleSprite flam,50,50
HideEntity flam

Global flam2 = LoadSprite("flame3.bmp") ;UFO Shot
ScaleSprite flam2,50,50
HideEntity flam2

;**********************************************
Global B3DLogo = LoadImage("b3dlogo.png")
MidHandle B3DLogo
Global TitleTex = LoadImage("Title.png")
MidHandle TitleTex
;enemy UFO Mesh
Global UFOMesh=LoadMesh("UFO.3ds")
ScaleMesh UFOMesh,50,50,50
HideEntity UFOMesh

;Load Asteroid Model (Large)
Global LargeAsteroidMesh=LoadMesh("asteroid.3ds")
ScaleMesh LargeAsteroidMesh,60,60,60
HideEntity LargeAsteroidMesh

;Load Asteroid Model (medium)
Global MediumAsteroidMesh=LoadMesh("asteroid.3ds")
ScaleMesh MediumAsteroidMesh,30,30,30
HideEntity MediumAsteroidMesh

;Load Asteroid Model (small)
Global SmallAsteroidMesh=LoadMesh("asteroid.3ds")
ScaleMesh SmallAsteroidMesh,15,15,15
HideEntity SmallAsteroidMesh

;Load Player Model
Global PlayerMesh=LoadMesh("hfighter.3ds")
ScaleMesh PlayerMesh,1,1,1
HideEntity PlayerMesh

;create backdrop
Global Stary=CreateBrush()
BrushColor Stary,155,155,155
StarTex=LoadTexture("Stars.bmp")
ScaleTexture startex,.1,.1
BrushTexture Stary,StarTex
Global BackdropSphere=CreateSphere()
EntityTexture BackDropsphere,startex
HideEntity BackdropSphere

ScaleMesh BackdropSphere,-30000,-30000,-30000 ;negative value so player can be put inside
                                              ;and still see the texture ;) 

;setup entity types
Const PLAYER_TYPE =1
Const CAMERA_TYPE =2
Const UFOSHOT_TYPE =3
Const PLAYERSHOT_TYPE =5
Const UFO_TYPE =6
Const ASTEROID_TYPE =10
;setup collision system
;1  is player
;3  UFO Shots
;5  player shots
;6  is UFO's
;10 are asteroids
Collisions PLAYER_TYPE, ASTEROID_TYPE,1,2
Collisions CAMERA_TYPE, UFOSHOT_TYPE,1,2
Collisions PLAYERSHOT_TYPE, ASTEROID_TYPE,1,2
Collisions UFOSHOT_TYPE, PLAYER_TYPE,1,2
Collisions PLAYER_TYPE, UFOSHOT_TYPE,1,2
Collisions PLAYER_TYPE, UFO_TYPE,1,2
Collisions PLAYERSHOT_TYPE, UFO_TYPE,1,2

Light=CreateLight(1)

Global Pivot=CreatePivot()
EntityType Pivot,PLAYER_TYPE
EntityRadius pivot,7

;create camera 
Global camera=CreateCamera(pivot)       ;Or nothing will be drawn
CameraRange camera,1,45000              ;were in space ;)
;CameraFogRange camera,44500,45000
;CameraFogColor camera,0,0,0
EntityType camera,PLAYER_TYPE
EntityRadius camera,7

Global Backing=CopyEntity(BackDropsphere)
Global PlayShip=CopyEntity(PlayerMesh,0)

EntityType PlayShip,1
EntityRadius playship,150
PositionEntity PlayShip,EntityX#(pivot),EntityY#(pivot),EntityZ#(pivot)
RotateEntity PlayShip,EntityPitch#(pivot),EntityYaw#(pivot),EntityRoll#(pivot)
MoveEntity PlayShip,0,0,-3.5

.restart

;declare a few globals
Global UFOCount=0      ;number of UFO's in local area
Global PlayerScore=0
Global HighScore=10000
Global PlayerLives=3
Global PX,PY,PZ        ;player position
Global FX,FY,FZ        ;player facing
Global AsteroidCount=5 ;number of asteroids in this level
Global Level=1         ;level player is on
Global ExtraLife=10000 ;Extra Life at 10000 points
Global Damage=100      ;how many damage points player has
Global Speed=0         ;current players ship speed
Global Actual=0        ;Actual speed
Global Roll#=0         ;Ships Rolling 
Global Hits=0          ;Shots hit
Global Fired=0         ;Shots fired
Global Accuracy=0      ;Accuracy

Global Asteroids=0     ;Current Asteroid number

Global TextTimer=0     ;Text Timer for messages
Global BonusTextTimer=0;Timer for Bonuses
Global BonusText$=""   ;message
Global BonusTime=0     ;Timer for Bonus (0 means its run out ;) )
Global Bonus=0         ;Bonus Active
Global GodMode=0       ;1 if godmode active

Global ShotTime=0      ;Shot Timer
Global MaxTime=10      ;Max Shot Timer 
Global UFOShotTime=0   ;UFO Firing Timer
Global OldDamage=0     ;Old damage for God Mode


Global Endgame=0       ;1 if player has been killed (and all his lives have run out)

CameraClsColor camera,0,0,0   ;set camera refresh color to black

AmbientLight 255,255,255

Title()
CreateLevel(1)
texttimer=100
Repeat
 If GodMode=1 Then OldDamage=Damage
 Cls
 mxs=MouseXSpeed() ;get mouse speed
 mys=MouseYSpeed()
 MoveMouse 320,240             ;reset mouse to center of screen (so mousex speed doesnt stop)

 TurnEntity Pivot,mys,-mxs,0
 If KeyDown(200)=1 Then Speed=Speed+2  ;up
 If KeyDown(208)=1 Then Speed=Speed-2  ;down
 If KeyDown(203)=1 Then Roll#=Roll#-0.5;left
 If KeyDown(205)=1 Then Roll#=Roll#+0.5;right
 If Speed<0 Then Speed=0
 If Speed>100 Then Speed=100
 If Actual>Speed Then Actual=Actual-1
 If Actual<Speed Then Actual=Actual+1
 MoveEntity pivot,0,0,Actual
 TurnEntity pivot,0,0,Roll#
 PositionEntity PlayShip,EntityX#(pivot),EntityY#(pivot),EntityZ#(pivot)
 RotateEntity PlayShip,EntityPitch#(pivot),EntityYaw#(pivot),EntityRoll#(pivot)
 MoveEntity PlayShip,0,0,-3.5
 If MouseDown(1)=1 And shottime<=0 Then FireShot(Pivot)
 If ShotTime>0 Then ShotTime=ShotTime-1
 Roll=0
 UpdateLevel()
 updateExplosions()
 UFOAppear=Rnd(100000)
 If UFOAppear<50
  PlaceUFO()
 EndIf
 If Damage<1
  PlaySound shipexplode
  ClearLevel()
  CreateLevel(Level)
  PositionEntity camera,0,0,0
  texttimer=100
  Damage=100
  PlayerLives=PlayerLives-1
 EndIf
 If PlayerLives<0 And texttime<1
  Repeat
  Until MouseDown(1)=0 And MouseDown(2)=0
  ClearLevel()
  Goto restart
 EndIf
 If PlayerLives<0
  Endgame=1
  texttime=200
 EndIf
 If Asteroids<1
  EndOfLevelAnim()
  AccuracyBonus()
  ClearLevel()
  Level=Level+1
  CreateLevel(Level)
  PositionEntity camera,0,0,0
  texttimer=100 
 EndIf
 PositionEntity PlayShip,EntityX#(pivot),EntityY#(pivot),EntityZ#(pivot)
 RotateEntity PlayShip,EntityPitch#(pivot),EntityYaw#(pivot),EntityRoll#(pivot)
 MoveEntity PlayShip,0,0,-4.5
 UpdateWorld
 RenderWorld
 If Texttimer>0
  SetFont ariallarge 
  Color 255,255,255
  If endgame=0 Then Text 320,240,"LEVEL "+Level,1,1
  If endgame=1 Then Text 320,240,"GAME OVER",1,1
  SetFont arial
  TextTimer=TextTimer-1
 EndIf
 If BonusTexttimer>0
  SetFont arialMid 
  Color 255,255,255
  Text 320,420,BonusText$,1,1
  SetFont arial
  BonusTextTimer=BonusTextTimer-1
 EndIf
 BonusTime=BonusTime-5
 If BonusTime<1
  MaxTime=10
  GodMode=0
  BonusTime=-2
 EndIf
 If PlayerScore>=HighScore
  HighScore=PlayerScore
  SetFont arial
  Text 320,60,"YOU HAVE A HIGH SCORE",1,1
 EndIf
 If PlayerScore>ExtraLife
  BonusText$="Extra Life"
  BonusTextTimer=100
  PlayerLives=PlayerLives+1
  ExtraLife=ExtraLife*2
 EndIf
 ShowStats()
 Flip
 TestCollisions()
 If GodMode=1 Then Damage=OldDamage
Until KeyDown(1)=1
End

Function AccuracyBonus()
 a=0
 If (Hits>Fired/4)*3 Then a=75
 If Hits=Fired Then a=100
 Select a
 Case 100:BonusText$="Accuracy Bonus"
          BonusTextTimer=100
          PlayerScore=PlayerScore+1000
 Case 75 :BonusText$="Accuracy Bonus"
          BonusTextTimer=100
          PlayerScore=PlayerScore+500
 End Select
 Hits=0
 Fired=0
 Accuracy=-1
End Function

Function PlaceUFO()
 UFO.UFOInfo = New UFOInfo
 UFO\x=Rnd(-10000,10000)
 UFO\y=Rnd(-10000,10000)
 UFO\z=-10000
 UFO\Model=CopyEntity(UFOMesh,0)
 EntityType UFO\Model,6  ;Set UFO Type to 6
 EntityRadius UFO\Model,150
 PositionEntity UFO\Model,UFO\x,UFO\y,UFO\z
 UFOCount=UFOCount+1
End Function

Function explosion(entity)
 Boom.Explode = New explode
 Boom\Model = CopyEntity(flam)
 PositionEntity boom\Model,EntityX#(entity),EntityY#(entity),EntityZ#(entity)
 PointEntity Boom\Model,pivot
 Boom\Size=5
 Boom\Mode=1
 Boom\Timer=0

 Fragments=Rnd(20)+10
 For a=0 To Fragments
  Frag.Fragment = New Fragment
  
  Frag\Model = CopyEntity(Flam)
  PositionEntity Frag\Model,EntityX#(entity),EntityY#(entity),EntityZ#(entity)
  TurnEntity Frag\Model,Rnd(360),Rnd(360),Rnd(360)
  Frag\Speed = (Rnd(5)+1)*5
  Frag\Timer = 0
 Next
End Function

Function UpdateExplosions()
 For Boom.Explode=Each explode
  If Boom<>Null
   If Boom\mode=1 
    Boom\Size=5+(Boom\Timer)
   Else
    Boom\Size=5+(25-(Boom\Timer-25))
   EndIf
   ScaleSprite Boom\Model,Boom\size*25,Boom\size*25
   Boom\Timer=Boom\Timer+1
   If Boom\Timer>25 Then Boom\Mode=0
   If Boom\Timer>50
    FreeEntity Boom\Model
    Delete Boom
   EndIf
  EndIf
 Next 

 For Frag.Fragment = Each fragment
  If Frag<>Null
   MoveEntity Frag\Model,0,0,Frag\Speed
   Frag\Timer = Frag\Timer + 1
   If Frag\Timer>50
    FreeEntity Frag\Model
    Delete Frag
   EndIf
  EndIf
 Next
End Function

Function UFOFire(entity)
 ;ufo firing function
 PlaySound Lazer
 UFOShot.UFOShots = New UFOShots ;create another Shot
 UFOShot\Model = CopyEntity(flam2)
 EntityType UFOShot\Model,3
 UFOShot\Speed=50
 PositionEntity UFOShot\Model,EntityX#(entity),EntityY#(entity),EntityZ#(entity)
 TurnEntity UFOShot\Model,EntityPitch#(entity),EntityYaw#(entity),EntityRoll#(entity)
 PointEntity UFOShot\Model,Pivot ;make shot face player (so shot moves towards player)
End Function

Function EndOfLevelAnim()
 ;remember old cam position
 ox#=EntityX#(pivot)
 oy#=EntityY#(pivot)
 oz#=EntityZ#(pivot)
 ofx#=EntityPitch#(pivot)
 ofy#=EntityYaw#(pivot)
 ofz#=EntityRoll#(pivot)

 ;create a player ship
 Ship=CopyEntity(PlayerMesh,0)
 PositionEntity Ship,0,0,0
 PositionEntity pivot,250,120,250 ;position camera to get a good view ;)
 PointEntity pivot,ship           ;point camera (attached to pivot) at ship
 ScaleEntity ship,3,3,3           ;scale ship so it looks better
 timer=50
 xm=0
 ;move ship
 Repeat
  MoveEntity ship,0,0,1
  UpdateWorld
  RenderWorld
  Flip
  timer=timer-1
  If KeyDown(1)=1 Then End
 Until timer<1
 timer=25
 ScaleEntity ship,3,3,12           ;scale ship to make it look like its jumping
 ;move ship forwards
 Repeat
  MoveEntity ship,0,0,-50
  UpdateWorld
  RenderWorld
  Flip
  timer=timer-1
  If KeyDown(1)=1 Then End
 Until timer<1
 jumpstar=CopyEntity(flam2)
 PositionEntity jumpstar,EntityX#(ship),EntityY#(ship),EntityZ#(ship)
 FreeEntity ship
 For a=1 To 10
  ScaleSprite jumpstar,50*a,50*a
  UpdateWorld
  RenderWorld
  Flip
 Next
 For a=10 To 1 Step -1
  ScaleSprite jumpstar,50*a,50*a
  UpdateWorld
  RenderWorld
  Flip
 Next
 FreeEntity jumpstar
 UpdateWorld
 RenderWorld
 Flip
 ;restore camera position  (attached to the pivot)
 PositionEntity pivot,ox#,oy#,oz#
 RotateEntity pivot,ofx#,ofy#,ofz#
End Function

Function FireShot(entity)
 PlaySound Lazer
 Shot.Shots = New Shots ;create another Shot
 Shot\Model = CopyEntity(flam) ;copy Tracers Sprite Entity ;)
 EntityType Shot\Model,5
 PositionEntity Shot\Model,EntityX#(Pivot),EntityY#(Pivot),EntityZ#(Pivot)
 TurnEntity Shot\Model,EntityPitch#(Pivot),EntityYaw#(Pivot),EntityRoll#(Pivot)
 Shot\Speed = 50+(Actual+5)
 ShotTime=MaxTime
 Fired=Fired+1
End Function

Function NewAsteroid(Entity,speed)
 ;when something hits an asteroid this function creates new asteroids
 If speed=5 Then count=Rnd(3,5)
 If speed=10 Then count=Rnd(2,4)
 Asteroids=Asteroids+count
 For a=1 To count
  Asteroid.AsteroidInfo=New AsteroidInfo
  Asteroid\x=EntityX#(entity)
  Asteroid\y=EntityY#(entity)
  Asteroid\z=EntityZ#(entity)
  If speed=5 Then Asteroid\Speed=10
  If speed=10 Then Asteroid\Speed=20
  If speed=5 Then Asteroid\Model=CopyEntity(MediumAsteroidMesh,0)
  If speed=10 Then Asteroid\Model=CopyEntity(SmallAsteroidMesh,0)
  If speed=5 Then EntityRadius Asteroid\Model,200
  If speed=10 Then EntityRadius Asteroid\Model,150
  If speed=5 Or speed=10
   PositionEntity Asteroid\Model,Asteroid\x,Asteroid\y,Asteroid\z
   RotateEntity Asteroid\Model,Rnd(360),Rnd(360),Rnd(360)
   EntityType Asteroid\Model,10
  Else
   FreeEntity Asteroid\Model
   Delete Asteroid
   Asteroids=Asteroids-1
  EndIf
 Next 
 FreeEntity entity
 Asteroids=Asteroids-1
End Function

Function TestCollisions()
 ;function to check collisions
 For Asteroid.AsteroidInfo=Each AsteroidInfo
  If Asteroid<>Null 
   ;if player has collided with asteroid
   If EntityCollided(Asteroid\Model,1)
    Actual=0
    If speed>0 
     damage=damage-(speed/2)
    Else
     damage=damage-5
    EndIf
    Speed=0
   EndIf
   If EntityCollided(Asteroid\Model,5)  ;if asteroid and player shot collides
    Hits=Hits+1
    PlaySound Explode
    Explosion(Asteroid\Model)
    If asteroid\Speed=5 Or asteroid\speed=10 Then NewAsteroid(Asteroid\Model,Asteroid\Speed)   
    If asteroid\speed=5 Then PlayerScore=PlayerScore+10
    If asteroid\speed=10 Then PlayerScore=PlayerScore+20
    If asteroid\Speed=20 
     PlayerScore=PlayerScore+50
     FreeEntity Asteroid\Model
     Asteroids=Asteroids-1
    EndIf
    Delete Asteroid
   EndIf
  EndIf
 Next

 For UFO.UFOInfo = Each UFOInfo
  If UFO<>Null
   If EntityCollided(Ufo\Model,1)
    Actual=0
    Speed=0
   EndIf
   If EntityCollided(Ufo\Model,5)
    Hits=Hits+1
    PlaySound ShipExplode
    Explosion(UFO\Model)
    PlayerScore=PlayerScore+200
    Bonus=Rnd(50)
    If Bonus<10
     BonusTextTimer=100
     Bonus=Rnd(7)
     If Bonus<0 Then Bonus=0
     If Bonus>5 Then Bonus=5
     If Bonus=0
      BonusText$="Rapid Fire"
      MaxTime=1
      BonusTime=10000
     EndIf
     If Bonus=1
      BonusText$="Extra Life"
      PlayerLives=PlayerLives+1
      BonusTime=0
     EndIf
     If Bonus=2
      BonusText$="Full Repair"
      Damage=100
      OldDamage=100
      BonusTime=0
     EndIf
     If Bonus=3
      BonusText$="Extra Score"
      PlayerScore=PlayerScore+1000
      BonusTime=0
     EndIf
     If Bonus=4
      BonusText$="Level Skip"
      Asteroids=0
      BonusTime=0
     EndIf
     If Bonus=5
      BonusText$="God Mode"
      GodMode=1
      BonusTime=10000
      OldDamage=Damage
     EndIf
    EndIf
    UFOCount=UFOCount-1
    FreeEntity UFO\Model
    Delete UFO
   EndIf
  EndIf
 Next

 ;ufo shots collide with player (type 1) damageing them
 For UFOShot.UFOShots = Each UFOShots
  If UFOShot<>Null
   If EntityCollided(UFOShot\Model,1)  ;if shot hits pivot
    PlaySound PlayerHit
    Damage=Damage-10
    FreeEntity UFOShot\Model
    Delete UFOShot
   EndIf 
   If UFOShot<>Null
    If EntityCollided(UFOShot\Model,2)  ;if shot hits cam
     PlaySound PlayerHit
     Damage=Damage-10
     FreeEntity UFOShot\Model
     Delete UFOShot
    EndIf 
   EndIf
  EndIf
 Next

 For Shot.Shots = Each Shots
  If Shot<>Null
   del=0
   If EntityCollided(Shot\Model,10)
    del=1
   EndIf 
   If EntityCollided(Shot\Model,6)
    del=1
   EndIf 
   If del=1
	FreeEntity Shot\Model
	Delete Shot
   EndIf
  EndIf
 Next
End Function

Function ShowStats()
 ;player information
 Color 255,255,255
 SetFont Arialmid
 Text 320,20,PlayerScore,1,1
 SetFont arial
 If PlayerLives>-1 
  Text 10,10,"LIVES "+PlayerLives,0,0
 Else
  Text 10,10,"LIVES 0"
 EndIf
 If BonusTime>0
  Rect 519,9,102,22,0
  Color 255,0,0
  Rect 520,10,BonusTime/100,20,1
  Color 255,255,255
  Text 570,20,"BONUS",1,1
 EndIf
 If UFOCount>0
  Color 255,0,0
  Text 320,280,"-UFO ALERT-",1,1
 EndIf
 Color 255,255,255
 Rect 9,449,102,22,0
 Color 0,255,0
 Rect 10,450,Damage,20,1
 Color 255,0,0
 Text 60,460,"DAMAGE",1,1
 Color 255,255,255
 Rect 519,449,102,22,0
 Color 0,255,0
 If speed>0 Then Rect 520,450,Speed,10,1
 Color 0,0,255
 If Actual>0 Then Rect 520,460,Actual,10,1
 Color 255,0,0
 Text 570,460,"THRUST",1,1
 Color 255,255,255
 Line 310,240,315,240
 Line 325,240,330,240
 Line 320,230,320,235
 Line 320,245,320,250
End Function

Function Title()
 Repeat
 Until MouseDown(1)=0 And MouseDown(2)=0
 CreateLevel(20) 
 While MouseDown(1)=0 And MouseDown(2)=0
  Cls
  PositionEntity backing,0,0,0
  UpdateLevel()
  di=0
  If KeyDown(1)=1 Then End
  UpdateWorld
  RenderWorld
  SetFont ArialLarge
  DrawImage TitleTex,320,260
  Color 0,0,0
  Rect 120,30,400,180
  DrawImage B3dLogo,320,120
  SetFont Arial
  Color 255,255,255
  Text 320,10,"Hi Score : "+HighScore,1,1
  Text 320,300,"Coded by EdzUp of Stonegoat Software",1,1
  Text 320,320,"Controls are as follows:",1
  Text 320,340,"Mouse - Move around",1
  Text 320,360,"Cursor Up - Speed Up",1
  Text 320,380,"Cursor Down - Slow Down",1
  Text 320,400,"Mouse Left Button - Fire weapons",1
  Text 320,460,"Click mouse to start game",1,1
  Flip
 Wend
 Repeat
 Until MouseDown(1)=0 And MouseDown(2)=0
 ClearLevel()
End Function

Function ClearLevel()
 Hits=0
 Fired=0
 Accuracy=0
 UFOCount=0
 ;clear out old level info
 ;Free up all the entities and types allocated (so level can restart afresh)
 For Asteroid.AsteroidInfo = Each AsteroidInfo
  If Asteroid<>Null
   FreeEntity Asteroid\Model
   Delete Asteroid
  EndIf
 Next
 For Shot.Shots = Each shots
  If Shot<>Null 
   FreeEntity shot\model
   Delete shot
  EndIf
 Next
 For UFOShot.UFOShots = Each UFOshots
  If UFOShot<>Null 
   FreeEntity UFOShot\model
   Delete UFOShot
  EndIf
 Next
 For Frag.Fragment = Each Fragment
  If Frag<>Null 
   FreeEntity frag\model
   Delete Frag
  EndIf
 Next
 For UFO.UFOinfo = Each UFOInfo
  If UFO<>Null
   FreeEntity UFO\Model 
   Delete UFO
  EndIf
 Next
 For Boom.Explode = Each Explode
  If Boom<>Null
   FreeEntity Boom\Model
   Delete Boom
  EndIf
 Next
End Function

Function fade(model,val#)
 vl#=val
 as#=((vl#+0.001)-9000)/1000
 ax=-1
 For asteroid.asteroidinfo = Each asteroidinfo
  ax=ax+1
  If ax=model Then EntityAlpha asteroid\model,1-as#
 Next
End Function

Function UpdateLevel()
 si=-1
 For Asteroid.AsteroidInfo = Each AsteroidInfo
  si=si+1
  MoveEntity Asteroid\Model,0,0,Asteroid\Speed
  x#=EntityX#(asteroid\Model)
  y#=EntityY#(asteroid\Model)
  z#=EntityZ#(asteroid\Model)
  min=0
  max=0
  If x#<0 Then min=x#
  If y#<0 And y#<min Then min=y#
  If z#<0 And z#<min Then min=z#
  If x#>0 Then max=x#
  If y#>0 And y#>max Then max=y#
  If z#>0 And z#>max Then max=z#
  If max>Abs(min)
   a=max
   fade(si,a)
;   If a>9000 Then EntityAlpha Asteroid\Model,.9
;   If a>9100 Then EntityAlpha Asteroid\Model,.8
;   If a>9200 Then EntityAlpha Asteroid\Model,.7
;   If a>9300 Then EntityAlpha Asteroid\Model,.6
;   If a>9400 Then EntityAlpha Asteroid\Model,.5
;   If a>9500 Then EntityAlpha Asteroid\Model,.4
;   If a>9600 Then EntityAlpha Asteroid\Model,.3
;   If a>9700 Then EntityAlpha Asteroid\Model,.2
;   If a>9800 Then EntityAlpha Asteroid\Model,.1
;   If a>9900 Then EntityAlpha Asteroid\Model,.0
  Else
   a=Abs(min)
   fade(si,a)
;   If a>9000 Then EntityAlpha Asteroid\Model,.9
;   If a>9100 Then EntityAlpha Asteroid\Model,.8
;   If a>9200 Then EntityAlpha Asteroid\Model,.7
;   If a>9300 Then EntityAlpha Asteroid\Model,.6
;   If a>9400 Then EntityAlpha Asteroid\Model,.5
;   If a>9500 Then EntityAlpha Asteroid\Model,.4
;   If a>9600 Then EntityAlpha Asteroid\Model,.3
;   If a>9700 Then EntityAlpha Asteroid\Model,.2
;   If a>9800 Then EntityAlpha Asteroid\Model,.1
;   If a>9900 Then EntityAlpha Asteroid\Model,.0
  EndIf
  If EntityX#(Asteroid\Model)>10000
   PositionEntity Asteroid\Model,-10000,EntityY#(Asteroid\Model),EntityZ#(Asteroid\Model)
  EndIf
  If EntityX#(Asteroid\Model)<-10000
   PositionEntity Asteroid\Model,10000,EntityY#(Asteroid\Model),EntityZ#(Asteroid\Model)
  EndIf
  If EntityY#(Asteroid\Model)>10000
   PositionEntity Asteroid\Model,EntityX#(Asteroid\Model),-10000,EntityZ#(Asteroid\Model)
  EndIf
  If EntityY#(Asteroid\Model)<-10000
   PositionEntity Asteroid\Model,EntityX#(Asteroid\Model),10000,EntityZ#(Asteroid\Model)
  EndIf
  If EntityZ#(Asteroid\Model)>10000
   PositionEntity Asteroid\Model,EntityX#(Asteroid\Model),EntityY#(Asteroid\Model),-10000
  EndIf
  If EntityZ#(Asteroid\Model)<-10000
   PositionEntity Asteroid\Model,EntityX#(Asteroid\Model),EntityY#(Asteroid\Model),10000
  EndIf
 Next
 For Shot.Shots=Each Shots
  If Shot<>Null
   MoveEntity Shot\Model,0,0,Shot\Speed
   Shot\Timer=Shot\Timer+50
   del=0
   If Shot\Timer>10000

  FreeEntity Shot\Model
    Del=1
   EndIf   
   If del=1 Then Delete Shot   
  EndIf
 Next
 For UFOShot.UFOShots=Each UFOShots
  If UFOShot<>Null
   MoveEntity UFOShot\Model,0,0,UFOShot\Speed
   UFOShot\Timer=UFOShot\Timer+10
   del=0
   If UFOShot\Timer>40000

   FreeEntity UFOShot\Model
    Del=1
   EndIf   
   If del=1 Then Delete UFOShot   
  EndIf
 Next
 For UFO.UFOInfo = Each UFOInfo
  If UFO<>Null
   If UFOSoundChannel=-1 Then UFOSoundChannel=PlaySound(UFOSound)
   If UFOSoundChannel<>-1 And ChannelPlaying(UFOSoundChannel)=0 
    UFOSoundChannel=PlaySound(UFOSound)
   EndIf
   MoveEntity UFO\Model,0,0,20

  UFOFire=Rnd(1000)
   If UFOFire<20
    UFOFire(UFO\Model)
   EndIf
   If EntityZ#(UFO\Model)>10000
    UFOCount=UFOCount-1
    FreeEntity UFO\Model
    Delete UFO
   EndIf
  EndIf
 Next
End Function

Function CreateLevel(Lev)
 AsteroidCount=1+(Lev-1)
 If AsteroidCount>20 Then AsteroidCount=20
 Asteroids=AsteroidCount
 For a=1 To AsteroidCount
  Asteroid.AsteroidInfo = New AsteroidInfo
  Asteroid\Model=CopyEntity(LargeAsteroidMesh,0)      ;create asteroid
  EntityRadius Asteroid\Model,450
  Repeat
   Asteroid\x=Rnd(-10000,10000)                      ;position asteroid
   Asteroid\y=Rnd(-10000,10000)
   Asteroid\z=Rnd(-10000,10000)
   ok=0 
   If Asteroid\x>EntityX(Camera)-1000 And asteroid\x<EntityX(Camera)+1000 Then ok=1 ;make sure asteroid wont be where player
   If Asteroid\y>EntityY(Camera)-1000 And asteroid\y<EntityY(Camera)+1000 Then ok=1 ;starts on level
   If Asteroid\z>EntityZ(Camera)-1000 And asteroid\z<EntityZ(Camera)+1000 Then ok=1
  Until ok=0
  PositionEntity Asteroid\Model,Asteroid\x,Asteroid\y,Asteroid\z
  RotateEntity Asteroid\Model,Rnd(360),Rnd(360),Rnd(360)
  EntityType Asteroid\Model,10
  Asteroid\speed=5 ;slow asteroid
 Next
End Function