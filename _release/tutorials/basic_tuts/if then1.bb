.start 
question$ = Input ("Shall I tell you the meaning of life yes/no? ")
If question$ = "no" Then Print "Alright, Then I won't":Goto Leave
If question$ = "yes" Then Print "42":Goto Leave
If question$ <> "yes" Or "no" Then Goto start
End
.Leave
Print "Press ESC to Exit"
Repeat
	VWait
Until KeyHit(1)