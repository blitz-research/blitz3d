;
; WATERY TERRAIN V1.00
; --------------------
; Creating and modifying terrain entities to create water (err, something that sort
; of resembles water,.. sort of :)). Mainly just to show how to modify terrain to
; create such effects. You could do crashing waves, or water ripples. (Might
; have a crack at that later :))
;
; Written by Rob Hutchinson 2001 for Blitz 3D
; Textures by Rob Hutchinson. Email: loki.sd@cableinet.co.uk / ICQ: 27181384
;
; Created with BB3D Beta 1.57
; Last Modified: 10/08/2001
;

; Modify these if you like :)
Const  WATER_GRID_SIZE = 16
Global WATER_HEIGHT# = 8.2

; Setup the water grid values
Dim WaterY(WATER_GRID_SIZE+1,WATER_GRID_SIZE+1)
For q = 0 To WATER_GRID_SIZE
	For j = 0 To WATER_GRID_SIZE
		WaterY(q,j) = Rnd(0,360)
	Next 
Next

; Setup the screen
Include "start.bb"

; Setup the camera
campiv = CreatePivot()
camera = CreateCamera(campiv)
CameraFogMode camera,1
CameraFogRange camera,1,100
MoveEntity campiv,0,5,0
MoveEntity camera,0,1,-10

; Create some terrain and load the water texture onto it
water = CreateTerrain(WATER_GRID_SIZE)
watert = LoadTexture("water.bmp")
ScaleTexture watert,2,2
EntityTexture water,watert
EntityAlpha water,.4

; Create a floor plane and texture it
bed  = CreatePlane()
bedt = LoadTexture("oceanfloor.bmp")
ScaleTexture bedt,15,15
EntityTexture bed,bedt
MoveEntity bed,0,-8,0

; Modify the detail of the terrain
TerrainShading water,True
Global curdetail = 2000
TerrainDetail water,curdetail,True

; Resize and position the water
MoveEntity water,-60,-10,-60
ScaleEntity water,8,WATER_HEIGHT#,8

; Some ambient lighting
AmbientLight 200,200,200

Repeat
	; Allow the user to switch to wireframe mode.
	If KeyHit(17) Then wire = Not wire
	WireFrame wire

	; Move the camera around
	RotateEntity campiv,Sin(ll#)*10,ll#,0
	ll# = ll# + 0.2
	PointEntity camera,campiv

	; Terrain Detail level
	If KeyDown(26) Then curdetail = curdetail - 10
	If KeyDown(27) Then curdetail = curdetail + 10	
	TerrainDetail water,curdetail,True

	; Here we itterate through all the cells on the terrain grid (switch wireframe on to
	; see them) and then make them simply bounce up and down.
	For z = 0 To WATER_GRID_SIZE-1
		For x = 0 To WATER_GRID_SIZE-1
			hite# = Sin(WaterY(z,x))
			ModifyTerrain water,x,z,Abs(hite#/2)+.5,False
			WaterY(z,x) = WaterY(z,x) + 1
		Next
	Next

	; Update the world stuff
	UpdateWorld
	RenderWorld
	
	; Print some simple details on screen.
	Text 0,0,"W = Wireframe Toggle"
	Text 0,12,"LOD = [ and ]  (" + curdetail + ")"
	
	; Flip display
	Flip
Until KeyDown(1)

; Terminate.
End