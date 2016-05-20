; Michael Newton and Anthony Zovich
; Programming Assignment 9
; Recursive Greatest Common Divisor
; Due 12/6/2015


INCLUDE Irvine32.inc

.data

	firstMessage BYTE "Give me number 1",0
	secondMessage BYTE "Give me number 2",0

.code

main PROC

	mov	edx,OFFSET firstMessage
	call	WriteString
	call	CrlF
	call	ReadInt

	call	abs
	push	eax			; pushes the dividend onto the stack

	mov	edx,OFFSET secondMessage
	call	WriteString
	call	CrlF
	call	ReadInt

	call	abs
	push	eax			; pushes the divisor onto the stack
		
	call	recursiveGCD		; call the GCD procedure
	call	WriteDec		; have the program write the number returned by GCD
	call	CrlF
	
	exit

main ENDP


abs PROC
	cmp 	eax, 0			; Check for a negative number
	jg	DONE
	neg 	eax

DONE:
	RET
abs ENDP


recursiveGCD PROC

	mov	edx,0			
	push	ebp			
	mov	ebp,esp			
	mov	eax,[ebp+12] 		; Gets the dividend
	mov	ebx,[ebp+8]  		; Gets the divisor
	div	ebx			; divides eax by ebx stores the remainder in edx
	cmp	edx,0			
	jnz	recursiveLoop		; if edx isn't 0, then the program will jump to recurse
	mov	eax,ebx			; else ebx is moved into eax, because ebx will be the gcd
	jmp	return			; jumps to return, which is the exit to the recursive loop



recursiveLoop:			
	push	ebx			; puts the second number into the stack first
	push	edx			; puts the remainder as the second number in the stack
	call	recursiveGCD		; calls the gcd procedure again with the new stack set up

return:
	pop		ebp		; removes eax from the stack and returns it
	RET		8		; clean up the stack

recursiveGCD ENDP 

END main

