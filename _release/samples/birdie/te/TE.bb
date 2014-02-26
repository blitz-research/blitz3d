;Multitexture on terrains
;David Bird
;dave@birdie72.freeserve.co.uk
;http://www.birdie72.freeserve.co.uk
;14/6/01 (beta 2.24)
;Example of belnding two textures

Graphics3D 640,480
SetBuffer BackBuffer()

HWMultiTex True 
lit=CreateLight()

cam=CreateCamera()
CameraRange cam,1,25000

PositionEntity cam,0,50,-152

terr=LoadTerrain("hmap.bmp")

TerrainShading terr,True
TerrainDetail terr,2000

;Load textures
tex0=LoadTexture("hmap.bmp")		;Mask 1
tex1=LoadTexture("forest.bmp")		;Grass Texture
tex2=LoadTexture("hmap2.bmp")		;Inverted Mask1
tex3=LoadTexture("rock.bmp")		;Rock texture

;sort out the blending here
;((grass*mask1)*rock)*mask2)
TextureBlend tex0,3					;Blend mode (add)
TextureBlend tex2,2					;Blend mode (multiply)
TextureBlend tex3,2

;resize textures
ScaleTexture tex2,128,128
ScaleTexture tex0,128,128
ScaleTexture tex3,4,4
ScaleTexture tex1,32,32

;Do the blending here
EntityTexture terr,tex1,0,0
EntityTexture terr,tex0,0,1
EntityTexture terr,tex3,0,2
EntityTexture terr,tex2,0,3

;scale terrain to size
ScaleEntity terr,50,375,50
EntityColor terr,30,30,30

;Add an infinate plane
plane=CreatePlane()
EntityTexture plane,tex1
ScaleEntity plane,50,1,50
EntityOrder plane,1

AmbientLight 30,30,30
While Not KeyDown(1)
	ClsColor flash,flash,flash
	Cls
	If KeyDown(203) TurnEntity cam,0,+1,0	;cursors to move
	If KeyDown(205) TurnEntity cam,0,-1,0
	If KeyDown(200) TurnEntity cam,+1,0,0
	If KeyDown(208) TurnEntity cam,-1,0,0

	If KeyDown(44) MoveEntity cam,0,0,-8	;A - fly foward
	If KeyDown(30) MoveEntity cam,0,0,8		;Z - fly backward

	UpdateWorld
	RenderWorld
	
	Flip
Wend

;Freeup entity's and textures
FreeTexture tex0
FreeTexture tex1
FreeTexture tex2
FreeTexture tex3
FreeEntity terr
FreeEntity plane
FreeEntity cam
EndGraphics
End