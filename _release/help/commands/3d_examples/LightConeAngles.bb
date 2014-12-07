Graphics3D 640,480

camera = CreateCamera()
MoveEntity camera,0,0,-40

flat = CreatePlane(10)
TurnEntity flat,-90,0,0

lite = CreateLight(3) ; try different lights 1 to 3
MoveEntity lite,0,0,-15

While Not KeyDown(1)
RenderWorld:Flip
If KeyHit(57) Then ; press SPACEBAR to randomly change the 'cone' of light
LightConeAngles lite, Rand(120),Rand(120)
EndIf
Wend
End