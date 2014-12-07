; CreateMirror Example
; --------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,0,1,-5

light=CreateLight()
RotateEntity light,90,0,0

; Create cone
cone=CreateCone(32)
PositionEntity cone,0,2,0

; Create plane
plane=CreatePlane()
grass_tex=LoadTexture( "media/chorme-2.bmp" )
EntityTexture plane,grass_tex
EntityAlpha plane,0.5

; Create mirror
mirror=CreateMirror()

While Not KeyDown( 1 )

If KeyDown( 203 )=True Then MoveEntity cone,-0.1,0,0
If KeyDown( 205 )=True Then MoveEntity cone,0.1,0,0
If KeyDown( 208 )=True Then MoveEntity cone,0,-0.1,0
If KeyDown( 200 )=True Then MoveEntity cone,0,0.1,0
If KeyDown( 44 )=True Then MoveEntity cone,0,0,-0.1
If KeyDown( 30 )=True Then MoveEntity cone,0,0,0.1

RenderWorld

Text 0,0,"Use cursor/A/Z keys to move cone above infinite mirror"

Flip

Wend

End