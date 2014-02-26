
Global info1$="Lights demo"
Global info2$="Features directional, point and spot lights"

Include "../start.bb"

AmbientLight 0,0,0

sphere=CreateSphere(32)

camera=CreateCamera()
PositionEntity camera,0,0,-3

;directional light
light1=CreateLight( 1 )
TurnEntity light1,0,-30,0
LightColor light1,255,0,0
HideEntity light1

;point light
light2=CreateLight( 2 )
PositionEntity light2,5,0,-10
LightColor light2,0,255,0
LightRange light2,15
HideEntity light2

;spot light
light3=CreateLight( 3 )
PositionEntity light3,0,0,-10
LightColor light3,0,0,255
LightConeAngles light3,0,10
LightRange light3,15
HideEntity light3

on1=True
on2=True
on3=True

If on1 Then ShowEntity light1
If on2 Then ShowEntity light2
If on3 Then ShowEntity light3

While Not KeyHit(1)
	If KeyHit(59)
		on1=Not on1
		If on1 Then ShowEntity light1 Else HideEntity light1
	EndIf
	If KeyHit(60)
		on2=Not on2
		If on2 Then ShowEntity light2 Else HideEntity light2
	EndIf
	If KeyHit(61)
		on3=Not on3
		If on3 Then ShowEntity light3 Else HideEntity light3
	EndIf
	
	If on3
		RotateEntity light3,Sin(MilliSecs()*.07)*5,Sin(MilliSecs()*.05)*5,0
;		TurnEntity light3,0,1,0
	EndIf
	
	UpdateWorld
	RenderWorld
	
	Text 0,FontHeight()*0,"(F1) Light1="+on1
	Text 0,FontHeight()*1,"(F2) Light2="+on2
	Text 0,FontHeight()*2,"(F3) Light3="+on3
	Flip
Wend

End