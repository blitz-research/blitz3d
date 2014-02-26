;-----------------------------------------------------------------------
; Binary Cage 1.1 (multi-texture)
; 07/17/01
; Written By Richard R Betson
; Copyright, 2001 Richard R Betson
; www.redeyeware.50megs.com
; vidiot@getgoin.net
;-----------------------------------------------------------------------
AppTitle "Binary Cage"
Include "../start.bb"
;Graphics3D 640,480,16

;----------------------------------
; Variables
;----------------------------------
Dim boxes(40)
z=60
y=-5
x=-5
Global speed#=.3 ;Camera Pull-Back speed
Global C_Rotation#=.7 ;Camera Rotation speed

AmbientLight 200,200,200 ;Set lighting

;----------------------------------
; Create Textures
;----------------------------------
texture1=CreateTexture(64,64)
SetBuffer TextureBuffer(texture1)
;Color 32,255,64
Color 255,32,64
Rect 0,0,64,64,1
Color 0,0,0
;Color 255,255,255
Rect 1,1,62,62,1

font=LoadFont("Arial",18,False,False,False)
SetFont font
;texture2=CreateTexture(64,64,1)
;SetBuffer TextureBuffer(texture2)
;Color 255,255,255
;Color 0,0,0
;Rect 0,0,64,64
Color 32,32,200
Text 0,32,"3D"
Text 0,48,"BB-3D"
SetBuffer BackBuffer()

;----------------------------------
; Create Camera
;----------------------------------
c_pivot=CreatePivot()
camera=CreateCamera(c_pivot)
CameraZoom camera,.7
CameraRange camera,.1,30
CameraFogMode camera,1 
CameraFogColor camera,16,0,128
CameraFogRange camera,10,60

;----------------------------------
; Create Boxes for Cage
;----------------------------------
box=CreateCube()
EntityTexture box,texture1,0,0
;EntityTexture box,texture2,0,1
UpdateNormals box
ScaleEntity box,5,5,5
EntityBlend box,3
EntityFX box,1
FlipMesh box

;hw=True
;HWMultiTex hw

;TextureBlend texture1,1
;TextureBlend texture2,3

;EntityBlend box,1

cage_pivot1=CreatePivot()
cage_pivot2=CreatePivot()

For b=1 To 20
boxes(b)=CopyEntity(box)
EntityParent boxes(b),cage_pivot1
Next
For b=21 To 40
boxes(b)=CopyEntity(box)
EntityParent boxes(b),cage_pivot2
Next
For b=1 To 20
PositionEntity boxes(b),y,x,z
y=y+5
ii=ii+1
If y=>5
y=-5
x=x+5
EndIf
If ii=10
x=-5
z=z-5
ii=0
EndIf
Next
For b=21 To 40
PositionEntity boxes(b),y,x,z
y=y+5
ii=ii+1
If y=>5
y=-5
x=x+5
;z=z-5

EndIf
If ii=10
x=-5
z=z-5
ii=0
EndIf
Next

FreeEntity box

;----------------------------------
; Line-up Camera and Cage
;----------------------------------
PositionEntity cage_pivot2,0,-10,-30
PositionEntity cage_pivot1,0,-40,-40
PositionEntity c_pivot,-2,-20,17
PositionEntity camera,0,0,0
TurnEntity c_pivot,-90,0,0


light=CreateLight()
;----------------------------------
; Main Loop
;----------------------------------
While Not KeyHit(1)

swap#=swap+speed ;Increment for cage swap

xx#=xx-speed ;increment to pull back camera

PositionEntity c_pivot,-2,-20+xx,17 ;Pull back parent entity of camera
TurnEntity camera,0,0,c_rotation ;Turn camera
PositionEntity light,-2,-10-xx,17
If swap=>25 ;Bring the furthest cage to the front
If cage=0
PositionEntity cage_pivot2,0,-40+xx,-30 ;Position cage just behind camera
cage=1
swap=0
Else
PositionEntity cage_pivot1,0,-40+xx,-40
swap=0
cage=0
EndIf
EndIf

UpdateWorld
RenderWorld 
Flip

Wend