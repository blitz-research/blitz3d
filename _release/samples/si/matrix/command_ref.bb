; --------
; Commands
; --------

; DB_LoadImage( filename_str$, image_no )
; MakeMatrix( matrix_no,width#,depth#,xsegmented#,zsegmented# )
; PositionMatrix( matrix_no,x#,y#,z# )
; PrepareMatrixTexture ( matrix_no,image_val,across,down )
; FillMatrix( matrix_no,height,tile_no )
; RandomizeMatrix( matrix_no,height_range# )
; SetMatrixHeight( matrix_no,x,z,height# )
; SetMatrixNormal( matrix_no,x,z,normalx#,normaly#,normalz# )
; SetMatrixTile( matrix_no,x,z,tile_no )
; GhostMatrixOn( matrix_no )
; GhostMatrixOff( matrix_no )
; DeleteMatrix( matrix_no )
; MatrixExist( matrix_no )
; MatrixPositionX( matrix_no )
; MatrixPositionY( matrix_no )
; MatrixPositionZ( matrix_no )
; GetMatrixHeight( matrix_no,x,z )
; GetGroundHeight( matrix_no,x#,z# )
; MatrixTileCount( matrix_no )
; MatrixTilesExist( matrix_no )