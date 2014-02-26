;Fire & Particles scene
;David Bird
;dave@birdie72.freeserve.co.uk
Graphics3D 640,480

HidePointer
SetBuffer BackBuffer()

lit=CreateLight()

TurnEntity lit,30,0,0

piv=CreatePivot()
cam=CreateCamera(piv)
CameraRange cam,.1,1000
PositionEntity cam,0,8,-25
TurnEntity cam,10,0,0

; Pre load sprites ** Hint Copy Entity is faster and more efficient **
Global flame01=LoadSprite("smk01.bmp"):HideEntity flame01
Global Part=LoadSprite("particle.bmp"):HideEntity Part

mirror=CreateMirror()	;Stick a mirror in there for fun
cube=CreateCube():PositionEntity cube,0,2,0
;
tfire.fire=Add_Fire(0,0,0)
rad#=10	;Radius of path
While Not KeyDown(1)
	TurnEntity cube,0,1,0.2
	;Simple movement of fire.
	PositionEntity tfire\piv,xp#,1,zp#
	xp=rad*Cos(MilliSecs()/30)
	zp=rad*Sin(MilliSecs()/30)
	

	If KeyDown(203) TurnEntity piv,0,1,0
	If KeyDown(205) TurnEntity piv,0,-1,0
	If KeyDown(200) TurnEntity cam,1,0,0
	If KeyDown(208) TurnEntity cam,-1,0,0
	If KeyDown(44) MoveEntity cam,0,0,-.2
	If KeyDown(30) MoveEntity cam,0,0,.2
	
	;Press Space to change the colour of particles
	If KeyDown(57) Then
		add_particle(EntityX(tfire\piv),EntityY(tfire\piv),EntityZ(tfire\piv),Rnd(255),Rnd(255),Rnd(255))
	Else
		add_particle(EntityX(tfire\piv),EntityY(tfire\piv),EntityZ(tfire\piv))
	End If
	
	;Update all fires and particles
	Update_Fires()
	Update_Particles()

	UpdateWorld 
	RenderWorld
	Flip
Wend

;Clean Up everything
Erase_Particles()
Erase_flames()
Erase_Fires()
FreeEntity Part
FreeEntity Flame01
FreeEntity lit
FreeEntity cam
FreeEntity piv
EndGraphics
End

;Each Flame of the fire
Type flame
	Field ent
	Field ang#
	Field size#
	Field alph#
	Field dis#
	Field dx#, dy#, dz#
End Type

;The fire itself
Type fire
	Field piv
	; Direction
	Field dx#, dy#, dz#
End Type

;Hot ashes
Type particle
	Field ent
	Field alpha#
	Field dx#,dy#,dz#
	Field pop
End Type

;Add a flame to the fire
Function Add_flame(x#,y#,z#,size#=1,dis#=.016,dx#=0,dy#=0.3,dz#=0)
	a.flame=New flame
	a\ent=CopyEntity(flame01)
	PositionEntity a\ent,x,y,z
	a\alph=1
	a\size=size
	a\dis=dis
	a\ang=Rnd(360)
	ScaleSprite a\ent,a\size,a\size
	EntityColor a\ent,Rnd(150,255),Rnd(0,100),0
	a\dx=dx
	a\dy=dy
	a\dz=dz
End Function

;Update flames
Function Update_flames()
	For a.flame=Each flame
		If a\alph>0.01 Then
			a\alph=a\alph-a\dis
			EntityAlpha a\ent,a\alph
			RotateSprite a\ent,a\ang
			a\ang=a\ang+.2
			MoveEntity a\ent,a\dx,a\dy,a\dz
		Else
			FreeEntity a\ent
			Delete a
		End If
	Next
End Function

;Erase all flames
Function Erase_flames()
	For a.flame=Each flame
		If a\ent<>0 Then FreeEntity a\ent
	Next
	Delete Each flame
End Function

;Update all fires
Function Update_Fires()
	For a.fire=Each fire
		Add_Flame(EntityX(a\piv),EntityY(a\piv),EntityZ(a\piv),Rnd(1,4),.04,a\dx,a\dy,a\dz)
	Next
	Update_Flames()
End Function

;Erase all fires
Function Erase_Fires()
	For a.fire=Each fire
		If a\piv<>0 Then FreeEntity a\piv
	Next
	Delete Each fire
End Function

;Add a fire to the scene
Function Add_Fire.fire(x#,y#,z#,dx#=0,dy#=.23,dz#=0)
	a.fire=New fire
	a\piv=CreatePivot()
	PositionEntity a\piv,x,y,z
	a\dx=dx:a\dy=dy:a\dz=dz
	Return a
End Function

;Add a particle to the scene
Function Add_Particle(x#,y#,z#,r=255,g=255,b=255)
	a.particle=New particle
	a\ent=CopyEntity(Part)
	PositionEntity a\ent,x,y,z
	a\dx=Rnd(-.1,.1)
	a\dy=Rnd(0.1,.7)
	a\dz=Rnd(-.1,.1)
	ScaleSprite a\ent,Rnd(.1,.2),Rnd(.1,.2)
	a\alpha=1
	a\pop=False
	EntityColor a\ent,r,g,b
End Function

;Update all particles
Function Update_Particles()
	For a.particle = Each particle
		MoveEntity a\ent,a\dx,a\dy,a\dz
		If EntityY(a\ent)<.3 Then 
			a\dy=-a\dy
			a\dy=a\dy*.62
			a\pop=True
		End If
		a\dy=a\dy-.02

		If a\pop Then
			a\alpha=a\alpha-.02
			EntityAlpha a\ent,a\alpha
			If a\alpha<0.05 Then
				FreeEntity a\ent
				Delete a
			End If
		End If
	Next
End Function

;Erase all particles
Function Erase_Particles()
	For a.particle = Each particle
		If a\ent<>0 Then FreeEntity a\ent
	Next
	Delete Each particle
End Function