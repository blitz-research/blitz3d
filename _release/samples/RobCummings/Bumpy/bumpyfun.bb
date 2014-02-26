; simple bumpmapping effect demo in blitz3d
; works on all graphics cards!
;
; by Rob Cummings (bloodlocust@blueyonder.co.uk)

Global camera,plane,mx,my,c,b,s,l
HidePointer
AppTitle "Blitz3D Bump"
Graphics3D 640,480,16,2
camera=CreateCamera()
plane=CreateSprite(camera)
MoveEntity plane,0,0,2
ScaleEntity plane,500,500,500
EntityFX plane,1 ;fullbright

c=LoadTexture("c.jpg",3)
b=LoadTexture("b.jpg",3)
s=LoadTexture("s.jpg",3)
l=LoadTexture("glow.bmp",48)

While Not KeyHit(1)
	mx=MouseX()-320
	my=MouseY()-240
	UpdateBump()
	UpdateWorld
	RenderWorld
	Text 0,0,"Move mouse to see bump effect!"
	Flip
Wend
End

Function UpdateBump()
	offset#=0.00006
	TextureBlend l,3

	EntityTexture plane,c,0,0
	EntityTexture plane,s,0,1
	EntityTexture plane,b,0,2	
	EntityTexture plane,l,0,3
	
	PositionTexture b,-mx*offset,-my*offset
	PositionTexture s,mx*offset,my*offset
	PositionTexture l,mx*0.002,my*0.002

End Function
	
	