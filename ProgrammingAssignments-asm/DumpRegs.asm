title DumpRegs
; Michael Newton
; Programming Assignment 2
; CSCI 305 - Dr. Moore
; Due 10/09/2015

; This program uses addition and subtraction to set and clear the Carry flag 
; (CF), Zero flag (ZF), Sign flag (SF), and Overflow flag (OF). 
; After each instruction the call to DumpRegs displays the registers and flags.
 
 include Irvine32.inc

.code
main proc
	; Set the CF
	mov		al,0FFh
	add		al,1
	call	DumpRegs
	
	; Clear the CF
	mov		al,0FFh
	sub		al,1
	call	DumpRegs

	; Set the ZF
	mov		cx,1
	sub		cx,1
	call	DumpRegs

	; Clear the ZF
	mov		cx,1
	add		cx,1
	call	DumpRegs

	; Set the SF
	mov		ax,7FFFh
	add		ax,2
	call	DumpRegs

	; Clear the SF
	mov		ax,7FFFh
	sub		ax,2
	call	DumpRegs

	; Set the OF
	mov		al,-128
	sub		al,1
	call	DumpRegs

	; Clear the OF
	mov		al,-128
	add		al,1
	call	DumpRegs


	invoke ExitProcess,0
main endp
end main