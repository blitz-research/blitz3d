
SetGfx()

Function SetGfx()
	If info1$<>""
		AppTitle info1$,"Exit "+info1$+" ?"
	EndIf
	
	FlushKeys()
	
	mode_cnt=CountGfxModes3D()
	If Not mode_cnt RuntimeError "Can't find any 3D graphics modes"
	
	mode=0	
	If Not Windowed3D() mode=1
	
	Graphics 640,480,16,2
	SetBuffer BackBuffer()
	
	image=LoadImage( "b3dlogo.jpg" )
	If Not image image=LoadImage( "../b3dlogo.jpg" )
	MidHandle image
	
	font=LoadFont( "verdana",16 )
	SetFont font
	
	tx=640+160
	nx=-160
	ty=280
	
	url$="www.blitzbasic.com"
	url_x=640-StringWidth( url$ )
	url_y=480-FontHeight()

	Repeat
		Cls
		
		DrawBlock image,320,144
					
		Color 0,255,0
		Text tx,ty+FontHeight()*0,info1$,True
		Text nx,ty+FontHeight()*1,info2$,True
		Text tx,ty+FontHeight()*2,info3$,True
		Text nx,ty+FontHeight()*3,info4$,True
	
		Color 255,255,255
		If mode=0
			Text tx,ty+FontHeight()*5,"Windowed",True
		Else
			Text tx,ty+FontHeight()*5,GfxModeWidth( mode )+","+GfxModeHeight( mode )+","+GfxModeDepth( mode ),True
		EndIf
		
		Color 255,0,0
		Text nx,ty+FontHeight()*7,"[Return] to begin",True
		Text tx,ty+FontHeight()*8,"[Arrows] change mode",True
		Text nx,ty+FontHeight()*9,"[Escape] to exit",True
		
		Color 0,0,255
		Text url_x,url_y,url$
		
		If KeyHit( 1 ) End
		If KeyHit( 28 )
			Cls:Flip
			Cls:Flip
			FreeFont font
			FreeImage image
			EndGraphics
			If mode
				Graphics3D GfxModeWidth(mode),GfxModeHeight(mode),GfxModeDepth(mode),1
			Else
				Graphics3D 640,480,0,2
			EndIf
			SetBuffer BackBuffer()
			Return
		EndIf
		If KeyHit( 203 )
			mode=mode-1
			If mode<0 Or (mode=0 And (Not Windowed3D()))
				mode=mode_cnt
			EndIf
		Else If KeyHit( 205 )
			mode=mode+1
			If mode>mode_cnt
				mode=0
				If Not Windowed3D() mode=1
			EndIf
		EndIf
		
		If tx>320 tx=tx-8
		If nx<320 nx=nx+8
		
		Flip
		
	Forever
	
End Function