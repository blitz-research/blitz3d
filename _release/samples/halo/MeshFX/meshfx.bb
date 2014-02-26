;========================
;Mesh deformation effects
;========================
;by Joshua 'H A L O' Klint
;halo9@planetquake.com

Include "../start.bb"

Global offset#

camera=CreateCamera()
CameraRange camera,1,1000
PositionEntity camera,0,0,-20

mesh=LoadMesh("test.3ds")
mesh2=LoadMesh("test.3ds")
t=LoadTexture("skin.bmp")
EntityTexture mesh,t
FreeTexture t
ScaleEntity mesh,0.1,0.1,0.1
HideEntity mesh2
RotateEntity mesh2,0,90,45

l=CreateLight()
PositionEntity l,5,5,-5

depth#=3
speed#=0.1
effect=3

While Not KeyDown(1)

If KeyDown(203) TurnEntity mesh,0,4,0
If KeyDown(205) TurnEntity mesh,0,-4,0
If KeyDown(200) TurnEntity mesh,4,0,0
If KeyDown(208) TurnEntity mesh,-4,0,0
If KeyDown(2) Then effect=2
If KeyDown(3) Then effect=3

If effect=2 Then Breath(mesh,mesh2,(Sin(MilliSecs()*speed)*depth)+(depth))
If effect=3 Then ripple(mesh,mesh2,depth,speed)

UpdateWorld
RenderWorld
Text 0,0,"Press 1 for 'breathe' effect."
Text 0,20,"Press 2 for 'egg tube' effect."
Flip
Wend
End

;============================
Function Breath(mesh,omesh,am#)
For k=1 To CountSurfaces(mesh)
surf=GetSurface(mesh,k)
surf2=GetSurface(omesh,k)
For index=0 To CountVertices(surf)-1
newx#=VertexX(surf2,index)+VertexNX(surf2,index)*am
newy#=VertexY(surf2,index)+VertexNY(surf2,index)*am
newz#=VertexZ(surf2,index)+VertexNZ(surf2,index)*am
VertexCoords surf,index,newx,newy,newz
Next
Next
End Function

;==========================
Function ripple(mesh,omesh,depth#,speed#)
For k=1 To CountSurfaces(mesh)
surf=GetSurface(mesh,k)
surf2=GetSurface(omesh,k)
For index=0 To CountVertices(surf)-1
i=i+1
add#=0
If wrapvalue#((MilliSecs()*speed)+i,0,720)>630 Or wrapvalue#((MilliSecs()*speed)+i,0,720)<270
add#=(Sin(MilliSecs()*speed+(i))*depth)
newx#=add*VertexNX(surf,index)+VertexX(surf2,index)
newy#=add*VertexNY(surf,index)+VertexY(surf2,index)
newz#=add*VertexNZ(surf,index)+VertexZ(surf2,index)
Else
add=depth/-1
newx#=add*VertexNX(surf,index)+VertexX(surf2,index)
newy#=add*VertexNY(surf,index)+VertexY(surf2,index)
newz#=add*VertexNZ(surf,index)+VertexZ(surf2,index)
EndIf
VertexCoords surf,index,newx,newy,newz
Next
Next
End Function

;====================
Function wrapvalue#(value#,lo#,hi#)
i=Floor(value/(hi-lo))
r#=i*(hi-lo)
Return (value-r)+lo
End Function

;====================
Function curvevalue#( newvalue#,oldvalue#,increments# )
If increments>1 Then oldvalue#=oldvalue#-(oldvalue#-newvalue#)/increments
If increments<=1 Then oldvalue=newvalue
Return oldvalue#
End Function