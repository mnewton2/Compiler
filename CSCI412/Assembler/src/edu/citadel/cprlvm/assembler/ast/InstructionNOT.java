package edu.citadel.cprlvm.assembler.ast;


import edu.citadel.cprlvm.OpCode;
import edu.citadel.cprlvm.assembler.Symbol;
import edu.citadel.cprlvm.assembler.Token;

import java.util.List;
import java.io.IOException;


/**
 * This class implements the abstract syntax tree for
 * the assembly language instruction NOT.
 */
public class InstructionNOT extends InstructionNoArgs
  {
    public InstructionNOT(List<Token> labels, Token opCode)
      {
        super(labels, opCode);
      }


    public void assertOpCode()
      {
        assertOpCode(Symbol.NOT);
      }


    @Override
    public void emit() throws IOException
      {
        emit(OpCode.NOT);
      }
  }
