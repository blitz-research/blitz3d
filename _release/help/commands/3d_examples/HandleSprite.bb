Graphics3D 640,480

cam = CreateCamera()
MoveEntity cam,0,0,-5

sp = CreateSprite()

handlepos# = 0.0
While Not KeyDown(1)
RenderWorld
Color 100,100,100
Plot 320,240
Text 320,250,"Handle",True
Flip
HandleSprite sp,handlepos,handlepos
handlepos = handlepos + 0.01
Wend
End