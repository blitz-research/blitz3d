[Setup]
OutputBaseFilename=Blitz3DUpdate1107
OutputDir=.

AppName=Blitz3D Update
AppVerName=Blitz3D Update 1.107
AppPublisher=Blitz Research Ltd
AppPublisherURL=http://www.blitzbasic.com
DefaultDirName={pf}\Blitz3D

Uninstallable=no
DisableStartupPrompt=yes
DisableProgramGroupPage=yes
DirExistsWarning=no
EnableDirDoesntExistWarning=yes
AllowRootDirectory=yes

[Dirs]
Name: "{app}\tmp"

[Files]
Source: "_release\Blitz3D.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "_release\versions.txt"; DestDir: "{app}"; Flags: ignoreversion
Source: "_release\bin\ide.exe"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "_release\bin\blitzcc.exe"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "_release\bin\linker.dll"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "_release\bin\runtime.dll"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "_release\bin\debugger.dll"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "_release\cfg\ide_toolbar.bmp"; DestDir: "{app}\cfg"; Flags: ignoreversion
Source: "_release\cfg\dbg_toolbar.bmp"; DestDir: "{app}\cfg"; Flags: ignoreversion
Source: "_release\userlibs\UserLibs.txt"; DestDir: "{app}\userlibs"; Flags: ignoreversion

