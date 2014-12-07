; TerrainDetail Example
; ---------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,1,1,1

light=CreateLight()
RotateEntity light,90,0,0

; Load terrain
terrain=LoadTerrain( "media/height_map.bmp" )

; Scale terrain
ScaleEntity terrain,1,50,1

; Texture terrain
grass_tex=LoadTexture( "media/mossyground.bmp" )
EntityTexture terrain,grass_tex,0,1

; Set terrain detail value
terra_detail=4000

; Set vertex morph value
vertex_morph=True

While Not KeyDown( 1 )

; Change terrain detail value depending on key pressed
If KeyDown(26) Then terra_detail=terra_detail-10
If KeyDown(27) Then terra_detail=terra_detail+10

; Toggle vertex morphing on/off when spacebar is pressed
If KeyHit(57)=True Then vertex_morph=1-vertex_morph

; Set terrain detail, vertex morphing
TerrainDetail terrain,terra_detail,vertex_morph

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
Text 0,20,"Use [ and ] keys to change terrain detail level"
Text 0,40,"Press spacebar to enable/disable vertex morphing"
Text 0,60,"Terrain Detail: "+terra_detail
If vertex_morph=True Then Text 0,80,"Vertex Morphing: True" Else Text 0,80,"Vertex  Morphing: False"

Flip

Wend

End