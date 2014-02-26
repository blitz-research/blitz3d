
Global info1$="Teapot demo"
Global info2$="Features spherical reflection mapping"

Include "../start.bb"

teapot=LoadMesh( "teapot.x" )
tex=LoadTexture( "spheremap.bmp",64+1 )
EntityTexture teapot,tex
EntityFX teapot,1

camera=CreateCamera()
PositionEntity camera,0,0,-3

While Not KeyHit(1)
	TurnEntity teapot,.5,.7,1.1
	UpdateWorld
	RenderWorld
	Flip
Wend
End