# Microsoft Developer Studio Project File - Name="gxruntime" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Static Library" 0x0104

CFG=gxruntime - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "gxruntime.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "gxruntime.mak" CFG="gxruntime - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "gxruntime - Win32 Release" (based on "Win32 (x86) Static Library")
!MESSAGE "gxruntime - Win32 Debug" (based on "Win32 (x86) Static Library")
!MESSAGE "gxruntime - Win32 Blitz3DRelease" (based on "Win32 (x86) Static Library")
!MESSAGE "gxruntime - Win32 Blitz2DRelease" (based on "Win32 (x86) Static Library")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "gxruntime - Win32 Release"

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
# ADD CPP /nologo /MT /W3 /GX /Ox /Ow /Og /Oi /Os /Ob2 /Gf /Gy /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /Yu"std.h" /FD /c
# SUBTRACT CPP /Ot
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo

!ELSEIF  "$(CFG)" == "gxruntime - Win32 Debug"

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
# ADD CPP /nologo /MTd /W3 /Gm /GX /Zi /Od /D "WIN32" /D "_DEBUG" /D "_MBCS" /D "_LIB" /Yu"std.h" /FD /GZ /c
# ADD BASE RSC /l 0x409 /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo

!ELSEIF  "$(CFG)" == "gxruntime - Win32 Blitz3DRelease"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "gxruntime___Win32_Blitz3DRelease"
# PROP BASE Intermediate_Dir "gxruntime___Win32_Blitz3DRelease"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "gxruntime___Win32_Blitz3DRelease"
# PROP Intermediate_Dir "gxruntime___Win32_Blitz3DRelease"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /Ox /Ow /Og /Oi /Os /Ob2 /Gf /Gy /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /Yu"std.h" /FD /c
# SUBTRACT BASE CPP /Ot
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

!ELSEIF  "$(CFG)" == "gxruntime - Win32 Blitz2DRelease"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "gxruntime___Win32_Blitz2DRelease"
# PROP BASE Intermediate_Dir "gxruntime___Win32_Blitz2DRelease"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "gxruntime___Win32_Blitz2DRelease"
# PROP Intermediate_Dir "gxruntime___Win32_Blitz2DRelease"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /Ox /Ow /Og /Oi /Os /Ob2 /Gf /Gy /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /Yu"std.h" /FD /c
# SUBTRACT BASE CPP /Ot
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

# Name "gxruntime - Win32 Release"
# Name "gxruntime - Win32 Debug"
# Name "gxruntime - Win32 Blitz3DRelease"
# Name "gxruntime - Win32 Blitz2DRelease"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\ddutil.cpp
# End Source File
# Begin Source File

SOURCE=.\gxaudio.cpp
# End Source File
# Begin Source File

SOURCE=.\gxcanvas.cpp
# End Source File
# Begin Source File

SOURCE=.\gxchannel.cpp
# End Source File
# Begin Source File

SOURCE=.\gxdevice.cpp
# End Source File
# Begin Source File

SOURCE=.\gxdir.cpp
# End Source File
# Begin Source File

SOURCE=.\gxfilesystem.cpp
# End Source File
# Begin Source File

SOURCE=.\gxfont.cpp
# End Source File
# Begin Source File

SOURCE=.\gxgraphics.cpp
# End Source File
# Begin Source File

SOURCE=.\gxinput.cpp
# End Source File
# Begin Source File

SOURCE=.\gxlight.cpp
# End Source File
# Begin Source File

SOURCE=.\gxmesh.cpp
# End Source File
# Begin Source File

SOURCE=.\gxmovie.cpp
# End Source File
# Begin Source File

SOURCE=.\gxruntime.cpp

!IF  "$(CFG)" == "gxruntime - Win32 Release"

# ADD CPP /FAs

!ELSEIF  "$(CFG)" == "gxruntime - Win32 Debug"

!ELSEIF  "$(CFG)" == "gxruntime - Win32 Blitz3DRelease"

# ADD BASE CPP /FAs
# ADD CPP /FAs

!ELSEIF  "$(CFG)" == "gxruntime - Win32 Blitz2DRelease"

# ADD BASE CPP /FAs
# ADD CPP /FAs

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\gxscene.cpp
# End Source File
# Begin Source File

SOURCE=.\gxsound.cpp
# End Source File
# Begin Source File

SOURCE=.\gxtimer.cpp
# End Source File
# Begin Source File

SOURCE=.\std.cpp
# ADD CPP /Yc"std.h"
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\asmcoder.h
# End Source File
# Begin Source File

SOURCE=.\ddutil.h
# End Source File
# Begin Source File

SOURCE=.\gxaudio.h
# End Source File
# Begin Source File

SOURCE=.\gxcanvas.h
# End Source File
# Begin Source File

SOURCE=.\gxchannel.h
# End Source File
# Begin Source File

SOURCE=.\gxdevice.h
# End Source File
# Begin Source File

SOURCE=.\gxdir.h
# End Source File
# Begin Source File

SOURCE=.\gxfilesystem.h
# End Source File
# Begin Source File

SOURCE=.\gxfont.h
# End Source File
# Begin Source File

SOURCE=.\gxgraphics.h
# End Source File
# Begin Source File

SOURCE=.\gxinput.h
# End Source File
# Begin Source File

SOURCE=.\gxlight.h
# End Source File
# Begin Source File

SOURCE=.\gxmesh.h
# End Source File
# Begin Source File

SOURCE=.\gxmovie.h
# End Source File
# Begin Source File

SOURCE=.\gxruntime.h
# End Source File
# Begin Source File

SOURCE=.\gxscene.h
# End Source File
# Begin Source File

SOURCE=.\gxsound.h
# End Source File
# Begin Source File

SOURCE=.\gxtimer.h
# End Source File
# Begin Source File

SOURCE=.\std.h
# End Source File
# End Group
# End Target
# End Project
