; you will notice the animation gradually
; drives itsself out of sync. this is because
; the start and end frames are not identical.
;
; looks like he's swimming after a while

Graphics3D 640,480,16,2

camera=CreateCamera()
PositionEntity camera,0,25,-60

light=CreateLight()
AmbientLight 128,128,128

;load flare
flare=LoadSprite("flare.bmp",1)
ScaleSprite flare,5,5
HideEntity flare

;load robot
TextureFilter "shine",65
mak=LoadAnimMesh( "mak_running.x" )
EntityShininess mak,0.5

;attach flares to bits. needs adjustment.
eyepos=FindChild(mak,"eye")
flareposa=FindChild(mak,"thrustA")
flareposb=FindChild(mak,"thrustB")

eyeflare=CopyEntity(flare,eyepos)
MoveEntity eyeflare,0,0,-1.5

flarea=CopyEntity(flare,flareposa)
MoveEntity flarea,0,-2,0

flareb=CopyEntity(flare,flareposb)
MoveEntity flareb,0,-2,0




AnimateEntity mak,1,.25

While Not KeyHit(1)
	If KeyDown(203) TurnEntity mak,0,-3,0
	If KeyDown(205) TurnEntity mak,0,+3,0
	UpdateWorld
	RenderWorld
	Flip
Wend

End