# Microsoft Developer Studio Project File - Name="blitz3d" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Static Library" 0x0104

CFG=blitz3d - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "blitz3d.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "blitz3d.mak" CFG="blitz3d - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "blitz3d - Win32 Release" (based on "Win32 (x86) Static Library")
!MESSAGE "blitz3d - Win32 Debug" (based on "Win32 (x86) Static Library")
!MESSAGE "blitz3d - Win32 Blitz3DRelease" (based on "Win32 (x86) Static Library")
!MESSAGE "blitz3d - Win32 Blitz2DRelease" (based on "Win32 (x86) Static Library")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "blitz3d - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /YX /FD /c
# ADD CPP /nologo /MT /W3 /GX /O2 /Ob2 /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /Yu"std.h" /FD /c
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo

!ELSEIF  "$(CFG)" == "blitz3d - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /YX /FD /GZ /c
# ADD CPP /nologo /MTd /W3 /Gm /GX /Zi /Od /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /YX /FD /GZ /c
# ADD BASE RSC /l 0x409 /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo

!ELSEIF  "$(CFG)" == "blitz3d - Win32 Blitz3DRelease"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "blitz3d___Win32_Blitz3DRelease"
# PROP BASE Intermediate_Dir "blitz3d___Win32_Blitz3DRelease"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "blitz3d___Win32_Blitz3DRelease"
# PROP Intermediate_Dir "blitz3d___Win32_Blitz3DRelease"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /O2 /Ob2 /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /Yu"std.h" /FD /c
# ADD CPP /nologo /G6 /Gz /MT /W3 /GX /O2 /Ob2 /D "_LIB" /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "PRO" /FD /c
# SUBTRACT CPP /YX /Yc /Yu
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo

!ELSEIF  "$(CFG)" == "blitz3d - Win32 Blitz2DRelease"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "blitz3d___Win32_Blitz2DRelease"
# PROP BASE Intermediate_Dir "blitz3d___Win32_Blitz2DRelease"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "blitz3d___Win32_Blitz2DRelease"
# PROP Intermediate_Dir "blitz3d___Win32_Blitz2DRelease"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /O2 /Ob2 /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /Yu"std.h" /FD /c
# ADD CPP /nologo /G6 /MT /W3 /GX /O2 /Ob2 /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /Yu"std.h" /FD /c
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo

!ENDIF 

# Begin Target

# Name "blitz3d - Win32 Release"
# Name "blitz3d - Win32 Debug"
# Name "blitz3d - Win32 Blitz3DRelease"
# Name "blitz3d - Win32 Blitz2DRelease"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\animation.cpp
# End Source File
# Begin Source File

SOURCE=.\animator.cpp
# End Source File
# Begin Source File

SOURCE=.\brush.cpp
# End Source File
# Begin Source File

SOURCE=.\cachedtexture.cpp
# End Source File
# Begin Source File

SOURCE=.\camera.cpp
# End Source File
# Begin Source File

SOURCE=.\collision.cpp
# End Source File
# Begin Source File

SOURCE=.\entity.cpp
# End Source File
# Begin Source File

SOURCE=.\frustum.cpp
# End Source File
# Begin Source File

SOURCE=.\geom.cpp
# End Source File
# Begin Source File

SOURCE=.\light.cpp
# End Source File
# Begin Source File

SOURCE=.\listener.cpp
# End Source File
# Begin Source File

SOURCE=.\loader_3ds.cpp
# End Source File
# Begin Source File

SOURCE=.\loader_b3d.cpp
# End Source File
# Begin Source File

SOURCE=.\loader_x.cpp
# End Source File
# Begin Source File

SOURCE=.\md2model.cpp
# End Source File
# Begin Source File

SOURCE=.\md2norms.cpp
# End Source File
# Begin Source File

SOURCE=.\md2rep.cpp
# End Source File
# Begin Source File

SOURCE=.\meshcollider.cpp
# End Source File
# Begin Source File

SOURCE=.\meshloader.cpp
# End Source File
# Begin Source File

SOURCE=.\meshmodel.cpp
# End Source File
# Begin Source File

SOURCE=.\meshutil.cpp
# End Source File
# Begin Source File

SOURCE=.\mirror.cpp
# End Source File
# Begin Source File

SOURCE=.\model.cpp
# End Source File
# Begin Source File

SOURCE=.\object.cpp
# End Source File
# Begin Source File

SOURCE=.\pivot.cpp
# End Source File
# Begin Source File

SOURCE=.\planemodel.cpp
# End Source File
# Begin Source File

SOURCE=.\q3bspmodel.cpp
# End Source File
# Begin Source File

SOURCE=.\q3bsprep.cpp
# End Source File
# Begin Source File

SOURCE=.\sprite.cpp
# End Source File
# Begin Source File

SOURCE=.\std.cpp

!IF  "$(CFG)" == "blitz3d - Win32 Release"

# ADD CPP /Yc"std.h"

!ELSEIF  "$(CFG)" == "blitz3d - Win32 Debug"

!ELSEIF  "$(CFG)" == "blitz3d - Win32 Blitz3DRelease"

# ADD BASE CPP /Yc"std.h"
# ADD CPP /Yc"std.h"

!ELSEIF  "$(CFG)" == "blitz3d - Win32 Blitz2DRelease"

# ADD BASE CPP /Yc"std.h"
# ADD CPP /Yc"std.h"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\surface.cpp
# End Source File
# Begin Source File

SOURCE=.\terrain.cpp
# End Source File
# Begin Source File

SOURCE=.\terrainrep.cpp
# End Source File
# Begin Source File

SOURCE=.\texture.cpp
# End Source File
# Begin Source File

SOURCE=.\world.cpp
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\animation.h
# End Source File
# Begin Source File

SOURCE=.\animator.h
# End Source File
# Begin Source File

SOURCE=.\blitz3d.h
# End Source File
# Begin Source File

SOURCE=.\brush.h
# End Source File
# Begin Source File

SOURCE=.\cachedtexture.h
# End Source File
# Begin Source File

SOURCE=.\camera.h
# End Source File
# Begin Source File

SOURCE=.\collision.h
# End Source File
# Begin Source File

SOURCE=.\entity.h
# End Source File
# Begin Source File

SOURCE=.\frustum.h
# End Source File
# Begin Source File

SOURCE=.\geom.h
# End Source File
# Begin Source File

SOURCE=.\light.h
# End Source File
# Begin Source File

SOURCE=.\listener.h
# End Source File
# Begin Source File

SOURCE=.\loader_3ds.h
# End Source File
# Begin Source File

SOURCE=.\loader_b3d.h
# End Source File
# Begin Source File

SOURCE=.\loader_x.h
# End Source File
# Begin Source File

SOURCE=.\md2model.h
# End Source File
# Begin Source File

SOURCE=.\md2norms.h
# End Source File
# Begin Source File

SOURCE=.\md2rep.h
# End Source File
# Begin Source File

SOURCE=.\meshcollider.h
# End Source File
# Begin Source File

SOURCE=.\meshloader.h
# End Source File
# Begin Source File

SOURCE=.\meshmodel.h
# End Source File
# Begin Source File

SOURCE=.\meshutil.h
# End Source File
# Begin Source File

SOURCE=.\mirror.h
# End Source File
# Begin Source File

SOURCE=.\model.h
# End Source File
# Begin Source File

SOURCE=.\object.h
# End Source File
# Begin Source File

SOURCE=.\pivot.h
# End Source File
# Begin Source File

SOURCE=.\planemodel.h
# End Source File
# Begin Source File

SOURCE=.\q3bspmodel.h
# End Source File
# Begin Source File

SOURCE=.\q3bsprep.h
# End Source File
# Begin Source File

SOURCE=.\rendercontext.h
# End Source File
# Begin Source File

SOURCE=.\sprite.h
# End Source File
# Begin Source File

SOURCE=.\std.h
# End Source File
# Begin Source File

SOURCE=.\surface.h
# End Source File
# Begin Source File

SOURCE=.\terrain.h
# End Source File
# Begin Source File

SOURCE=.\terrainrep.h
# End Source File
# Begin Source File

SOURCE=.\texture.h
# End Source File
# Begin Source File

SOURCE=.\world.h
# End Source File
# End Group
# End Target
# End Project
