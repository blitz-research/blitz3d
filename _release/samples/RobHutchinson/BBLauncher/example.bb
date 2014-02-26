;
; LAUNCHER 2D EXAMPLE V1.00
; -------------------------
; Simply shows usage of the BBLauncher includes.
; Written by Rob Hutchinson 2001
; Email: loki.sd@cableinet.co.uk / ICQ: 27181384
;
; Last Modified: 19/08/2001
;
; DONT FORGET:
; During development you can fiddle with the options that the launcher would
; return with the commandline. Goto menu - Program > Program Command Line..
; and enter a string in the format:
;
; "/TYPE<Screen> /WIDTH<Width> /HEIGHT<Height> /BPP<Depth> /ADDIT<Additional>"
; Where <Screen> is either:
;  0 = FULLSCREEN
;  2 = WINDOWED or
;  1 = SCALED
;
; <Width> = display width, <Height> = display height, <Depth> = display depth.
;
; If you do not specify a command line, the default resolution of: 640x480x16 
; (fullscreen always), will be assumed.
;


; Include the bblauncher 2D include here in place of Graphics() and SetBuffer BackBuffer()
Include "launcher2d.bb"

; Draw something screen scalable..
iOffset = 50
iCount = 0
Repeat
	; Print the status of the additional option, -1 = no /ADDIT, 0 = Additional option -
	; false, 1 = Additional option - true. Set /ADDIT1 in the commandline.
	Color 255,255,255
	Text 0,0,"Additional Option: " + iAdditional

	; Print a scalable square in the center of the screen.
	iCount = QWrap(iCount+4,0,255)
	Color iCount,-iCount,iCount
	Rect iOffset,iOffset,GraphicsWidth()-(iOffset*2),GraphicsHeight()-(iOffset*2)
	Flip
Until KeyDown(1)
End


; Functions:

Function QWrap(Value#,Low#,High#)			; Remember this? where is the Blitz
	Return Low#+(Value# Mod High#)			; PC version when you need it? :))
End Function 
