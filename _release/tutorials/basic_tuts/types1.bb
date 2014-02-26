
; Types example 1

; Creating objects we want to be able to refer to individually (eg. players).

; ----------------------------------------------------------------------------

; We'll create a "playerdata" type:

Type playerdata

	Field x		; player's x position
	Field y		; player's y position

	Field r     ; red value
	Field g     ; green value
	Field b     ; blue value

End Type

; ----------------------------------------------------------------------------

; Now we'll create two players, both of "playerdata" type:

player1.playerdata = New playerdata
player2.playerdata = New playerdata

; The computer now creates two "playerdata" objects in memory, and we have a
; reference to each one, called player1 and player2.

; ----------------------------------------------------------------------------

; Unrelated set-up stuff:

Graphics 640, 480		; Screen dimensions
SeedRnd (MilliSecs ())	; Make Rnd () more random!

; ----------------------------------------------------------------------------

; Set some positions and colours:

; Player 1
; --------

; Position:

player1\x = Rnd (GraphicsWidth () - 50)
player1\y = Rnd (GraphicsHeight () - 50)

; Colour:

player1\r = 255
player1\g = 0
player1\b = 0

; Player 2
; --------

; Position:

player2\x = Rnd (GraphicsWidth () - 50)
player2\y = Rnd (GraphicsHeight () - 50)

; Colour:

player2\r = 0
player2\g = 0
player2\b = 255

; ----------------------------------------------------------------------------

; Now we'll draw and label the players...

; Player 1:

Color player1\r, player1\g, player1\b
Oval player1\x, player1\y, 50, 50, 1
Color 255, 255, 255
Text player1\x, player1\y, "Player 1"

; Player 2:

Color player2\r, player2\g, player2\b
Oval player2\x, player2\y, 50, 50, 1
Color 255, 255, 255
Text player2\x, player2\y, "Player 2"

; ----------------------------------------------------------------------------

; Click mouse to quit:
Print "Click mouse to end"
MouseWait:End