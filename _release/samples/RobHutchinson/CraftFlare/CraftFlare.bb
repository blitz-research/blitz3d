;
; Small SpaceCraft and flame. Code, Textures and Models By Rob Hutchinson.
; loki.sd@cableinet.co.uk
;
; CONTROLS:
; LMB + Up/Down = Zoom in/out
; Move Mouse    = Rotate
;
; LAST UPDATED: 18/08/2001
;

Global obfile$ = "craft1.x"

; Sorry about this, switch screen mode if it doesn`t work..
; But this was only a small test.
Include "start.bb"

Global light=CreateLight()
LightColor light,130,130,130
TurnEntity light,45,45,0

Global camera=CreateCamera()
CameraViewport camera,0,0,1024,768

Global shippivot = CreatePivot()
Global shipobj   = LoadMesh(obfile$,shippivot)
ScaleEntity shipobj,.3,.3,.3


Global flameobj1 = LoadSprite("Flame1.bmp",1,shippivot)
ScaleSprite flameobj1,2.5,2.5
MoveEntity flameobj1,0,0,-7
EntityAlpha flameobj1,.3
HideEntity flameobj1
Global flameobj2 = LoadSprite("Flame2.bmp",1,shippivot)
ScaleSprite flameobj2,2.5,2.5
MoveEntity flameobj2,0,0,-7
EntityAlpha flameobj2,.3
HideEntity flameobj2
;flameob2  = LoadSprite("Flame2.bmp")

Global pointflameent = CopyEntity(flameobj1,shippivot)
MoveEntity pointflameent,0,0,-50
HideEntity pointflameent

Type ship_flame
	Field obj,speed#,count#,untildist#,prescale#,scaledown#,alphadown#,alphacn#
End Type

MoveEntity camera,0,0,-60
Global x_speed#,y_speed#,y_zoom#

Global k
Global newflameAfterFrames = 0
Global flameCountFrame = 0

While Not KeyHit(1)
	
	If MouseDown(1)
		y_zoom=(MouseYSpeed()-y_zoom)/13+y_zoom
		x_speed=(0-x_speed)/13+x_speed
		y_speed=(0-y_speed)/13+y_speed

	Else
		x_speed=(MouseXSpeed()-x_speed)/13+x_speed
		y_speed=(MouseYSpeed()-y_speed)/13+y_speed
		y_zoom=(0-y_zoom)/13+y_zoom
	
	EndIf
	TurnEntity shippivot,0,-x_speed,0
	TurnEntity shippivot,-y_speed,0,0
	MoveEntity camera,0,0,y_zoom
	MoveMouse GraphicsWidth()/2,GraphicsHeight()/2

	DoFlame()
	
	flameCountFrame = flameCountFrame + 1
	If flameCountFrame > newflameAfterFrames
		j = Rnd(100)
		If j > 50
			CreateNewFlame(flameobj1,0.8,20,6)
		Else
			CreateNewFlame(flameobj2,0.8,20,6)
		EndIf
		flameCountFrame = 0
	EndIf

	UpdateWorld
	RenderWorld
	Text 0,0,"Craft Flare: [By Rob Hutchinson 2001]"
	k = 0
	Flip 

Wend

End

Function CreateNewFlame(object,speed#,untildist#,scale#)
	n.ship_flame = New ship_flame
	n\obj = CopyEntity(object)
	RotateEntity n\obj,Rnd(0,360),Rnd(0,360),Rnd(0,360)
	PointEntity    n\obj,pointflameent
	MoveEntity n\obj,0,0,7
	rn1# = Rnd(-.5,.5)
	rn2# = Rnd(-.5,.5)
	MoveEntity n\obj,rn1,rn2,0
	ShowEntity n\obj
	If scale <> 1 Then ScaleSprite n\obj,scale,scale
	n\speed = speed
	n\untildist = untildist
	n\prescale = (n\speed / n\untildist)
	n\scaledown = scale
	n\alphacn   = 1.0 / (n\untildist / n\speed)
	n\alphadown = 1
End Function

Function DoFlame()
	For n.ship_flame = Each ship_flame
		MoveEntity  n\obj,0,0,n\speed
		n\scaledown = n\scaledown - (n\prescale*4)
		ScaleSprite n\obj,n\scaledown#,n\scaledown#
		n\alphadown = n\alphadown - n\alphacn
		EntityAlpha n\obj,n\alphadown
		
		n\count = n\count + n\speed
		If n\count > n\untildist
			FreeEntity n\obj
			Delete n
		EndIf
		k = k + 1
	Next
End Function