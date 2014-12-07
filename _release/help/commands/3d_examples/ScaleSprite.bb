Graphics3D 640,480

cam = CreateCamera()
MoveEntity cam,0,0,-5

sp = CreateSprite()

size# = 1.0
While Not KeyDown(1)
RenderWorld:Flip
ScaleSprite sp,size,size
size = size + 0.01
Wend
End