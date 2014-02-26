;Lightning Effect
;David Bird
;dave@birdie72.freeserve.co.uk
;http://www.birdie72.freeserve.co.uk
;16/6/01 (beta 2.25)
;Example of creating a lightning mesh
Graphics3D 640,480
SetBuffer BackBuffer()

;Set Up collisions here
;
Const TYPE_MOVE=1
Const TYPE_TERR=2
Collisions TYPE_MOVE,TYPE_TERR,2,3

;Create a camera
cam=CreateCamera()
CameraRange cam,1,25000
CameraClsMode cam,False,True
PositionEntity cam,0,50,-152
EntityRadius cam,10
EntityType cam,TYPE_MOVE

;Load terrain
terr=LoadTerrain("hmap.bmp")
TerrainDetail terr,4000

;Load textures
tex1=LoadTexture("forest.bmp")		;Grass Texture

;resize textures
ScaleTexture tex1,32,32
EntityTexture terr,tex1

;scale terrain to size
ScaleEntity terr,50,375,50
EntityType terr,TYPE_TERR

;Add an infinate plane
plane=CreatePlane()
EntityTexture plane,tex1
ScaleEntity plane,50,1,50
EntityOrder plane,1
EntityType plane,TYPE_TERR

;Load brush for lightning
Global lightning_brush=LoadBrush("tex0.bmp",3)
;lightning forking ammount
Const Lightning_Count=3
Global flash=0
;

;Load a texture for use with as a cloud
cloud_tex=LoadTexture("tex1.bmp",3)

;Create a simple plane for the sky
;and texture it
sky=CreatePlane()
EntityTexture sky,cloud_tex
PositionEntity sky,0,500,0
ScaleTexture cloud_tex,5000,5000
;Now do the other side
sky2=CopyEntity(sky)
RotateEntity sky2,180,0,0
EntityParent sky2,sky

;
; Do audio here
;
listen=CreateListener(cam,.00001,10,5)	;doh 
Global thunder=LoadSound("thunder.wav")
wind_snd=LoadSound("wind.wav")
LoopSound wind_snd

EmitSound(wind_snd,terr)



AmbientLight 30,30,30
While Not KeyDown(1)
	ClsColor flash,flash,flash
	If flash>30 Then
		AmbientLight flash,flash,flash
	End If
	Cls
	If KeyDown(203) TurnEntity cam,0,+1,0	;cursors to turn
	If KeyDown(205) TurnEntity cam,0,-1,0
	TranslateEntity cam,0,-9.8,0				;drop camera to surface

	If KeyDown(44) MoveEntity cam,0,0,-8	;A - fly foward
	If KeyDown(30) MoveEntity cam,0,0,8		;Z - fly backward

	If KeyHit(2) Then Create_lightning(Rnd(1000),500,Rnd(1000),500,20)

	;Do update
	Update_Lightning(cam)
	
	;Move the clouds a bit
	MoveEntity sky,Sin(MilliSecs()/1000),0,3
	UpdateWorld
	RenderWorld
	
	Flip
Wend

;Freeup entity's and textures
Free_Lightning()
FreeTexture tex1
FreeTexture cloud_tex
FreeEntity terr
FreeEntity sky2
FreeEntity sky
FreeEntity plane
FreeEntity cam
EndGraphics
End

Type lightning
	Field ent
	Field surf
	Field alpha#
End Type

;Update the lightning bolts
Function Update_Lightning(cam)
	For a.lightning=Each lightning
		a\alpha=a\alpha-.1
		If a\alpha<1 Then 
			EntityAlpha a\ent,a\alpha
			EntityColor a\ent,255*a\alpha,255*a\alpha,255
		End If
		If a\alpha<0 Then FreeEntity a\ent:Delete a	
	Next
	If flash>0 Then flash=flash-20
	If flash<0 Then flash=0
End Function

;Free up any memory used on with lightning.
Function Free_Lightning()
	For a.lightning=Each lightning
		If a\ent Then FreeEntity a\ent
		Delete a
	Next
End Function

;Create's Lightning_count bolts of
;lightning
Function Create_lightning(gx#,gy#,gz#,high#,seg)
	flash=255
	pseg=seg
	For cnt=0 To Lightning_Count
		seg=Rand(4,pseg)
		a.lightning=New lightning
		a\alpha=2
		a\ent=CreateMesh()
		EntityFX a\ent,1
		a\surf=CreateSurface(a\ent,lightning_brush)
		x#=0
		y#=0
		z#=0
		stp#=high/Float(seg)
		For b=0 To seg
			AddVertex a\surf,x,y,z,0,0
			x=x-30
			AddVertex a\surf,x,y,z,1,0
			x=x+Rnd(-80,80)
			y=y-stp
			AddVertex a\surf,x,y,z,1,1
			x=x+30
			AddVertex a\surf,x,y,z,0,1
		Next
		For b=0 To (4*seg) Step 4
			AddTriangle(a\surf,b,b+1,b+2)
			AddTriangle(a\surf,b,b+2,b+3)
			
			AddTriangle(a\surf,b,b+2,b+1)
			AddTriangle(a\surf,b,b+3,b+2)
		Next
		PositionEntity a\ent,gx,gy,gz
	Next
	EmitSound thunder,a\ent
End Function