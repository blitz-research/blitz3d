; ScaleEntity Example
; -------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
light=CreateLight()

cone=CreateCone( 32 )
PositionEntity cone,0,0,5

; Set scale values so that cone is default size to begin with
x_scale#=1
y_scale#=1
z_scale#=1

While Not KeyDown( 1 )

; Change scale values depending on the key pressed
If KeyDown( 203 )=True Then x_scale#=x_scale#-0.1
If KeyDown( 205 )=True Then x_scale#=x_scale#+0.1
If KeyDown( 208 )=True Then y_scale#=y_scale#-0.1
If KeyDown( 200 )=True Then y_scale#=y_scale#+0.1
If KeyDown( 44 )=True Then z_scale#=z_scale#-0.1
If KeyDown( 30 )=True Then z_scale#=z_scale#+0.1

; Scale cone using scale values
ScaleEntity cone,x_scale#,y_scale#,z_scale#

RenderWorld

Text 0,0,"Use cursor/A/Z keys to scale cone"
Text 0,20,"X Scale: "+x_scale#
Text 0,40,"Y Scale: "+y_scale#
Text 0,60,"Z Scale: "+z_scale#

Flip

Wend

End