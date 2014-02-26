
;Blitz media viewer.
;
;Create executable in 'bin'

AppTitle CommandLine$()
fil$=Lower$( CommandLine$() )

index=Instr( fil$,"." )
If index>0 ext$=Mid$( fil$,index+1 )
Select ext$
Case "x","3ds"
	ShowModel( fil$,False )
Case "md2"
	ShowModel( fil$,True )
Case "bmp","jpg","png","pcx","tga","iff"
	ShowImage( fil$ )
Case "wav"
	ShowSound( fil$ )
Case "mp3","mid","mod","x3m","xm","it"
	ShowMusic( fil$ )
Default
	RuntimeError "Unknown File Extension"
End Select

End

Function ShowModel( fil$,md2 )
	If Windowed3D()
		Graphics3D 400,300,0,2
	Else
		Graphics3D 640,480,0,1
	EndIf
	If md2
		model=LoadMD2( fil$ )
		If model ScaleEntity model,.025,.025,.025
	Else
		model=LoadMesh( fil$ )
		If model FitMesh model,-1,-1,-1,2,2,2,True
	EndIf
	If model=0 RuntimeError "Unable to load 3D mesh:"+fil$
	sc=CountSurfaces(model)
	For k=1 To sc
		vc=vc+CountVertices( GetSurface( model,k ) )
		tc=tc+CountTriangles( GetSurface( model,k ) )
	Next
	camera=CreateCamera()
	CameraClsColor camera,0,0,64
	CameraRange camera,.01,10
	xr#=0:yr#=0:z#=2.1
	light=CreateLight()
	TurnEntity light,45,45,0
	Repeat
		RotateEntity model,xr,yr,0
		PositionEntity model,0,0,z
		UpdateWorld
		RenderWorld
		Text 0,0,"Triangles:"+tc+" Vertices:"+vc+" Surfaces:"+sc
		Flip
		key=False
		Repeat
			If KeyHit(1) End
			If KeyDown(200) xr=xr-3:key=True
			If KeyDown(208) xr=xr+3:key=True
			If KeyDown(203) yr=yr+3:key=True
			If KeyDown(205) yr=yr-3:key=True
			If KeyDown( 30) z=z-.1:key=True
			If KeyDown( 44) z=z+.1:key=True
			If Not key WaitKey
		Until key
	Forever
End Function

Function ShowImage( fil$ )
	Graphics 400,300,0,2
	SetBuffer BackBuffer()
	image=LoadImage( fil$ )
	If image=0 RuntimeError "Unable to load image:"+fil$
	MidHandle image
	x=200:y=150:t=4
	Repeat
		Cls
		DrawImage image,x,y
		Flip
		key=False
		Repeat
			If KeyHit(1) End
			If KeyDown(200) y=y-t:key=True
			If KeyDown(208) y=y+t:key=True
			If KeyDown(203) x=x-t:key=True
			If KeyDown(205) x=x+t:key=True
			If Not key WaitKey
		Until key
	Forever
End Function

Function ShowSound( fil$ )
	sound=LoadSound( fil$ )
	If sound=0 RuntimeError "Unable to load sound:"+fil$
	Repeat
		PlaySound sound
		WaitKey
		If KeyHit(1) End
	Forever
End Function

Function ShowMusic( fil$ )
	music=PlayMusic( fil$ )
	If music=0 RuntimeError "Unable to play music: "+fil$
	WaitKey
End Function