Global info1$="Birds Demo, by Adam Gore"
Global info2$="A small Blitz3D compatibility test"
Global info3$="Spline data imported from Lightwave"

Include "../start.bb"

Include "KBSplines.bb"

Global gwidth = 640;800
Global gheight = 480;600
Const FPS = 30
Global fstep

;Graphics3D gwidth,gheight

period=1000/FPS
time=MilliSecs()-period

cmot.Motion = New motion
b2mot.Motion = New Motion
b1mot.Motion = New Motion

If Load_Motion( "Cam.bbm", cmot ) = False Then RuntimeError "Error loading file" : End 
If Load_Motion( "Bird1.bbm", b1mot ) = False Then RuntimeError "Error loading file" : End 
If Load_Motion( "Bird2.bbm", b2mot ) = False Then RuntimeError "Error loading file" : End 

camera=CreateCamera()
CameraRange camera,1,3000

AmbientLight 90,90,90
light_sun = CreateLight(1)
LightColor light_sun,200,200,100
RotateEntity light_sun,60,-90,0

mesh_canyon = LoadMesh( "Canyon.x" )
mesh_skybox = MakeSkyBox("Textures\sky") 
mesh_bird = LoadMD2("Bird.md2")
tex1 = LoadTexture( "Textures\Bird.bmp" )
EntityTexture mesh_bird,tex1
mesh_bird2 = CopyEntity( mesh_bird )

AnimateMD2 mesh_bird,1,2.5,0,31
AnimateMD2 mesh_bird2,1,2.5,0,31

Apply_Motion(cmot,0,camera)
Apply_Motion(b1mot,0,mesh_bird)
Apply_Motion(b2mot,0,mesh_bird2)
fstep = 1

While KeyHit(1)<>True
	
	Repeat
		elapsed=MilliSecs()-time
	Until elapsed
	
	ticks=elapsed/period
	tween#=Float(elapsed Mod period)/Float(period)
	
	For k=1 To ticks
		time=time+period
		If k=ticks Then CaptureWorld
	
		Apply_Motion(cmot,fstep,camera)
		Apply_Motion(b1mot,fstep,mesh_bird)
		Apply_Motion(b2mot,fstep,mesh_bird2)
		fstep = fstep + 1
		If fstep > cmot\nsteps Then fstep = 1

		PositionEntity mesh_skybox,EntityX(camera,1),EntityY(camera,1),EntityZ(camera,1)

		UpdateWorld
	
	Next
	
	RenderWorld tween

	Flip
	
Wend
End

Function MakeSkyBox( file$ )

	m=CreateMesh()
	;front face
	b=LoadBrush( file$+"_FR.bmp",49 )
	s=CreateSurface( m,b )
	AddVertex s,-1,+1,-1,0,0:AddVertex s,+1,+1,-1,1,0
	AddVertex s,+1,-1,-1,1,1:AddVertex s,-1,-1,-1,0,1
	AddTriangle s,0,1,2:AddTriangle s,0,2,3
	FreeBrush b
	;right face
	b=LoadBrush( file$+"_LF.bmp",49 )
	s=CreateSurface( m,b )
	AddVertex s,+1,+1,-1,0,0:AddVertex s,+1,+1,+1,1,0
	AddVertex s,+1,-1,+1,1,1:AddVertex s,+1,-1,-1,0,1
	AddTriangle s,0,1,2:AddTriangle s,0,2,3
	FreeBrush b
	;back face
	b=LoadBrush( file$+"_BK.bmp",49 )
	s=CreateSurface( m,b )
	AddVertex s,+1,+1,+1,0,0:AddVertex s,-1,+1,+1,1,0
	AddVertex s,-1,-1,+1,1,1:AddVertex s,+1,-1,+1,0,1
	AddTriangle s,0,1,2:AddTriangle s,0,2,3
	FreeBrush b
	;left face
	b=LoadBrush( file$+"_RT.bmp",49 )
	s=CreateSurface( m,b )
	AddVertex s,-1,+1,+1,0,0:AddVertex s,-1,+1,-1,1,0
	AddVertex s,-1,-1,-1,1,1:AddVertex s,-1,-1,+1,0,1
	AddTriangle s,0,1,2:AddTriangle s,0,2,3
	FreeBrush b
	;top face
	b=LoadBrush( file$+"_UP.bmp",49 )
	s=CreateSurface( m,b )
	AddVertex s,-1,+1,+1,0,1:AddVertex s,+1,+1,+1,0,0
	AddVertex s,+1,+1,-1,1,0:AddVertex s,-1,+1,-1,1,1
	AddTriangle s,0,1,2:AddTriangle s,0,2,3
	FreeBrush b

	ScaleMesh m,1700,1700,1700
	FlipMesh m
	EntityFX m,1
	Return m
	
End Function