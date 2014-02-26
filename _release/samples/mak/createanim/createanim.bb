
Global info1$="Manual animation creation demo"
Global info2$="Hit <return> to swap between sequences"
Global info3$="Hit <space> to toggle transitions"

Include "../start.bb"

tex=CreateTexture( 64,64 )
SetBuffer TextureBuffer( tex )
Color 255,0,0:Rect 0,0,32,32:Rect 32,32,32,32
Color 255,128,0:Rect 32,0,32,32:Rect 0,32,32,32
SetBuffer BackBuffer()
Color 255,255,255

cube=CreateCube()
EntityTexture cube,tex

;set animation keys
RotateEntity cube,0,0,0
PositionEntity cube,0,0,0
SetAnimKey cube,0

RotateEntity cube,0,90,0
PositionEntity cube,0,0,10
SetAnimKey cube,60

RotateEntity cube,0,180,0
PositionEntity cube,10,0,10
SetAnimKey cube,120

RotateEntity cube,0,270,0
PositionEntity cube,10,0,0
SetAnimKey cube,180

RotateEntity cube,0,0,0
PositionEntity cube,0,0,0
SetAnimKey cube,240

;create animation sequence 0
AddAnimSeq cube,240

;set animation keys
ScaleEntity cube,1,1,1
PositionEntity cube,5,0,5
SetAnimKey cube,0

ScaleEntity cube,5,1,1
SetAnimKey cube,30

ScaleEntity cube,1,1,1
SetAnimKey cube,60

ScaleEntity cube,1,5,1
SetAnimKey cube,90

ScaleEntity cube,1,1,1
SetAnimKey cube,120

AddAnimSeq cube,120

camera=CreateCamera()
PositionEntity camera,5,3,-10

light=CreateLight()
TurnEntity light,45,45,0

Animate cube

FlushKeys

trans=20

While Not KeyHit(1)

	If KeyHit(57) trans=20-trans

	If KeyHit(28) Animate cube,1,1,1-AnimSeq(cube),trans
		
	UpdateWorld
	RenderWorld
	Text 0,FontHeight()*0,"Anim seq: "+AnimSeq( cube )
	Text 0,FontHeight()*1,"Anim len: "+AnimLength( cube )
	Text 0,FontHeight()*2,"Anim time:"+AnimTime( cube )
	Text 0,FontHeight()*4,"Transition time:"+trans
	
	Flip
Wend

End