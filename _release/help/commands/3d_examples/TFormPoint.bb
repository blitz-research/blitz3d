
Graphics3D 640, 480

cam = CreateCamera ()
MoveEntity cam, 5, 0, -5

box = CreateCube ()
MoveEntity box, 5, 0, 0

Repeat

	If KeyDown (203) TurnEntity cam, 0, 0.5, 0
	If KeyDown (205) TurnEntity cam, 0, -0.5, 0
	If KeyDown (200) MoveEntity cam, 0, 0, 0.1
	If KeyDown (208) MoveEntity cam, 0, 0, -0.1

	bx#	= EntityX (box)
	by#	= EntityY (box)
	bz#	= EntityZ (box)

	cx#	= EntityX (cam, 1)
	cy#	= EntityY (cam, 1)
	cz#	= EntityZ (cam, 1)
	
	TFormPoint 0, 0, 0, cam, box
	xdist# = TFormedX ()
	ydist# = TFormedY ()
	zdist# = TFormedZ ()
		
	UpdateWorld
	RenderWorld
	
	Text 0, 20, "Box's global position"
	Text 20, 40, "x: " + bx
	Text 20, 60, "y: " + by
	Text 20, 80, "z: " + bz
	
	Text 0, 120, "Camera's global position"
	Text 20, 140, "x: " + cx
	Text 20, 160, "y: " + cy
	Text 20, 180, "z: " + cz

	Text 0, 220, "Camera position relative to box"
	Text 20, 240, "x: " + xdist
	Text 20, 260, "y: " + ydist
	Text 20, 280, "z: " + zdist
	
	Flip

Until KeyHit (1)

End
