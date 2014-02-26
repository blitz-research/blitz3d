; Pyromania demo by R.Cummings (bloodlocust@blueyonder.co.uk)
; if you decide to use my animation of an explosion, please
; either give me credit where due, or let me know :)

Const FPS=30
Global boomtex,bangsprite,camera,count,pitch#,yaw#,roll#,d
AppTitle "BLITZ3D PYROMANIA"
HidePointer
Type bang
	Field frame#,obj
End Type

d=16 ; size of explosion grid

Include "start.bb"
;Graphics3D 640,480,16,3
SetBuffer BackBuffer()
boomtex=LoadAnimTexture("boomstrip.bmp",49,64,64,0,39)
bangsprite=CreateSprite()
EntityTexture bangsprite,boomtex,39
EntityBlend bangsprite,3
HideEntity bangsprite
camera=CreateCamera()
PositionEntity camera,0,0,0


period=1000/FPS
time=MilliSecs()-period

While Not KeyHit(1)
	Repeat
		elapsed=MilliSecs()-time
	Until elapsed

	ticks=elapsed/period
	tween#=Float(elapsed Mod period)/Float(period)
	
	For k=1 To ticks
		time=time+period
		If k=ticks Then CaptureWorld
		UpdateGame()
		UpdateWorld
	Next
	
	RenderWorld tween
	Flip
Wend
End


Function UpdateGame()
	pitch#=pitch#+.4
	yaw#=yaw#-.2
	roll#=roll#+.1

	RotateEntity camera,pitch#,yaw#,roll#

	For i=0 To Rnd(d)+20
		AddBang(Rnd(-d,d),Rnd(-d,d),Rnd(-d,d))
	Next 
	
	UpdateBangs()
End Function


Function AddBang(x#,y#,z#)
	b.bang=New bang
	b\obj=CopyEntity(bangsprite)
	PositionEntity b\obj,x,y,z
	RotateSprite b\obj,Rnd(-20,20)
;	EntityColor b\obj,Rnd(255),Rnd(255),Rnd(255)

	count=count+1
End Function

Function UpdateBangs()
	For b.bang=Each bang
		If b\frame#>38.5
			FreeEntity b\obj
			Delete b
			count=count-1
		Else
			EntityTexture b\obj,boomtex,b\frame#
			b\frame#=b\frame#+1
		EndIf
	Next
End Function

;	EntityTexture sprite,boomtex,Floor(frame)
;	frame=(frame+.5)Mod 39