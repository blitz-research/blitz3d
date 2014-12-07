; CreatePivot Example
; -------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,0,0,-10

light=CreateLight()
RotateEntity light,90,0,0

; Create pivot. This is invisible.
pivot=CreatePivot()

; Create planet (i.e. a sphere). Make the pivot the parent of the planet.
planet=CreateSphere(16,pivot)

; Position planet so that it is offset from the pivot
PositionEntity planet,5,0,0

While Not KeyDown(1)

; Turn pivot, thus making planet spin around it
TurnEntity pivot,0,1,0

RenderWorld
Flip

Wend

End