;Mirror in the wall
;Only works on one plane at the moment

;David Bird
;dave@birdie72.freeserve.co.uk

Graphics3D 640,480
SetBuffer BackBuffer()

lit=CreateLight()
cam=CreateCamera()
tex0=LoadTexture("tex0.bmp")

;Create a camera
CameraRange cam,1,200

;Create a sphere to visualise the camera
cam_cube=CreateCube()
EntityTexture cam_cube,tex0
ScaleEntity cam_cube,.5,.5,.5

;Create an flipped cube to place everything in
test=CreateCube()
ScaleMesh test,10,10,10
FlipMesh test
EntityTexture test,tex0

;Add a mirror.
Create_Mirror(0,0,9,5,0)

While Not KeyDown(1)

	If KeyDown(203) MoveEntity cam,-.1,0,0
	If KeyDown(205) MoveEntity cam,.1,0,0

	If KeyDown(44) MoveEntity cam,0,0,-.1
	If KeyDown(30) MoveEntity cam,0,0,.1
	PositionEntity cam_cube,EntityX(cam),EntityY(cam),EntityZ(cam)
	TurnEntity cam_cube,2,1,4
	
	;Do updates of mirrors
	Update_Mirrors(cam)

	RenderWorld
	Flip
Wend

;Free up all mirrors
free_Mirrors()
FreeEntity cam
EndGraphics
End

Type Mirror
	Field plane
	Field camera
	Field texture
End Type

;Add a mirror to the scene
Function Create_Mirror(x#,y#,z#,size#,yrot#)
	a.mirror=New mirror
	a\plane=CreateMesh()
	surf=CreateSurface(a\plane)
	;Add all vertices to the surface. (Simple plane)
	AddVertex(surf,1*size,1*size,0,0,0)
	AddVertex(surf,-1*size,1*size,0,1,0)
	AddVertex(surf,-1*size,-1*size,0,1,1)
	AddVertex(surf,1*size,-1*size,0,0,1)
	AddTriangle(surf,0,2,1)
	AddTriangle(surf,0,3,2)
	;the idea of a mirror is a camera behind the mirror
	a\camera=CreateCamera(a\plane)
	;which is rendered to a texture
	CameraViewport a\camera,0,0,256,256
	a\texture=CreateTexture(256,256)
	EntityTexture a\plane,a\texture
	;postion mirror where you want it
	PositionEntity a\plane,x,y,z
	;rotate it on y to the given
	TurnEntity a\plane,0,yrot,0
End Function

;Update all mirrors
Function Update_Mirrors(camera)
	ox#=EntityX(camera,True);Get the position of the main camera in x
	oz#=EntityZ(camera,True);and in z

	HideEntity camera	;hide the main camera
	For a.mirror=Each mirror				;do updates First
		dx#=EntityX(a\camera,True)-ox		;Get the angle between the mirror
		dz#=EntityZ(a\camera,True)-oz		;and the main camera.
		ang#=ATan2(dx,dz)+EntityYaw(a\plane);taking into account yaw of mirror

		mag#=1/(Sqr(dx^2+dz^2))				;Magnify according to distance.
		RotateEntity a\camera,0,180+ang,0	;rotate the mirrors camera to the correct orientation
		CameraZoom a\camera,0.5+mag			;set the magnification of the mirror
	Next
	UpdateWorld								;Update everything.
	For a.mirror=Each mirror				;rendering loop here. (all mirrors)
		ShowEntity a\camera					;show the camera
		RenderWorld							;render
		CopyRect 0,0,256,256,0,0,0,TextureBuffer(a\texture);Copy the render to texture
		HideEntity a\camera					;Hide the camera again
	Next
	ShowEntity camera						;Show Main camera again.
End Function

;Free up all mirrors here.
Function Free_Mirrors()
	For a.mirror=Each mirror
		If a\camera FreeEntity a\camera
		If a\plane FreeEntity a\plane
		If a\texture FreeTexture a\texture
		Delete a
	Next
End Function