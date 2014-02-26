BB Launcher V1.00

Written by Rob Hutchinson 19/08/2001

Requires VB Runtimes. You can get these from:
ftp://ftp.microsoft.com/Softlib/MSLFILES/VBRUN60sp3.EXE

BBLauncher specifically needs the MSVBVM60.DLL file. Which is the VB6 runtime dll.
It is included with the above runtimes and resides in <SYSTEM>\System dir, (IE
C:\Windows\System\, or C:\WINNT\System\) windows now comes with the VB runtimes
already installed (WinME, 2K and XP), and anyone running 95/98/NT will probably
already have installed the VB runtimes or another application might have.
But if you want to make sure that it definately runs on the end user machine,
just drop the MSVBVM60.DLL file in the same directory as Launcher.EXE.


INI Files:
There are 2 INI files (Launcher.INI and Launcher_CLEAN.INI) that come with the
launcher and 1 that is generated when you run BBLauncher (Settings.INI).
BBLauncher uses Launcher.INI to set itself up. And generates Settings.INI 
on exit, depending on the options the user picks (provided they enable setting
saving which is on by default). Launcher_CLEAN.INI is just a "CLEAN" version of
Launcher.INI which is full of comments to help you set it up (not that its hard :))
But use Launcher_CLEAN.INI once you know what everything does. Just rename it
to Launcher.INI.

Programming:
Its pretty simple to use in your own program. If you're writing a 2D program
include "launcher2d.bb" and if you're writing a 3d program include "launcher3d.bb"
in place of  GraphicsXX x,x,x,x : Setbuffer BackBuffer().
Basically the include will setup the screen for you, by default the screenmode
will be 640x480x16. But you can override this during development by adding a 
command line from the blitz menu. The params are formatted: "/<OPTIONS><PARAMS>"

This: /TYPE0 /WIDTH1024 /HEIGHT768 /BPP32 /ADDIT1

Will open a 1024x768x32 screen in Fullscreen-Always, with the additional option
set to true.
The additional option param is /ADDITx, and to query it in your program just
read the  iAdditional  variable, this will either be 0 or 1 depending on what
the user selected. Don't forget you can disable the additional option in the INI
and ignore it in code, if you like.
Check out the example.bb program for usage.



Well thats about it really..
Have fun. And email me if you use it in something you wrote, I`d like to see how 
many people would actually use it. If people are interested in it then I have a 
lot of features I`d like to add that I probably wouldn`t use myself but would be
very useful to others. But I wont add them if noone is interested in it. So email
me if you are: loki.sd@cableinet.co.uk 
One of the planned features is a fully customisable options section, which you
could put things like "Enable WBuffer", or well, basically anything... I didn`t
do this originally because it would need some more components that I didn`t want
to have to include in the distro ATM BBLauncher only uses the standard VB
components, and it was written purposly that way.

-- 
Rob Hutchinson, loki.sd@cableinet.co.uk / 27181384

End