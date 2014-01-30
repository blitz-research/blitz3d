
Classes:

ProgNode	- output of parse - tree of Nodes
TProg		- output of translate - vector of TNodes
string      - output of codegen
Module		- output of assemble

reserved asm labels.

_b???? reserved for private blitz lib calls
_l???? reserved for compiler generated labels
_c???? reserved for codegen generated labels
_a???? reserved for asm generated labels

Codegen should probably be able to create it's own 'data' for constants.

The IR_CONST instruction may have to generate a label to store, say, 
strings (and floats on x86?) 


* libs prototype info:

	graphics%%%			- 3 int args
	rect%%%%			- 4 int args
	vwait
	%mousex				- int result
	%mousey				- int result
	<Image>loadImage$	- obj result, 1 string arg

* floats, strings, user types

	Stick with old BB newtypes:

	NewType mytype
		x.f:y.f:z.f
		image.Image
	End NewType

	Dim List lst.mytype
	Dim Queue que.mytype
	Dim Vector vec.mytype

	test=new mytype
	test\x=100
	test\y=100
	test\z=100

	name="Hello"

	move test

	statement move t.mytype
		t\x=t\x+t\xs
		t\y=t\y+t\ys
	end statement

* stop/step/continue/debug

* error handling

	Errors are sent to cerr, and an exception thrown.

	Error string format:

	E charpos message - eg:

	cerr<<"E 100 Blitz expects an identifier here"<<endl;
