; Include matrix source code file. We can then use the functions contained within it.
Include "matrix.bb"

Graphics3D 640,480,16
SetBuffer BackBuffer()

camera=CreateCamera()
CameraClsColor camera,0,0,127
PositionEntity camera,500,500,0
RotateEntity camera,45,0,0

light=CreateLight()
RotateEntity light,90,0,0

; Clear texture filters - this means textures won't be mipmapped for that rough DB look
ClearTextureFilters

; Load image to be used as matrix texture
DB_LoadImage("tiles.bmp",1)

; Make matrix
MakeMatrix(1,1000,1000,10,10)

; Prepare matrix texture
PrepareMatrixTexture(1,1,2,2)

; Fill matrix with texture
FillMatrix(1,0,1)

; Randomize matrix to make it hilly
RandomizeMatrix(1,100)

While KeyDown(1)<>True

	mxs=MouseXSpeed()
	mys=MouseYSpeed()
	
	dest_xang#=dest_xang#+mys
	dest_yang#=dest_yang#-mxs
	
	xang#=CurveValue#(xang#,dest_xang#,5)
	yang#=CurveValue#(yang#,dest_yang#,5)
	
	RotateEntity camera,xang#,yang#,0
	
	MoveMouse GraphicsWidth()/2,GraphicsHeight()/2
	
	If MouseDown(1)=True Then MoveEntity camera,0,0,1
	
	; Position camera at ground height+50
	PositionEntity camera,EntityX#(camera),GetGroundHeight#(1,EntityX#(camera),EntityZ#(camera))+50,EntityZ#(camera)

	RenderWorld
	Flip

Wend


;---------------------
; Curve value function
;---------------------

Function CurveValue#(current#,destination#,curve)

	current#=current#+((destination#-current#)/curve)
	
	Return current#

End Function