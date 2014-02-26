; Level of detail ball
; Idea one.
; (c)2001 David Bird
; dave@birdie72.freeserve.co.uk

Graphics3D 640,480
SetBuffer BackBuffer()

piv=CreatePivot()
cam=CreateCamera(piv)
PositionEntity cam,0,0,-2
lit=CreateLight()
tex=LoadTexture("tex0.bmp")
Global refsphere=LoadMesh("geo1.3ds"):EntityTexture refsphere,tex
PositionEntity refsphere,0,0,0
HideEntity refsphere

sphere=CopyEntity(refsphere)
rad#=1
stp=1
While Not KeyDown(1)
	TurnEntity piv,0,1,0
	
	If KeyHit(200) And stp<5 Then
		stp=stp+1
		If dest<>0 Then FreeEntity dest:dest=0
		sphere=Complex_divide(sphere)
		EntityTexture sphere,tex
	End If
	If KeyHit(208) And stp>1 Then
		stp=stp-1
		If dest<>0 Then FreeEntity dest:dest=0
		sphere=Reduce_Complex(sphere)
		EntityTexture sphere,tex
	End If
	If KeyHit(17) Then wire=1-wire

	WireFrame wire

	UpdateWorld
	RenderWorld
	Text 0,0,"Sphere LOD five levels. (c)David Bird. dave@birdie72.freeserve.co.uk"
	Text 0,16,"W - for wireframe"
	Text 0,32,"Up arrow increase detail"
	Text 0,48,"down arrow decrease detail"
	
	Flip
Wend
FreeEntity sphere
FreeEntity refsphere
FreeEntity cam
EndGraphics
End

Function get_Differential#(rad#,x#,y#,z#)
	 Return rad/Sqr(x^2+y^2+z^2)
End Function

Function Sub_Divide_Face(mesh)
	destmesh=CreateMesh()
	surf=CreateSurface(destmesh)
	refsurf=GetSurface(mesh,CountSurfaces(mesh))
	cnt=CountTriangles(refsurf)
	For a=0 To cnt-1
		x1#=VertexX(refsurf,TriangleVertex(refsurf,a,0))
		y1#=VertexY(refsurf,TriangleVertex(refsurf,a,0))
		z1#=VertexZ(refsurf,TriangleVertex(refsurf,a,0))
		x2#=VertexX(refsurf,TriangleVertex(refsurf,a,1))
		y2#=VertexY(refsurf,TriangleVertex(refsurf,a,1))
		z2#=VertexZ(refsurf,TriangleVertex(refsurf,a,1))
		x3#=VertexX(refsurf,TriangleVertex(refsurf,a,2))
		y3#=VertexY(refsurf,TriangleVertex(refsurf,a,2))
		z3#=VertexZ(refsurf,TriangleVertex(refsurf,a,2))
		AddVertex surf,x1,y1,z1
		AddVertex surf,x2,y2,z2
		AddVertex surf,x3,y3,z3
		x4#=(x1+x2+x3)/3.0
		y4#=(y1+y2+y3)/3.0
		z4#=(z1+z2+z3)/3.0
		dif#=get_Differential#(1,x4,y4,z4)
		AddVertex surf,x4*dif,y4*dif,z4*dif
		AddTriangle surf,cnt2,cnt2+1,cnt2+3
		AddTriangle surf,cnt2+1,cnt2+2,cnt2+3
		AddTriangle surf,cnt2+2,cnt2,cnt2+3
		cnt2=cnt2+4
	Next
	UpdateNormals destmesh
	FreeEntity mesh
	Return destmesh
End Function

Function Sub_Divide_Edge(mesh)
	destmesh=CreateMesh()
	surf=CreateSurface(destmesh)
	refsurf=GetSurface(mesh,CountSurfaces(mesh))
	cnt=CountTriangles(refsurf)
	For a=0 To cnt-1
		x1#=VertexX(refsurf,TriangleVertex(refsurf,a,0))
		y1#=VertexY(refsurf,TriangleVertex(refsurf,a,0))
		z1#=VertexZ(refsurf,TriangleVertex(refsurf,a,0))
		x2#=VertexX(refsurf,TriangleVertex(refsurf,a,1))
		y2#=VertexY(refsurf,TriangleVertex(refsurf,a,1))
		z2#=VertexZ(refsurf,TriangleVertex(refsurf,a,1))
		x3#=VertexX(refsurf,TriangleVertex(refsurf,a,2))
		y3#=VertexY(refsurf,TriangleVertex(refsurf,a,2))
		z3#=VertexZ(refsurf,TriangleVertex(refsurf,a,2))
		x4#=(x1+x2)/2.0
		y4#=(y1+y2)/2.0
		z4#=(z1+z2)/2.0
		x5#=(x2+x3)/2.0
		y5#=(y2+y3)/2.0
		z5#=(z2+z3)/2.0
		x6#=(x3+x1)/2.0
		y6#=(y3+y1)/2.0
		z6#=(z3+z1)/2.0

		AddVertex surf,x1,y1,z1
		AddVertex surf,x2,y2,z2
		AddVertex surf,x3,y3,z3
		dif1#=get_Differential#(1,x4,y4,z4)
		dif2#=get_Differential#(1,x5,y5,z5)
		dif3#=get_Differential#(1,x6,y6,z6)
		AddVertex surf,x4*dif1,y4*dif1,z4*dif1
		AddVertex surf,x5*dif2,y5*dif2,z5*dif2
		AddVertex surf,x6*dif3,y6*dif3,z6*dif3

		AddTriangle surf,cnt2+0,cnt2+3,cnt2+5
		AddTriangle surf,cnt2+1,cnt2+4,cnt2+3
		AddTriangle surf,cnt2+2,cnt2+5,cnt2+3
		AddTriangle surf,cnt2+3,cnt2+4,cnt2+2
		cnt2=cnt2+6
	Next
	UpdateNormals destmesh
	FreeEntity mesh
	Return destmesh
End Function

Function Complex_Divide(mesh)
	destmesh=CreateMesh()
	surf=CreateSurface(destmesh)
	refsurf=GetSurface(mesh,CountSurfaces(mesh))
	cnt=CountTriangles(refsurf)
	For a=0 To cnt-1
		x1#=VertexX(refsurf,TriangleVertex(refsurf,a,0))
		y1#=VertexY(refsurf,TriangleVertex(refsurf,a,0))
		z1#=VertexZ(refsurf,TriangleVertex(refsurf,a,0))
		u1#=VertexU(refsurf,TriangleVertex(refsurf,a,0))
		v1#=VertexV(refsurf,TriangleVertex(refsurf,a,0))
		x2#=VertexX(refsurf,TriangleVertex(refsurf,a,1))
		y2#=VertexY(refsurf,TriangleVertex(refsurf,a,1))
		z2#=VertexZ(refsurf,TriangleVertex(refsurf,a,1))
		u2#=VertexU(refsurf,TriangleVertex(refsurf,a,1))
		v2#=VertexV(refsurf,TriangleVertex(refsurf,a,1))
		x3#=VertexX(refsurf,TriangleVertex(refsurf,a,2))
		y3#=VertexY(refsurf,TriangleVertex(refsurf,a,2))
		z3#=VertexZ(refsurf,TriangleVertex(refsurf,a,2))
		u3#=VertexU(refsurf,TriangleVertex(refsurf,a,2))
		v3#=VertexV(refsurf,TriangleVertex(refsurf,a,2))
		;Sub Divide face edge plus center of face
		x4#=(x1+x2)/2.0
		y4#=(y1+y2)/2.0
		z4#=(z1+z2)/2.0
		u4#=(u1+u2)/2.0
		v4#=(v1+v2)/2.0
		
		x5#=(x2+x3)/2.0
		y5#=(y2+y3)/2.0
		z5#=(z2+z3)/2.0
		u5#=(u2+u3)/2.0
		v5#=(v2+v3)/2.0

		x6#=(x3+x1)/2.0
		y6#=(y3+y1)/2.0
		z6#=(z3+z1)/2.0
		u6#=(u3+u1)/2.0
		v6#=(v3+v1)/2.0

		x7#=(x1+x2+x3)/3.0
		y7#=(y1+y2+y3)/3.0
		z7#=(z1+z2+z3)/3.0
		u7#=(u1+u2+u3)/3.0
		v7#=(v1+v2+v3)/3.0

		AddVertex surf,x1,y1,z1,u1,v1	;Original
		AddVertex surf,x2,y2,z2,u2,v2	;Original
		AddVertex surf,x3,y3,z3,u3,v3	;Original
		dif1#=get_Differential(1,x4,y4,z4)
		dif2#=get_Differential(1,x5,y5,z5)
		dif3#=get_Differential(1,x6,y6,z6)
		dif4#=get_Differential(1,x7,y7,z7)
		AddVertex surf,x4*dif1,y4*dif1,z4*dif1,u4,v4
		AddVertex surf,x5*dif2,y5*dif2,z5*dif2,u5,v5
		AddVertex surf,x6*dif3,y6*dif3,z6*dif3,u6,v6
		AddVertex surf,x7*dif4,y7*dif4,z7*dif4,u7,v7

		AddTriangle surf,cnt2+0,cnt2+3,cnt2+6
		AddTriangle surf,cnt2+6,cnt2+3,cnt2+1
		AddTriangle surf,cnt2+4,cnt2+6,cnt2+1
		AddTriangle surf,cnt2+2,cnt2+6,cnt2+4
		AddTriangle surf,cnt2+2,cnt2+5,cnt2+6
		AddTriangle surf,cnt2+5,cnt2+0,cnt2+6
		cnt2=cnt2+7
	Next
	UpdateNormals destmesh
	FreeEntity mesh
	Return destmesh
End Function

Function Reduce_Complex(mesh)
	destmesh=CreateMesh()
	surf=CreateSurface(destmesh)
	refsurf=GetSurface(mesh,CountSurfaces(mesh))
	cnt=CountTriangles(refsurf)
	For a=0 To cnt-1 Step 6
		x1#=VertexX(refsurf,TriangleVertex(refsurf,a+0,0))
		y1#=VertexY(refsurf,TriangleVertex(refsurf,a+0,0))
		z1#=VertexZ(refsurf,TriangleVertex(refsurf,a+0,0))
		u1#=VertexU(refsurf,TriangleVertex(refsurf,a+0,0))
		v1#=VertexV(refsurf,TriangleVertex(refsurf,a+0,0))
		
		x2#=VertexX(refsurf,TriangleVertex(refsurf,a+1,2))
		y2#=VertexY(refsurf,TriangleVertex(refsurf,a+1,2))
		z2#=VertexZ(refsurf,TriangleVertex(refsurf,a+1,2))
		u2#=VertexU(refsurf,TriangleVertex(refsurf,a+1,2))
		v2#=VertexV(refsurf,TriangleVertex(refsurf,a+1,2))

		x3#=VertexX(refsurf,TriangleVertex(refsurf,a+3,0))
		y3#=VertexY(refsurf,TriangleVertex(refsurf,a+3,0))
		z3#=VertexZ(refsurf,TriangleVertex(refsurf,a+3,0))
		u3#=VertexU(refsurf,TriangleVertex(refsurf,a+3,0))
		v3#=VertexV(refsurf,TriangleVertex(refsurf,a+3,0))
		
		AddVertex surf,x1,y1,z1,u1,v1	;Original
		AddVertex surf,x2,y2,z2,u2,v2	;Original
		AddVertex surf,x3,y3,z3,u3,v3	;Original
		AddTriangle surf,cnt2+0,cnt2+1,cnt2+2
		cnt2=cnt2+3
	Next
	UpdateNormals destmesh
	FreeEntity mesh
	Return destmesh
End Function