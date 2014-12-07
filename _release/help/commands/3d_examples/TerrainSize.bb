; TerrainSize Example
; -------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,1,1,1

light=CreateLight()
RotateEntity light,90,0,0

; Load terrain
terrain=LoadTerrain( "media/height_map.bmp" )

; Set terrain detail, enable vertex morphing
TerrainDetail terrain,4000,True

; Scale terrain
ScaleEntity terrain,1,50,1

; Texture terrain
grass_tex=LoadTexture( "media/mossyground.bmp" )
EntityTexture terrain,grass_tex,0,1

While Not KeyDown( 1 )

If KeyDown( 203 )=True Then x#=x#-0.1
If KeyDown( 205 )=True Then x#=x#+0.1
If KeyDown( 208 )=True Then y#=y#-0.1
If KeyDown( 200 )=True Then y#=y#+0.1
If KeyDown( 44 )=True Then z#=z#-0.1
If KeyDown( 30 )=True Then z#=z#+0.1

If KeyDown( 205 )=True Then TurnEntity camera,0,-1,0
If KeyDown( 203 )=True Then TurnEntity camera,0,1,0
If KeyDown( 208 )=True Then MoveEntity camera,0,0,-0.1
If KeyDown( 200 )=True Then MoveEntity camera,0,0,0.1

x#=EntityX(camera)
y#=EntityY(camera)
z#=EntityZ(camera)

terra_y#=TerrainY(terrain,x#,y#,z#)+5

PositionEntity camera,x#,terra_y#,z#

RenderWorld

Text 0,0,"Use cursor keys to move about the terrain"

; Output terrain size to screen
Text 0,20,"Terrain Size: "+TerrainSize(terrain)

Flip

Wend

End