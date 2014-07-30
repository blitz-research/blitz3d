# Microsoft Developer Studio Project File - Name="debugger" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Dynamic-Link Library" 0x0102

CFG=debugger - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "debugger.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "debugger.mak" CFG="debugger - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "debugger - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "debugger - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "debugger - Win32 Blitz3DRelease" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "debugger - Win32 Blitz2DRelease" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "debugger - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 1
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "DEBUGGER_EXPORTS" /YX /FD /c
# ADD CPP /nologo /MT /W3 /GX /O1 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "DEBUGGER_EXPORTS" /D "_WINDLL" /YX /FD /c
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x1409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /machine:I386
# ADD LINK32 /nologo /subsystem:windows /dll /machine:I386 /out:"..\blitzbasic\bin\debugger.dll"
# SUBTRACT LINK32 /pdb:none /force

!ELSEIF  "$(CFG)" == "debugger - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 1
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "DEBUGGER_EXPORTS" /YX /FD /GZ /c
# ADD CPP /nologo /MTd /W3 /Gm /GX /Zi /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "DEBUGGER_EXPORTS" /D "_WINDLL" /YX /FD /GZ /c
# SUBTRACT CPP /Gy
# ADD BASE MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x1409 /d "_DEBUG"
# ADD RSC /l 0x1409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /debug /machine:I386 /pdbtype:sept
# ADD LINK32 /nologo /dll /incremental:no /debug /machine:I386 /out:"..\blitzbasic\bin\debugger.dll" /fixed:no
# SUBTRACT LINK32 /pdb:none

!ELSEIF  "$(CFG)" == "debugger - Win32 Blitz3DRelease"

# PROP BASE Use_MFC 1
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "debugger___Win32_Blitz3DRelease"
# PROP BASE Intermediate_Dir "debugger___Win32_Blitz3DRelease"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 1
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "debugger___Win32_Blitz3DRelease"
# PROP Intermediate_Dir "debugger___Win32_Blitz3DRelease"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /O1 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "DEBUGGER_EXPORTS" /D "_WINDLL" /YX /FD /c
# ADD CPP /nologo /G6 /Gz /MT /W3 /GX /O1 /D "_WINDOWS" /D "_USRDLL" /D "DEBUGGER_EXPORTS" /D "_WINDLL" /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "PRO" /FD /c
# SUBTRACT CPP /YX
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 /nologo /subsystem:windows /dll /machine:I386 /out:"..\blitzbasic\bin\debugger.dll"
# SUBTRACT BASE LINK32 /pdb:none /force
# ADD LINK32 /nologo /subsystem:windows /dll /machine:I386 /out:"../_release/bin/debugger.dll"
# SUBTRACT LINK32 /pdb:none /force

!ELSEIF  "$(CFG)" == "debugger - Win32 Blitz2DRelease"

# PROP BASE Use_MFC 1
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "debugger___Win32_Blitz2DRelease"
# PROP BASE Intermediate_Dir "debugger___Win32_Blitz2DRelease"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 1
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "debugger___Win32_Blitz2DRelease"
# PROP Intermediate_Dir "debugger___Win32_Blitz2DRelease"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /O1 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "DEBUGGER_EXPORTS" /D "_WINDLL" /YX /FD /c
# ADD CPP /nologo /MT /W3 /GX /O1 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "DEBUGGER_EXPORTS" /D "_WINDLL" /YX /FD /c
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 /nologo /subsystem:windows /dll /machine:I386 /out:"..\..\release\blitz3drelease\bin\debugger.dll"
# SUBTRACT BASE LINK32 /pdb:none /force
# ADD LINK32 /nologo /subsystem:windows /dll /machine:I386 /out:"..\..\release\blitz2drelease\bin\debugger.dll"
# SUBTRACT LINK32 /pdb:none /force

!ENDIF 

# Begin Target

# Name "debugger - Win32 Release"
# Name "debugger - Win32 Debug"
# Name "debugger - Win32 Blitz3DRelease"
# Name "debugger - Win32 Blitz2DRelease"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\debugger.rc
# End Source File
# Begin Source File

SOURCE=.\debuggerapp.cpp
# End Source File
# Begin Source File

SOURCE=.\debugtree.cpp
# End Source File
# Begin Source File

SOURCE=.\mainframe.cpp
# End Source File
# Begin Source File

SOURCE=.\prefs.cpp
# End Source File
# Begin Source File

SOURCE=.\sourcefile.cpp
# End Source File
# Begin Source File

SOURCE=.\stdafx.cpp

!IF  "$(CFG)" == "debugger - Win32 Release"

# ADD CPP /Yc"stdafx.h"

!ELSEIF  "$(CFG)" == "debugger - Win32 Debug"

!ELSEIF  "$(CFG)" == "debugger - Win32 Blitz3DRelease"

# ADD BASE CPP /Yc"stdafx.h"
# ADD CPP /Yc"stdafx.h"

!ELSEIF  "$(CFG)" == "debugger - Win32 Blitz2DRelease"

# ADD BASE CPP /Yc"stdafx.h"
# ADD CPP /Yc"stdafx.h"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\stdutil.cpp
# End Source File
# Begin Source File

SOURCE=.\tabber.cpp
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\debugger.h
# End Source File
# Begin Source File

SOURCE=.\debuggerapp.h
# End Source File
# Begin Source File

SOURCE=.\debugtree.h
# End Source File
# Begin Source File

SOURCE=.\mainframe.h
# End Source File
# Begin Source File

SOURCE=.\prefs.h
# End Source File
# Begin Source File

SOURCE=.\sourcefile.h
# End Source File
# Begin Source File

SOURCE=.\stdafx.h
# End Source File
# Begin Source File

SOURCE=.\tabber.h
# End Source File
# End Group
# End Target
# End Project
