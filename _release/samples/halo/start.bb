
SetGfx()

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
	Print
	
	If CountGfxDrivers()>1
		Repeat
			driver=Input$( "Display driver (1-"+CountGfxDrivers()+"):" )
		Until driver>=1 And driver<=CountGfxDrivers()
		SetGfxDriver driver
	EndIf
	
	cnt=CountGfxModes3D()
	If cnt=0 Print "No 3D Graphics modes detected.":WaitKey:End
	
	Print ""
	Print "Display modes:"
	Print "--------------"
	For k=1 To CountGfxModes3D()
		Print k+":"+GfxModeWidth(k)+","+GfxModeHeight(k)+","+GfxModeDepth(k)
	Next
	
	Repeat
		mode=Input$( "Display Mode (1-"+cnt+"):" )
	Until mode>=1 And mode<=cnt
	
	Graphics3D GfxModeWidth(mode),GfxModeHeight(mode),GfxModeDepth(mode),1
	SetBuffer BackBuffer()
	
End Function