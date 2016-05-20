INCLUDE Irvine32.inc

.data
buffer DWORD 47 DUP(0)
filehandle DWORD ?
filename BYTE "myfile.txt", 0

.code
main PROC
	mov edx, OFFSET filename
	call CreateOutputFile
	cmp eax, INVALID_HANDLE_VALUE
	je file_error
	mov filehandle, eax

	mov esi, OFFSET buffer	; offset of array of fib nums
	
	mov   eax,1
		
	mov  [esi], eax	
	add	 esi, 4

	mov   ebx,0		; initial setup of the fib sequence
	mov   edx,1
    mov   ecx,46	; count of fib nums 
L1:
	mov  eax,ebx	; eax = ebx + edx
	add  eax,edx
	
	mov  [esi], eax
	add	 esi, 4

	mov  ebx,edx
	mov  edx,eax
    Loop L1

	mov  eax, filehandle
	mov  edx, OFFSET buffer
	mov  ecx, 47*4
	call WriteToFile

	mov  eax, filehandle
	call CloseFile
file_error:
	exit
main ENDP
END main