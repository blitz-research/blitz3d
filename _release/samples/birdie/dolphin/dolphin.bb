;Blend Meshes
;Concept and models from directX7 SDK.
;Converted to Blitz3D by
;David Bird
;dave@birdie72.freeserve.co.uk

Graphics3D 640,480
SetBuffer BackBuffer()

lit=CreateLight()
LightColor lit,50,50,170
AmbientLight 51,51,51

piv=CreatePivot()
cam=CreateCamera()
CameraRange cam,1,2000
CameraClsColor cam,0,102,136
CameraFogColor cam,0,102,136
CameraFogMode cam,1
CameraFogRange cam,1,50

PositionEntity cam,0,4,-30

;	LOAD DOLPHIN INFORMATION HERE
;	All triangles and vertices MUST be the same
;	for this to work.
upmesh=LoadMesh("dup.x"):HideEntity upmesh	;load mesh back fin uppermost
md=LoadMesh("dolphin.x"):HideEntity md;load mesh for middle
dnmesh=LoadMesh("ddown.x"):HideEntity dnmesh ;load mesh back fin lowermost
final=LoadMesh("dolphin.x")
ScaleMesh upmesh,.01,.01,.01
ScaleMesh md,.01,.01,.01
ScaleMesh dnmesh,.01,.01,.01

seafloor=LoadMesh("seafloor.x")

While Not KeyDown(1)
	TurnEntity final,0,1,0:MoveEntity final,-.2,0,0
	PointEntity cam,final
	If KeyDown(203) TurnEntity cam,0,+1,0
	If KeyDown(205) TurnEntity cam,0,-1,0
	If KeyDown(200) TurnEntity cam,+1,0,0
	If KeyDown(208) TurnEntity cam,-1,0,0

	If KeyDown(44) MoveEntity cam,0,0,-10
	If KeyDown(30) MoveEntity cam,0,0,10
	q#=Sin(p)
	p=(p+4) Mod 360
	If q<0 Then
		Blend_meshes(final,dnmesh,md,-q#)
	Else
		Blend_meshes(final,upmesh,md,q#)
	End If
	TranslateEntity final,0,Sin(-q),0

	UpdateWorld
	RenderWorld
	Flip
Wend

FreeEntity upmesh
FreeEntity dnmesh
FreeEntity final
FreeEntity lit
FreeEntity piv
FreeEntity cam
EndGraphics
End

Function Blend_Meshes(dst,sc1,sc2,w#)
	iw#=1-w
	sc1s=GetSurface(sc1,CountSurfaces(sc1))
	sc2s=GetSurface(sc2,CountSurfaces(sc2))
	dsts=GetSurface(dst,CountSurfaces(dst))
	For a=0 To CountVertices(sc1s)-1
        x# =  w*VertexX(sc1s,a)   + iw*VertexX(sc2s,a)
        y# =  w*VertexY(sc1s,a)   + iw*VertexY(sc2s,a)
        z# =  w*VertexZ(sc1s,a)   + iw*VertexZ(sc2s,a)
        nx# = w*VertexNX(sc1s,a)  + iw*VertexNX(sc2s,a)
        nx# = w*VertexNY(sc1s,a)  + iw*VertexNY(sc2s,a)
        nx# = w*VertexNZ(sc1s,a)  + iw*VertexNZ(sc2s,a)
		VertexCoords dsts,a,x,y,z
		VertexNormal dsts,a,nx,ny,nz
	Next
End Function