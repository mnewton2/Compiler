; Michael Newton
; Programming Assignment 7 - GCD
; Due 11/19/2015
; CSCI305 - DR. Moore

; This Assembly program reads two positive integers from the terminal 
; and returns the GCD of the two integers. The greatest common divisor 
; of two integers is the largest integer that will evenly divide both integers.

INCLUDE Irvine32.inc

.data

messageDividend BYTE "Enter one integer. ",0
messageDivisor  BYTE "Enter a second integer. ",0
endMessage BYTE "GCD= ",0

int1 DWORD ?
int2 DWORD ?
gcd DWORD ?

.code
main PROC

	mov	edx, OFFSET messageDividend
	call	WriteString 
	call	ReadInt
	
	call 	abs
	mov	int1, eax

	mov	edx, OFFSET messageDivisor
	call	WriteString
	call	ReadInt
	
	call 	abs
	mov	int2, eax
	
	call	generateGCD

exit
main ENDP

abs PROC
	cmp 	eax, 0		; Check for a negative number
	jg	DONE
	neg 	eax
	mov	ebx, eax

DONE:
	RET
abs ENDP


generateGCD PROC
	mov	eax, int1	; Move integers into eax and ebx
	mov	ebx, int2

; Check if the dividend is > divisor
; if so, then swap
	cmp	eax, ebx		
	jg	checkRemainder
	mov	edx, eax
	mov	eax, ebx
	mov	ebx, edx 

checkRemainder:
	mov	edx, 0		; Clear edx	
	div	ebx		; Divide eax by ebx
	cmp	edx, 0		; Check to see if remainder is 0
	jg	notZero		; Jump to notZero if edx > 0

	mov	eax, ebx	; If remainder is 0, ebx conatins gcd
	mov	gcd, ebx
	jmp	DONE	

; Move divisor to eax and remainder to ebx
; eax = new dividend, ebx = new divisor 
notZero:
	mov	eax, ebx
	mov	ebx, edx
	jmp	checkRemainder

DONE:
	mov	edx, OFFSET endMessage
	call	WriteString
	call	WriteDec
	
	RET 
generateGCD ENDP

end main