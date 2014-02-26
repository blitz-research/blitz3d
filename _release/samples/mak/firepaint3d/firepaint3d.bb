
;EnableDirectInput False

Global info1$="Firepaint3D Demo"
Global info2$="Features dynamically colored sprites"

Include "../start.bb"

AmbientLight 0,0,0

Const grav#=-.02,intensity=5

Type Frag
	Field ys#,alpha#,entity
End Type

pivot=CreatePivot()

camera=CreateCamera( pivot )
CameraClsMode camera,False,True

;create blitzlogo 'cursor'
cursor=CreateSphere( 8,camera )
EntityTexture cursor,LoadTexture( "blitzlogo.bmp",3 )
MoveEntity cursor,0,0,25
EntityBlend cursor,3
EntityFX cursor,1

;create sky sphere
sky=CreateSphere()
tex=LoadTexture( "stars.bmp" )
ScaleTexture tex,.125,.25
EntityTexture sky,tex
ScaleEntity sky,500,500,500
EntityFX sky,1
FlipMesh sky

spark=LoadSprite( "bluspark.bmp" )

time=MilliSecs()

MoveMouse 0,0

While Not KeyDown(1)

	Repeat
		elapsed=MilliSecs()-time
	Until elapsed>0
	
	time=time+elapsed
	dt#=elapsed*60.0/1000.0
	
	Local x_speed#,y_speed#
	
	x_speed=(MouseXSpeed()-x_speed)/4+x_speed
	y_speed=(MouseYSpeed()-y_speed)/4+y_speed
	MoveMouse 320,240

	TurnEntity pivot,0,-x_speed,0	;turn player left/right
	TurnEntity camera,-y_speed,0,0	;tilt camera
	TurnEntity cursor,0,dt*5,0
	
	If MouseDown(1)
		For t=1 To intensity
			f.Frag=New Frag
			f\ys=0
			f\alpha=Rnd(2,3)
			f\entity=CopyEntity( spark,cursor )
			EntityColor f\entity,Rnd(255),Rnd(255),Rnd(255)
			EntityParent f\entity,0
			RotateEntity f\entity,Rnd(360),Rnd(360),Rnd(360)
			num=num+1
		Next
	EndIf
	
	For f.Frag=Each Frag
		f\alpha=f\alpha-dt*.02
		If f\alpha>0
			al#=f\alpha
			If al>1 Then al=1
			EntityAlpha f\entity,al
			MoveEntity f\entity,0,0,dt*.4
			ys#=f\ys+grav*dt
			dy#=f\ys*dt
			f\ys=ys
			TranslateEntity f\entity,0,dy,0
		Else
			FreeEntity f\entity
			Delete f
			num=num-1
		EndIf
	Next

	UpdateWorld
	RenderWorld
;	GetColor 0,0
;	Color 255,0,255
;	Rect 0,ScanLine(),640,1
	Flip
Wend

End