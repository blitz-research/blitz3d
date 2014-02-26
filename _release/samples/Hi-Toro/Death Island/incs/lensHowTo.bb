
; ---------------------------
; (Don't try to run this! :)
; ---------------------------

; --------------
; How to use...
; --------------

; ---------------------------
; At the start of your code:
; ---------------------------

Global lensDiameter = 25 ; This line MUST come before the include line!!!
Include "lensIncs.bb"

; --------------------------------
; Somwhere before your main loop:
; --------------------------------

; ----------------------------------------------------------------------
; Set the amount to magnify (haven't quite figured this out -- a little
; under half of lensDiameter seems to work well):
; ----------------------------------------------------------------------

magnification = 5

; ----------------------------------------------------------------------
; Do some pre-calc stuff:
; ----------------------------------------------------------------------

CreateLens (lensDiameter, magnification)

; --------------------------------------------
; During your main loop, before calling Flip:
; --------------------------------------------

DrawLens (MouseX (), MouseY (), lensDiameter)

; ----------------------------------------------------------------
; MouseX () and MouseY () make a good demo, but change to suit...
; ----------------------------------------------------------------
