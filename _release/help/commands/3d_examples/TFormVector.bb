; TFormVector example

Graphics3D 640, 480

p = CreatePivot()

PositionEntity p, 10, 20, 30   ; easy to visualize
TurnEntity p, -5, -15, 25      ; hard to visualize

; Question: what would happen if we took one step 'forward'?
; The local vector corresponding to one step forward is (0,0,1)
; in the pivot's local space. We need the global version.

TFormVector 0,0,1, p,0    ;  transform from pivot to world

message$ = "'One step forward' vector is  ( "
message = message + TFormedX() + ",  " + TFormedY() + ",  " + TFormedZ() + " )"

Text 70, 180, message

; Now actually take the step. The new location should be
; (10,20,30) plus the vector we just computed.

MoveEntity p, 0,0,1

message$ = "New location of pivot is  ( "
message = message + EntityX(p) + ",  "
message = message + EntityY(p) + ",  " + EntityZ(p) + " )"

Text 100, 210, message

Flip

WaitKey()
End