; AmbientLight Example
; --------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()

sphere=CreateSphere( 32 )
PositionEntity sphere,-2,0,5

cone=CreateCone( 32 )
PositionEntity cone,2,0,5

; Set initial ambient light colour values
red#=127
green#=127
blue#=127

While Not KeyDown( 1 )

; Change red, green, blue values depending on key pressed
If KeyDown( 2 )=True And red#>0 Then red#=red#-1
If KeyDown( 3 )=True And red#<255 Then red#=red#+1
If KeyDown( 4 )=True And green#>0 Then green#=green#-1
If KeyDown( 5 )=True And green#<255 Then green#=green#+1
If KeyDown( 6 )=True And blue#>0 Then blue#=blue#-1
If KeyDown( 7 )=True And blue#<255 Then blue#=blue#+1

; Set ambient light color using red, green, blue values
AmbientLight red#,green#,blue#

RenderWorld

Text 0,0,"Press keys 1-6 to change AmbientLight red#,green#,blue# values
Text 0,20,"Ambient Red: "+red#
Text 0,40,"Ambient Green: "+green#
Text 0,60,"Ambient Blue: "+blue#

Flip

Wend

End