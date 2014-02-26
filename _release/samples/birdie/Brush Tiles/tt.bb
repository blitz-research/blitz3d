;Terrain Tiling 2
;David Bird
;dave@birdie72.freeserve.co.uk
;use of brush tiles
;also example of texture coords set

;size of map
Const Gridx=32
Const Gridz=32
;Global vars here
Global Tile_mask
Global Lightmap


;Setup 3d graphics mode
Graphics3D 640,480
SetBuffer BackBuffer()
Collisions 1,2,2,3

;Add Lights and Camera
lit=CreateLight()
cam=CreateCamera()
CameraRange cam,.1,6000
CameraClsColor cam,10,10,50
EntityType cam,1
EntityRadius cam,4

PositionEntity cam,0,256,0
TurnEntity cam,0,0,0

Dim tex(8)				;Holds textures used for tiles
Load_Textures()

;Create and store map heights from a heightmap
Dim map.tile(gridx,gridz)	;Now holds all info including brush tiles set 1 and 2,mask and lightmap
Create_MapLayout("hmap.bmp");create a height map and store it in dimension map(x,y)
;Create a map mesh and scale to size
ground=Create_Map(gridx,gridz,3)
PositionEntity ground,-1600,0,-1600
ScaleMesh ground,100,400,100
EntityType ground,2
;Finally add a lightmap texture
EntityTexture ground,Lightmap,0,3

groundtex=LoadTexture("tex0.bmp"):ScaleTexture groundtex,50,50
Color 255,255,255
;Load logo and resize
im=LoadImage("bl3d_big.jpg")
ResizeImage im,100,50

;Make a sky box and make its parent be the camera
sky=LoadSkyBox( "sky" )
EntityParent sky,cam


While Not KeyDown(1)
	TranslateEntity cam,0,-.5,0	;Translate the camera down **Gravity**
	RotateEntity sky,0,0,0,True	;rotate the skybox true to the scene
	If KeyDown(203) TurnEntity cam,0,1,0
	If KeyDown(205) TurnEntity cam,0,-1,0
	If KeyDown(200) TurnEntity cam,1,0,0
	If KeyDown(208) TurnEntity cam,-1,0,0

	If KeyDown(44) MoveEntity cam,0,0,-2
	If KeyDown(30) MoveEntity cam,0,0,2
	UpdateWorld
	RenderWorld
	Text 0,0,"Terrain brush tiling by"
	Text 0,16,"David Bird"
	Text 0,32,"dave@birdie72.freeserve.co.uk"
	DrawImage im,540,0
	Flip
Wend

FreeEntity ground
FreeEntity lit
FreeEntity cam
EndGraphics
End

;
;	Create a flat plane all with tile 1
;
Function Create_Map(tilex,tilez,tile)
	mesh=CreateMesh()
	u#=0
	v#=0
	stp#=1.0/Float(tilex)
	For z#=0 To tilez-1
		u=0
		For x#=0 To tilex-1
			h1#=map(x,z)\y							;top left height
			h2#=map(x+1,z)\y						;top right height
			h3#=map(x+1,z+1)\y						;bot right height
			h4#=map(x,z+1)\y						;bot left height
			br=map(x,z)\brush						; this brush contains both tiles + mask + lightmap
			surf=FindSurface( mesh,br )				; Find a surface in the mesh that contains the same brush
			If surf=0 surf=CreateSurface(mesh,br) 	; If it can't find one create a new surface
			cnt=CountVertices( surf )				; Get count of vertices

			;Add vertices including tile coords
			AddVertex surf,x,h1,z,0,0
			AddVertex surf,x+1.0,h2,z,1,0
			AddVertex surf,x+1.0,h3,z+1.0,1,1
			AddVertex surf,x,h4,z+1.0,0,1
			;Add second set of tex coords to fit grid
			VertexTexCoords surf,cnt,u,v,0,1
			VertexTexCoords surf,cnt+1,u+stp,v,0,1
			VertexTexCoords surf,cnt+2,u+stp,v+stp,0,1
			VertexTexCoords surf,cnt+3,u,v+stp,0,1
			;Finally add the two triangles
			AddTriangle surf,cnt,cnt+2,cnt+1
			AddTriangle surf,cnt,cnt+3,cnt+2
			u=u+stp
		Next
		v=v+stp
	Next
	UpdateNormals mesh
	Return mesh
End Function

Function Load_Textures()
	For a=0 To 7
		b$="tex"+a+".bmp"
		tex(a+1)=LoadTexture(b$)
	Next
	Tile_mask=LoadTexture("mask.bmp")
	TextureCoords Tile_mask,1
	TextureBlend Tile_mask,3
	Lightmap=LoadTexture("lightmap.bmp",4)
	TextureCoords Lightmap,1
	TextureBlend Lightmap,3
End Function

Function Create_MapLayout(file$)
	hmap=LoadImage(file)
	DrawImage hmap,0,0
	For z=0 To gridz
		For x=0 To gridx
			map.tile(x,z)=New tile
			GetColor x,z
			map(x,z)\y=Float(ColorRed())/255
			map(x,z)\brush=CreateBrush()
			Read track
			BrushTexture map(x,z)\brush,tex(track),0,0		;Bottom Tile
			BrushTexture map(x,z)\brush,tex(2),0,2			;Top Tile
			BrushTexture map(x,z)\brush,Tile_mask,0,1		;Mask
		Next
	Next
End Function

;
;Tile information
;
Type tile
	Field y#		;yhieght at top left eg inc u0,v0
	Field brush		;Remember that there are four brushes used ontop of each other blended
End Type

.mapdata
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,5,3,3,3,3,3,6,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,5,3,6,1,1,1,5,3,3,3,6,1,4,1,1,1,1,1,4,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,4,1,4,1,5,3,8,1,1,1,4,1,4,1,1,1,1,1,4,1,1,1,1,1,1,1
Data 1,1,5,3,3,3,3,8,1,7,3,8,1,1,1,1,1,7,3,8,1,1,1,1,1,4,1,1,1,1,1,1,1
Data 1,1,4,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,4,1,1,1,1,1,1,1
Data 1,1,7,3,3,3,3,6,1,5,3,3,3,3,6,1,1,1,1,1,1,1,1,1,1,4,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,4,1,4,1,1,1,1,4,1,1,1,1,1,1,1,1,1,1,4,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,4,1,4,1,1,1,1,4,1,1,1,1,1,1,1,1,1,1,4,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,4,1,4,1,1,1,1,4,1,1,1,5,3,3,3,3,3,3,8,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,7,3,8,1,1,1,1,4,1,1,1,4,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,4,1,1,1,4,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,4,1,1,1,4,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,7,3,3,3,8,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1
Data 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1

Function LoadSkyBox( file$ )
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
	ScaleMesh m,3300,3300,3300
	FlipMesh m
	EntityFX m,1
	Return m
End Function