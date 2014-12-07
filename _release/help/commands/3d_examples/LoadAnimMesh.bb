; LoadAnimMesh Example
; --------------------

; In this example we will demonstrate the use of the LoadAnimMesh command.

; Quite simply, we will load an anim mesh from file, animate it, and then view it.

Graphics3D 640,480
SetBuffer BackBuffer()

camera=CreateCamera()
PositionEntity camera,0,20,-100 ; position camera so that robot will be in view when loaded

light=CreateLight()
RotateEntity light,90,0,0

; Load anim mesh
robot=LoadAnimMesh("media/makbot/mak_robotic.3ds")

; Animate mesh - this will begin an animation sequence which is updated when UpdateWorld is called
Animate robot,2

While Not KeyDown(1)

UpdateWorld ; Update anim - without this our anim mesh will freeze
RenderWorld ; Render everything
Flip ; Show everything

Wend

End