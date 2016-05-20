; Programming Assignment 6 - Random Strings
; Michael Newton
; CSCI305 - Dr.Moore
; 11/11/2015

INCLUDE Irvine32.inc

STR_COUNT = 20
STR_SIZE  = 10

.data
	strArray BYTE STR_SIZE DUP(0),0

.code
main PROC
	mov	ecx, STR_COUNT
L0:	
	mov	esi, OFFSET strArray			; Pass the pointer
	mov	eax, STR_SIZE				; Pass the size
	call	generateRandomString

	mov	edx, OFFSET strArray
	call	WriteString				; Pass offset of each string in edx
	call	Crlf					; Have each string displayed on new line
	loop	L0

	exit
main ENDP

generateRandomString PROC
; Recieves the value of eax/ length of string

	push	ecx
	mov	ecx, eax				; Set the loop counter

L0:
	mov	eax, 26					; Create a range from 0-25
	call	RandomRange
	add	al, 'A'
	mov	[esi], al				; indirect addressing
	inc	esi
	loop	L0
	pop ecx						; Restore ecx to original length
	ret
generateRandomString ENDP
end main