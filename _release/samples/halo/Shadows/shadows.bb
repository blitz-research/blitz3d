;=================
;Geometric Shadows
;=================
;by Joshua 'H A L O' Klint
;halo9@planetquake.com

Include "../start.bb"

camera=CreateCamera()
PositionEntity camera,40,0,-200

pivot=CreatePivot()

;Make our cube object
mesh=CreateCube()
t=LoadTexture("blitzlogo.bmp")
EntityTexture mesh,t
FreeTexture t
ScaleMesh mesh,10,10,10

;Create light and flare
l=CreateLight()
piv=CreatePivot()
PointEntity camera,piv
LightColor l,255,255,255
LightRange l,10
s=CreateSprite()
t=LoadTexture("flare0.bmp")
ScaleSprite s,10,10
EntityBlend s,3
EntityTexture s,t
FreeTexture t
EntityColor s,255,255,255
EntityParent s,l
PositionEntity l,30,0,-30
PointEntity l,piv
EntityParent l,piv

;Create wall
plane=CreatePlane()
t=LoadTexture("wall.bmp")
ScaleTexture t,100,100
EntityTexture plane,t
FreeTexture t
PositionEntity plane,0,0,100
RotateEntity plane,270,35,0
EntityType plane,10
EntityFX plane,1

Collisions 1,10,2,1

Dim tri(2)
Dim shadow(999,999,5)

;Create a pivot to keep track of the position of each vertex
mesh2=CreateCube()
EntityColor mesh2,0,0,0
EntityAlpha mesh2,0.2
EntityFX mesh2,1
EntityParent mesh2,0
For s=1 To CountSurfaces(mesh2)
	surf=GetSurface(mesh,s)
	For k=0 To CountTriangles(surf)-1
			tri(0)=TriangleVertex(surf,k,0)
			tri(1)=TriangleVertex(surf,k,1)
			tri(2)=TriangleVertex(surf,k,2)
		For i=0 To 2
			PositionEntity pivot,VertexX(surf,tri(i)),VertexY(surf,tri(i)),VertexZ(surf,tri(i))		
			shadow(s,k,i)=CopyEntity(pivot);CreatePivot()
			shadow(s,k,i+3)=CopyEntity(pivot);CreatePivot()
			PositionEntity shadow(s,k,i+3),EntityX(pivot),EntityY(pivot),EntityZ(pivot)
			EntityParent shadow(s,k,i+3),mesh
			EntityType shadow(s,k,i),1
		Next
	Next
Next

;Eliminate redundant pivots
For s=1 To CountSurfaces(mesh2)
	surf=GetSurface(mesh,s)
	For k=0 To CountTriangles(surf)-1
		For i=0 To 2
			For sd=1 To CountSurfaces(mesh2)
				surfd=GetSurface(mesh,s)
				For kd=0 To CountTriangles(surfd)-1
					For id=0 To 2
						If EntityX(shadow(s,k,i+3))=EntityX(shadow(sd,kd,id+3))
							If EntityY(shadow(s,k,i+3))=EntityY(shadow(sd,kd,id+3))
								If EntityZ(shadow(s,k,i+3))=EntityZ(shadow(sd,kd,id+3))
									If shadow(s,k,i+3)<>shadow(sd,kd,id+3)
										FreeEntity shadow(sd,kd,id+3)
										shadow(sd,kd,id+3)=shadow(s,k,i+3)
									EndIf
								EndIf
							EndIf						
						EndIf
					Next
				Next
			Next
		Next
	Next	
Next


While Not KeyHit(1)

TurnEntity mesh,1,1,0
TurnEntity piv,0,-1,0
RotateEntity piv,0,EntityYaw(piv),Cos(MilliSecs()/5)*20

;Here's how it works.  We have 6 pivots per triangle.  We already attached
;3 to each triangle.  Now we are going to take 3 more for each triangle, 
;position them at the light, point them at their corresponding vertices
;we attached to the model, and move them until they hit the wall.
	For s=1 To CountSurfaces(mesh)
	surf=GetSurface(mesh,s)
		For k=0 To CountTriangles(surf)-1
			For i=0 To 2				
				PositionEntity shadow(s,k,i),EntityX(l,1),EntityY(l,1),EntityZ(l,1)
				PositionEntity pivot,EntityX(shadow(s,k,i+3),1),EntityY(shadow(s,k,i+3),1),EntityZ(shadow(s,k,i+3),1)
				PointEntity shadow(s,k,i),pivot
				ResetEntity shadow(s,k,i)
				MoveEntity shadow(s,k,i),0,0,9999
			Next
		Next
	Next
	
UpdateWorld

If KeyDown(30) MoveEntity l,0,0,2
If KeyDown(44) MoveEntity l,0,0,-2
If KeyDown(203) TurnEntity mesh,0,1,0
If KeyDown(205) TurnEntity mesh,0,-1,0

;Now that are pivots have collided with the wall, we set the shadow mesh's
;vertices to their positions.
For s=1 To CountSurfaces(mesh2)
	surf=GetSurface(mesh2,s)
	For k=0 To CountTriangles(surf)-1
		For i=0 To 2
			v=TriangleVertex(surf,k,i)
			VertexCoords surf,v,EntityX(shadow(s,k,i)),EntityY(shadow(s,k,i)),EntityZ(shadow(s,k,i))
		Next
	Next
Next

RenderWorld
Flip
Wend
End