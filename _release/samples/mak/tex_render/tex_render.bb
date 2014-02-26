
Global info1$="Texture render demo"
Global info2$="Renders a 3D scene onto a texture"
Global info3$="Use Arrow keys to Pan, A/Z to zoom"

Include "../start.bb"

grid_tex=CreateTexture( 16,16,8,1 )
ScaleTexture grid_tex,10,10
SetBuffer TextureBuffer( grid_tex )
ClsColor 255,255,255:Cls:ClsColor 0,0,0
Color 192,192,192:Rect 0,0,8,8:Rect 8,8,8,8
SetBuffer BackBuffer()

plane=CreatePlane()
EntityTexture plane,grid_tex

pivot=CreatePivot()
PositionEntity pivot,0,2,0

t_sphere=CreateSphere( 8 )
EntityShininess t_sphere,.2
For t=0 To 359 Step 36
	sphere=CopyEntity( t_sphere,pivot )
	EntityColor sphere,Rnd(256),Rnd(256),Rnd(256)
	TurnEntity sphere,0,t,0
	MoveEntity sphere,0,0,10
Next
FreeEntity t_sphere

texture=CreateTexture( 128,128 )

cube=CreateCube()
EntityTexture cube,texture
PositionEntity cube,0,7,0
ScaleEntity cube,3,3,3

light=CreateLight()
TurnEntity light,45,45,0

camera=CreateCamera()

plan_cam=CreateCamera()
TurnEntity plan_cam,90,0,0
PositionEntity plan_cam,0,20,0
CameraViewport plan_cam,0,0,128,128
CameraClsColor plan_cam,0,128,0

d#=-20

While Not KeyHit(1)

	If KeyDown(30) d=d+1
	If KeyDown(44) d=d-1
	If KeyDown(203) TurnEntity camera,0,-3,0
	If KeyDown(205) TurnEntity camera,0,+3,0
	
	PositionEntity camera,0,7,0
	MoveEntity camera,0,0,d
	
	TurnEntity cube,.1,.2,.3
	TurnEntity pivot,0,1,0
	
	UpdateWorld
	
	HideEntity camera
	ShowEntity plan_cam
	RenderWorld
	
	CopyRect 0,0,128,128,0,0,0,TextureBuffer( texture )
	
	ShowEntity camera
	HideEntity plan_cam
	RenderWorld
	
	Flip
Wend