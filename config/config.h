
#ifndef CONFIG_H
#define CONFIG_H

#define BASE_VER	1108

#ifdef	PRO
#define	PRO_F	0x010000
#else
#define	PRO_F	0
#endif

#ifdef	DEMO
#define	DEMO_F	0x080000
#else
#define	DEMO_F	0
#endif

#ifdef	EDU
#define EDU_F	0x200000
#else
#define	EDU_F	0
#endif

#define VERSION		(BASE_VER|PRO_F|DEMO_F|EDU_F)

#endif
