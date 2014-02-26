;-----------------------------------------------------------------------
; Emerald Gate 1.0
; 07/26/01
;
; Written By Richard R Betson
; Copyright, 2001 Richard R Betson
; www.redeyeware.50megs.com
; vidiot@getgoin.net
; Texture wave motion code from Flag Demo, Blitz Basic 3D (Author Unknown)
;-----------------------------------------------------------------------
AppTitle "Emerald Gate"
Include "../start.bb"
;Graphics3D 800,600,16
SetBuffer BackBuffer()


AmbientLight 100,100,100


Const segs=40,width#=4,depth#=.09
time=MilliSecs()+8000

;----------------------------------
; Load Media
;----------------------------------
gate=LoadMesh("my_stargate3.3ds")
g_tex=LoadTexture("gate1.bmp",65)
tex=LoadAnimTexture( "water_anim.bmp",4,128,128,0,8 )
floor_t=LoadTexture("stone.bmp",2)

;----------------------------------
; Create Camera
;----------------------------------
c_pivot=CreatePivot()			;Camera Pivot
camera=CreateCamera(c_pivot)	;Create Camera
PositionEntity camera,0,-8,1.5	;Position camera

CameraZoom camera,1.4			;Zoom in jus a bit
CameraRange camera,.1,100		;Limit camera range
CameraFogMode camera,1 			;Fog on
CameraFogColor camera,16,0,128	;Color fog
CameraFogRange camera,0,60		;Visible fog range



;----------------------------------
; Create Gate
;----------------------------------
FitMesh gate,-1,-1,-1,2,2,2,True
ScaleMesh gate,3,3,3
TurnEntity gate,90,0,0
PositionEntity gate,0,0,0
EntityTexture gate,g_tex
UpdateNormals gate


;----------------------------------
; Create Water Mesh
;----------------------------------
mesh=CreateMesh()		   ;Create water mesh
surf=CreateSurface( mesh ) ;Create a suface for mesh

For k=0 To segs						;Loop through segments of suface
	x#=Float(k)*width/segs-width/2  ;Get x position as F. Point
	u#=Float(k)/segs                ;Get u pos. as F. Point
	AddVertex surf,x,1,0,u,0        ;Add a vertex/index (texture) to surface
	AddVertex surf,x,-1,0,u,1
Next

For k=0 To segs-1                    ;Loop through segments of surface
	AddTriangle surf,k*2,k*2+2,k*2+3 ;Add triangles to suface texture front and back
	AddTriangle surf,k*2,k*2+3,k*2+1
	AddTriangle surf,k*2,k*2+3,k*2+2
	AddTriangle surf,k*2,k*2+1,k*2+3
Next

EntityTexture mesh,tex     ;Texture water mesh
TurnEntity mesh,90,0,0     ;Turn
ScaleMesh mesh,1.24,2.50,0 ;Scale
PositionMesh mesh,0,.25,0  ;Position
EntityAlpha mesh,.6        ;Adjust mesh alpha (see through)
EntityFX mesh,.7
UpdateNormals mesh         ;Update mesh normals

;----------------------------------
; Create Mirror and Plane (Floor)
;----------------------------------
plane=CreatePlane(10,gate)	   ;Create Plane
PositionEntity plane,0,-3.0,0  ;Position Plane
EntityTexture plane,Floor_t    ;Apply texture to plane

mirror=CreateMirror(gate)      ;Create mirror
PositionEntity mirror,0,-3.05,0


;----------------------------------
; Create Light
;----------------------------------
light1=CreateLight(3)		   ;Create Light
LightConeAngles light1,1,90    ;Adjust Light Cone Angle
PositionEntity light1,0,-10,20 ;Position light
LightColor light1,255,255,255  ;Full brightt white light
LightRange light1,100	       ;Set light range
EntityParent light1,c_pivot	   ;Make the lights parent camera's pivot

light2=CreateLight(2)
LightColor light2,255,255,255
PositionEntity light2,-.6,0,-2
PointEntity light2,mesh

light3=CreateLight(2)
PositionEntity light2,-3,0,-2
PointEntity light3,mesh



;Point light and Camera at water mesh
PointEntity camera,mesh  ;Point camera to water mesh
PointEntity light1,mesh	 ;Point light to water mesh

;----------------------------------
; Main Loop
;----------------------------------
While Not KeyHit(1)
	ph#=MilliSecs()/1.3        ;Frequency of waves (time)
	cnt=CountVertices(surf)-1  ;Count all of the Vertices in water mesh
		
	For k=0 To cnt             ;Loop through mesh and update vertices
		x#=VertexX(surf,k)
		y#=VertexY(surf,k)	
		z#=Sin(ph+x*200)*depth ;Create wave
		VertexCoords surf,k,x,y,z ;Update
	Next


	If tnt<MilliSecs()            ;Timer
		EntityTexture mesh,tex,tn ;Texture water mesh with next frame of animated texture
		tn=tn+1					  ;Counter
			If tn=8 Then tn=0     ;Check if end of frame is reached
		tnt=MilliSecs()+32        ;Reset timer
	EndIf

	UpdateNormals mesh            ;Update water mesh normals
		
	
	TurnEntity c_pivot,0,0,.5     ;Orbit camera and light

	UpdateWorld
	RenderWorld
	
	If time>MilliSecs()
		Color 255,0,0
		Text 10,10,"Emerald Gate * Written By Richard Betson"
		Text 10,25,"www.redeyeware.50megs.com"
		Color 64,255,32
		Text 10,40,"Press Escape to Exit."
	EndIf
	
	Flip

Wend



Function error()
SetBuffer FrontBuffer()
Color 255,255,255
Text 20,20,"ERROR LOADING MEDIA FILES!!! PLEASE INSTALL AGAIN!!"
Delay 4000
End
End Function