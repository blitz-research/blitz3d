
smooth=True

Global info1$="Tron demo"
Global info2$="Features dynamic mesh creation"
Global info3$="Use arrow keys to steer, A/Z to zoom"

Include "../start.bb"

grid_tex=CreateTexture( 32,32,8 )
ScaleTexture grid_tex,10,10
SetBuffer TextureBuffer( grid_tex )
Color 0,0,64:Rect 0,0,32,32
Color 0,0,255:Rect 0,0,32,32,False
SetBuffer BackBuffer()

grid_plane=CreatePlane()
EntityTexture grid_plane,grid_tex
EntityBlend grid_plane,1
EntityAlpha grid_plane,.6
EntityFX grid_plane,1

mirror=CreateMirror()

pivot=CreatePivot()

p=CreatePivot( p )
cube=CreateCube( p )
ScaleEntity cube,1,1,5
SetAnimKey cube,0
RotateEntity cube,0,120,0
SetAnimKey cube,60
RotateEntity cube,0,240,0
SetAnimKey cube,120
RotateEntity cube,0,0,0
SetAnimKey cube,180
AddAnimSeq p,180

For x=-100 To 100 Step 25
For z=-100 To 100 Step 25
e=CopyEntity( p,pivot )
PositionEntity e,x,5,z
Animate e
Next
Next
FreeEntity cube

trail_mesh=CreateMesh()
trail_brush=CreateBrush()
BrushColor trail_brush,255,0,0
BrushBlend trail_brush,3
BrushFX trail_brush,1
trail_surf=CreateSurface( trail_mesh,trail_brush )
AddVertex trail_surf,0,2,0,0,0
AddVertex trail_surf,0,0,0,0,1
AddVertex trail_surf,0,2,0,0,0
AddVertex trail_surf,0,0,0,0,1
AddTriangle trail_surf,0,2,3
AddTriangle trail_surf,0,3,1
AddTriangle trail_surf,0,3,2
AddTriangle trail_surf,0,1,3
trail_vert=2

bike=CreateSphere()
ScaleMesh bike,.75,1,2
PositionEntity bike,0,1,0
EntityShininess bike,1
EntityColor bike,192,0,255

camera=CreateCamera()
TurnEntity camera,45,0,0
cam_d#=30

light=CreateLight()
TurnEntity light,45,45,0

add_flag=False

While Not KeyHit(1)

	If KeyHit(17)
		wire=Not wire
		WireFrame wire
	EndIf
	
	If KeyDown(30) cam_d=cam_d-1
	If KeyDown(44) cam_d=cam_d+1
	
	turn=0
	If smooth
		If KeyDown(203) turn=5
		If KeyDown(205) turn=-5
		If turn
			add_cnt=add_cnt+1
			If add_cnt=3
				add_cnt=0
				add_flag=True
			Else
				add_flag=False
			EndIf
		Else If add_cnt
			add_cnt=0
			add_flag=True
		Else
			add_flag=False
		EndIf
	Else
		If KeyHit(203) turn=90
		If KeyHit(205) turn=-90
		If turn Then add_flag=True Else add_flag=False
	EndIf
	
	If turn
		TurnEntity bike,0,turn,0
	EndIf
	
	MoveEntity bike,0,0,1
	
	If add_flag
		AddVertex trail_surf,EntityX(bike),2,EntityZ(bike),0,0
		AddVertex trail_surf,EntityX(bike),0,EntityZ(bike),0,1
		AddTriangle trail_surf,trail_vert,trail_vert+2,trail_vert+3
		AddTriangle trail_surf,trail_vert,trail_vert+3,trail_vert+1
		AddTriangle trail_surf,trail_vert,trail_vert+3,trail_vert+2
		AddTriangle trail_surf,trail_vert,trail_vert+1,trail_vert+3
		trail_vert=trail_vert+2
	Else
		VertexCoords trail_surf,trail_vert,EntityX(bike),2,EntityZ(bike)
		VertexCoords trail_surf,trail_vert+1,EntityX(bike),0,EntityZ(bike)
	EndIf
	
	UpdateWorld
	
	PositionEntity camera,EntityX(bike)-5,0,EntityZ(bike)
	MoveEntity camera,0,0,-cam_d

;	PositionEntity camera,0,20,0
;	PointEntity camera,bike
	
	RenderWorld
	Flip

Wend
End