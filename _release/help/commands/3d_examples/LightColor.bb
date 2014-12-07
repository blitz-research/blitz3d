Graphics3D 640,480

camera = CreateCamera()
MoveEntity camera,0,0,-3

ball = CreateSphere()

lite = CreateLight() ; try different lights 1 to 3
MoveEntity lite,5,0,-15
PointEntity lite,ball


While Not KeyDown(1)
RenderWorld:Flip
If KeyHit(57) Then LightColor lite,Rnd(255),Rnd(255),Rnd(255) ; press SPACEBAR to try different light colors
Wend
End