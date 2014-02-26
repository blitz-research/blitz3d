; Lighting ***
; By Paul Gerfen (www.gamecoding.co.uk)

Graphics3D 800,600

SetBuffer BackBuffer()

camera=CreateCamera()
CameraViewport camera,0,0,800,600

AmbientLight 0,0,0

;cube 1
cube=CreateCube()
PositionEntity cube,0,0,5

light=CreateLight(3)
LightColor light,100,20,30
LightConeAngles light,0,45
PositionEntity light,0,0,0.5
LightRange light,8
PointEntity light,cube

;cube 2
cube2=CopyEntity(cube)
PositionEntity cube2,-5,0,8

light2=CreateLight(3)
LightColor light2,40,150,60
LightConeAngles light,0,45
PositionEntity light2,-5,0,4.5
LightRange light2,8
PointEntity light2,cube2

;cube 3
cube3=CopyEntity(cube)
PositionEntity cube3,5,0,8

light3=CreateLight(3)
LightColor light3,70,80,190
LightConeAngles light,0,45
PositionEntity light3,5,0,4.5
LightRange light3,8
PointEntity light3,cube3


While Not KeyHit(1)

	TurnEntity cube,0.1,0.2,0.3
	TurnEntity cube2,0.3,0.2,0.1
	TurnEntity cube3,0.3,0.2,0.1
	
	UpdateWorld
	RenderWorld
	
	Text 310,500,"Coloured Lighting Demo"
	
	Flip

Wend
End