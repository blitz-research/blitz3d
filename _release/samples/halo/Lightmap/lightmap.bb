Include "../start.bb"

Global Log=WriteFile("log.txt")

camera=CreateCamera()

map=LoadAnimMesh("level.x")
litemap=LoadAnimMesh("lightmap.x")
t=LoadTexture("lightmap.bmp")

MapVertices(map,litemap,t)
FreeEntity litemap
FreeTexture t

MoveEntity camera,0,200,-500
PointEntity camera,map

While Not KeyHit(1)
TurnEntity map,0,0.1,0
UpdateWorld
RenderWorld
Flip
Wend
End

;=====================
Function MapVertices(model,copy,texture)

WriteLine Log,"Limb "+EntityName(model)+" contains "+CountChildren(model)+" children, "+CountSurfaces(model)+" surfaces.
For n=1 To CountSurfaces(model)
surf=GetSurface(model,n)
WriteLine Log,"Limb "+EntityName(model)+", surface "+n+" contains "+CountVertices(surf)+" vertices."
Next

For n=1 To CountSurfaces(model)
	WriteLine Log,"Testing surface "+n
	surf=GetSurface(model,n)
	lsurf=GetSurface(copy,n)
	For v=0 To CountVertices(surf)-1
		WriteLine Log,"Testing vertex "+v+1
		tu#=VertexU#(lsurf,v)
		tv#=VertexV#(lsurf,v)
		tw#=VertexW#(lsurf,v)
		VertexTexCoords surf,v,tu,tv,tw,1
	Next
Next

WriteLine Log,""

TextureCoords texture,1
EntityTexture model,texture,0,1

For e=1 To CountChildren(Model)
limb=GetChild(model,e)
limb2=GetChild(copy,e)
MapVertices(limb,limb2,texture)
Next

End Function