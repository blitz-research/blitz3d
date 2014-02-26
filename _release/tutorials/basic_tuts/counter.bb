; Set display mode variables 
Const width=640,height=480,depth=16

; Set the display mode
Graphics width,height,depth

;Draw to the back buffer
SetBuffer BackBuffer ()
counter = 1
;Set font to arial size 128
fontno=LoadFont("verdana",128)
SetFont fontno
While Not KeyDown(1); Repeat the following loop until the escape key is pressed
;Set font colour to psychedelic
Color Rnd(256),Rnd(256),Rnd(256)
counter = counter + 1

Cls; Clear screen

;Print current counter number to the screen 195 pixels across by 160 pixels high
Text 195,160,counter

; Flip the screen buffers
Flip

Wend



