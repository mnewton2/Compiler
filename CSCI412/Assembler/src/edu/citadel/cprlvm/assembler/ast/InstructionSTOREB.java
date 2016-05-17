package edu.citadel.cprlvm.assembler.ast;


import edu.citadel.cprlvm.OpCode;
import edu.citadel.cprlvm.assembler.Symbol;
import edu.citadel.cprlvm.assembler.Token;

import java.util.List;
import java.io.IOException;


/**
 * This class implements the abstract syntax tree for
 * the assembly language instruction STOREB.
 */
public class InstructionSTOREB extends InstructionNoArgs
  {
    public InstructionSTOREB(List<Token> labels, Token opCode)
      {
        super(labels, opCode);
      }


    public void assertOpCode()
      {
        assertOpCode(Symbol.STOREB);
      }


    @Override
    public void emit() throws IOException
      {
        emit(OpCode.STOREB);
      }
  }
