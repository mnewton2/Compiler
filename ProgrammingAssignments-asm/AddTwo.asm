; Michael Newton 
; Programming Assignment 1 - Ch.3 Programming Exercise 1
; Dr. Moore - CSCI305
; Due: 09/30/2015

; When assembled this program assigns integer values to the EAX, EBX, ECX, and EDX registers
; and computes the following mathematical expression: A = (A+B) - (C+D).
; The result should be displayed in eax as 35d

.386
.model flat,stdcall
.stack 4096
ExitProcess proto,dwExitCode:dword


.data
	result DWORD ?
	var1 DWORD 20		; Initialize variables to the instructed values
	var2 DWORD 30
	var3 DWORD 10
	var4 DWORD 5

.code
main proc
	
	mov		eax,var1	; Move the values in to the respective registers
	mov		ebx,var2
	mov		ecx,var3
	mov		edx,var4

	add		eax,ebx		; Mathematical expression: A = (A+B) - (C+D)	
	add		ecx,edx		
	sub		eax,ecx		
	mov		result,eax

	invoke ExitProcess,0
main endp
end main