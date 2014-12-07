;Create 3d animation example

;Set up a simple nice looking level
Graphics3D 640,480
camera=CreateCamera()
PositionEntity camera,0,12,-12
RotateEntity camera,35,0,0
light=CreateLight(2)
PositionEntity light,1000,1000,-1000
ground=CreatePlane(2)
EntityAlpha ground,0.5
EntityColor ground,0,0,255
mirror=CreateMirror()

;Lets make a bouncing ball that squashes on impact with the floor.
ball=CreateSphere(16)
EntityShininess ball,1
EntityColor ball,255,0,0

; Lets animate him and "record" the 3D animation for later playback
bloat#=0 : flatten#=0 : ypos#=10

For frame=1 To 10
;Drop the ball from height 10 to 2
ypos = ypos - spd#
spd#=spd#+.2
PositionEntity ball,0,ypos,0
ScaleEntity ball,1+bloat,1+flatten,1+bloat

;If the ball is low enough make it look increasingly squashed
If frame>8
bloat=bloat+1.5
flatten=flatten-.25
Else
flatten=flatten+.05
EndIf

;Record the frame!
SetAnimKey ball,frame
Next

;Now we need to add the frames we've just made to the sequence of "film"!
seq = AddAnimSeq(ball,frame-1) ; total number of frames

;Play it back ping-pong!
Animate ball,2,0.15
While Not KeyHit(1)
UpdateWorld
RenderWorld
Flip
Wend
End