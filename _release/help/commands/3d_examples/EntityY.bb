; EntityX / EntityY / EntityZ example.

; Escape quits, other keys move or pause the display.

Const width = 640, height = 480
Const KEY_ESC = 1, KEY_LEFT = 203, KEY_RIGHT = 205 

Graphics3D 640, 480
AmbientLight 50, 50, 50

Global isMoving = False     ; used to pause/resume movement 
Global count                ; how many updates have been done


; Set up a camera, light and three entities...

cam = CreateCamera()
PositionEntity cam, 0, 2, -50
CameraZoom cam, 4

lt = CreateLight() : TurnEntity lt, 30, 40, 0


Global oSphere, pCone, cSphere

oSphere = CreateSphere()
EntityColor oSphere, 250, 50, 0        ; Orange = Origin, parent of cone

pCone = CreateCone( 8, True, oSphere)  ;  will be a parent of small sphere
ScaleEntity pCone, .8, 2.0, .8
PositionEntity pCone, 8, 0, 0
EntityColor pCone, 255, 255, 0

cSphere = CreateSphere( 8, pCone )         ; child of the cone
EntityColor cSphere, 150, 150, 0
ScaleEntity cSphere, .4/.8, .4/2.0, .4/.8  ; try commenting out this line
PositionEntity cSphere, 0, 2, 0            ; above parent

; ... and we are ready run.

While Not KeyDown( KEY_ESC )

UpdateEverything	
RenderWorld
ShowInfo

Flip

Wend

End



Function UpdateEverything( )

; Nothing moves relative to its parent, so local coordinates are constant.
; Try uncommenting the PositionEntity command to change this.


If GetKey() Then isMoving = Not isMoving

If isMoving
TurnEntity oSphere, 0, .5, 0
TurnEntity pCone,  .2,  0, 0		

count = count + 1
a# = count Mod 360
;	PositionEntity cSphere, 0, 2 + Sin( a ), 0 ; experiment with this 

End If 

End Function

Function ShowInfo( )   ; global and local coordinates for all entities
Local x$, y$, z$

Color 255, 255, 255
Text 185, 20, "Global"
Text 495, 20, "Local"

Color 250, 50, 0 
Text  20, 50, "oSphere: " + XYZ( oSphere, True )
Text 400, 50, XYZ( oSphere, False )

Color 255, 255, 0 
Text  20, 75, "  pCone: " + XYZ( pCone, True )
Text 400, 75, XYZ( pCone, False )

Color 150, 150, 0 
Text  20, 100, "cSphere: " + XYZ( cSphere, True )
Text 400, 100, XYZ( cSphere, False )

End Function

; ******************************************************************

; These two functions just format the text display.
; Without them there are too many numbers crowding the screen.

Function Round#( x#, m# )		; returns x rounded to multiple of m
If m < 0.0 Then m = -m
s# = Sgn( x )
If x < 0.0 Then x = -x
diff# = x Mod m
If diff < .5 * m 
Return ( x - diff ) * s
Else
Return ( m + x - diff ) * s
End If
End Function


Function XYZ$( entity, globalFlag )

ex# = Round( EntityX( entity, globalFlag ), .001 )
ey# = Round( EntityY( entity, globalFlag ), .001 )
ez# = Round( EntityZ( entity, globalFlag ), .001 )

Return RSet( ex, 8 ) + RSet( ey, 8 ) + RSet( ez, 8 )

End Function