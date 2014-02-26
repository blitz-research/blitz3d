
;startup kludge
;
Start3d_Kludge()

Type GfxMode
	Field width,height,depth
End Type

Function Start3d_Kludge()
	If Windowed3D()
		yn$=Input$( "Use windowed mode?" )
		If Left$( Lower$( yn$ ),1 )="y"
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