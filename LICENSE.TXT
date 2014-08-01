Blitz3D open source release!

Workspace and project files are in MSVC 6.0 format.

Ide and Debugger require MFC which is included with MSVC 6.0, but not with later free/express versions of MSVC.

You can grab the prebuilt free version of blitz3d from www.blitzbasic.com.

Steps to build:

1) Install freeimage241 into same dir as blitz3d: http://monkeycoder.co.nz/downloads/freeimage241.zip

2) Install fmodapi375win into same dir as blitz3d: http://www.fmod.org/files/public/fmodapi375win.zip

3) Open blitz3d workspace in MSVC 6.0.

4) Build project 'bblaunch' using config 'Win32 Blitz3D Release'.

5) Output files should end up in _release subdir.

6) Also copy fmodapi375win/api/fmod.dll to _release/bin.

7) Done?

Blitz3d is released under the zlib/libpng license.

The zlib/libpng License

Copyright (c) 2013 Blitz Research Ltd

This software is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any damages arising from the use of this software.

Permission is granted to anyone to use this software for any purpose, including commercial applications, and to alter it and redistribute it freely, subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.

2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.

3. This notice may not be removed or altered from any source distribution.
