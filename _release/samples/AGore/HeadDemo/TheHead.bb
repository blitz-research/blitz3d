Global info1$="The Head Demo, by Adam Gore"

Include "../start.bb"
Include "KBSplines.bb"

Const FPS = 24
Global fstep

period=1000/FPS
time=MilliSecs()-period

headmot.Motion = New Motion
eyetmot.Motion = New Motion

If Load_Motion( "Head.bbm", headmot ) = False Then RuntimeError "Error loading file" : End 
If Load_Motion( "EyeFocus.bbm", eyetmot ) = False Then RuntimeError "Error loading file" : End 

camera=CreateCamera()
CameraRange camera,.05,150
PositionEntity camera,0,0,-1
RotateEntity camera,0,0,0

mesh_head = LoadMesh("Head.x")
mesh_reye = LoadMesh("Eye.x",mesh_head)
brush=LoadBrush( "Eye.jpg",1)
BrushShininess brush,1
PaintMesh mesh_reye,brush
mesh_leye = CopyEntity(mesh_reye,mesh_head)
piv_eyet = CreatePivot(mead_hesh)

bkgd=CreateMesh()
s=CreateSurface(bkgd)
AddVertex s,+1,+1,+1,0,0:AddVertex s,-1,+1,+1,1,0
AddVertex s,-1,-1,+1,1,1:AddVertex s,+1,-1,+1,0,1
AddTriangle s,0,1,2:AddTriangle s,0,2,3
ScaleMesh bkgd,130,130,100;225,225,225
EntityFX bkgd,1
FlipMesh bkgd
EntityOrder bkgd,10

PositionEntity mesh_reye,-.058,.256,-.146
PositionEntity mesh_leye,.058,.256,-.146

tex0=LoadTexture("Face.jpg")
tex1=LoadTexture("Reflection.jpg",64)
tex2=LoadTexture("Bkgd.jpg")
EntityTexture mesh_head,tex0
EntityTexture bkgd,tex2

AmbientLight 5,5,5
light1=CreateLight(1)
LightColor light1,255,255,255
RotateEntity light1,0,60,0
light2=CreateLight(1)
LightColor light2,200,0,0
RotateEntity light2,-5,-95,0

fstep = 1
Apply_Motion(headmot,fstep,mesh_head)
Apply_Motion(eyetmot,fstep,piv_eyet)

While KeyHit(1)<>True
	
	Repeat
		elapsed=MilliSecs()-time
	Until elapsed
	
	ticks=elapsed/period
	tween#=Float(elapsed Mod period)/Float(period)
	
	For k=1 To ticks
		time=time+period
		If k=ticks Then CaptureWorld

		fstep = fstep + 1 : If fstep > headmot\nsteps Then fstep = 1
		Apply_Motion(headmot,fstep,mesh_head)
		Apply_Motion(eyetmot,fstep,piv_eyet)
		PointEntity mesh_reye,piv_eyet : PointEntity mesh_leye,piv_eyet
		TurnEntity bkgd,0,0,1
		If fstep = 574 EntityTexture mesh_head,tex1
		If fstep = 1 EntityTexture mesh_head,tex0

		UpdateWorld
	Next
	
	RenderWorld tween
	Flip
	
Wend
End