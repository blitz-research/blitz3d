; ModifyTerrain Example
; ---------------------

Graphics3D 640,480
SetBuffer BackBuffer()

terra_size=32 ; initial size of terrain, and no. of grids segments, along each  side
x_scale=10 ; x scale of terrain
y_scale=50 ; y scale of terrain
z_scale=10 ; z scale of terrain
marker_x=terra_size/2 ; initial x position of marker
marker_z=terra_size/2 ; initial z position of marker

camera=CreateCamera()
PositionEntity camera,(terra_size*x_scale)/2,50,0 ; position wherever; just  try and get good view of terrain!
RotateEntity camera,30,0,0 ; again, try and get good view of terrain

light=CreateLight()
RotateEntity light,90,0,0

; Create terrain
terra=CreateTerrain(terra_size)
ScaleEntity terra,x_scale,y_scale,z_scale

; Texture terrain
grass_tex=LoadTexture("media/mossyground.bmp")
EntityTexture terra,grass_tex

; Create marker
marker=CreateSphere()
ScaleEntity marker,1,1,1
EntityColor marker,255,0,0

While Not KeyDown(1)

; Change marker position values depending on cursor key pressed
If KeyHit(205)=True Then marker_x=marker_x+1
If KeyHit(203)=True Then marker_x=marker_x-1
If KeyHit(208)=True Then marker_z=marker_z-1
If KeyHit(200)=True Then marker_z=marker_z+1

; Get terrain height at marker position
marker_y#=TerrainHeight(terra,marker_x,marker_z)

; If A pressed then increase marker_y value and modify terrain
If KeyDown(30)=True
If marker_y#<1 Then marker_y#=marker_y#+0.005
ModifyTerrain terra,marker_x,marker_z,marker_y#
EndIf

; If Z pressed then decrease marker_y value and modify terrain
If KeyDown(44)=True
If marker_y#>0 Then marker_y#=marker_y#-0.005
ModifyTerrain terra,marker_x,marker_z,marker_y#
EndIf

; Position marker, taking into account x, y and z scales of terrain
PositionEntity marker,marker_x*x_scale,marker_y#*y_scale,marker_z*z_scale

RenderWorld

Text 0,0,"Use cursor keys to move marker over the terrain"
Text 0,20,"Press A or Z to alter height of terrain at marker's position"
Text 0,40,"Terrain Height: "+marker_y#

Flip

Wend

End