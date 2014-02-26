
Global info1$="Insaner demo!"
Global info2$="Arrow keys pan, A/Z zoom"
Global info3$="space to toggle hardware multitexturing"
Global info4$="Features multitextured dynamic LOD terrain"

Include "../start.bb"

terrain=LoadTerrain( "hmap.bmp" )
TerrainDetail terrain,500,True
PositionEntity terrain,-128,0,-128
ScaleEntity terrain,1,50,1
EntityFX terrain,1

;setup multitexturing:
;
;dest=( ( (rock-grass) * track ) + grass ) * lighting
;
tex0=LoadTexture( "CrackedStone_diff.bmp" )
ScaleTexture tex0,16,16
tex1=LoadTexture( "track.bmp" )
ScaleTexture tex1,256,256
tex2=LoadTexture( "MossyGround.bmp" )
ScaleTexture tex2,16,16
TextureBlend tex2,3
tex3=LoadTexture( "lmap.bmp" )
ScaleTexture tex3,256,256

EntityTexture terrain,tex0,0,0
EntityTexture terrain,tex1,0,1
EntityTexture terrain,tex2,0,2
EntityTexture terrain,tex3,0,3

camera=CreateCamera()
PositionEntity camera,0,200,0
RotateEntity camera,90,0,0

hwmt=True
HWMultiTex hwmt

While Not KeyHit(1)
	If KeyHit(17) wire=Not wire:WireFrame wire
	If KeyHit(57) hwmt=Not hwmt:HWMultiTex hwmt
	If KeyDown(30) TranslateEntity camera,0,-2,0
	If KeyDown(44) TranslateEntity camera,0,+2,0
	If KeyDown(203) TranslateEntity camera,-2,0,0
	If KeyDown(205) TranslateEntity camera,+2,0,0
	If KeyDown(200) TranslateEntity camera,0,0,+2
	If KeyDown(208) TranslateEntity camera,0,0,-2
	UpdateWorld
	RenderWorld
	Text 0,0,"HWMultiTex "+hwmt
	Flip
Wend
End