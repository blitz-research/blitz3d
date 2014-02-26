; CreateSurface Example
; ---------------------

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,0,0,-5

light=CreateLight()
RotateEntity light,90,0,0

mesh=CreateMesh()
surf=CreateSurface( mesh )

Const segs=128,width#=4,depth#=.125

For k=0 To segs
	x#=Float(k)*width/segs-width/2
	u#=Float(k)/segs
	AddVertex surf,x,1,0,u,0
	AddVertex surf,x,-1,0,u,1
Next

For k=0 To segs-1
	AddTriangle surf,k*2,k*2+2,k*2+3
	AddTriangle surf,k*2,k*2+3,k*2+1
Next

b=LoadBrush( "../media/b3dlogo.jpg" )
PaintSurface surf,b

While Not KeyDown( 1 )
	
	RenderWorld
	Flip

Wend

End