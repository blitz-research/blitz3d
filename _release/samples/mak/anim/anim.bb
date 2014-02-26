
Global info1$="Animation demo"
Global info2$="Hold down <return> to run"
Global info3$="Hit <space> to toggle transitions"

Include "../start.bb"

mesh_3ds=LoadAnimMesh( "makbot\mak_robotic.3ds" )	;anim seq 0
LoadAnimSeq mesh_3ds,"makbot\mak_running.3ds"		;anim seq 1
PositionEntity mesh_3ds,-15,-15,0

mesh_x=LoadAnimMesh( "makbot\mak_robotic.x" )		;anim seq 0
LoadAnimSeq mesh_x,"makbot\mak_running.x"			;anim seq 1
PositionEntity mesh_x,+15,-15,0

pivot=CreatePivot()
cam=CreateCamera( pivot )
PositionEntity cam,0,0,-100

lit=CreateLight()
RotateEntity lit,45,45,0

Animate mesh_3ds,2	;start ping-pong anims...
Animate mesh_x,  2

trans=10

While Not KeyHit(1)

	If KeyHit(57)
		trans=10-trans
	EndIf

	If KeyDown(28)
		If AnimSeq(mesh_3ds)=0 Animate mesh_3ds,1,.5,1,trans
		If AnimSeq(mesh_x  )=0 Animate mesh_x,  1,.5,1,trans
	Else
		If AnimSeq(mesh_3ds)=1 Animate mesh_3ds,2,1,0,trans
		If AnimSeq(mesh_x  )=1 Animate mesh_x  ,2,1,0,trans
	EndIf
	
	If KeyDown(30) MoveEntity cam,0,0,10
	If KeyDown(44) MoveEntity cam,0,0,-10
	
	If KeyDown(203) TurnEntity pivot,0,3,0
	If KeyDown(205) TurnEntity pivot,0,-3,0

	UpdateWorld
	RenderWorld
	Text 0,FontHeight()*0,"Anim seq: "+AnimSeq( mesh_3ds )
	Text 0,FontHeight()*1,"Anim len: "+AnimLength( mesh_3ds )
	Text 0,FontHeight()*2,"Anim time:"+AnimTime( mesh_3ds )
	
	Text 0,FontHeight()*4,"Anim seq: "+AnimSeq( mesh_x )
	Text 0,FontHeight()*5,"Anim len: "+AnimLength( mesh_x )
	Text 0,FontHeight()*6,"Anim time:"+AnimTime( mesh_x )
	
	Text 0,FontHeight()*8,"Transition time="+trans
	Flip
	
Wend

End