;
; MODEL CHILDREN V1.00
; --------------------
; Simple example of how to play around with a model's children objects.
; Here, the flares and backplate are part of the "Models\facia.x" model.
; The example gets entities from each of the child flare subobjects in
; the facia model and moves them around on the X and Y axis. Simple.
;
; Email: loki.sd@cableinet.co.uk / ICQ: 27181384
;
; Created with BB3D Beta 1.60
; Last Modified: 13/08/2001
;

AppTitle "Model Children V1.00 - By Rob Hutchinson 2001"

; Randomize from the timer.
SeedRnd MilliSecs()

; Setup the screen
Include "start.bb"

; Setup the camera
camera = CreateCamera()

; Load in our model
mainmesh = LoadAnimMesh("models\facia.x")
MoveEntity mainmesh,7,3,20
TurnEntity mainmesh,180,0,0

; Some ambient lighting
AmbientLight 200,200,200

; Flies:
Type fly
	Field obj
	Field posx#,posy#
	Field rotx#,roty#
	Field radx#,rady#
	Field spdx#,spdy#
End Type

; Count the number of children in the model. We already know
; that child 1 is the backface, so we use all other objects (the flares).
Global maxFlies = CountChildren(mainmesh)-1
Global TopSpeed# = 2.5
Dim flies.fly(maxFlies+1)

For a=0 To CountChildren(mainmesh)-2
	flies(a)     = New fly
	; Get a handle to the child object.
	flies(a)\obj = GetChild(mainmesh,a+2)
	; Addition blend the object so it appears as a light rather than a brick.
	EntityBlend flies(a)\obj,3
	
	; Randomize some movement values.
	flies(a)\rotx = Rnd(360)
	flies(a)\roty = Rnd(360)
	flies(a)\radx = Rnd(-15,15)
	flies(a)\rady = Rnd(-15,15)
	flies(a)\spdx = Rnd(-TopSpeed#,TopSpeed#)	
	flies(a)\spdy = Rnd(-TopSpeed#,TopSpeed#)
Next


Repeat
	
	; Allow the user to switch to wireframe mode.
	If KeyHit(17) Then wire = Not wire
	WireFrame wire

	; Move them all!
	For a=0 To CountChildren(mainmesh)-2
		flies(a)\posx=Sin(flies(a)\rotx)*flies(a)\radx
		flies(a)\posy=Cos(flies(a)\roty)*flies(a)\rady
		flies(a)\rotx=flies(a)\rotx+flies(a)\spdx
		flies(a)\roty=flies(a)\roty+flies(a)\spdy
		flies(a)\rotx=QWrap(flies(a)\rotx,0,360)
		flies(a)\roty=QWrap(flies(a)\roty,0,360)
		PositionEntity flies(a)\obj,flies(a)\posx,flies(a)\posy,EntityZ(flies(a)\obj)
	Next

	; Update the world stuff
	UpdateWorld
	RenderWorld
	
	; Flip display
	Flip
Until KeyDown(1)

; Terminate.
End

Function CurveValue#(current#,destination#,curve)
	current#=current#+((destination#-current#)/curve)
	Return current#
End Function

Function QWrap(Value#,Low#,High#)			; Remember this? where is the Blitz
	Return Low#+(Value# Mod High#)			; PC version when you need it? :))
End Function 