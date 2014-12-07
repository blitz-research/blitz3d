For a = 1 To CountGfxDrivers ()
If GfxDriver3D (a)
Print GfxDriverName (a) + " is 3D-capable"
Else
Print GfxDriverName (a) + " is NOT 3D-capable"
EndIf
Delay 100
Next