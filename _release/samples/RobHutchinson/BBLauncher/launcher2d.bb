;
; LAUNCHER 2D INCLUDE V1.02
; -------------------------
; Simple Include to handle startup for programs written
; to use BBLauncher.
; Written by Rob Hutchinson 2001
; Email: loki.sd@cableinet.co.uk / ICQ: 27181384
;
; Last Modified: 18/08/2001
;

; Some default values, change these if you like...
Const RESOLUTION_DEFAULT_WIDTH  = 640
Const RESOLUTION_DEFAULT_HEIGHT = 480
Const RESOLUTION_DEFAULT_DEPTH  = 16

; This is the command line..
Global sCL$ = Upper(CommandLine())

; Find the screen mode...
iScrType = bblGetOption("TYPE",0)
Select iScrType
	Case 1
		iScreenMode = 3
		HidePointer()
	Case 2
		iScreenMode = 2
		HidePointer()
	Case 0
		iScreenMode = 1
End Select

; Get the screen details!
Global iScreenResX = bblGetOption("WIDTH",Str(RESOLUTION_DEFAULT_WIDTH))
Global iScreenResY = bblGetOption("HEIGHT",Str(RESOLUTION_DEFAULT_HEIGHT))
Global iBitsPerPixel = bblGetOption("BPP",Str(RESOLUTION_DEFAULT_DEPTH))
Global iAdditional = bblGetOption("ADDIT",-1)

; Work with the details!
If GfxModeExists(iScreenResX,iScreenResY,iBitsPerPixel)
	Graphics iScreenResX,iScreenResY,iBitsPerPixel,iScreenMode
	SetBuffer BackBuffer()
Else
	; Fire it back out to the launcher cos screen res is unsupported...
	ExecFile "Launcher.exe"
	RuntimeError "The graphics card in this machine does not support the selected screenmode!" + Chr(10) + Chr(13) + "Please select another..."
	End
EndIf

; FUNCTIONS ->
;------------------
Function bblGetOption$(opts$,defaultstr$="")
	iInTmp  = Instr(sCL$,"/" + opts$)
	If iInTmp <> 0
		iInTmp2 = Instr(Mid$(sCL$,iInTmp)," ")
		Return Mid$(Mid$(sCL$,iInTmp+Len(opts$)+1),1,iInTmp2-(Len(opts$)+2))
	Else
		Return defaultstr$
	EndIf
End Function