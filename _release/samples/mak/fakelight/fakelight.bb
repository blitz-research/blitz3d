
Global info1$="Fakelighting demo"
Global info2$="Hit <return> to toggle real/fake lighting"
Include "../start.bb"

AmbientLight 0,0,0

cube=CreateSphere( 32 )
tex=LoadTexture( "brick.bmp" )
EntityTexture cube,tex

range#=5

For k=1 To 6
	Read lx#,ly#,lz#,lr#,lg#,lb#
	light=CreateLight(2,cube)
	LightRange light,range
	LightColor light,lr,lg,lb
	PositionEntity light,lx,ly,lz
	LightMesh cube,lr,lg,lb,range,lx,ly,lz
Next

camera=CreateCamera()
PositionEntity camera,0,0,-5

fake=0

While Not KeyHit(1)

	TurnEntity cube,.1,.2,.3

	If KeyHit(28)
		fake=3-fake
		EntityFX cube,fake
	EndIf

	UpdateWorld
	RenderWorld
	If fake t$="Y" Else t$="N"
	Text 0,0,"Fake light:"+t$
	Flip
Wend

End

Data -2,0,0,255,0,0
Data +2,0,0,0,255,0
Data 0,+2,0,255,255,0
Data 0,-2,0,255,0,255
Data 0,0,-2,0,255,255
Data 0,0,+2,255,255,255