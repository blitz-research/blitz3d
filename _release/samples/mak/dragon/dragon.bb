
;0,40  : idle
;40,46 : run
;46,54 : attack
;54,58 : paina
;58,62 : painb
;62,66 : painc
;66,72 : jump
;72,84 : flip

Global info1$="Dragon Demo"
Global info2$="Use arrows keys to pan, A/Z to zoom"
Global info3$="MD2 Dragon model courtesy of Polycount"

Include "../start.bb"

;environment cube
cube=CreateCube()
FitMesh cube,-250,0,-250,500,500,500
FlipMesh cube
tex=LoadTexture( "chorme-2.bmp" )
ScaleTexture tex,1.0/3,1.0/3
EntityTexture cube,tex
EntityAlpha cube,.4
EntityFX cube,1

;floor mirror
m=CreateMirror()

;simple light
light=CreateLight()
TurnEntity light,45,45,0

;camera
camera=CreateCamera()
cam_xr#=30:cam_yr#=0:cam_zr#=0:cam_z#=-100

;cool dragon model!
tex=LoadTexture( "model\dragon.bmp" )
dragon=LoadMD2( "model\dragon.md2" )
EntityTexture dragon,tex
PositionEntity dragon,0,25,0
TurnEntity dragon,0,150,0

AnimateMD2 dragon,1,.05,0,40

While Not KeyHit(1)

	If KeyDown(203)
		cam_yr=cam_yr-2
	Else If KeyDown(205)
		cam_yr=cam_yr+2
	EndIf
	
	If KeyDown(200)
		cam_xr=cam_xr+2
		If cam_xr>90 cam_xr=90
	Else If KeyDown(208)
		cam_xr=cam_xr-2
		If cam_xr<5 cam_xr=5
	EndIf
	
	If KeyDown(26)
		cam_zr=cam_zr+2
	Else If KeyDown(27)
		cam_zr=cam_zr-2
	EndIf
	
	If KeyDown(30)
		cam_z=cam_z+1:If cam_z>-10 cam_z=-10
	Else If KeyDown(44)
		cam_z=cam_z-1:If cam_z<-180 cam_z=-180
	EndIf
	
	PositionEntity camera,0,0,0
	RotateEntity camera,cam_xr,cam_yr,cam_zr
	MoveEntity camera,0,0,cam_z

	UpdateWorld
	RenderWorld
	Flip
Wend

End