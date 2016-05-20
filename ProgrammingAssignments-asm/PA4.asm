; Programming Assignment 4
; CSCI 305 - Dr. Moore
; Michael Newton
; Due 10/23/2015


;	This programming assignment utilizes direct-offset addressing, indirect operands, and indexed operands
; to move the values declared in the the UArray and SArray to the respective registers
; stated in the directions. The call to DumpRegs is used to display the register
; values.

INCLUDE Irvine32.inc

.data
	UArray WORD 1000h, 2000h, 3000h, 4000h
	SArray SWORD -1, -2, -3, -4
	
.code
main proc
	; Direct-Offset Addressing (Word Array)
	movzx	eax, UArray
	movzx	ebx, [UArray+2]
	movzx	ecx, [UArray+4]
	movzx	edx, [UArray+6]
	call DumpRegs

	; Direct-Offset Addressing (Signed Word Array)
	movsx	eax, SArray
	movsx	ebx, [SArray+2]
	movsx	ecx, [SArray+4]
	movsx	edx, [SArray+6]
	call DumpRegs

	; Indirect Operands
	mov		esi, OFFSET UArray
	movzx	eax, WORD PTR [esi]
	movzx	ebx, WORD PTR [esi+2]
	movzx	ecx, WORD PTR [esi+4]
	movzx	edx, WORD PTR [esi+6]
	call DumpRegs

	mov		esi, OFFSET SArray
	movsx	eax, SWORD PTR [esi]
	movsx	ebx, SWORD PTR [esi+2]
	movsx	ecx, SWORD PTR [esi+4]
	movsx	edx, SWORD PTR [esi+6]
	call DumpRegs

	; Indexed Operands
	mov		esi, 0
	movzx	eax, UArray[esi]
	movzx	ebx, UArray[esi+2]
	movzx	ecx, UArray[esi+4]
	movzx	edx, UArray[esi+6]
	call DumpRegs

	mov		esi, 0
	movsx	eax, SArray[esi]
	movsx	ebx, SArray[esi+2]
	movsx	ecx, SArray[esi+4]
	movsx	edx, SArray[esi+6]
	call DumpRegs

	
	exit
main endp
end main