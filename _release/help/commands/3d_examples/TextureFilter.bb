; ClearTextureFilters and TextureFilter Example.
; ----------------------------------------------

Const tex_color 	= 1		; Color texture
Const tex_alpha 	= 2		; Alpha texture (Include alpha channel data)
Const tex_mask 		= 4		; Masked texture (black is transparent)
Const tex_mipmap 	= 8		; Create texture mipmaps
Const tex_clampu 	= 16	; Restrict U texture coords from "bleeding over"
Const tex_clampv	= 32	; Restrict V texture coords from "bleeding over"
Const tex_envshpere	= 64	; Load texture as a spherical environment map
Const tex_vram 		= 256	; Force texture graphics to vram
Const tex_highcolor	= 512	; Forces texture graphics to be 32-bits per pixel

Graphics3D 640,480 

; Removes any texture filters that might apply.

ClearTextureFilters

; Add an alpha texture to the list of 
; texture filters to apply to files
; that have "_alpha" in their filenames.

TextureFilter "_alpha",tex_color + tex_alpha + tex_mipmap

; Set appropriate texture flags for loading
; suitable skybox textures from files named
; something with "_skybox".

TextureFilter "_skybox", tex_color + tex_mipmap + tex_clampu + tex_clampv

; Set the flags for loading a spherical refletction
; map to apply to all "_refmap" files.

TextureFilter "_refmap", tex_color + tex_mipmap + tex_envshpere

; Setup a texture filter to allow faster
; (and easier) pixel manipulation on all
; loaded "_fastblit" files.

TextureFilter "_fastblit", tex_color + tex_vram + tex_highcolor

; This is where you would normally load your special
; textures.

; The next bit resets the texture filters to their
; standard settings.

ClearTextureFilters
TextureFilter "", tex_color + tex_mipmap

End