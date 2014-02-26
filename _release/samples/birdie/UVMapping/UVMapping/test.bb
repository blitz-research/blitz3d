; UV Mapping
; dave@birdie72.freeserve.co.uk
; todo: Make it work right..
Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,0,0,-4

sphere=CreateSphere(10)
ScaleMesh sphere,2,1,1

tex=LoadTexture("tex0.jpg")
ScaleTexture tex,.1,.1
EntityTexture sphere,tex
While Not KeyDown(1)
	TurnEntity sphere,1,1,0

	If KeyDown(2) Then Apply_FlatWrap(sphere,.1,.1)
	If KeyDown(3) Then Apply_CylWrap(sphere,.01,.1,True)
	If KeyDown(4) Then Apply_SphereWrap(sphere,.01,.1)
	If KeyDown(5) Then Apply_flatWrap(sphere,.1,.1,True)
	UpdateWorld
	RenderWorld
	Flip
Wend
EndGraphics
End


Function Apply_CylWrap(mesh,scaleu#,scalev#,crome=False)
	For b=1 To CountSurfaces(mesh)
		surf=GetSurface(mesh,b)
		For a=0 To CountVertices(surf)-1
			originu#=0
			originv#=0
			x#=VertexX(surf,a)
			y#=VertexY(surf,a)
			z#=VertexZ(surf,a)
			If crome=True Then
				TFormPoint x,y,z,mesh,0
				x=TFormedX()
				y=TFormedY()
				z=TFormedZ()
			End If
			u#=((scaleu/(2*Pi))*ATan(x/y))-originu
			v#=(scalev*z)-originv
			VertexTexCoords surf,a,u,v
		Next
	Next
End Function

Function Apply_FlatWrap(mesh,scaleu#,scalev#,crome=False)
	For b=1 To CountSurfaces(mesh)
		surf=GetSurface(mesh,b)
		For a=0 To CountVertices(surf)-1
			originu#=0
			originv#=0
			x#=VertexX(surf,a)
			y#=VertexY(surf,a)
			z#=VertexZ(surf,a)
			If crome=True Then
				TFormPoint x,y,z,mesh,0
				x=TFormedX()
				y=TFormedY()
				z=TFormedZ()
			End If
			u#=(scaleu*x)-originu
			v#=(scalev*y)-originv
			VertexTexCoords surf,a,u,v
		Next
	Next
End Function

Function Apply_SphereWrap(mesh,scaleu#,scalev#,crome=False)
	For b=1 To CountSurfaces(mesh)
		surf=GetSurface(mesh,b)
		For a=0 To CountVertices(surf)-1
			originu#=0
			originv#=0
			x#=VertexX(surf,a)
			y#=VertexY(surf,a)
			z#=VertexZ(surf,a)
			If crome=True Then
				TFormPoint x,y,z,mesh,0
				x=TFormedX()
				y=TFormedY()
				z=TFormedZ()
			End If
			u#=((scaleu/2*Pi)*ATan(x/y))-Originu
			v#=((scalev/Pi)*ATan(z/Sqr(x^2+y^2+z^2)))-originv
			VertexTexCoords surf,a,u,v
		Next
	Next
End Function

