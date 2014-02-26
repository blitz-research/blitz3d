
; Types example 2

; Creating objects we don't need to be able to refer to individually (eg. aliens).

; ----------------------------------------------------------------------------

; We'll create an "invader" type:

Type invader

	Field x			; alien's x position
	Field y			; alien's y position
	Field fallrate	; alien's fall rate

End Type

; ----------------------------------------------------------------------------

; Unrelated set-up thing:

SeedRnd (MilliSecs ())		; Makes result of Rnd () more random!

; ----------------------------------------------------------------------------

; Set up 4 invaders across the screen, with a y-position of 0:

Const invadernumber = 4		; Change this to any number (eg. 100) -- part of the
							; power of types!

For a = 1 To invadernumber

	alien.invader	= New invader
	alien\x			= a * 6
	alien\y			= 0
	alien\fallrate	= Rnd (6) + 1

Next

; The line "alien.invader	= New invader" tells the computer to add a new invader
; to its hidden list. Each of the 4 invaders is called "alien", so we can't refer
; to them individually, but we can act upon them all at once, with a For...Each...Next
; loop!

; ----------------------------------------------------------------------------

; Unrelated set-up stuff:

Graphics 640, 480			; Open graphics display
SetBuffer BackBuffer ()		; Set up double-buffering

Text 20, 20, "Alien Racers Ready...":Flip:Delay 2500

; ----------------------------------------------------------------------------

Repeat

	Cls												; Clear the screen (unrelated)

	For moveall.invader = Each invader				; Read each invader into "moveall", in turn

		moveall\y	= moveall\y + moveall\fallrate	; Increase each invader's y position
		Oval moveall\x, moveall\y, 4, 4, 1			; Draw each invader
		If moveall\y > 480 Then atbottom = 1		; If any are at bottom of screen, end!

	Next

	Text 20, 20, "Go, Alien Racers!"

	Flip											; Show draw result (unrelated)

	Delay 10										; Slow things down a bit

Until MouseDown (1) = 1 Or atbottom = 1				; Click mouse or someone at bottom of screen

; ----------------------------------------------------------------------------

End