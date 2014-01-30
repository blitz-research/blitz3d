
#ifndef LIBS_H
#define LIBS_H

#include <map>
#include <list>
#include <vector>
#include <string>

using namespace std;

#include "../linker/linker.h"
#include "../compiler/environ.h"
#include "../compiler/parser.h"
#include "../bbruntime_dll/bbruntime_dll.h"

extern int bcc_ver;
extern int lnk_ver;
extern int run_ver;
extern int dbg_ver;

//openLibs
extern string home;
extern Linker *linkerLib;
extern Runtime *runtimeLib;

//linkLibs
extern Module *runtimeModule;
extern Environ *runtimeEnviron;
extern vector<string> keyWords;
extern vector<UserFunc> userFuncs;

const char *openLibs();

const char *linkLibs();

void closeLibs();

#endif
