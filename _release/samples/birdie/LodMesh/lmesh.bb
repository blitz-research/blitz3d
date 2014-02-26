;Lod Mesh
;David Bird
;dave@birdie72.freeserve.co.uk
Graphics3D 640,480
SetBuffer BackBuffer()
lit=CreateLight()

cam=CreateCamera()
CameraRange cam,1,1000
PositionEntity cam,0,0,-100

;Load in mesh and texture
mesh=LoadMesh("dolphin.x")

tex=LoadTexture("dolphin.bmp")
EntityTexture mesh,tex
ScaleMesh mesh,.2,.2,.2

Get_Data(mesh)						;Store mesh inforamtion
;mesh=Create_New_Mesh(mesh,1)		;Reduce mesh by 1 triangle
;WireFrame True
While Not KeyDown(1)
	TurnEntity mesh,0,1,0

	If KeyDown(57) Then 
		mesh=Create_New_Mesh(mesh,1)	; Reduce mesh by 1 triangle
		EntityTexture mesh,tex
	End If
	tri_s=CountTriangles(GetSurface(mesh,CountSurfaces(mesh)))
	UpdateWorld
	RenderWorld
	Text 0,0,"Lod Mesh. (c)2001 David Bird, dave@birdie72.freeserve.co.uk"
	Text 0,16,"Press space to reduce triangles in mesh"
	Text 0,32,"Current triangle count="+tri_s
	Flip
Wend

FreeEntity cam
EndGraphics
End

Type vert
	Field x#
	Field y#
	Field z#
	Field u#
	Field v#
	Field oindex
	Field index
	Field nindex
	Field use
	Field cost#
	Field nei.vert
End Type

Type tri
	Field ind[3]
	Field mv[2]
	Field index
	Field tcost#
	Field use
End Type

Function Get_Data(mesh)
	surf=GetSurface(mesh,CountSurfaces(mesh))
	For index=0 To CountVertices(surf)-1
		a.vert=New vert
		a\index=index
		a\oindex=index
		a\use=True
		a\cost=1000000
		a\x=VertexX(surf,index)
		a\y=VertexY(surf,index)
		a\z=VertexZ(surf,index)
		a\u=VertexU(surf,index)
		a\v=VertexV(surf,index)
	Next
	For index=0 To CountTriangles(surf)-1
		b.tri=New tri
		b\use=True
		For c=0 To 2
			b\ind[c]=TriangleVertex(surf,index,c)
		Next
		Get_Tri_Cost#(b)
		cnt=cnt+1
		b\index=index
	Next
	
End Function

Function Find_Vert.vert(index)
	For a.vert=Each vert
		If a\index=index Then Return a
	Next
	Return Null
End Function

Function Create_New_Mesh(mesh,red)
	For it=1 To red
		del.tri=Get_Lowest_Cost_Tri()
		If del<>Null Then
			del\use=False
			reindex_tris(del\ind[del\mv[0]],del\ind[del\mv[1]])
		End If
	Next
	;find the lowest cost (used vert)
	rol#=EntityRoll(mesh)
	pit#=EntityPitch(mesh)
	yaw#=EntityYaw(mesh)
	px#=EntityX(mesh,True)
	py#=EntityY(mesh,True)
	pz#=EntityZ(mesh,True)

	surf=GetSurface(mesh,1)
	ClearSurface surf,False,True

	For b.tri=Each tri
		If b\use=True Then
			AddTriangle surf,b\ind[0],b\ind[1],b\ind[2]
		End If
	Next
	UpdateNormals mesh
	RotateEntity mesh,pit,yaw,rol
	PositionEntity mesh,px,py,pz
	Return mesh
End Function

Function Get_Lowest_Cost_Tri.tri()
	cost#=100000
	pnt.tri=Null
	For a.tri=Each tri
		If a\use=True Then
			If a\tcost<cost Then 
				cost=a\tcost
				pnt.tri=a
			End If
		End If
	Next
	Return pnt.tri
End Function

Function ReIndex_Tris(oldtri,newtri)
	For a.tri=Each tri
		p=0
		If a\ind[0]=oldtri Then a\ind[0]=newtri:p=1
		If a\ind[1]=oldtri Then a\ind[1]=newtri:p=1
		If a\ind[2]=oldtri Then a\ind[2]=newtri:p=1
		If p=1 Then Get_Tri_Cost(a)
	Next
End Function

Function Get_Tri_Cost#(a.tri)
	v0.vert=Find_Vert(a\ind[0])
	v1.vert=Find_Vert(a\ind[1])
	v2.vert=Find_Vert(a\ind[2])
	x0#=v0\x:y0#=v0\y:z0#=v0\z
	x1#=v1\x:y1#=v1\y:z1#=v1\z
	x2#=v2\x:y2#=v2\y:z2#=v2\z
	coord0#=Sqr((x0-x1)^2+(y0-y1)^2+(z0-z1)^2)
	coord1#=Sqr((x1-x2)^2+(y1-y2)^2+(z1-z2)^2)
	coord2#=Sqr((x2-x0)^2+(y2-y0)^2+(z2-z0)^2)
	
	If coord0<coord1 And coord0<coord2 Then p=0
	If coord1<coord0 And coord1<coord2 Then p=1
	If coord2<coord0 And coord2<coord1 Then p=2
	Select p
		Case 0
			a\mv[0]=1:a\mv[1]=0
		Case 1
			a\mv[0]=2:a\mv[1]=1
		Case 2
			a\mv[0]=0:a\mv[1]=2
	End Select
	a\tcost=coord1+coord2+coord3
	Return a\tcost
End Function