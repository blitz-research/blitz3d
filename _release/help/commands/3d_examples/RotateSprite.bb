Graphics3D 640,480

cam = CreateCamera()
MoveEntity cam,0,0,-5

sp = CreateSprite()

ang# = 0
While Not KeyDown(1)
RenderWorld:Flip
RotateSprite sp,ang
ang = ang +3
Wend
End