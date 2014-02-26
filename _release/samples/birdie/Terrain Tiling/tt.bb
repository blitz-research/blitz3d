;Terrain Tiling 2
;David Bird
;dave@birdie72.freeserve.co.uk
;use of u/v coords for tileset
;also example of texture coords set

;size of map
Const Gridx=64
Const Gridz=64

Graphics3D 640,480
SetBuffer BackBuffer()

lit=CreateLight()
cam=CreateCamera()

CameraRange cam,.1,1000
PositionEntity cam,32,20,32
TurnEntity cam,90,0,0

;Load brush with 16tiles
b1=LoadBrush("16tex.bmp")

Const Texture_Grid=4			;4x4 square texture grid

;Create and store map heights from a hightmap
Dim map#(gridx,gridz)
hmap=LoadImage("hmap.bmp")
DrawImage hmap,0,0

For z=0 To gridz
	For x=0 To gridx
		GetColor x,z
		map(x,z)=Float(ColorRed())/255
	Next
Next

;Create a map mesh and scale to size
mesh=Create_Map(gridx,gridz,b1,3)
ScaleMesh mesh,1,6,1

;do next level of texturing using second
;set of texture coords

tex1=LoadTexture("mask.bmp")
TextureCoords tex1,1

tex2=LoadTexture("tex0.bmp")
TextureCoords tex2,1
TextureBlend tex2,3

EntityTexture mesh,tex1,0,1		;remember texture index 0 is for tileset
EntityTexture mesh,tex2,0,2

Color 255,255,255

pl=CreatePlane()
tex3=LoadTexture("tex0.bmp"):ScaleTexture tex3,64,64
EntityTexture pl,tex3
EntityOrder pl,1
EntityFX pl,1

;WireFrame True
While Not KeyDown(1)
	If KeyDown(203) TranslateEntity cam,-1,0,0
	If KeyDown(205) TranslateEntity cam,1,0,0
	If KeyDown(200) TranslateEntity cam,0,0,1
	If KeyDown(208) TranslateEntity cam,0,0,-1

	If KeyDown(44) TranslateEntity cam,0,1,0
	If KeyDown(30) TranslateEntity cam,0,-1,0
	
	If KeyHit(57) Then
		For y=0 To gridz-1
			For x=0 To gridx-1
				tl=Rand(15)
				Paint_Tile(x,y,tl,mesh)
			Next
		Next
	End If
			
	UpdateWorld
	RenderWorld
	Text 0,0,"Press <Space> to randomize tiles"
	Flip
Wend

FreeEntity mesh
FreeEntity lit
FreeEntity cam
EndGraphics
End

;
;	Create a flat plane all with tile 1
;
Function Create_Map(tilex,tilez,brush1,tile)
	mesh=CreateMesh()
	surf=CreateSurface(mesh,brush1)
	wid#=Float(1)/Float(Texture_grid)
	u0#=wid*Float(tile Mod texture_Grid)
	v0#=wid*Float(tile/texture_grid)
	u1#=u0+wid
	v1#=v0
	u2#=u1
	v2#=v0+wid
	u3#=u0
	v3#=v2
	u#=0
	v#=0
	stp#=1.0/Float(tilex)
	For z#=0 To tilez-1
		u=0
		For x#=0 To tilex-1
			h1#=map(x,z)
			h2#=map(x+1,z)
			h3#=map(x+1,z+1)
			h4#=map(x,z+1)
			AddVertex surf,x,h1,z,u0,v0
			VertexTexCoords surf,cnt,u,v,0,1
			
			AddVertex surf,x+1,h2,z,u1,v1
			VertexTexCoords surf,cnt+1,u+stp,v,0,1
			
			AddVertex surf,x+1,h3,z+1,u2,v2
			VertexTexCoords surf,cnt+2,u+stp,v+stp,0,1
			
			AddVertex surf,x,h4,z+1,u3,v3
			VertexTexCoords surf,cnt+3,u,v+stp,0,1
			
			AddTriangle surf,cnt,cnt+2,cnt+1
			AddTriangle surf,cnt,cnt+3,cnt+2
			cnt=cnt+4
			u=u+stp
		Next
		v=v+stp
	Next
	UpdateNormals mesh
	Return mesh
End Function

;paint one tile with new tile texture
Function Paint_Tile(tx,tz,texture_index,mesh)
	;firstly find texture coords
	wid#=Float(1)/Float(Texture_grid)
	u0#=(wid*Float(texture_index Mod texture_Grid))
	v0#=(wid*Float(texture_index/texture_grid))
	u1#=u0+wid
	v1#=v0
	u2#=u1
	v2#=v0+wid
	u3#=u0
	v3#=v2
	;retrieve the surface from the mesh
	surf=GetSurface(mesh,CountSurfaces(mesh))
	;find triangle at grid position tx,tz
	cnt=((tz*Gridz)+tx)*2
	;Now reset its texture coords
	index=TriangleVertex(surf,cnt,0)
	VertexTexCoords surf,index,u0,v1
	VertexTexCoords surf,index+1,u1,v1
	VertexTexCoords surf,index+2,u2,v2
	VertexTexCoords surf,index+3,u3,v3
End Function