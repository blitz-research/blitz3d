; --------------
; Egypt function
; --------------

Function Egypt()

	plane1_y#=250
	plane1_dy#=250
	cam1_y#=250
	cam1_dy#=250
	
	plane2_y#=250
	plane2_dy#=250
	cam2_y#=250
	cam2_dy#=250

	statue=sphinx
	solar=sun
	solar_y=500
	solar_z=1000
	solar_scale=300
	solar_scale_shake=10
	
	; Hide USA scenery
	HideEntity liberty
	HideEntity moon
	TextureBlend lights_multi_tex,0
	
	; Show Egypt scenery
	AmbientLight 127,127,127
	ShowEntity sphinx

	EntityTexture ground,sand_tex
	EntityColor ground,168,133,55

	EntityTexture sky,sky_tex

	If no_players=1 Then ShowEntity sky : ShowEntity sun : Else HideEntity sky : HideEntity sun

	CameraClsColor main_cam1,96,160,248
	CameraFogColor main_cam1,96,160,248
	If no_players=1 Then CameraFogMode main_cam1,False Else CameraFogMode main_cam1,True
	CameraClsColor top_cam1,96,160,248
	
	If no_players=2
		CameraClsColor main_cam2,96,160,248
		CameraFogColor main_cam2,96,160,248 
		CameraClsColor top_cam2,96,160,248
	EndIf

End Function


; ------------
; USA function
; ------------

Function USA()

	plane1_y#=750
	plane1_dy#=750
	cam1_y#=750
	cam1_dy#=750

	plane2_y#=750
	plane2_dy#=750
	cam2_y#=750
	cam2_dy#=750

	statue=liberty
	solar=moon
	solar_y=1000
	solar_z=2000
	solar_scale=300
	solar_scale_shake=0
	
	; Hide  Egypt scenery
	HideEntity sphinx
	HideEntity sun
	
	; Show USA scenery
	AmbientLight 63,63,63
	ShowEntity liberty
	
	EntityTexture ground,lights_tex,0,0
	EntityColor ground,255,255,255
	
	; Multi-texture
	EntityTexture ground,lights_multi_tex,0,1
	TextureBlend lights_multi_tex,2 
	
	EntityTexture sky,night_sky_tex

	If no_players=1 Then ShowEntity sky : ShowEntity moon : Else HideEntity sky : HideEntity moon

	CameraClsColor main_cam1,0,0,0
	CameraFogColor main_cam1,0,0,0
	CameraFogMode main_cam1,True
	CameraClsColor top_cam1,0,0,0
	
	If no_players=2
		CameraClsColor main_cam2,0,0,0
		CameraFogColor main_cam2,0,0,0 
		CameraClsColor top_cam2,0,0,0
	EndIf
	
End Function