
Global info1$="Detail texturing"
Global info2$="Space to toggle, arrows to zoom"

Include "../start.bb"

tex1=LoadTexture( "texture.jpg" )
tex2=LoadTexture( "detail2.jpg" )

ScaleTexture tex2,.1,.1

sphere=CreateSphere( 16 )
ScaleMesh sphere,10,10,10

EntityTexture sphere,tex1,0,0
EntityTexture sphere,tex2,0,1

camera=CreateCamera()
PositionEntity camera,0,0,-100

light=CreateLight()
TurnEntity light,45,45,0

on=1

While Not KeyHit(1)

	If KeyHit(57)
		on=1-on
		If on TextureBlend tex2,2 Else TextureBlend tex2,0
	EndIf

	TurnEntity sphere,0,.1,0

	If KeyDown( 200 ) MoveEntity camera,0,0,.5
	If KeyDown( 208 ) MoveEntity camera,0,0,-.5

	UpdateWorld
	RenderWorld
	If on Text 0,0,"Detail On" Else Text 0,0,"Detail Off"
	Flip

Wend