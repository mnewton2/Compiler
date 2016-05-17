package edu.citadel.cprlvm.assembler.ast;


import edu.citadel.compiler.ConstraintException;
import edu.citadel.cprlvm.OpCode;
import edu.citadel.cprlvm.assembler.Symbol;
import edu.citadel.cprlvm.assembler.Token;

import java.util.List;
import java.io.IOException;


/**
 * This class implements the abstract syntax tree for
 * the assembly language instruction LOADCB.
 */
public class InstructionLOADCB extends InstructionOneArg
  {
    public InstructionLOADCB(List<Token> labels, Token opCode, Token arg)
      {
        super(labels, opCode, arg);
      }


    public void assertOpCode()
      {
        assertOpCode(Symbol.LOADCB);
      }


    public void checkArgType() throws ConstraintException
      {
        checkArgType(Symbol.intLiteral);
      }


    public int getArgSize()
      {
        return 1;
      }


    @Override
    public void emit() throws IOException
      {
        emit(OpCode.LOADCB);
        emit(argToByte());
      }
  }
