Graphics3D 640,480

camera = CreateCamera()
MoveEntity camera,0,0,-3

ball = CreateSphere()

lite = CreateLight(1) ; change this to 2 or 3 to see different lights
MoveEntity lite,5,0,0
PointEntity lite,ball ; make sure light is pointing at ball

While Not KeyDown(1)
RenderWorld:Flip
Wend
End