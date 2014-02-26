
;Logo()
SetGfx()

Type GfxMode
	Field width,height,depth
End Type

Function Logo()
	Graphics3D 640,480
	SetBuffer BackBuffer()
	image=LoadImage( "b3dlogo.jpg" )
	If image=0 image=LoadImage( "../b3dlogo.jpg" )
	MidHandle image
	Color 255,0,0
	SetFont LoadFont( "helvetica",16 )
	t=MilliSecs()
	txt1$="(C)opyright Blitz Research Limited"
	txt2$="Published by Guildhall Leisure Services"
	txt3$="Find out more at www.blitz-3d.com"
	yy=320
	Repeat
		Cls
		DrawBlock image,320,yy-120
		Color 0,0,192
		Text 0,480-FontHeight()*2,txt1$
		Text 0,480-FontHeight()*1,txt2$
		Color 0,255,0
		Text 640-StringWidth(txt3$),480-FontHeight()*1,txt3$
		Color 255,0,0
		Text 320,yy+FontHeight()*0,info1$,True,True
		Text 320,yy+FontHeight()*1,info2$,True,True
		Text 320,yy+FontHeight()*2,info3$,True,True
		Text 320,yy+FontHeight()*3,info4$,True,True
		d=MilliSecs()-t
		If d>5000
			If tick=0
				tick=127
				FlushKeys
			Else
				tick=tick-1
				If tick=0 tick=127
			EndIf
			If tick And 64
				Color 255,255,255 
				Text 320,yy+FontHeight()*5,"Press any key",True,True
			EndIf
			If GetKey() Then Exit
		EndIf
		Flip
	Forever
	For k=1 To 2
		Cls:Flip
	Next
	FreeImage image
	FlushKeys
End Function

Function SetGfx()
	Print info1$:Print info2$:Print info3$:Print info4$:Print
	If Windowed3D()
		yn$=Input$( "Use windowed mode?" )
		If Left$( Lower$( yn$ ),1 )="y"
			HidePointer
			Graphics3D 640,480,0,2
			SetBuffer BackBuffer()
			Return
		EndIf
	EndIf
	
	Print ""
	Print "Display drivers:"
	Print "----------------"
	For k=1 To CountGfxDrivers()
		Print k+":"+GfxDriverName$(k)
	Next
	
	Print ""
	
	If CountGfxDrivers()>1
		Repeat
			driver=Input$( "Display driver (1-"+CountGfxDrivers()+"):" )
		Until driver>=1 And driver<=CountGfxDrivers()
		SetGfxDriver driver
	EndIf
	
	Print ""
	Print "Display modes:"
	Print "--------------"
	cnt=0
	For k=1 To CountGfxModes()
		If GfxMode3D(k)
			t.GfxMode=New GfxMode
			t\width=GfxModeWidth(k)
			t\height=GfxModeHeight(k)
			t\depth=GfxModeDepth(k)
			cnt=cnt+1
			Print cnt+":"+t\width+","+t\height+","+t\depth
		EndIf
	Next
	
	If Not cnt
		Print "No 3D Graphics modes available!"
		Print "Press any key..."
		WaitKey
		End
	EndIf
	
	Repeat
		mode=Input$( "Display Mode (1-"+cnt+"):" )
	Until mode>=1 And mode<=cnt
	
	t.GfxMode=First GfxMode
	
	While mode>1
		t=After t
		mode=mode-1
	Wend
	
	Graphics3D t\width,t\height,t\depth,1
	SetBuffer BackBuffer()
	
	Delete Each GfxMode
	
End Function