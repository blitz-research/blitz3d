For a = 1 To CountGfxModes ()
If GfxMode3D (a)
Print "Mode " + a + " is 3D-capable"
Else
Print "Mode " + a + " is NOT 3D-capable"
EndIf
Delay 100
Next