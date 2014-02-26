Graphics3D 800,600

Const CUBE_COL=1
Const SPHERE_COL=2

SetBuffer BackBuffer()

camera=CreateCamera()
CameraViewport camera,0,0,800,600
PositionEntity camera,0,0,-5

light=CreateLight()

cube=CreateCube()
PositionEntity cube,-5,0,5
EntityColor cube,70,80,190
EntityType cube,CUBE_COL

sphere=CreateSphere(12)
PositionEntity sphere,5,0,5
EntityColor sphere,170,80,90
EntityType sphere,SPHERE_COL

Collisions SPHERE_COL,CUBE_COL,3,1

While Not KeyHit(1)
	
	MoveEntity sphere,-0.02,0,0
	
	UpdateWorld
	RenderWorld
	
	If EntityCollided(sphere,CUBE_COL)
	Text 370,80,"Collided !!!"
	EndIf

	Text 335,500,"Collision Detection"
	
	Flip

Wend
End