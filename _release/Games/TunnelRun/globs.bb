;	Tunnel Run
;	(c)2001 David Bird
;	dave@birdie72.freeserve.co.uk
;

;
;	Globals here
;

;meshes
Global Tunnel_mesh
Global Background
Global Aster_mesh
Global player_mesh
Global shield_mesh
Global missile1_mesh
Global Bad01_mesh
Global claw_anim_mesh

;textures
Global Tunnel_tex
Global Shield_tex
Global tex0
Global tex1

;Sprites
Global shot1_spr
Dim smoke_spr(4)
Global Target_spr


;images
Global TitleScreen1
Global MousePointer
Global Lives_image
Global Gover_image

;Sounds
Global shoot1_wave
Global Explode_wave


;Misc globals
Global Tunnel_Texture_Coord#=0
Global Shield_tex_pos#=0
Global Scroll_Speed#=0.002
Global EFX_SPEED=34
Global CameraZpos=-300
Global Num_Players=0
Global Dead_BandX#=.2
Global Dead_BandY#=.2
Global Flash_Cnt=128
Global Go1_Timer=0
Global Go2_timer=0
Global cntr
Global Exited=False
Global t_r1,t_r2,ta_r1,ta_r2
Global t_g1,t_g2,ta_g1,ta_g2
Global t_b1,t_b2,ta_b1,ta_b2
Global G_Brightness=100
Global G_StartLives=5
Global G_CurrentGFX=1
Global Max_GFXModes=0
Global G_Diff=3

;Options entities
Global optcamera
Global opttunnel
Global optbackground
Global optlight
;
Global g_width
Global g_height

;redefinable keys
Dim keys(6)
keys(1)=203	;	Left  Key
keys(2)=205	;	Right Key
keys(3)=200 ;	Down  Key
keys(4)=208	;	Up    Key
keys(5)=57	;	Fire  Key
keys(6)=58  ;   fire 2 key

;High Score Table
Dim hn$(11),hs(11),score(2)
Restore highs
For a=1 To 11
	Read hn$(a),hs(a)
Next
Global player1type=1
Global player2type=3
Global gametype=1
Global Control=1

Dim GM_TYPE$(10)
Dim CN_TYPE$(10)
Dim MENU$(10)

Restore dats
For a=1 To 2 : Read GM_TYPE(a):Next
For a=1 To 6 : Read CN_TYPE(a):Next
For a=1 To 7 : Read MENU(a):Next