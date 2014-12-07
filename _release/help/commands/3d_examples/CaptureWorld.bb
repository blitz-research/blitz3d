; CaptureWorld and RenderWorld with tween.

; Left/Right arrow keys change tween. Escape quits.

Const width = 640, height = 480
Const KEY_ESC = 1, KEY_LEFT = 203, KEY_RIGHT = 205 

Graphics3D 640, 480
AmbientLight 50, 50, 50

c1 = CreateCone( )
PositionEntity c1, -5, 0, 0		; on the left side of the screen
ScaleEntity c1, 1, 3, 1
EntityColor c1, 255, 0, 0

cam = CreateCamera()
PositionEntity cam, 0, 0, -50
CameraZoom cam, 4

lt = CreateLight() : TurnEntity lt, 30, 40, 0


c2 = CopyEntity( c1 )

CaptureWorld	; with c1 and c2 identically placed

MoveEntity c2, 8, 0, 0		; to the right side of the screen
TurnEntity c2, 0, 0, 90		; and tilted

tw# = 100		; 100 * tween

; Interpolate between the pre-CaptureWorld ( tween = 0 )
; arrangement and the current one ( tween = 1 ).

While Not KeyDown( KEY_ESC )

If KeyDown( KEY_LEFT  ) Then tw = tw - 1
If KeyDown( KEY_RIGHT ) Then tw = tw + 1
RenderWorld tw / 100
Text 250, 100, "tween = " + ( tw / 100 )
Flip

Wend

End