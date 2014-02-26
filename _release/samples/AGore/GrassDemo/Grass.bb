Global info1$="Grass Demo, by Adam Gore"

Include "../start.bb"

;Field of grass + kludged lens flare

Global gwidth = 640
Global gheight = 480
Global length = 16			;up this if you have a *very* fast computer
Global ystep# = .005
Global an#

Dim plane( length-1 )

;Graphics3D gwidth,gheight

mesh_skybox = MakeSkyBox("Data\sky") 

camera=CreateCamera()
CameraZoom camera,1
PositionEntity camera,0,.1,0
CameraFogMode camera,1
CameraFogRange camera,0,75
CameraFogColor camera,222,252,255
AmbientLight 255,255,255

ClearTextureFilters

tex=LoadTexture( "Data\Ground.bmp",9 )
ScaleTexture tex,4,4
tex1=LoadTexture( "Data\GrassClip.bmp",10 )
ScaleTexture tex1,1.5,1.5
ground = CreatePlane(1)
EntityTexture ground,tex : PositionEntity ground,0,-1-.005,0
EntityOrder ground,9

flare = CreatePivot( mesh_skybox )
PositionEntity flare,0,120,-300

s1 = LoadSprite("Data\lens1.jpg",2,camera) : EntityFX s1,9 : ScaleSprite s1,6,6	: EntityColor s1,255,255,242
s2 = LoadSprite("Data\lens2.jpg",2,camera) : EntityFX s2,9 : ScaleSprite s2,1.1,1.1	: EntityColor s2,255,255,220
s3 = LoadSprite("Data\lens3.jpg",2,camera) : EntityFX s3,9 : ScaleSprite s3,1.5,1.5	: EntityColor s3,255,255,200
s4 = LoadSprite("Data\lens4.jpg",2,camera) : EntityFX s4,9 : ScaleSprite s4,1.8,1.8	: EntityColor s4,255,255,180

For a#=0 To length-1
 plane(a) = CreatePlane()
 EntityTexture plane(a),tex1
 PositionEntity plane(a),0,-1 + (a * ystep),0
 EntityColor plane(a),32,96+((a/(length-1))*96),0
 EntityAlpha plane(a),.5+((1-(a/(length-1)))*2)
 EntityOrder plane(a),8
Next


While Not KeyHit(1)

	an=an+1 : If an>359 Then an=0
	s# = Cos(an)/15000
	For b = 0 To length-1
		MoveEntity plane(b),s*b,0,0
	Next

	If KeyDown(203) TurnEntity camera,0,.5,0
	If KeyDown(205) TurnEntity camera,0,-.5,0
	If KeyDown(200) MoveEntity camera,0,0,.01
	If KeyDown(208) MoveEntity camera,0,0,-.01

	PositionEntity mesh_skybox,EntityX(camera,1),EntityY(camera,1),EntityZ(camera,1)
	
	CameraProject( camera,EntityX(flare,1),EntityY(flare,1),EntityZ(flare,1) )
	
    vx# = ProjectedX() - (gwidth/2)
    vy# = ProjectedY() - (gheight/2)

	sx = vx + (gwidth/2) : sy = vy + (gheight/2)
	PositionEntity s1,SpriteX(sx,sy,128),SpriteY(sx,sy,128),SpriteZ(sx,sy,128)
	sx = -vx/2 + (gwidth/2) : sy = -vy/2 + (gheight/2)
	PositionEntity s2,SpriteX(sx,sy,128),SpriteY(sx,sy,128),SpriteZ(sx,sy,128)
	sx = -vx/5 + (gwidth/2) : sy = -vy/5 + (gheight/2)
	PositionEntity s3,SpriteX(sx,sy,128),SpriteY(sx,sy,128),SpriteZ(sx,sy,128)
	sx = -vx/1.2 + (gwidth/2) : sy = -vy/1.2 + (gheight/2)
	PositionEntity s4,SpriteX(sx,sy,128),SpriteY(sx,sy,128),SpriteZ(sx,sy,128)
	
	RotateSprite s1,-EntityYaw(camera)
	EntityAlpha s1,ProjectedZ()
	EntityAlpha s2,1-(Abs(vx)/(gwidth/2))
	EntityAlpha s3,1-(Abs(vx)/(gwidth/2))
	EntityAlpha s4,1-(Abs(vx)/(gwidth/2))	
	
	UpdateWorld : RenderWorld :	Flip

Wend
End


Function SpriteX#(x#,y#,size#)
	Return 2.0*(x-(gwidth/2))/size
End Function

Function SpriteY#(x#,y#,size#)
	Return -2.0*(y-(gheight/2))/size
End Function

Function SpriteZ#(x#,y#,size#)
	Return 1*gwidth/size
End Function

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

	ScaleMesh m,100,100,100
	FlipMesh m
	EntityFX m,9
	EntityOrder m,10
	Return m
	
End Function