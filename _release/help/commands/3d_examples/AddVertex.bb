Graphics3D 640,480
SetBuffer BackBuffer()

mesh = CreateMesh()
surf = CreateSurface(mesh)

v0 = AddVertex (surf, -5,-5,0,  0  ,0)
v1 = AddVertex (surf,  5,-5,0,  1  ,0)
v2 = AddVertex (surf,  0, 5,0,  0.5,1)

tri = AddTriangle (surf,v0,v2,v1)

cam = CreateCamera()
MoveEntity cam, 0,0,-7

RenderWorld
Flip

WaitKey
End