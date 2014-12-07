Graphics3D 640,480

camera = CreateCamera()
MoveEntity camera,0,0,-3

ball = CreateSphere()

lite = CreateLight(2) ; try different lights 1 to 3
MoveEntity lite,5,0,-5
PointEntity lite,ball

range# = 0.5
LightRange lite,range

While Not KeyDown(1)
RenderWorld:Flip
If KeyHit(57) Then ; hit SPACEBAR to increase light range
range = range + 0.5
LightRange lite,range
EndIf
Wend
End