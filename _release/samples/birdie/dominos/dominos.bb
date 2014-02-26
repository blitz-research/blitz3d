; Silly collision demo
; Delete me
; dave@birdie72.freeserve.co.uk

Graphics3D 640,480
SetBuffer BackBuffer()
Const D_TYPE=1
Const DB_TYPE=2
Collisions DB_TYPE,D_TYPE,2,1

cam=CreateCamera():PositionEntity cam,0,30,-50
TurnEntity cam,20,0,0
lit=CreateLight()

Global domi=CreateCube()
tex=LoadTexture("tex0.bmp"):ScaleTexture tex,2.2,1
EntityTexture domi,tex
ScaleMesh domi,.1,1,.5
HideEntity domi
p=1
For a#=355 To 90 Step -2.7
	r.dom=add_domi(30*Cos(a),30*Sin(a),90+a,p)
	EntityColor r\ent,Rnd(255),Rnd(255),Rnd(255)
	p=p-1
Next
For a#=85 To 355 Step 2.7
	r.dom=add_domi(-30*Cos(a),60-30*Sin(a),90+a,p)
	EntityColor r\ent,Rnd(255),Rnd(255),Rnd(255)
	p=p-1
Next
For a=1 To 100 Step 1
	r.dom=add_domi(a,30,180,p)
	EntityColor r\ent,Rnd(255),Rnd(255),Rnd(255)

Next

cube=CreateCube()
FitMesh cube,-250,0,-250,500,500,500
FlipMesh cube
tex=LoadTexture( "floortile.bmp" )
ScaleTexture tex,1.0/3,1.0/3
EntityTexture cube,tex
EntityAlpha cube,.29
EntityFX cube,1
;mr=CreateMirror()

While Not KeyDown(1)
	If KeyDown(203) Then TurnEntity cam,0,2,0
	If KeyDown(205) Then TurnEntity cam,0,-2,0
	UpdateWorld
	RenderWorld
	Update_domi()	
	Flip
Wend

FreeEntity cam
EndGraphics
End

Type dom
	Field piv
	Field ent
	Field col
	Field fall
	Field start
	Field miss
End Type

Function Add_Domi.dom(x#,z#,ang#,f)
	a.dom=New dom
	a\piv=CreatePivot()
	a\ent=CopyEntity(domi,a\piv)
	PositionEntity a\ent,0,1,0
	PositionEntity a\piv,x,0,z
	TurnEntity a\piv,0,ang,0
	a\col=CreatePivot(a\piv)
	EntityRadius a\col,.015
	EntityType a\ent,D_TYPE
	PositionEntity a\col,0,2,0
	EntityType a\col,DB_TYPE
	a\miss=False
	If f=1 Then 
		a\fall=True
		a\start=True
	Else 
		a\fall=False
		a\start=False
	End If
	Return a
End Function

Function update_Domi()
	For a.dom=Each dom
		If a\miss=False Then
			If EntityRoll(a\piv)<70 Then
				If a\fall=True Then
					If CountCollisions(a\col)=0 Then 
						TurnEntity a\piv,0,0,20
					Else 
						a\fall=False
						ent=CollisionEntity (a\col,CountCollisions(a\col))
						b.dom=Find_Domi(ent)
						If b<>Null Then
							b\start=True
						End If
					End If
				End If
			Else
				a\miss=True
			End If
		End If
	Next
	For a.dom=Each dom
		If a\start=True
			If CountCollisions(a\col)=0 Then a\fall=True
		End If
	Next
End Function

Function Find_Domi.dom(ent)
	For a.dom=Each dom
		If a\ent=ent Then Return a
	Next
	Return Null
End Function