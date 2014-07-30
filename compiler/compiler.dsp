# Microsoft Developer Studio Project File - Name="compiler" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Static Library" 0x0104

CFG=compiler - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "compiler.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "compiler.mak" CFG="compiler - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "compiler - Win32 Release" (based on "Win32 (x86) Static Library")
!MESSAGE "compiler - Win32 Debug" (based on "Win32 (x86) Static Library")
!MESSAGE "compiler - Win32 Blitz3DRelease" (based on "Win32 (x86) Static Library")
!MESSAGE "compiler - Win32 Blitz2DRelease" (based on "Win32 (x86) Static Library")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "compiler - Win32 Release"

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

!ELSEIF  "$(CFG)" == "compiler - Win32 Debug"

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
# SUBTRACT CPP /Gy
# ADD BASE RSC /l 0x409 /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo

!ELSEIF  "$(CFG)" == "compiler - Win32 Blitz3DRelease"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "compiler___Win32_Blitz3DRelease"
# PROP BASE Intermediate_Dir "compiler___Win32_Blitz3DRelease"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "compiler___Win32_Blitz3DRelease"
# PROP Intermediate_Dir "compiler___Win32_Blitz3DRelease"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /O2 /Ob2 /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /Yu"std.h" /FD /c
# ADD CPP /nologo /G6 /Gz /MT /W3 /GX /O1 /D "_LIB" /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "PRO" /FD /c
# SUBTRACT CPP /YX /Yc /Yu
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LIB32=link.exe -lib
# ADD BASE LIB32 /nologo
# ADD LIB32 /nologo

!ELSEIF  "$(CFG)" == "compiler - Win32 Blitz2DRelease"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "compiler___Win32_Blitz2DRelease"
# PROP BASE Intermediate_Dir "compiler___Win32_Blitz2DRelease"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "compiler___Win32_Blitz2DRelease"
# PROP Intermediate_Dir "compiler___Win32_Blitz2DRelease"
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /O2 /Ob2 /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /Yu"std.h" /FD /c
# ADD CPP /nologo /MT /W3 /GX /O1 /D "WIN32" /D "NDEBUG" /D "_MBCS" /D "_LIB" /Yu"std.h" /FD /c
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

# Name "compiler - Win32 Release"
# Name "compiler - Win32 Debug"
# Name "compiler - Win32 Blitz3DRelease"
# Name "compiler - Win32 Blitz2DRelease"
# Begin Group "nodes"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\declnode.cpp
# End Source File
# Begin Source File

SOURCE=.\declnode.h
# End Source File
# Begin Source File

SOURCE=.\exprnode.cpp
# End Source File
# Begin Source File

SOURCE=.\exprnode.h
# End Source File
# Begin Source File

SOURCE=.\node.cpp
# End Source File
# Begin Source File

SOURCE=.\node.h
# End Source File
# Begin Source File

SOURCE=.\nodes.h
# End Source File
# Begin Source File

SOURCE=.\prognode.cpp
# End Source File
# Begin Source File

SOURCE=.\prognode.h
# End Source File
# Begin Source File

SOURCE=.\stmtnode.cpp
# End Source File
# Begin Source File

SOURCE=.\stmtnode.h
# End Source File
# Begin Source File

SOURCE=.\varnode.cpp
# End Source File
# Begin Source File

SOURCE=.\varnode.h
# End Source File
# End Group
# Begin Group "environ"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\decl.cpp
# End Source File
# Begin Source File

SOURCE=.\decl.h
# End Source File
# Begin Source File

SOURCE=.\environ.cpp
# End Source File
# Begin Source File

SOURCE=.\environ.h
# End Source File
# Begin Source File

SOURCE=.\label.h
# End Source File
# Begin Source File

SOURCE=.\type.cpp
# End Source File
# Begin Source File

SOURCE=.\type.h
# End Source File
# End Group
# Begin Group "parser"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\parser.cpp
# End Source File
# Begin Source File

SOURCE=.\parser.h
# End Source File
# Begin Source File

SOURCE=.\toker.cpp
# End Source File
# Begin Source File

SOURCE=.\toker.h
# End Source File
# End Group
# Begin Group "codegen"

# PROP Default_Filter ""
# Begin Group "codegen_x86"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\codegen_x86\codegen_x86.cpp
# ADD CPP /Yu"../std.h"
# End Source File
# Begin Source File

SOURCE=.\codegen_x86\codegen_x86.h
# End Source File
# Begin Source File

SOURCE=.\codegen_x86\tile.cpp
# ADD CPP /Yu"../std.h"
# End Source File
# Begin Source File

SOURCE=.\codegen_x86\tile.h
# End Source File
# End Group
# Begin Source File

SOURCE=.\codegen.h
# End Source File
# End Group
# Begin Group "assem"

# PROP Default_Filter ""
# Begin Group "assem_x86"

# PROP Default_Filter ""
# Begin Source File

SOURCE=.\assem_x86\asm_insts.cpp
# ADD CPP /Yu"../std.h"
# End Source File
# Begin Source File

SOURCE=.\assem_x86\assem_x86.cpp
# ADD CPP /Yu"../std.h"
# End Source File
# Begin Source File

SOURCE=.\assem_x86\assem_x86.h
# End Source File
# Begin Source File

SOURCE=.\assem_x86\insts.h
# End Source File
# Begin Source File

SOURCE=.\assem_x86\operand.cpp
# ADD CPP /Yu"../std.h"
# End Source File
# Begin Source File

SOURCE=.\assem_x86\operand.h
# End Source File
# End Group
# Begin Source File

SOURCE=.\assem.h
# End Source File
# End Group
# Begin Source File

SOURCE=.\ex.h
# End Source File
# Begin Source File

SOURCE=.\std.cpp
# ADD CPP /Yc"std.h"
# End Source File
# Begin Source File

SOURCE=.\std.h
# End Source File
# End Target
# End Project
