; TFormNormal example

Graphics3D 640, 480

; Just a quick demonstration of the 'normalization' feature.

TFormNormal 1,2,2, 0,0    ;  transform from world to world

; The transformation from world to world does nothing.
; But afterward the vector (1,2,2) is divided by the length 3.

message$ = "The normalized vector is  ( "
message = message + TFormedX() + ",  " + TFormedY() + ",  " + TFormedZ() + " )"

Text 70, 180, message

Flip

WaitKey()
End