
Dim day$(6)				; Dim the array... 7 elements (0 to 6)

day$(0)="Monday"		; Set the array data
day$(1)="Tuesday"		;
day$(2)="Wednesday"		;
day$(3)="Thursday"		;
day$(4)="Friday"		;
day$(5)="Saturday"		;
day$(6)="Sunday"		;

For loop=0 To 6 Step 2
	Print day$(loop)		; Loop through and print the data
Next

