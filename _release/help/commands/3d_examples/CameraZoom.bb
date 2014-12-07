; CreateZoom Example
; ------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,0,1,0

light=CreateLight()
RotateEntity light,90,0,0

ground=CreatePlane()
sand_tex=LoadTexture("media/sand.bmp")
ScaleTexture sand_tex,10,10
EntityTexture ground,sand_tex
EntityColor ground,168,133,55

cactus1=LoadMesh("media/CACTUS2.x")
cactus2=LoadMesh("media/CACTUS2.x")
camel=LoadMesh("media/camel.x")
PositionEntity cactus1,-1,2,10
PositionEntity cactus2,1,2,10
PositionEntity camel,0,1,1000

; Set initial zoom value
zoom#=1

While Not KeyDown( 1 )

If KeyDown( 205 )=True Then TurnEntity camera,0,-1,0
If KeyDown( 203 )=True Then TurnEntity camera,0,1,0

; Change zoom value depending on key pressed
If KeyDown( 208 )=True Then zoom#=zoom#-0.1
If KeyDown( 200 )=True Then zoom#=zoom#+0.1

; Put a minimum and maximum cap on zoom value
If zoom#<1 Then zoom#=1
If zoom#>100 Then zoom#=100

; Set camera zoom
CameraZoom camera,zoom#

RenderWorld

Text 0,0,"Use left and right cursor keys to turn around"
Text 0,20,"Use up and down cursor keys to change camera zoom"
Text 0,40,"There is a camel on the horizon, inbetween the cacti. Zoom in to  see it."
Text 0,60,"CameraZoom camera,"+zoom#

Flip

Wend

End