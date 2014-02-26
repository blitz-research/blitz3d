;
;	Functions
;
;	(c)David Bird
;	dave@birdie72.freeserve.co.uk

;
;	Types Universal
;
Type vector
	Field x#
	Field y#
	Field z#
	Field u#
	Field v#
End Type
;
;	Globals
;
Global G_width
Global G_height
Global G_Depth

Global Bearing.vector=New vector
Global Normal.vector=New vector
Global CProd.vector=New vector
Global Tnorm.vector=New vector

Global DProd#
;
;Cross and DotProduct functions
Function CrossProduct(x1#,y1#,z1#,x2#,y2#,z2#)
	CProd\x=(y1*z2)-(z1*y2)
	CProd\y=(z1*x2)-(x1*z2)
	CProd\z=(x1*y2)-(y1*x2)
End Function
Function DotProduct#(x1#,y1#,z1#,x2#,y2#,z2#)
	DProd=((x1*x2)+(y1*y2)+(z1*z2))
	Return DProd
End Function
;Return Cross product answers
Function CproductX#()
	Return CProd\x
End Function
Function CproductY#()
	Return CProd\y
End Function
Function CproductZ#()
	Return CProd\z
End Function
;Return Dot product answers
Function DProduct#()
	Return DProd
End Function



Function Delete_Vectors()
	Delete Each vector
End Function

;
;	Math Functions
;
;Bearing between two entity's
Function GetBearingVector(ent1,ent2)
	Bearing\x#=EntityX(ent2)-EntityX(ent1)
	Bearing\y#=EntityY(ent2)-EntityY(ent1)
	Bearing\z#=EntityZ(ent2)-EntityZ(ent1)
	Normalise(Bearing)
End Function

;Return Bearing
Function BearingX#()
	Return Bearing\x
End Function
Function BearingY#()
	Return Bearing\y
End Function
Function BearingZ#()
	Return Bearing\z
End Function

Function Normalise(a.vector)
	l# = Mag(a\x,a\y,a\z)
	a\x=a\x/l
	a\y=a\y/l
	a\z=a\z/l
	Return
End Function


; Normalise x/y/z store answer in Normal.vector
Function Norm(x#,y#,z#)
	l# = Mag(x,y,z)
	Normal\x=x/l
	Normal\y=y/l
	Normal\z=z/l
End Function
Function NormX#()
	Return Normal\x
End Function
Function NormY#()
	Return Normal\y
End Function
Function NormZ#()
	Return Normal\z
End Function
; *********************************************


Function Mag#(x#,y#,z#)
	Return Sqr(x^2+y^2+z^2)
End Function

Function TurntoFace(ent1,ent2,tol#)
	GetBearingVector(ent1,ent2)
	dx#=BearingX()*360/tol
	dy#=BearingY()*360/tol
	dz#=BearingZ()*360/tol
	TurnEntity ent1,-dy,-dx,0
End Function

Function range#(ent1,ent2)
	x#=EntityX(ent2)-EntityX(ent1)
	y#=EntityY(ent2)-EntityY(ent1)
	z#=EntityZ(ent2)-EntityZ(ent1)
	Return Sqr(x^2+y^2+z^2)
End Function
Function range2d#(ent1,ent2)
	x#=EntityX(ent2)-EntityX(ent1)
	z#=EntityZ(ent2)-EntityZ(ent1)
	Return Sqr(x^2+z^2)
End Function
Function rangepoint#(x1#,y1#,z1#,x2#,y2#,z2#)
	x#=x1-x2
	y#=y1-y2
	z#=z1-z2
	Return Sqr(x^2+y^2+z^2)
End Function

Function SetGraphics(tag3d,targetwidth)
	;tag3d - true to setup graphics mode, false for normal mode
	;targetwidth - aim for width
	wid=0
	mode=0
	For a=1 To CountGfxModes()
		g_width=GfxModeWidth(a)
		g_height=GfxModeHeight(a)
		g_depth=GfxModeDepth(a)
		If g_width>wid And g_width<=targetwidth Then 
			wid=g_width
			hig=g_height
			dep=g_depth
			mode=a
		End If
	Next 
	If wid>0 Then
		If tag3d=True Then
			Graphics3D wid,hig
		Else
			Graphics wid,hig
		End If
		g_width=wid
		g_height=hig
		g_depth=dep
	End If
End Function

;
;	Get Normal of a picked point
;	On a terrain
;
Function Get_Terrain_Normal(terr,x#,z#)
	x1#=x:z1#=z:y1#=TerrainY(terr,x1,0,z1)
	x2#=x+.5:z2#=z:y2#=TerrainY(terr,x2,0,z2)
	x3#=x:z3#=z+.5:y3#=TerrainY(terr,x3,0,z3)
	
	xx1#=x2-x1:yy1#=y2-y1:zz1#=z2-z1
	xx2#=x3-x1:yy2#=y3-y1:zz2#=z3-z1
	Norm(xx1,yy1,zz1)
	xx1=NormX():yy1=normy():zz1=normz()
	Norm(xx2,yy2,zz2)
	xx2=NormX():yy2=normy():zz2=normz()
	CrossProduct(xx2,yy2,zz2,xx1,yy1,zz1)
	TNorm\x=CProductX()
	TNorm\y=CProductY()
	TNorm\z=CProductZ()
End Function

Function TNormalX#()
	Return Tnorm\x
End Function
Function TNormalY#()
	Return Tnorm\y
End Function
Function TNormalZ#()
	Return Tnorm\z
End Function
;
;
;

Function Vect_Product#(a.vector,b.vector)
	DProd=((a\x*b\x)+(a\y*b\y)+(a\z*b\z))
	Return DProd
End Function

Function distance_to_plane#(x1#,y1#,z1#,x2#,y2#,z2#,x3#,y3#,z3#)
	
End Function