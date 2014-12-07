Graphics3D 640,480

cam = CreateCamera()
MoveEntity cam,0,0,-5

sp = CreateSprite()
RotateSprite sp,20

RenderWorld:Flip
WaitKey
End