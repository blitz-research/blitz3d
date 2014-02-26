;Example of exploding an entity
;David Bird
;dave@birdie72.freeserve.co.uk
;15/6/01 (beta 2.24)

Graphics3D 640,480
SetBuffer BackBuffer()

piv=CreatePivot()

cam=CreateCamera(piv)
PositionEntity cam,0,0,-5

mesh=CreateSphere(10)
tex=LoadTexture("tex0.bmp")
ScaleTexture tex,.5,.5
EntityTexture mesh,tex

While Not KeyDown(1)
	If KeyHit(57) Then Create_Explosion(mesh,tex)	;Space Bar to produce explosion
	If KeyDown(203) TurnEntity piv,0,-1,0			;Arrow keys to
	If KeyDown(205) TurnEntity piv,0,+1,0			;swing camera
	If KeyDown(200) TurnEntity piv,1,0,0			;round the object
	If KeyDown(208) TurnEntity piv,-1,0,0			;

	Update_Particles()
	UpdateWorld
	RenderWorld
	Text 0,0,"Hit <space> to blow up the sphere!"
	Flip
Wend

;
;Freeup memory used in demo
Free_Particles()
FreeEntity mesh			
FreeEntity cam
EndGraphics
End

Type particle
	Field ent
	Field surf
	Field dx#
	Field dy#
	Field dz#
	Field cnt
End Type

Function Update_Particles()
	;Each particle is a triangle
	;as there cnt is reduced and become less than
	;100 the particles alpha is reduced
	;to slowerly fade to nothing.
	For a.particle=Each particle
		TranslateEntity a\ent,a\dx,a\dy,a\dz
		TurnEntity a\ent,4*a\dx,4*a\dy,2
		a\cnt=a\cnt-1
		If a\cnt<100 Then
			EntityAlpha a\ent,Float(a\cnt)/100.0
		End If
		If a\cnt<1 Then 
			FreeEntity a\ent
			Delete a
		End If
	Next
End Function

Function Free_Particles()
	For a.particle=Each particle
		If a\ent Then FreeEntity a\ent
		Delete a
	Next
End Function

Function Create_Explosion(mesh,texture)
	surf=GetSurface(mesh,CountSurfaces(mesh))	;Get the surface from mesh
	nt=CountTriangles(surf)						;How many triangles are in there.
	
	For cnt=0 To nt-1 Step 2
		a.particle=New particle
		a\ent=CreateMesh()
		a\surf=CreateSurface(a\ent)
		ind1=TriangleVertex(surf,cnt,0);get the verices 0
		ind2=TriangleVertex(surf,cnt,1); ~   ~   --~--  1
		ind3=TriangleVertex(surf,cnt,2); ~   ~   --~--  2
		x#=VertexX(surf,ind1)	;retrieve vertex index 0 for this triangle
		y#=VertexY(surf,ind1)	;going tro use this for particle velocity
		z#=VertexZ(surf,ind1)
		a\dx=x*(Rnd(.015,.035))	;reduce velocity by a random number in x
		a\dy=y*(Rnd(.015,.035))	;reduce velocity by a random number in y
		a\dz=z*(Rnd(.015,.035))	;reduce velocity by a random number in z
		;Now recreate this tri as an entity
		AddVertex(a\surf,VertexX(surf,ind1)-x,VertexY(surf,ind1)-y,VertexZ(surf,ind1)-z,VertexU(surf,ind1),VertexV(surf,ind1))
		VertexColor a\surf,0,VertexRed(surf,ind1),VertexGreen(surf,ind1),VertexBlue(surf,ind1)
		AddVertex(a\surf,VertexX(surf,ind2)-x,VertexY(surf,ind2)-y,VertexZ(surf,ind2)-z,VertexU(surf,ind2),VertexV(surf,ind2))
		VertexColor a\surf,0,VertexRed(surf,ind2),VertexGreen(surf,ind2),VertexBlue(surf,ind2)
		AddVertex(a\surf,VertexX(surf,ind3)-x,VertexY(surf,ind3)-y,VertexZ(surf,ind3)-z,VertexU(surf,ind3),VertexV(surf,ind3))
		VertexColor a\surf,0,VertexRed(surf,ind3),VertexGreen(surf,ind3),VertexBlue(surf,ind3)

		;Now add the triangle to the entity
		;double sided		
		AddTriangle(a\surf,0,1,2)
		AddTriangle(a\surf,0,2,1)
		
		PositionEntity a\ent,x,y,z
		a\cnt=Rnd(150,200)
		EntityTexture a\ent,texture
	Next
End Function