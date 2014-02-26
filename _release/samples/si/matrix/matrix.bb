; ------------------------------------
; Name:		Matrix Command Set
; Version:	V1.0
; Author:	Simon Harrison
; Email:	si@si-design.co.uk
; Website:	http://www.si-design.co.uk
; Date:		18/09/01
; ------------------------------------

Dim db_matrix(65535)
Dim db_matrix_data(65535,5)
Dim db_image(65535)


; ------------------
; Name: DB_LoadImage
; ------------------

Function DB_LoadImage(filename$,image_no)

	db_image(image_no)=LoadTexture(filename$)

End Function


; ----------------
; Name: MakeMatrix
; ----------------

Function MakeMatrix(no,width#,depth#,xseg#,zseg#)

	db_matrix_data(no,0)=width#
	db_matrix_data(no,1)=depth#
	db_matrix_data(no,2)=xseg#
	db_matrix_data(no,3)=zseg#
	
	db_backdrop=True
	
	db_matrix(no)=CreateMesh()
	surf=CreateSurface(db_matrix(no))

	For z#=1 To depth# : z#=z#-1
		For x#=1 To width# : x#=x#-1
	
			vert0=AddVertex(surf,x#,0,z#)
			vert1=AddVertex(surf,x#,0,z#+(depth#/zseg#))
			vert2=AddVertex(surf,x#+(width#/xseg#),0,z#+(depth#/zseg#))
			vert3=AddVertex(surf,x#+(width#/xseg#),0,z#)
						
			tri0=AddTriangle(surf,vert0,vert1,vert2)
			tri1=AddTriangle(surf,vert0,vert2,vert3)
			
			VertexNormal surf,vert0,0,1,0
			VertexNormal surf,vert1,0,1,0
			VertexNormal surf,vert2,0,1,0
			VertexNormal surf,vert3,0,1,0

			x#=x#+(width#/xseg#)
		Next
		z#=z#+(depth#/zseg#)
	Next

End Function


; --------------------
; Name: PositionMatrix
; --------------------

Function PositionMatrix(no,x#,y#,z#)

	PositionEntity db_matrix(no),x#,y#,z#

End Function


; --------------------------
; Name: PrepareMatrixTexture
; --------------------------

Function PrepareMatrixTexture(matrix_no,image_no,across,down)

	db_matrix_data(matrix_no,4)=across
	db_matrix_data(matrix_no,5)=down

	brush=CreateBrush()
	BrushTexture brush,db_image(image_no)
	
	surf=GetSurface(db_matrix(matrix_no),1)
	PaintSurface surf,brush

End Function


; ----------------
; Name: FillMatrix
; ----------------

Function FillMatrix(matrix_no,height#,tile_no)

	tile_no=tile_no-1

	xseg=db_matrix_data(matrix_no,2)
	zseg=db_matrix_data(matrix_no,3)
	across=db_matrix_data(matrix_no,4)
	down=db_matrix_data(matrix_no,5)

	surf=GetSurface(db_matrix(matrix_no),1)
	cv=CountVertices(surf)
	
	; Set height
	For z=0 To zseg
		For x=0 To xseg
			
			index=(x*4)+(z*(xseg*4))
			
			If index>=0 And index<cv Then VertexCoords surf,index,VertexX#(surf,index),height#,VertexZ#(surf,index) : index=index-1 : Else index=index-1 
			If index>=0 And index<cv Then VertexCoords surf,index,VertexX#(surf,index),height#,VertexZ#(surf,index) : index=index-((xseg*4)+1) Else index=index-((xseg*4)+1) 
			If index>=0 And index<cv Then VertexCoords surf,index,VertexX#(surf,index),height#,VertexZ#(surf,index) : index=index+3 : Else index=index+3 
			If index>=0 And index<cv Then VertexCoords surf,index,VertexX#(surf,index),height#,VertexZ#(surf,index)
			
		Next
	Next
	
	; Set tiles
	For z=0 To zseg-1
		For x=0 To xseg-1
		
			index=(x*4)+(z*(xseg*4))
		
			useg#=(1.0/across)
			vseg#=(1.0/down)
	
			x_tile=tile_no Mod across
			y_tile=(down-1)-((tile_no-x_tile)/across)

			u#=useg#*x_tile
			v#=1-(vseg#*y_tile)

			VertexTexCoords surf,index,(u#)+0.002,(v#)-0.002
			VertexTexCoords surf,index+1,(u#)+0.002,(v#-vseg#)+0.002
			VertexTexCoords surf,index+2,(u#+useg#)-0.002,(v#-vseg#)+0.002
			VertexTexCoords surf,index+3,(u#+useg#)-0.002,(v#)-0.002
			
		Next
	Next

End Function


; ---------------------
; Name: RandomizeMatrix
; ---------------------

Function RandomizeMatrix(matrix_no,height_range#)

	xseg=db_matrix_data(matrix_no,2)
	zseg=db_matrix_data(matrix_no,3)

	surf=GetSurface(db_matrix(matrix_no),1)
	cv=CountVertices(surf)
	
	For z=0 To zseg
		For x=0 To xseg
		
			height#=Rnd(0,height_range#)
			
			index=(x*4)+(z*(xseg*4))
			
			If index>=0 And index<cv Then VertexCoords surf,index,VertexX#(surf,index),height#,VertexZ#(surf,index) : index=index-1 : Else index=index-1
			If index>=0 And index<cv Then VertexCoords surf,index,VertexX#(surf,index),height#,VertexZ#(surf,index) : index=index-((xseg*4)+1) : Else index=index-((xseg*4)+1)
			If index>=0 And index<cv Then VertexCoords surf,index,VertexX#(surf,index),height#,VertexZ#(surf,index) : index=index+3 : Else index=index+3
			If index>=0 And index<cv Then VertexCoords surf,index,VertexX#(surf,index),height#,VertexZ#(surf,index)
		
		Next
	Next

End Function


; ---------------------
; Name: SetMatrixHeight
; ---------------------

Function SetMatrixHeight(matrix_no,x,z,height#)

	xseg=db_matrix_data(matrix_no,2)

	surf=GetSurface(db_matrix(matrix_no),1)
	cv=CountVertices(surf)

	index=(x*4)+(z*(xseg*4))

	If index>=0 And index<cv Then VertexCoords surf,index,VertexX#(surf,index),height#,VertexZ#(surf,index) : index=index-1 : Else index=index-1
	If index>=0 And index<cv Then VertexCoords surf,index,VertexX#(surf,index),height#,VertexZ#(surf,index) : index=index-((xseg*4)+1) : Else index=index-((xseg*4)+1)
	If index>=0 And index<cv Then VertexCoords surf,index,VertexX#(surf,index),height#,VertexZ#(surf,index) : index=index+3 : Else index=index+3
	If index>=0 And index<cv Then VertexCoords surf,index,VertexX#(surf,index),height#,VertexZ#(surf,index)
	
End Function


; ---------------------
; Name: SetMatrixNormal
; ---------------------

Function SetMatrixNormal(matrix_no,x,z,nx#,ny#,nz#)

	xseg=db_matrix_data(matrix_no,2)

	surf=GetSurface(db_matrix(matrix_no),1)
	cv=CountVertices(surf)

	index=(x*4)+(z*(xseg*4))

	If index>=0 And index<cv Then VertexNormal surf,index,nx#,ny#,nz# : index=index-1 : Else index=index-1
	If index>=0 And index<cv Then VertexNormal surf,index,nx#,ny#,nz# : index=index-((xseg*4)+1) : Else index=index-((xseg*4)+1)
	If index>=0 And index<cv Then VertexNormal surf,index,nx#,ny#,nz# : index=index+3 : Else index=index+3
	If index>=0 And index<cv Then VertexNormal surf,index,nx#,ny#,nz#
	
End Function


; -------------------
; Name: SetMatrixTile
; -------------------

Function SetMatrixTile(matrix_no,x,z,tile_no)

	tile_no=tile_no-1
	
	xseg=db_matrix_data(matrix_no,2)
	across=db_matrix_data(matrix_no,4)
	down=db_matrix_data(matrix_no,5)
	
	surf=GetSurface(db_matrix(matrix_no),1)
	
	index=(x*4)+(z*(xseg*4))
		
	useg#=(1.0/across)
	vseg#=(1.0/down)
	
	x_tile=tile_no Mod across
	y_tile=(down-1)-((tile_no-x_tile)/across)

	u#=useg#*x_tile
	v#=1-(vseg#*y_tile)

	VertexTexCoords surf,index,(u#)+0.002,(v#)-0.002
	VertexTexCoords surf,index+1,(u#)+0.002,(v#-vseg#)+0.002
	VertexTexCoords surf,index+2,(u#+useg#)-0.002,(v#-vseg#)+0.002
	VertexTexCoords surf,index+3,(u#+useg#)-0.002,(v#)-0.002

End Function


; -------------------
; Name: GhostMatrixOn
; -------------------

Function GhostMatrixOn(no)

	EntityBlend db_matrix(no),3

End Function


; --------------------
; Name: GhostMatrixOff
; --------------------

Function GhostMatrixOff(no)

	EntityBlend db_matrix(no),0

End Function


; ------------------
; Name: DeleteMatrix
; ------------------

Function DeleteMatrix(no)
	
	FreeEntity db_matrix(no)
	
	db_matrix(no)=0
	db_matrix_data(no,0)=0
	db_matrix_data(no,1)=0
	db_matrix_data(no,2)=0
	db_matrix_data(no,3)=0
	db_matrix_data(no,4)=0
	db_matrix_data(no,5)=0

End Function


; -------------------
; Name: MatrixExist()
; -------------------

Function MatrixExist(no)

	If db_matrix(no)<>0 Then Return True Else Return False

End Function


; ---------------------
; Name: MatrixPositionX
; ---------------------

Function MatrixPositionX(no)

	EntityX#(db_matrix(no))

End Function


; ---------------------
; Name: MatrixPositionY
; ---------------------

Function MatrixPositionY(no)

	EntityY#(db_matrix(no))

End Function


; ---------------------
; Name: MatrixPositionZ
; ---------------------

Function MatrixPositionZ(no)

	EntityZ#(db_matrix(no))

End Function


; ------------------------
; Name: GetMatrixHeight#()
; ------------------------

Function GetMatrixHeight#(matrix_no,x,z)

	xseg=db_matrix_data(matrix_no,2)

	surf=GetSurface(db_matrix(matrix_no),1)
	cv=CountVertices(surf)

	index=(x*4)+(z*(xseg*4))
	If index>=0 And index<cv
		y#=VertexY#(surf,index)
	Else
		index=index-1
		If index>=0 And index<cv
			y#=VertexY#(surf,index)
		Else
			index=index-((xseg*4)+1)
			If index>=0 And index<cv
				y#=VertexY#(surf,index)
			Else
				index=index+3
				If index>=0 And index<cv
					y#=VertexY#(surf,index)
				EndIf
			EndIf
		EndIf
	EndIf

	Return y#
	
End Function


; ------------------------
; Name: GetGroundHeight#()
; ------------------------

Function GetGroundHeight#(matrix_no,x_pos#,z_pos#)

	width#=db_matrix_data(matrix_no,0)
	depth#=db_matrix_data(matrix_no,1)
	xseg#=db_matrix_data(matrix_no,2)
	zseg#=db_matrix_data(matrix_no,3)
	
	x#=x_pos#/(width#/xseg#)
	z#=z_pos#/(depth#/zseg#)

	ix=Floor#(x#) ;integer parts of x,z
	iz=Floor#(z#)
	
	fx#=x#-ix ;fractional remainders
	fz#=z#-iz

	surf=GetSurface(db_matrix(matrix_no),1)
	cv=CountVertices(surf)
	
	index=(ix*4)+(iz*(xseg#*4))
	If index>=0 And index<cv Then y00#=VertexY#(surf,index) : index=index+1 : Else index=index+1
	If index>=0 And index<cv Then y01#=VertexY#(surf,index) : index=index+1 : Else index=index+1
	If index>=0 And index<cv Then y11#=VertexY#(surf,index) : index=index+1 : Else index=index+1
	If index>=0 And index<cv Then y10#=VertexY#(surf,index)
	
	ya#=(y10-y00)*fx+y00
	yb#=(y11-y01)*fx+y01
	y#=(yb-ya)*fz+ya
	
	Return y#
	
End Function


; -----------------------
; Name: MatrixTileCount()
; -----------------------

Function MatrixTileCount()

	across=db_matrix_data(matrix_no,4)
	down=db_matrix_data(matrix_no,5)

	Return across*down

End Function


; ------------------------
; Name: MatrixTilesExist()
; ------------------------

Function MatrixTilesExist()

	across=db_matrix_data(matrix_no,4)
	down=db_matrix_data(matrix_no,5)

	If across*down<>0 Then Return True Else Return False

End Function


; ----------------------------
; Name: MatrixWireframeState()
; ----------------------------

Function MatrixWireframeState()

	Return 0

End Function