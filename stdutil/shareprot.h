
#ifndef SHAREPROT_H
#define SHAREPROT_H

//bump counter
void shareProtStart();

//return counter
int  shareProtCheck();

//error if counter==0
void shareProtAssert();

//clean stack then jump! Never returns!
void _stdcall shareProtJump( void(*target)() );

#endif