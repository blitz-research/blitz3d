[Setup]
OutputBaseFilename=Blitz3DDemo1101
OutputDir=.

AppName=Blitz3D Demo
AppVerName=Blitz3D Demo V1.101
AppPublisher=Blitz Research Ltd
AppPublisherURL=http://www.blitzbasic.com
DefaultDirName={sd}\Blitz3D Demo
DefaultGroupName=Blitz3D Demo

AllowRootDirectory=yes
DisableStartupPrompt=yes

[Icons]
Name: "{group}\Blitz3D Demo"; Filename: "{app}\Blitz3DDemo.exe"; WorkingDir: "{app}";
Name: "{group}\Uninstall Blitz3D Demo"; Filename: "{uninstallexe}";

[Files]
Source: "_demo\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs
Source: "_demo\tmp\tmp.bb"; DestDir: "{app}\tmp"; Flags: ignoreversion deleteafterinstall
Source: "_demo\cfg\blitzide.prefs"; DestDir: "{app}\cfg"; Flags: ignoreversion deleteafterinstall

[Run]
Filename: "{app}\Blitz3DDemo.exe"; Description: "Launch Blitz3D Demo"; Flags: postinstall nowait skipifsilent

