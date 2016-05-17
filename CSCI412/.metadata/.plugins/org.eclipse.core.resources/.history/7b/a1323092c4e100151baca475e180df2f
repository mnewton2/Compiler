package edu.citadel.cprlvm;


import edu.citadel.compiler.util.ByteUtil;

import java.io.*;


/**
 * This class implements a virtual machine for the programming language CPRL.
 * It interprets instructions for a hypothetical CPRL computer.
 */
public class CprlVm
  {
    private static final int FAILURE = -1;

    /** virtual machine constant for false */
    private static final byte FALSE = (byte) 0;

    /** virtual machine constant for true */
    private static final byte TRUE = (byte) 1;

    /** virtual machine constant for byte value 0 */
    private static final byte ZERO = (byte) 0;

    /** virtual machine constant for byte value 1 */
    private static final byte ONE = (byte) 1;

    /** virtual machine constant for byte value -1 */
    private static final byte MINUS_ONE = (byte) -1;

    /** end of file */
    private static final int EOF = -1;

    /** 1K = 2**10 */
    private static final int K = 1024;

    /** virtual machine constant for false */
    private static final int DEFAULT_MEMORY_SIZE = 8*K;

    /** computer memory (for the virtual CPRL machine) */
    private byte[] memory;

    /** program counter (index of the next instruction in memory) */
    private int pc;

    /** base pointer */
    private int bp;

    /** stack pointer (index of the top of the stack) */
    private int sp;

    /** bottom of the stack */
    private int sb;

    /** true if the virtual computer is currently running */
    private boolean running;


    /**
     * This method constructs a CPRL virtual machine, loads the byte code
     * from the specified file into memory, and runs the byte code. <br>
     * Usage:  java CprlVm filename <br>
     * where filename is the name of a file containing the byte code
     * for a CPRL program.
     */
    public static void main(String[] args) throws FileNotFoundException
      {
        if (args.length != 1)
          {
            System.err.println("Usage:  java CprlVm filename");

            // stop the VM with a nonzero status code
            System.exit(FAILURE);
          }

        CprlVm vm = new CprlVm(DEFAULT_MEMORY_SIZE);
        FileInputStream codeFile = new FileInputStream(args[0]);
        vm.loadProgram(codeFile);
        vm.run();
      }


    /**
     * Construct a CPRL virtual machine with a given number of bytes of memory.
     * 
     * @param numOfBytes the number of bytes in memory of the virtual machine
     */
    public CprlVm(int numOfBytes)
      {
        // create and zero out memory
        memory = new byte[numOfBytes];
        for (int i = 0;  i < memory.length;  ++i)
            memory[i] = 0;

        // initialize registers
        pc = 0;
        bp = 0;
        sp = 0;
        sb = 0;

        running = false;
      }


    /**
     * Loads the program into memory.
     * 
     * @param codeFile the FileInputStream containing the object code
     */
    public void loadProgram(FileInputStream codeFile)
      {
        int address = 0;
        int inByte;

        try
          {
            while ((inByte = codeFile.read()) != -1)
                memory[address++] = (byte) inByte;

            bp = address;
            sb = address;
            sp = bp - 1;
            codeFile.close();
          }
        catch (IOException e)
          {
            error(e.toString());
          }
      }


    /**
     * Runs the program currently in memory.
     */
    public void run()
      {
        byte opCode;

        running = true;
        pc = 0;
        while (running)
          {
            opCode = fetchByte();

            switch (opCode)
              {
                case OpCode.ADD:
                    add();
                    break;
                case OpCode.ALLOC:
                    allocate();
                    break;
                case OpCode.BG:
                    branchGreater();
                    break;
                case OpCode.BGE:
                    branchGreaterOrEqual();
                    break;
                case OpCode.BL:
                    branchLess();
                    break;
                case OpCode.BLE:
                    branchLessOrEqual();
                    break;
                case OpCode.BNZ:
                    branchNonZero();
                    break;
                case OpCode.BR:
                    branch();
                    break;
                case OpCode.BZ:
                    branchZero();
                    break;
                case OpCode.CALL:
                    call();
                    break;
                case OpCode.CMP:
                    compare();
                    break;
                case OpCode.DEC:
                  decrement();
                  break;
                case OpCode.DIV:
                    divide();
                    break;
                case OpCode.GETCH:
                    getCh();
                    break;
                case OpCode.GETINT:
                    getInt();
                    break;
                case OpCode.HALT:
                    halt();
                    break;
                case OpCode.INC:
                    increment();
                    break;
                case OpCode.LDCCH:
                    loadConstCh();
                    break;
                case OpCode.LDCINT:
                    loadConstInt();
                    break;
                case OpCode.LDCSTR:
                    loadConstStr();
                    break;
                case OpCode.LDADDR:
                    loadAddress();
                    break;
                case OpCode.LDGADDR:
                    loadGlobalAddress();
                    break;
                case OpCode.LOAD:
                    load();
                    break;
                case OpCode.LOADB:
                    loadByte();
                    break;
                case OpCode.LOAD2B:
                    load2Bytes();
                    break;
                case OpCode.LOADCB:
                    loadConstByte();
                    break;
                case OpCode.LDMEM:
                    loadMemory();
                    break;
                case OpCode.LOADW:
                    loadWord();
                    break;
                case OpCode.MOD:
                    modulo();
                    break;
                case OpCode.MUL:
                    multiply();
                    break;
                case OpCode.NEG:
                    negate();
                    break;
                case OpCode.NOT:
                    not();
                    break;
                case OpCode.PROC:
                    procedure();
                    break;
                case OpCode.PROGRAM:
                    program();
                    break;
                case OpCode.PUTBYTE:
                    putByte();
                    break;
                case OpCode.PUTCH:
                    putChar();
                    break;
                case OpCode.PUTEOL:
                    putEOL();
                    break;
                case OpCode.PUTINT:
                    putInt();
                    break;
                case OpCode.PUTSTR:
                    putString();
                    break;
                case OpCode.RET:
                    returnInst();
                    break;
                case OpCode.SHL:
                  shiftLeft();
                  break;
                case OpCode.SHR:
                  shiftRight();
                  break;
                case OpCode.STORE:
                    store();
                    break;
                case OpCode.STOREB:
                    storeByte();
                    break;
                case OpCode.STORE2B:
                    store2Bytes();
                    break;
                case OpCode.STOREW:
                    storeWord();
                    break;
                case OpCode.SUB:
                    subtract();
                    break;
                default:
                    error("invalid machine instruction");
              }
          }
      }


    // Start:  internal machine instructions that do NOT correspond to OpCodes
    //------------------------------------------------------------------------


    /**
     * Print an error message and exit with nonzero status code.
     */
    private static void error(String message)
      {
        System.err.println(message);
        System.exit(1);
      }


    /**
     * Pop the top byte off the stack and return its value.
     */
    private byte popByte()
      {
        return memory[sp--];
      }


    /**
     * Pop the top character off the stack and return its value.
     */
    private char popChar()
      {
        byte b1 = popByte();
        byte b0 = popByte();

        return ByteUtil.bytesToChar(b0, b1);
      }


    /**
     * Pop the top integer off the stack and return its value.
     */
    private int popInt()
      {
        byte b3 = popByte();
        byte b2 = popByte();
        byte b1 = popByte();
        byte b0 = popByte();

        return ByteUtil.bytesToInt(b0, b1, b2, b3);
      }


    /**
     * Push a byte onto the stack.
     */
    private void pushByte(byte b)
      {
        memory[++sp] = b;
      }


    /**
     * Push a character onto the stack.
     */
    private void pushChar(char c)
      {
        byte[] bytes = ByteUtil.charToBytes(c);

        pushByte(bytes[0]);
        pushByte(bytes[1]);
      }


    /**
     * Push an integer onto the stack.
     */
    private void pushInt(int n)
      {
        byte[] bytes = ByteUtil.intToBytes(n);

        pushByte(bytes[0]);
        pushByte(bytes[1]);
        pushByte(bytes[2]);
        pushByte(bytes[3]);
      }


    /**
     * Fetch the next instruction/byte from memory.
     */
    private byte fetchByte()
      {
        return memory[pc++];
      }


    /**
     * Fetch the next instruction int operand from memory.
     */
    private int fetchInt()
      {
        byte b0 = fetchByte();
        byte b1 = fetchByte();
        byte b2 = fetchByte();
        byte b3 = fetchByte();

        return ByteUtil.bytesToInt(b0, b1, b2, b3);
      }


    /**
     * Fetch the next instruction char operand from memory.
     */
    private char fetchChar()
      {
        byte b0 = fetchByte();
        byte b1 = fetchByte();

        return ByteUtil.bytesToChar(b0, b1);
      }


    /**
     * Returns the integer at the specified memory address.
     * Does not alter pc, sp, or bp.
     */
    private int getInt(int address)
      {
        byte b0 = memory[address + 0];
        byte b1 = memory[address + 1];
        byte b2 = memory[address + 2];
        byte b3 = memory[address + 3];

        return ByteUtil.bytesToInt(b0, b1, b2, b3);
      }


    //----------------------------------------------------------------------
    // End:  internal machine instructions that do NOT correspond to OpCodes


    // Start:  machine instructions corresponding to OpCodes
    //------------------------------------------------------

    private void add()
      {
        int operand2 = popInt();
        int operand1 = popInt();

        pushInt(operand1 + operand2);
      }


    private void allocate()
      {
        int numBytes = fetchInt();

        sp = sp + numBytes;
      }


    /**
     * Unconditional branch.
     */
    private void branch()
      {
        int opCodeAddr   = pc - 1;
        int displacement = fetchInt();

        pc = opCodeAddr + displacement;
      }


    private void branchGreater()
      {
        int  opCodeAddr   = pc - 1;
        int  displacement = fetchInt();
        byte value        = popByte();

        if (value > 0)
            pc = opCodeAddr + displacement;
      }


    private void branchGreaterOrEqual()
      {
        int  opCodeAddr   = pc - 1;
        int  displacement = fetchInt();
        byte value        = popByte();

        if (value >= 0)
            pc = opCodeAddr + displacement;
      }


    private void branchLess()
      {
        int  opCodeAddr   = pc - 1;
        int  displacement = fetchInt();
        byte value        = popByte();

        if (value < 0)
            pc = opCodeAddr + displacement;
      }


    private void branchLessOrEqual()
      {
        int  opCodeAddr   = pc - 1;
        int  displacement = fetchInt();
        byte value        = popByte();

        if (value <= 0)
            pc = opCodeAddr + displacement;
      }


    /**
     * Branch if the byte on the top of the stack is nonzero (true).
     */
    private void branchNonZero()
      {
        int  opCodeAddr   = pc - 1;
        int  displacement = fetchInt();
        byte value        = popByte();

        if (value != 0)
            pc = opCodeAddr + displacement;
      }


    /**
     * Branch if ZF (zero flag) is true.
     */
    private void branchZero()
      {
        int  opCodeAddr   = pc - 1;
        int  displacement = fetchInt();
        byte value        = popByte();

        if (value == 0)
            pc = opCodeAddr + displacement;
      }


    private void call()
      {
        int opCodeAddr   = pc - 1;
        int displacement = fetchInt();
        
        pushInt(bp);          // dynamic link
        pushInt(pc);          // return address

        // set bp to starting address of new frame
        bp = sp - Constants.BYTES_PER_FRAME + 1;
        
        // set pc to first statement of called procedure
        pc = opCodeAddr + displacement;
      }


    private void compare()
      {
        int operand2 = popInt();
        int operand1 = popInt();

        if (operand1 == operand2)
            pushByte(ZERO);
        else if (operand1 > operand2)
            pushByte(ONE);
        else
            pushByte(MINUS_ONE);
      }


    private void decrement()
      {
        int operand = popInt();
        pushInt(operand - 1);
      }


    private void divide()
      {
        int operand2 = popInt();
        int operand1 = popInt();

        if (operand2 != 0)
            pushInt(operand1/operand2);
        else
            error("*** FAULT:  Divide by zero ***");
      }


    private void getInt()
      {
        try
          {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line = in.readLine();

            int n = line != null ? Integer.parseInt(line) : 0;

            pushInt(n);
          }
        catch (NumberFormatException e)
          {
            error("Invalid input");
          }
        catch (IOException ex)
          {
            ex.printStackTrace();
            error("IO Error");
          }
      }


    private void getCh()
      {
        try
          {
            Reader in = new InputStreamReader(System.in);

            int ch = in.read();

            if (ch == EOF)
                error("Invalid input");

            pushChar((char) ch);
          }
        catch (IOException ex)
          {
            ex.printStackTrace();
            error("Invalid input");
          }
      }


    private void halt()
      {
        running = false;
      }


    private void increment()
      {
        int operand = popInt();
        pushInt(operand + 1);
      }


    /**
     * Loads a multibyte variable onto the stack.  The number of bytes
     * is an argument of the instruction, and the address of the
     * variable is obtained by popping it off the top of the stack.
     */
    private void load()
      {
        int length  = fetchInt();
        int address = popInt();

        for (int i = 0;  i < length;  ++i)
            pushByte(memory[address + i]);
      }


    private void loadConstInt()
      {
        int value = fetchInt();

        pushInt(value);
      }


    private void loadConstCh()
      {
        char ch = fetchChar();

        pushChar(ch);
      }


    private void loadConstByte()
      {
        byte b = fetchByte();

        pushByte(b);
      }


    private void loadConstStr()
      {
        int strLength = fetchInt();
        int strAddr   = pc;

        pushInt(strLength);
        pushInt(strAddr);
        pc = pc + 2*strLength;
      }


    private void loadAddress()
      {
        int displacement = fetchInt();

        pushInt(bp + displacement);
      }


    private void loadGlobalAddress()
      {
        int displacement = fetchInt();

        pushInt(sb + displacement);
      }


    /**
     * Loads the contents of a memory address onto the top of the stack.
     */
    private void loadMemory()
      {
        int displacement = fetchInt();
        int address = bp + displacement;

        byte b0 = memory[address + 0];
        byte b1 = memory[address + 1];
        byte b2 = memory[address + 2];
        byte b3 = memory[address + 3];

        int value = ByteUtil.bytesToInt(b0, b1, b2, b3);

        pushInt(value);
      }


    /**
     * Loads a single byte onto the stack.  The address of the
     * byte is obtained by popping it off the top of the stack.
     */
    private void loadByte()
      {
        int address = popInt();

        byte b = memory[address];

        pushByte(b);
      }


    /**
     * Loads two bytes onto the stack.  The address of the first
     * byte is obtained by popping it off the top of the stack.
     */
    private void load2Bytes()
      {
        int address = popInt();

        byte b0 = memory[address + 0];
        byte b1 = memory[address + 1];

        pushByte(b0);
        pushByte(b1);
      }


    /**
     * Loads a single word-size variable (four bytes) onto the
     * stack.  The address of the variable is obtained by poping
     * it off the top of the stack.
     */
    private void loadWord()
      {
        int address = popInt();

        byte b0 = memory[address + 0];
        byte b1 = memory[address + 1];
        byte b2 = memory[address + 2];
        byte b3 = memory[address + 3];

        int value = ByteUtil.bytesToInt(b0, b1, b2, b3);

        pushInt(value);
      }


    private void modulo()
      {
        int operand2 = popInt();
        int operand1 = popInt();

        pushInt(operand1 % operand2);
      }


    private void multiply()
      {
        int operand2 = popInt();
        int operand1 = popInt();

        pushInt(operand1*operand2);
      }


    private void negate()
      {
        int operand1 = popInt();

        pushInt(-operand1);
      }


    private void not()
      {
        byte operand = popByte();

        if (operand == FALSE)
            pushByte(TRUE);
        else
            pushByte(FALSE);
      }


    private void procedure()
      {
        allocate();
      }


    private void program()
      {
        int varLength = fetchInt();

        bp = sb;

        // init stack pointer to allow for a dummy frame
        //sp = bp + Constants.BYTES_PER_FRAME + varLength - 1;
        sp = bp + varLength - 1;

        if (sp >= memory.length)
            error("*** Out of memory ***");
      }


    private void putChar()
      {
        System.out.print(popChar());
      }


    private void putByte()
      {
        System.out.print(popByte());
      }


    private void putInt()
      {
        System.out.print(popInt());
      }


    private void putEOL()
      {
        System.out.println();
      }


    private void putString()
      {
        int strAddr   = popInt();
        int strLength = popInt();

        char[] str = new char[strLength];

        for (int i = 0;  i < strLength;  ++i)
          {
            byte b0 = memory[strAddr++];
            byte b1 = memory[strAddr++];

            str[i] = ByteUtil.bytesToChar(b0, b1);
          }

        System.out.print(str);
      }


    private void shiftLeft()
      {
        int  operand = popInt();

        // zero out left three bits of shiftAmount
        byte mask = 0x1F;   // = 00011111 in binary
        byte shiftAmount = (byte) (fetchByte() & mask);
        
        pushInt(operand << shiftAmount);
      }


    private void shiftRight()
      {
        int  operand = popInt();

        // zero out left three bits of shiftAmount
        byte mask = 0x1F;   // = 00011111 in binary
        byte shiftAmount = (byte) (fetchByte() & mask);
        
        pushInt(operand >> shiftAmount);
      }


    private void returnInst()
      {
        int bpSave = bp;
        int paramLength = fetchInt();

        sp = bpSave - paramLength - 1;
        bp = getInt(bpSave);
        pc = getInt(bpSave + Constants.BYTES_PER_INTEGER);
      }


    private void store()
      {
        int length  = fetchInt();
        byte[] data = new byte[length];

        // pop bytes of data, storing in reverse order
        for (int i = length - 1;  i >= 0;  --i)
            data[i] = popByte();

        int destAddr = popInt();

        for (int i = 0;  i < length;  ++i)
            memory[destAddr + i] = data[i];
      }


    private void storeByte()
      {
        byte value   = popByte();
        int destAddr = popInt();

        memory[destAddr] = value;
      }


    private void store2Bytes()
      {
        byte byte1 = popByte();
        byte byte0 = popByte();
        int  destAddr = popInt();

        memory[destAddr + 0] = byte0;
        memory[destAddr + 1] = byte1;
      }


    private void storeWord()
      {
        int value = popInt();
        int destAddr = popInt();

        byte[] bytes = ByteUtil.intToBytes(value);

        memory[destAddr + 0] = bytes[0];
        memory[destAddr + 1] = bytes[1];
        memory[destAddr + 2] = bytes[2];
        memory[destAddr + 3] = bytes[3];
      }


    private void subtract()
      {
        int operand2 = popInt();
        int operand1 = popInt();
        int result = operand1 - operand2;
        
        pushInt(result);
      }


    // End:  machine instructions corresponding to OpCodes
    //----------------------------------------------------
  }
