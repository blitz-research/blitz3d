; CreateTerrain Example
; ---------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,0,1,0

light=CreateLight()
RotateEntity light,90,0,0

; Create terrain
terrain=CreateTerrain(128)

; Texture terrain
grass_tex=LoadTexture( "media/mossyground.bmp" )
EntityTexture terrain,grass_tex

While Not KeyDown( 1 )

If KeyDown( 205 )=True Then TurnEntity camera,0,-1,0
If KeyDown( 203 )=True Then TurnEntity camera,0,1,0
If KeyDown( 208 )=True Then MoveEntity camera,0,0,-0.05
If KeyDown( 200 )=True Then MoveEntity camera,0,0,0.05

RenderWorld

Text 0,0,"Use cursor keys to move about the terrain"

Flip

Wend

End