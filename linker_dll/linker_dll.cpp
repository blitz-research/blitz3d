
#include "../linker/std.h"
#include "../linker/linker.h"

#include <windows.h>

BOOL APIENTRY DllMain(HANDLE hModule,DWORD ul_reason_for_call,LPVOID lpReserved){

    switch( ul_reason_for_call ){
    case DLL_PROCESS_ATTACH:
		linkerGetLinker();
		break;
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
    case DLL_PROCESS_DETACH:
	default:
		;
    }
    return TRUE;
}
