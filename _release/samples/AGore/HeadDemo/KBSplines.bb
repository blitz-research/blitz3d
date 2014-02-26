;Kochanek-Bartels Splines

;-------------------------------------------------------------------------------------
;									VARIABLES
;-------------------------------------------------------------------------------------

Const nkeyframes = 100				;maximum number of keyframes per motion
Const nchannels = 9					;number of channel values
Dim icv#(nchannels-1)				;stores interpolated channel values
Global key0.KeyFrame, key1.KeyFrame
key0.KeyFrame = New keyframe : key1.KeyFrame = New keyframe

Type Motion
 Field nkeys						;number of keyframes
 Field nsteps						;last frame number
 Field keylist.KeyFrame[nkeyframes-1];array of keyframes
End Type

Type KeyFrame
 Field fstep						;frame number
 Field cv#[nchannels-1]				;array of channel values (x y z h p b sx sy sz)
 Field linear						;linear flag
 Field tens#						;Kochanek-Bartels parameters
 Field cont#
 Field bias#
End Type

;-------------------------------------------------------------------------------------
;									FUNCTIONS
;-------------------------------------------------------------------------------------

Function Load_Motion( file$, m.Motion )

 Local f,s$, i, ii

 f = ReadFile( file )
 If f = False Then Return False

 If ReadLine$(f) <> "B3D Motion" Then Return False
 If Int(ReadLine$(f)) <> "1" Then Return False
 ReadLine$(f)

 m\nkeys = Int(ReadLine$(f))
 
 For i = 0 To m\nkeys									;read keyframe data
 	s$ = ReadLine$( f )
	m\keylist[i] = New KeyFrame
	m\keylist[i]\fstep = Int(Parse(s,1))
	For ii = 0 To nchannels-1 : m\keylist[i]\cv[ii] = Float(Parse(s,2+ii)) : Next
	m\keylist[i]\linear = Int(Parse(s,11))
	m\keylist[i]\tens = Float(Parse(s,12))
	m\keylist[i]\cont = Float(Parse(s,13))
	m\keylist[i]\bias = Float(Parse(s,14))
 Next

 m\nsteps = m\keylist[m\nkeys-1]\fstep

 CloseFile f
 Return True

End Function
;------------------------------------------------------------------------

;------------------------------------------------------------------------
Function Apply_Motion(m.Motion, tstep#, e)

 Local dd0a#, dd0b#, ds1a#, ds1b#
 Local adj0#, adj1#, dd0#, ds1#, d2#, t#, d10#
 Local t2#, t3#, z#, h#[3]
 Local i, tlen, key

 ;*** if there's only one key, the channel values are constant ***

 If (m\nkeys = 1) Then
  For i = 0 To nchannels-1 : icv(i) = m\keylist[0]\cv[i] : Next
  Return
 End If

 ;*** find keyframe pair to interpolate between ***
   
 If tstep < m\keylist[0]\fstep Then Return

 For key = 1 To m\nkeys						
   If tstep <= m\keylist[key]\fstep Then Exit
 Next
 key1 = m\keylist[key]

 If key = m\nkeys Then Return

 key0 = m\keylist[key - 1]

 tlen = key1\fstep - key0\fstep
 t = (tstep - key0\fstep) / tlen

 If key1\linear = 0 Then
   
  ;*** precompute hermite spline coefficients ***

  t2 = t * t
  t3 = t * t2
  z = 3 * t2 - t3 - t3

  h[0] = 1 - z
  h[1] = z
  h[2] = t3 - t2 - t2 + t
  h[3] = t3 - t2
      
  dd0a = (1 - key0\tens) * (1 + key0\cont) * (1 + key0\bias)
  dd0b = (1 - key0\tens) * (1 - key0\cont) * (1 - key0\bias)
  ds1a = (1 - key1\tens) * (1 - key1\cont) * (1 + key1\bias)
  ds1b = (1 - key1\tens) * (1 + key1\cont) * (1 - key1\bias)

  If key0\fstep <> 0 Then
	d2 = (key1\fstep -  m\keylist[key - 2]\fstep)
	adj0 = tlen / d2
  End If
  
  If key1\fstep <>  m\nsteps Then
	d2 = (m\keylist[key+1]\fstep - key0\fstep)
	adj1 = tlen / d2
  End If
      
 End If

 ;*** compute channel components and store in icv() ***

 For i = 0 To nchannels-1

   d10 = key1\cv[i] - key0\cv[i]

   If key1\linear = 0 Then
         
    If key0\fstep = 0 Then
     dd0 = 0.5 * (dd0a + dd0b) * d10
    Else
     dd0 = adj0 * (dd0a * (key0\cv[i] -  m\keylist[key - 2]\cv[i]) + dd0b * d10)
    End If
         
    If key1\fstep = m\nsteps Then
     ds1 = 0.5 * (ds1a + ds1b) * d10
    Else
     ds1 = adj1 * (ds1a * d10 + ds1b * ( m\keylist[key + 1]\cv[i] - key1\cv[i] ))
    End If
         
    icv(i) = (h[0] * key0\cv[i]) + (h[1] * key1\cv[i]) + (h[2] * dd0) + (h[3] * ds1)

   Else
    icv(i) = key0\cv[i] + t * d10
   End If

 Next

 PositionEntity e,icv(0),icv(1),icv(2)
 RotateEntity e,icv(3),icv(4),icv(5)
 ScaleEntity e,icv(6),icv(7),icv(8)

End Function
;-------------------------------------------------------------------------------------

;-------------------------------------------------------------------------------------
Function Parse$(s$,o)

 Local a$,d$,bb,b,e#,even
 s=Trim(s)+" " : d="" : bb=0 : e=0 : even=True

 For b=1 To Len(s)

	a = Mid(s,b,1)
	
	If a = "'" Then e=e+1 : even = ( (e/2) = Int(e/2) )
	
	If a = " "
	 If even = True		
	  bb=bb+1
	  If bb = o Then Return Trim( Replace(d,"'"," ") ) Else d=""
	 EndIf
	EndIf
	d=d+a
	
 Next

End Function