Graphics3D 640,480

campivot = CreatePivot()
cam = CreateCamera(campivot)
MoveEntity cam,0,0,-5

sp = LoadSprite("grass.bmp")
SpriteViewMode sp,4

While Not KeyDown(1)
RenderWorld:Flip
TurnEntity campivot,1,1,3
Wend
End