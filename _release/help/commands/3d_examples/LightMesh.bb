Graphics3D 640,480 ,16,2
camera=CreateCamera() 

ent=CreateSphere()
EntityFX ent,2 ; enable vertex colors
Lightmesh ent,-255,-255,-255 ; reset vertex colors from 255,255,255 (default) to 0,0,0
LightMesh ent,255,255,0,50,-20,20,-20 ; apply fake lighting

MoveEntity camera,0,2,-10 
PointEntity camera,ent

While Not KeyDown(1) 
RenderWorld
Flip 
Wend
End