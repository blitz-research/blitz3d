;--------------------
; Init menus function
;--------------------

Function InitMenus()

	; Load logo/get info

	logo=LoadImage("media\logo.bmp") ; Load background 'block'
	logo_width=ImageWidth(logo)
	logo_height=ImageHeight(logo)
	Dim greyscale(logo_width,logo_height)

	SetBuffer ImageBuffer(logo)
	LockBuffer()
	For x=0 To ImageWidth(logo)
		For y=0 To ImageHeight(logo)
			pix=ReadPixelFast(x,y)
			red=(pix Shr 16) And 255
			green=(pix Shr 8) And 255
			blue=pix And 255
			grey=(red+green+blue)/3.0
			greyscale(x,y)=grey
		Next
	Next
	UnlockBuffer()
	SetBuffer BackBuffer()
	
	; Random starting background colour
	randy=Int(Rnd(4))
	Select randy
		Case 0 : col=15 : cr=255 : cg=0 : cb=0
		Case 1 : col=3 : cr=255 : cg=255 : cb=0
		Case 2 : col=6 : cr=0 : cg=255 : cb=0 
		Case 3 : col=9 : cr=0 : cg=255 : cb=255
		Case 4 : col=12 : cr=255 : cg=0 : cb=255
	End Select

	; Get graphics mode info

	count_gfx_modes=CountGfxModes()
	
	For g=1 To count_gfx_modes
		If GfxMode3D(g)=True Then gm3d=gm3d+1
	Next
	
	Dim gfx_modes(gm3d,2) : gm3d=0

	For g=1 To count_gfx_modes
		If GfxMode3D(g)=True
			gg=gg+1
			gfx_modes(gg,0)=GfxModeWidth(g)
			gfx_modes(gg,1)=GfxModeHeight(g)
			gfx_modes(gg,2)=GfxModeDepth(g)
			If gfx_modes(gg,0)=width And gfx_modes(gg,1)=height And gfx_modes(gg,2)=GraphicsDepth() Then gfx_mode_option=gg
		EndIf
	Next

	; Load high score data
	load_file=ReadFile("media\high_scores.sav")
	For s=1 To 10
		names$(s)=ReadLine(load_file)
		scores(s)=ReadLine(load_file)
	Next
	CloseFile load_file

	; Play music
	music=PlayMusic("media\tune1.mid")

End Function


; -------------------
; Background function
; -------------------

Function Background()

	; A bit of 'waiting code' inbetween colour changes
	If col=1 Or col=4 Or col=7 Or col=10 Or col=13
		sr=0
		sg=0
		sb=0
		ct=ct+1
	EndIf
	If ct>500 Then ct=0 : col=col+1

	; Update current red, green, blue values with red, green, blue step values
	cr=cr+sr
	cg=cg+sg
	cb=cb+sb

	; Various colour stages
	
	If cr=>255 And cg=>0 And cb=<0 And col=15 Then col=1 : cr=255 : cg=0 : cb=0
	If col=2 Then col=3 : dr=255 : dg=255 : db=0 : sr=(dr-cr)/fade : sg=(dg-cg)/fade : sb=(db-cb)/fade

	If cr>=255 And cg>=255 And cb<=0 And col=3 Then col=4 : cr=255 : cg=255 : cb=0
	If col=5 Then col=6 : dr=0 : dg=255 : db=0 : sr=(dr-cr)/fade : sg=(dg-cg)/fade : sb=(db-cb)/fade

	If cr<=0 And cg>=255 And cb<=0 And col=6 Then col=7 : cr=0 : cg=255 : cb=0
	If col=8 Then col=9 : dr=0 : dg=255 : db=255 : sr=(dr-cr)/fade : sg=(dg-cg)/fade : sb=(db-cb)/fade

	If cr<=0 And cg>=255 And cb>=255 And col=9 Then col=10 : cr=0 : cg=255 : cb=255
	If col=11 Then col=12 : dr=255 : dg=0 : db=255 : sr=(dr-cr)/fade : sg=(dg-cg)/fade : sb=(db-cb)/fade

	If cr>=255 And cg<=0 And cb>=255 And col=12 Then col=13 : cr=255 : cg=0 : cb=255
	If col=14 Then col=15 : dr=255 : dg=0 : db=0 : sr=(dr-cr)/fade : sg=(dg-cg)/fade : sb=(db-cb)/fade

	; Draw block each frame!
	SetBuffer ImageBuffer(logo)
	LockBuffer()
	For x=0 To ImageWidth(logo)
		For y=0 To ImageHeight(logo)
			red=greyscale(x,y)*(cr/255.0)
			green=greyscale(x,y)*(cg/255.0)
			blue=greyscale(x,y)*(cb/255.0)
			pix=(red Shl 16) Or (green Shl 8) Or blue
			WritePixelFast x,y,pix
		Next
	Next
	UnlockBuffer()
	SetBuffer BackBuffer()

	; Tile the block
	TileBlock logo,0,tile_y
	tile_y=tile_y+1
	If tile_y>119 Then tile_y=tile_y-120

End Function


; ----------------
; Credits function
; ----------------

Function Credits()

	; Shadow text
	Color 0,0,0
	Text (width/2)+1,(y_space*1)+1,"Wing Ring V1.0",1
	Text (width/2)+1,(y_space*3)+1,"Credits",1
	Text (width/2)+1,(y_space*5)+1,"Programmed by Si",1
	Text (width/2)+1,(y_space*6)+1,"si@si-design.co.uk",1
	Text (width/2)+1,(y_space*9)+1,"In Blitz3D",1
	If option=1 Then Text (width/2)+1,(y_space*7)+1,"> www.si-design.co.uk <",1 Else Text (width/2)+1,(y_space*7)+1,"www.si-design.co.uk",1
	If option=2 Then Text (width/2)+1,(y_space*10)+1,"> www.blitzbasic.com <",1 Else Text (width/2)+1,(y_space*10)+1,"www.blitzbasic.com",1
	If option=3 Then Text (width/2)+1,(y_space*11)+1,"> www.blitz3d.co.uk <",1 Else Text (width/2)+1,(y_space*11)+1,"www.blitz3d.co.uk",1
	If option=4 Then Text (width/2)+1,(y_space*13)+1,"> 1 Player <",1 Else Text (width/2)+1,(y_space*13)+1,"1 Player",1
	If option=5 Then Text (width/2)+1,(y_space*14)+1,"> 2 Player <",1 Else Text (width/2)+1,(y_space*14)+1,"2 Player",1
	If option=6 Then Text (width/2)+1,(y_space*15)+1,"> High Scores <",1 Else Text (width/2)+1,(y_space*15)+1,"High Scores",1
	If option=7 Then Text (width/2)+1,(y_space*16)+1,"> Options <",1 Else Text (width/2)+1,(y_space*16)+1,"Options",1

	; White text
	Color 255,255,255
	Text (width/2)+0,(y_space*1)+0,"Wing Ring V1.0",1
	Text (width/2)+0,(y_space*3)+0,"Credits",1
	Text (width/2)+0,(y_space*5)+0,"Programmed by Si",1
	Text (width/2)+0,(y_space*6)+0,"si@si-design.co.uk",1
	Text (width/2)+0,(y_space*9)+0,"In Blitz3D",1
	If option=1 Then Text (width/2)+0,(y_space*7)+0,"> www.si-design.co.uk <",1 Else Text (width/2)+0,(y_space*7)+0,"www.si-design.co.uk",1
	If option=2 Then Text (width/2)+0,(y_space*10)+0,"> www.blitzbasic.com <",1 Else Text (width/2)+0,(y_space*10)+0,"www.blitzbasic.com",1
	If option=3 Then Text (width/2)+0,(y_space*11)+0,"> www.blitz3d.co.uk <",1 Else Text (width/2)+0,(y_space*11)+0,"www.blitz3d.co.uk",1
	If option=4 Then Text (width/2)+0,(y_space*13)+0,"> 1 Player <",1 Else Text (width/2)+0,(y_space*13)+0,"1 Player",1
	If option=5 Then Text (width/2)+0,(y_space*14)+0,"> 2 Player <",1 Else Text (width/2)+0,(y_space*14)+0,"2 Player",1
	If option=6 Then Text (width/2)+0,(y_space*15)+0,"> High Scores <",1 Else Text (width/2)+0,(y_space*15)+0,"High Scores",1
	If option=7 Then Text (width/2)+0,(y_space*16)+0,"> Options <",1 Else Text (width/2)+0,(y_space*16)+0,"Options",1

	; Up/down option
	If KeyHit(200)=1 Then option=option-1
	If KeyHit(208)=1 Then option=option+1
	If option>7 Then option=1
	If option<1 Then option=7

	; Enter option
	If KeyHit(28)=True
		If option=1 Then ExecFile "http://www.si-design.co.uk/index.htm?referrer=wing_ring"
		If option=2 Then ExecFile "http://www.blitzbasic.com"
		If option=3 Then ExecFile "http://www.blitz3d.co.uk/index.htm?referrer=wing_ring"
		If option>3 Then screen=option
	EndIf

End Function


; -------------------
; Highscores function
; -------------------

Function HighScores()

	; Shadow text
	Color 0,0,0
	Text (width/2)+1,(y_space*1)+1,"Wing Ring V1.0",1
	Text (width/2)+1,(y_space*3)+1,"High Scores",1
	If option=1 And rank=1 Then Text x_nos-17,(y_space*5)+1,"> 1." Else Text x_nos+1,(y_space*5)+1,"1."
	If option=1 And rank=2 Then Text x_nos-17,(y_space*6)+1,"> 2." Else Text x_nos+1,(y_space*6)+1,"2."
	If option=1 And rank=3 Then Text x_nos-17,(y_space*7)+1,"> 3." Else Text x_nos+1,(y_space*7)+1,"3."
	If option=1 And rank=4 Then Text x_nos-17,(y_space*8)+1,"> 4." Else Text x_nos+1,(y_space*8)+1,"4."
	If option=1 And rank=5 Then Text x_nos-17,(y_space*9)+1,"> 5." Else Text x_nos+1,(y_space*9)+1,"5."
	If option=1 And rank=6 Then Text x_nos-17,(y_space*10)+1,"> 6." Else Text x_nos+1,(y_space*10)+1,"6."
	If option=1 And rank=7 Then Text x_nos-17,(y_space*11)+1,"> 7." Else Text x_nos+1,(y_space*11)+1,"7."
	If option=1 And rank=8 Then Text x_nos-17,(y_space*12)+1,"> 8." Else Text x_nos+1,(y_space*12)+1,"8."
	If option=1 And rank=9 Then Text x_nos-17,(y_space*13)+1,"> 9." Else Text x_nos+1,(y_space*13)+1,"9."
	If option=1 And rank=10 Then Text x_nos-17,(y_space*14)+1,"> 10." Else Text x_nos+1,(y_space*14)+1,"10."
	Text x_names+1,(y_space*5)+1,names$(1)
	Text x_names+1,(y_space*6)+1,names$(2)
	Text x_names+1,(y_space*7)+1,names$(3)
	Text x_names+1,(y_space*8)+1,names$(4)
	Text x_names+1,(y_space*9)+1,names$(5)
	Text x_names+1,(y_space*10)+1,names$(6)
	Text x_names+1,(y_space*11)+1,names$(7)
	Text x_names+1,(y_space*12)+1,names$(8)
	Text x_names+1,(y_space*13)+1,names$(9)
	Text x_names+1,(y_space*14)+1,names$(10)
	If option=1 And rank=1 Then Text x_scores+1,(y_space*5)+1,scores(1)+" <" Else Text x_scores+1,(y_space*5)+1,scores(1)
	If option=1 And rank=2 Then Text x_scores+1,(y_space*6)+1,scores(2)+" <" Else Text x_scores+1,(y_space*6)+1,scores(2)
	If option=1 And rank=3 Then Text x_scores+1,(y_space*7)+1,scores(3)+" <" Else Text x_scores+1,(y_space*7)+1,scores(3)
	If option=1 And rank=4 Then Text x_scores+1,(y_space*8)+1,scores(4)+" <" Else Text x_scores+1,(y_space*8)+1,scores(4)
	If option=1 And rank=5 Then Text x_scores+1,(y_space*9)+1,scores(5)+" <" Else Text x_scores+1,(y_space*9)+1,scores(5)
	If option=1 And rank=6 Then Text x_scores+1,(y_space*10)+1,scores(6)+" <" Else Text x_scores+1,(y_space*10)+1,scores(6)
	If option=1 And rank=7 Then Text x_scores+1,(y_space*11)+1,scores(7)+" <" Else Text x_scores+1,(y_space*11)+1,scores(7)
	If option=1 And rank=8 Then Text x_scores+1,(y_space*12)+1,scores(8)+" <" Else Text x_scores+1,(y_space*12)+1,scores(8)
	If option=1 And rank=9 Then Text x_scores+1,(y_space*13)+1,scores(9)+" <" Else Text x_scores+1,(y_space*13)+1,scores(9)
	If option=1 And rank=10 Then Text x_scores+1,(y_space*14)+1,scores(10)+" <" Else Text x_scores+1,(y_space*14)+1,scores(10)
	If option=0 Then Text (width/2)+1,(y_space*16)+1,"> Back <",1 Else Text (width/2)+1,(y_space*16)+1,"Back",1

	; White text
	Color 255,255,255
	Text (width/2)+0,(y_space*1)+0,"Wing Ring V1.0",1
	Text (width/2)+0,(y_space*3)+0,"High Scores",1
	If option=1 And rank=1 Then Text x_nos-18,(y_space*5)+0,"> 1." Else Text x_nos+0,(y_space*5)+0,"1."
	If option=1 And rank=2 Then Text x_nos-18,(y_space*6)+0,"> 2." Else Text x_nos+0,(y_space*6)+0,"2."
	If option=1 And rank=3 Then Text x_nos-18,(y_space*7)+0,"> 3." Else Text x_nos+0,(y_space*7)+0,"3."
	If option=1 And rank=4 Then Text x_nos-18,(y_space*8)+0,"> 4." Else Text x_nos+0,(y_space*8)+0,"4."
	If option=1 And rank=5 Then Text x_nos-18,(y_space*9)+0,"> 5." Else Text x_nos+0,(y_space*9)+0,"5."
	If option=1 And rank=6 Then Text x_nos-18,(y_space*10)+0,"> 6." Else Text x_nos+0,(y_space*10)+0,"6."
	If option=1 And rank=7 Then Text x_nos-18,(y_space*11)+0,"> 7." Else Text x_nos+0,(y_space*11)+0,"7."
	If option=1 And rank=8 Then Text x_nos-18,(y_space*12)+0,"> 8." Else Text x_nos+0,(y_space*12)+0,"8."
	If option=1 And rank=9 Then Text x_nos-18,(y_space*13)+0,"> 9." Else Text x_nos+0,(y_space*13)+0,"9."
	If option=1 And rank=10 Then Text x_nos-18,(y_space*14)+0,"> 10." Else Text x_nos+0,(y_space*14)+0,"10."
	Text x_names+0,(y_space*5)+0,names$(1)
	Text x_names+0,(y_space*6)+0,names$(2)
	Text x_names+0,(y_space*7)+0,names$(3)
	Text x_names+0,(y_space*8)+0,names$(4)
	Text x_names+0,(y_space*9)+0,names$(5)
	Text x_names+0,(y_space*10)+0,names$(6)
	Text x_names+0,(y_space*11)+0,names$(7)
	Text x_names+0,(y_space*12)+0,names$(8)
	Text x_names+0,(y_space*13)+0,names$(9)
	Text x_names+0,(y_space*14)+0,names$(10)
	If option=1 And rank=1 Then Text x_scores+0,(y_space*5)+0,scores(1)+" <" Else Text x_scores+0,(y_space*5)+0,scores(1)
	If option=1 And rank=2 Then Text x_scores+0,(y_space*6)+0,scores(2)+" <" Else Text x_scores+0,(y_space*6)+0,scores(2)
	If option=1 And rank=3 Then Text x_scores+0,(y_space*7)+0,scores(3)+" <" Else Text x_scores+0,(y_space*7)+0,scores(3)
	If option=1 And rank=4 Then Text x_scores+0,(y_space*8)+0,scores(4)+" <" Else Text x_scores+0,(y_space*8)+0,scores(4)
	If option=1 And rank=5 Then Text x_scores+0,(y_space*9)+0,scores(5)+" <" Else Text x_scores+0,(y_space*9)+0,scores(5)
	If option=1 And rank=6 Then Text x_scores+0,(y_space*10)+0,scores(6)+" <" Else Text x_scores+0,(y_space*10)+0,scores(6)
	If option=1 And rank=7 Then Text x_scores+0,(y_space*11)+0,scores(7)+" <" Else Text x_scores+0,(y_space*11)+0,scores(7)
	If option=1 And rank=8 Then Text x_scores+0,(y_space*12)+0,scores(8)+" <" Else Text x_scores+0,(y_space*12)+0,scores(8)
	If option=1 And rank=9 Then Text x_scores+0,(y_space*13)+0,scores(9)+" <" Else Text x_scores+0,(y_space*13)+0,scores(9)
	If option=1 And rank=10 Then Text x_scores+0,(y_space*14)+0,scores(10)+" <" Else Text x_scores+0,(y_space*14)+0,scores(10)
	If option=0 Then Text (width/2)+0,(y_space*16)+0,"> Back <",1 Else Text (width/2)+0,(y_space*16)+0,"Back",1

	; Input routine

	If rank>=1 And rank<=10
	gk=GetKey()

		If Len(names$(rank))<=4 Or gk=8 ; If size of string is less than six characters long or backspace key is pressed
	
		Select gk
			Case 0 ; Nothing! Just stops no key press (0) being used with the default option which results in ||| being shown
			Case 13 ; Stops return key being used
			Case 28 ; Stops up cursor key being used
			Case 29 ; Stops down cursor key being used
			Case 8 : If Len(names$(rank))>0 Then names$(rank)=Left$(names$(rank),Len(names$(rank))-1)					
			Default : names$(rank)=names$(rank)+Chr$(gk)
		End Select
		
		EndIf
		
	EndIf

	; Up/down option
	If KeyHit(200)=1 Then option=option-1
	If KeyHit(208)=1 Then option=option+1

	; Choice of options depending on whether high score is yet to be entered or not
	If rank<1 Or rank>10
		If option>0 Then option=0
		If option<0 Then option=0
	Else
		If option>1 Then option=0
		If option<0 Then option=1
	EndIf

	; Enter option
	
	If KeyHit(28)=True

		If option=0 Then screen=option : option=6 ; Back

		; Save scores
		If option=1
			save_file=WriteFile("high_scores.sav")
			For s=1 To 10
				WriteLine save_file,names$(s)
				WriteLine save_file,scores(s)
			Next
			CloseFile save_file
			rank=11
		EndIf

	EndIf

End Function


; ----------------
; Options function
; ----------------

Function Options()

	kh=KeyHit(28)

	; Shadow text
	
	Color 0,0,0
	
	Text (width/2)+1,(y_space*1)+1,"Wing Ring V1.0",1
	Text (width/2)+1,(y_space*3)+1,"Options",1
	Text (width/2)+1,(y_space*5)+1,"Graphics Mode:",1
	Text (width/2)+1,(y_space*8)+1,"Windowed:",1

	If option=1
		Text (width/2)+1,(y_space*6)+1,">"+gfx_modes(gfx_mode_option,0)+"x"+gfx_modes(gfx_mode_option,1)+"x"+gfx_modes(gfx_mode_option,2)+"<",1
	Else
		Text (width/2)+1,(y_space*6)+1,gfx_modes(gfx_mode_option,0)+"x"+gfx_modes(gfx_mode_option,1)+"x"+gfx_modes(gfx_mode_option,2),1
	EndIf
	
	If option=2
		If Windowed3D()=False
			Text (width/2)+1,(y_space*9)+1,"> Unavailable <",1
		Else
			If mode_option=1 Then Text (width/2)+1,(y_space*9)+1,"> No <",1
			If mode_option=2 Then Text (width/2)+1,(y_space*9)+1,"> Yes <",1
		EndIf
	Else
		If Windowed3D()=False
			Text (width/2)+1,(y_space*9)+1,"Unavailable",1
		Else
			If mode_option=1 Then Text (width/2)+1,(y_space*9)+1,"No",1
			If mode_option=2 Then Text (width/2)+1,(y_space*9)+1,"Yes",1
		EndIf
	EndIf

	If option=0 Then Text (width/2)+1,(y_space*16)+1,"> Back <",1 Else Text (width/2)+1,(y_space*16)+1,"Back",1
	
	; White text
	
	Color 255,255,255
	
	Text (width/2)+0,(y_space*1)+0,"Wing Ring V1.0",1
	Text (width/2)+0,(y_space*3)+0,"Options",1
	Text (width/2)+0,(y_space*5)+0,"Graphics Mode:",1
	Text (width/2)+0,(y_space*8)+0,"Windowed:",1

	If option=1
		Text (width/2)+0,(y_space*6)+0,">"+gfx_modes(gfx_mode_option,0)+"x"+gfx_modes(gfx_mode_option,1)+"x"+gfx_modes(gfx_mode_option,2)+"<",1
	Else
		Text (width/2)+0,(y_space*6)+0,gfx_modes(gfx_mode_option,0)+"x"+gfx_modes(gfx_mode_option,1)+"x"+gfx_modes(gfx_mode_option,2),1
	EndIf
	
	If option=2
		If Windowed3D()=False
			Text (width/2)+0,(y_space*9)+0,"> Unavailable <",1
		Else
			If mode_option=1 Then Text (width/2)+0,(y_space*9)+0,"> No <",1
			If mode_option=2 Then Text (width/2)+0,(y_space*9)+0,"> Yes <",1
		EndIf
	Else
		If Windowed3D()=False
			Text (width/2)+0,(y_space*9)+0,"Unavailable",1
		Else
			If mode_option=1 Then Text (width/2)+0,(y_space*9)+0,"No",1
			If mode_option=2 Then Text (width/2)+0,(y_space*9)+0,"Yes",1
		EndIf
	EndIf

	If option=0 Then Text (width/2)+0,(y_space*16)+0,"> Back <",1 Else Text (width/2)+0,(y_space*16)+0,"Back",1

	; Up/down option
	If KeyHit(200)=1 Then option=option-1
	If KeyHit(208)=1 Then option=option+1
	If option>2 Then option=0
	If option<0 Then option=2

	; Enter option
	If kh=True And option=1 Then gfx_mode_option=gfx_mode_option+1 : If gfx_mode_option=>count_gfx_modes Then gfx_mode_option=1
	If kh=True And option=2 And Windowed3D()=True ; Only allow user to toggle windowed mode option if windowed 3d mode is available
		If mode_option=1 Then mode_option=2 Else mode_option=1
	EndIf
	If kh=True And option=0
		screen=option
		w=gfx_modes(gfx_mode_option,0)
		h=gfx_modes(gfx_mode_option,1)
		d=gfx_modes(gfx_mode_option,2)
		If width<>w Or height<>h Or depth<>d Or mode_option<>mode
			width=w : height=h : depth=d : mode=mode_option
			Graphics3D width,height,depth,mode
			logo=LoadImage("logo.bmp")
			y_space#=height/18
			x_space=width/71 : x_nos=((width/4)-((9*3)/2)) : x_names=((width/2)-((9*5)/2)) : x_scores=((width/1.33)-((9*5)/2))
			LoadMedia() : CreateEntities() : InitCollisions()
		EndIf
	EndIf		

End Function