.start 
number = Input ("How old are you ")
If number < 18 Then Print "You are under 18": Goto Leave
If number > 18 Then Print "You are over 18":Goto Leave
If number = 18 Then Print "you are 18":Goto Leave
.Leave
Print "Press ESC to Exit"
Repeat
	VWait
Until KeyHit(1)