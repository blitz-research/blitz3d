name$=Input("Filename: ")
Print name
If Right(name,3)=".bb" Then name=Left(name,Len(name)-3)
While FileType(name+".bb")<>1
	Cls
	Locate 0,0
	r=r+1
	If r>6
		Print "That's it. I've had enough! I'll get the gun."
		Delay 1500
		Cls
		Locate 0,0
		For c=1 To 5
			ClsColor 255,255,0
			Cls
			Delay 50
			VWait
			red=red+51
			ClsColor red,0,0
			Cls
			Delay 500
			VWait 15
		Next
		red=255
		While red>0
			ClsColor red,0,0
			red=red-1
			Cls
			VWait
		Wend
		Delay 1500
		Cls
		Locate 0,0
		Print "Oh, you monster!"
		Delay 2000
		Cls
		End
	EndIf
	Select r
		Case 1
			evil$="No, really. Filename: "
		Case 2
			evil$="Try again. Filename: "
		Case 3
			evil$="Not too good at this, are you? Filename: "
		Case 4
			evil$="You think this is funny, don't you? Filename: "
		Case 5
			evil$="Aren't you bored yet? Filename: "
		Case 6
			Print "Enter a valid filename or I'll terminate myself."
			Delay 1500
			Cls
			Locate 0,0
			Print "I'm serious, you know."
			Delay 1500
			Cls
			Locate 0,0
			evil$="Filename: "
	End Select
	name$=Input(evil$)
	If Right(name,3)=".bb" Then name=Left(name,Len(name)-3)
Wend
If Len(name)>3
	If Lower(Right(name,3))=".bb"
		name=Left(name,Len(name)-3)
	EndIf
EndIf

file=ReadFile(name+".bb")
outfile=WriteFile(name+".html")


;;********************************************************************************************
;;Start
WriteLine outfile,"<html><head><title>BlitzDoc Documentation for "+name+".bb</title></head><body>"
WriteLine outfile,"<h1 align=center>BlitzDoc Documentation for "+name+".bb</h1>"
;;********************************************************************************************

While Not Eof(file)
	in$=Trim(ReadLine(file))
	;;*************************************************************************************
	;;Find Globals and Constants
	If Lower(Left(in,7))="global " Or Lower(Left(in,6))="const "
		If Lower(Left(in,6))="global"
			vtype$="Global"
			bgcol$="ccccff"
			c=8
		Else
			vtype="Constant"
			bgcol$="ccffcc"
			c=7
		EndIf
		
		Print "shloop"
		name$=""
		While c<=Len(in)
			If Mid(in,c,1)="," Or Mid(in,c,1)="=" Or Mid(in,c,1)="[" Or c=Len(in)
				name=Trim(name)
				arraylength$=""
				If Mid(in,c,1)="["
					While Mid(in,c,1)<>"]" And c<=Len(in)
						c=c+1
						arraylength=arraylength+Mid(in,c,1)
					Wend
				EndIf
				If arraylength<>""
					arraylength="["+arraylength+"]"
				EndIf
				If c=Len(in) Then name=name+Right(in,1)
				Print "spong"
				Select Right(name,1)
					Case "$"
						dtype$="String"
						name=Left(name,Len(name)-1)
					Case "#"
						dtype$="Float"
						name=Left(name,Len(name)-1)
					Case "%"
						dtype$="Integer"
						name=Left(name,Len(name)-1)
					Default
						Print "ook"
						ca=Len(name)
						If Len(name)>2
							While ca>1 And Mid(name,ca,1)<>"."
								ca=ca-1
							Wend
							If Mid(name,ca,1)="."
								Print "pook"
								dtype$=Right(name,Len(name)-ca)
								dtype="<font color=ff0000><i>"+Upper(Left(dtype,1))+Lower(Right(dtype,Len(dtype)-1))+"</i></font>"
								name=Left(name,ca-1)
							Else
								dtype$="Integer"
							EndIf
						Else
							dtype$="Integer"
						EndIf
				End Select
				WriteLine outfile,"<table width=100%>"
				WriteLine outfile,"<tr bgcolor="+bgcol+"><td><p><b>"+dtype+"</b> - <i>"+name+"</i>"+arraylength+"</p></td><td align=Right width=80><p><i>"+vtype+" </i></p></td></tr>"
				WriteLine outfile,"</table>"
				name=""
				If Mid(in,c,1)="="
					While Mid(in,c,1)<>"," And c<=Len(in)
						c=c+1
					Wend
				EndIf
			Else
				name=name+Mid(in,c,1)
			EndIf
			c=c+1
		Wend
	EndIf
	;;*************************************************************************************
	
	
	;;*************************************************************************************
	;;Find Types
	If Lower(Left(in,5))="type "
		name$=Right(in,Len(in)-5)
		name=Upper(Left(in,1))+Lower(Right(in,Len(in)-1))
		
		WriteLine outfile,"<table width=100%>"
		WriteLine outfile,"<tr bgcolor=cccccc><td><p><b>"+name+"</b></p></td><td width=80 align=right><p><i>Type</i></p></td></tr>"
		WriteLine outfile,"<tr><td><p><i>Fields:</i></p><ul>"
	
		While Lower(in)<>"end type"
			in=Trim(ReadLine(file))
			If Lower(Left(in,5))="field"
				c=7
				pname$=""
				While c<=Len(in)
					If Mid(in,c,1)="," Or Mid(in,c,1)="[" Or c=Len(in)
						pname=Trim(pname)
						arraylength$=""
						If Mid(in,c,1)="["
							While Mid(in,c,1)<>"]" And c<=Len(in)
								c=c+1
								arraylength=arraylength+Mid(in,c,1)
							Wend
						EndIf
						If arraylength<>""
							arraylength="["+arraylength+"]"
						EndIf
						If c=Len(in) Then pname=pname+Right(in,1)
						Print "spong"
						Select Right(pname,1)
							Case "$"
								dtype$="String"
								pname=Left(pname,Len(pname)-1)
							Case "#"
								dtype$="Float"
								pname=Left(pname,Len(pname)-1)
							Case "%"
								dtype$="Integer"
								pname=Left(pname,Len(pname)-1)
							Default
								Print "ook"
								ca=Len(pname)
								If Len(pname)>2
									While ca>1 And Mid(pname,ca,1)<>"."
										ca=ca-1
									Wend
									If Mid(pname,ca,1)="."
										Print "pook"
										dtype$=Right(pname,Len(pname)-ca)
										dtype="<font color=ff0000><i>"+Upper(Left(dtype,1))+Lower(Right(dtype,Len(dtype)-1))+"</i></font>"
										pname=Left(pname,ca-1)
									Else
										dtype$="Integer"
									EndIf
								Else
									dtype$="Integer"
								EndIf
						End Select
						WriteLine outfile,"<li><b>"+dtype$+"</b> - <i>"+pname+"</i>"+arraylength
						pname=""
					Else
						pname=pname+Mid(in,c,1)
					EndIf
					c=c+1
				Wend
			EndIf
		Wend
		WriteLine outfile,"</ul></td></tr></table>"
	EndIf
	;;*************************************************************************************
	
	;;*************************************************************************************
	;;Document functions
	If Lower(Left(in,9))="function "
		c=10
		name$=""
		While Mid(in,c,1)<>"("
			name$=name+Mid(in,c,1)
			c=c+1
		Wend
		name=Trim(name)
		WriteLine outfile,"<table width=100%>"
		WriteLine outfile,"<tr bgcolor=ffaaaa><td><p><b>"+name+"</b></p></td><td width=80 align=right><p><i>Function</i></p></td></tr>"
		WriteLine outfile,"<tr><td><p><i>Parameters:</i></p><ul>"
		pname$=""
		While Mid(in,c,1)<>")"
			c=c+1
			If Mid(in,c,1)="," Or Mid(in,c,1)="[" Or Mid(in,c,1)=")"
				pname=Trim(pname)
				arraylength$=""
				If Mid(in,c,1)="["
					While Mid(in,c,1)<>"]" And c<=Len(in)
						c=c+1
						arraylength=arraylength+Mid(in,c,1)
					Wend
				EndIf
				If arraylength<>""
					arraylength="["+arraylength
				EndIf
				Select Right(pname,1)
					Case "$"
						dtype$="String"
						pname=Left(pname,Len(pname)-1)
					Case "#"
						dtype$="Float"
						pname=Left(pname,Len(pname)-1)
					Case "%"
						dtype$="Integer"
						pname=Left(pname,Len(pname)-1)
					Default
						Print "ook"
						ca=Len(pname)
						If Len(pname)>2
							While ca>1 And Mid(pname,ca,1)<>"."
								ca=ca-1
							Wend
							If Mid(pname,ca,1)="."
								Print "pook"
								dtype$=Right(pname,Len(pname)-ca)
								dtype="<font color=ff0000><i>"+Upper(Left(dtype,1))+Lower(Right(dtype,Len(dtype)-1))+"</i></font>"
								pname=Left(pname,ca-1)
							Else
								dtype$="Integer"
							EndIf
						Else
							dtype$="Integer"
						EndIf
				End Select
				WriteLine outfile,"<li><b>"+dtype$+"</b> - <i>"+pname+"</i>"+arraylength
				pname=""
			Else
				pname=pname+Mid(in,c,1)
			EndIf
		Wend
		WriteLine outfile,"</ul></td></tr>"
		comments$=""
		While Lower(Left(in,12))<>"end function"
			in=Trim(ReadLine(file))
			If Lower(Left(in,5))=";doc " And Len(in)>5
				comments$=comments+Right(in,Len(in)-5)+"<br>"
			EndIf
		Wend
		If comments<>""
			WriteLine outfile,"<tr><td bgcolor=eeeeee colspan=2><p><b>Comments</b></p>"
			WriteLine outfile,"<p>"+comments+"</p>"
			WriteLine outfile,"</td></tr>"
		EndIf
		WriteLine outfile,"</table>"
	EndIf
	;;*************************************************************************************
Wend

;;*************************************************************************************
;;Ending Text
WriteLine outfile,"</table>"
WriteLine outfile,"<p><font size=-1>This documentation was generated by BlitzDoc. BlitzDoc is copyright Christian Perfect 2001</font></p>"
WriteLine outfile,"</body></html>"
;;*************************************************************************************

;;****************
;;End
CloseFile file
CloseFile outfile
Print " "
Color 255,255,0
Cls
Locate 0,0
Print "DONE!"
Print "Press a key to quit"
VWait
Flip
Flip
WaitKey
End
;;****************