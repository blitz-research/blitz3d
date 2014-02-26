;Test Bed
;David Bird
;dave@birdie72.freeserve.co.uk
Graphics3D 640,480
SetBuffer BackBuffer()
lit=CreateLight()

cam=CreateCamera()
CameraRange cam,.1,1000
PositionEntity cam,0,0,-10
pl=CreatePlane(1,cam)
PositionEntity pl,0,0,.15
RotateEntity pl,-90,0,0
EntityAlpha pl,0
EntityPickMode pl,2

sprite=LoadSprite("test.jpg")
x#=320
y#=240
xd=1
yd=1
ang#=0
sz#=32
sd=1
alpha#=.9
ald#=-.01
While Not KeyDown(1)
	TurnEntity cam,0,0,1
	Sprite2d(cam,sprite,x,y,sz,ang,alpha)
	x=x+xd
	y=y+yd
	ang=ang+1
	sz=sz+sd
	alpha=alpha+ald
	If alpha>.98 Or alpha<.02 Then ald=-ald
	If sz>128 Or sz<32 Then sd=-sd
	If x>624 Or x<16 Then xd=-xd
	If y>464 Or y<16 Then yd=-yd
	
	UpdateWorld
	RenderWorld
	Flip
Wend

FreeEntity pl
FreeEntity cam
EndGraphics
End

Function Sprite2D(cam,sprite,x#,y#,size#,ang#,alpha#)
	ent=CameraPick(cam,x,y)
	If ent Then 
		PositionEntity sprite,PickedX(),PickedY(),PickedZ()
		ScaleSprite sprite,size/2560,size/2560
		RotateSprite sprite,ang
		EntityAlpha sprite,alpha
	End If
End Function