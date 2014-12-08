[Setup]
OutputBaseFilename=Blitz3DSetup1108b
OutputDir=installs

AppName=Blitz3D
AppVerName=Blitz3D 1.108b
AppPublisher=Blitz Research Ltd
AppPublisherURL=http://www.blitzbasic.com
DefaultDirName={pf}\Blitz3D
DefaultGroupName=Blitz3D

AllowRootDirectory=yes
DisableStartupPrompt=yes

[Icons]
Name: "{group}\Blitz3D"; Filename: "{app}\Blitz3D.exe"; WorkingDir: "{app}";
Name: "{group}\Uninstall Blitz3D"; Filename: "{uninstallexe}";

[Files]
Source: "..\_release\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs
Source: "..\_release\cfg\blitzide.prefs"; DestDir: "{app}\cfg"; Flags: ignoreversion deleteafterinstall

[Run]
Filename: "{app}\Blitz3D.exe"; Description: "Launch Blitz3D"; Flags: postinstall nowait skipifsilent

