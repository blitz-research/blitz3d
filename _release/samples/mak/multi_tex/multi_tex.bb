
Global info1$="Multitexturing demo"

Include "../start.bb"

AmbientLight 0,0,0

;Load base texture
logo=LoadTexture( "blitzlogo.bmp" )

;create next grid texture
grid=CreateTexture( 64,64,0 )
ScaleTexture grid,.5,.5
SetBuffer TextureBuffer( grid )
Color 255,255,255:Rect 0,0,32,32
Color 128,128,128:Rect 32,0,32,32
Color 128,128,128:Rect 0,32,32,32
Color 255,255,255:Rect 32,32,32,32
Color 0,0,255
SetBuffer BackBuffer()
Color 255,255,255

;load top environment texture
env=LoadTexture( "spheremap.bmp",67 )

;create cube
cube=CreateCube()
EntityTexture cube,logo,0,0
EntityTexture cube,grid,0,1
EntityTexture cube,env,0,2
UpdateNormals cube

camera=CreateCamera()
PositionEntity camera,0,0,-4

light=CreateLight()
TurnEntity light,45,45,0
LightColor light,255,0,0

light=CreateLight()
TurnEntity light,-45,45,0
LightColor light,0,255,0

light=CreateLight()
TurnEntity light,45,-45,0
LightColor light,0,0,255

hw=True
HWMultiTex hw

blend1=2
blend2=2
blend3=1
TextureBlend logo,blend1
TextureBlend grid,blend2
TextureBlend env,blend3

While Not KeyHit(1)
	If KeyHit(59)
		blend1=blend1+1
		If blend1=4 blend1=0
		TextureBlend logo,blend1
	EndIf
	If KeyHit(60)
		blend2=blend2+1
		If blend2=4 blend2=0
		TextureBlend grid,blend2
	EndIf
	If KeyHit(61)
		blend3=blend3+1
		If blend3=4 blend3=0
		TextureBlend env,blend3
	EndIf
	If KeyHit(57)
		hw=Not hw
		HWMultiTex hw
	EndIf
	If KeyDown(30)
		TranslateEntity camera,0,0,.1
	EndIf
	If KeyDown(44)
		TranslateEntity camera,0,0,-.1
	EndIf

	off#=off#-.01
	PositionTexture logo,off,0
	
	TurnEntity cube,.1,.2,.3
	
	UpdateWorld
	RenderWorld
	
	Text 0,FontHeight()*0,"(F1) TextureBlend 1="+mode$(blend1)
	Text 0,FontHeight()*1,"(F2) TextureBlend 2="+mode$(blend2)
	Text 0,FontHeight()*2,"(F3) TextureBlend 3="+mode$(blend3)
	If hw t$="On" Else t$="Off"
	Text 0,FontHeight()*4,"(SPC) HW Multitexture="+t$
	
	Flip
Wend

End

Function mode$( mode )
	Select mode
	Case 0:Return "0 (Off)"
	Case 1:Return "1 (Alpha)"
	Case 2:Return "2 (Multiply)"
	Case 3:Return "3 (Add)"
	End Select
	Return "Unkown"
End Function