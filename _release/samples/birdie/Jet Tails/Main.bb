;Motion Blur
;David Bird
;dave@birdie72.freeserve.co.uk

Graphics3D 640,480
SetBuffer BackBuffer()
lit=CreateLight()
Global demo=True
Global ax#,ay#,az#

cam=CreateCamera()
CameraRange cam,1,10000
CameraClsColor cam,50,50,255
PositionEntity cam,0,1,3

Global jet=LoadMesh("jet.x")
RotateMesh jet,0,90,0
HideEntity jet ; load and hide jet entity

ply.obj=Create_Player()

While Not KeyDown(1)
	PointEntity cam,ply\piv
	Add_Effect(ply\ent)
	Update_Players()
	Update_Effects()
	UpdateWorld
	RenderWorld
	dx#=(EntityX(ply\piv,True)-EntityX(cam))*.1
	dy#=((EntityY(ply\piv,True)+15)-EntityY(cam))*.1
	dz#=((EntityZ(ply\piv,True)-50)-EntityZ(cam))*.1
	TranslateEntity cam,dx,dy,dz
	
	Flip
Wend

FreeEntity cam
EndGraphics
End

Type Obj
	Field piv
	Field ent
	Field thrust#
	Field rudpitch#
	Field rudroll#
End Type


Function Create_Player.obj()
	a.obj=New obj
	a\piv=CreatePivot()
	a\ent=CopyEntity(jet,a\piv)
	Return a
End Function

Function Update_Players()
	For a.obj=Each obj
		Get_Rudder(a)
		TurnEntity a\piv,1*a\rudpitch,0,2*a\rudroll
		MoveEntity a\piv,0,0,-1*a\thrust
	Next
End Function

Function Get_Rudder(a.obj)
	If demo=True Then
		a\thrust=-.47
		ax=ax-.01
		If ax<.1 Then ax=Rnd(4)
		az=az-.01
		If az<.1 Then az=Rnd(4)
		a\rudpitch=ax
		a\rudroll=az
	Else
		a\rudpitch=-JoyY()
		a\rudroll=-JoyX()
		a\thrust=JoyZ()
	End If
End Function

Type eff
	Field ent
	Field alpha#
End Type


Function Add_effect(ent)
	a.eff=New eff
	a\ent=CopyEntity(ent)
	EntityParent a\ent,0
	a\alpha=.5
	EntityAlpha a\ent,a\alpha
	PositionEntity a\ent,EntityX(ent,True),EntityY(ent,True),EntityZ(ent,True)
	RotateEntity a\ent,EntityPitch(ent,True),EntityYaw(ent,True),EntityRoll(ent,True)
	ScaleEntity a\ent,.95,.95,.95
End Function

Function Update_effects()
	For a.eff=Each eff
		If a\alpha>0 Then
			a\alpha=a\alpha-.02
			EntityAlpha a\ent,a\alpha
		Else
			FreeEntity a\ent
			Delete a
		End If
	Next
End Function