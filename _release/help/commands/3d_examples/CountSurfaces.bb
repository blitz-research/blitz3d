; LoadMesh Example
; ----------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
CameraClsColor camera,0,0,255

light=CreateLight()
RotateEntity light,90,0,0

drum=LoadMesh( "media/teapot.x" )
PositionEntity drum,0,0,10
RotateEntity drum,0,45,0

While Not KeyDown( 1 )
	RenderWorld
	
	; Count no. of surfaces in mesh
	count_surf=CountSurfaces(drum)
	Text 0,0,"This mesh has "+count_surf+" surface(s)"
	
	; Press space
	If KeyDown( 57 )=True Then surf=GetSurface(drum,1) : ClearSurface surf
	
	Flip
Wend

End