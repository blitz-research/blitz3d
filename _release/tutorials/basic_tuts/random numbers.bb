; Very simple random number game by George Bray
; georgebray@breathemail.net
; http://users.breathemail.net/georgebray

Print "Escape from the furry animals of Ancipital"
SeedRnd(MilliSecs()) 
f=Rnd(20) 
For g=1 To 5
 guess = Input ("Guess the frequency of the menagerie? (0 - 20) ")
If guess=f Then Goto goat
If guess<f Then Gosub cow
If guess>f Then Gosub head

.g Next
Print"*******BooooooooooooooM******* The menagerie exploded. The correct frequency was: ": Print f
MouseWait
End
.cow Print "Frequency too low - Careful":Return 
.goat Print "WELL DONE DUDE - YOU ESCAPED FROM THE FURRY ANIMALS OF ANCIPITAL"
MouseWait
End
.head Print"Frequency too high - try again":Return
MouseWait
End


