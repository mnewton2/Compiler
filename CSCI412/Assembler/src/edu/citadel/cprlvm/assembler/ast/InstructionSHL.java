package edu.citadel.cprlvm.assembler.ast;


import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.Position;
import edu.citadel.cprlvm.OpCode;
import edu.citadel.cprlvm.assembler.Symbol;
import edu.citadel.cprlvm.assembler.Token;

import java.util.List;
import java.io.IOException;


/**
 * This class implements the abstract syntax tree for
 * the assembly language instruction SHL.
 */
public class InstructionSHL extends InstructionOneArg
  {
    public InstructionSHL(List<Token> labels, Token opCode, Token arg)
      {
        super(labels, opCode, arg);
      }


    public void assertOpCode()
      {
        assertOpCode(Symbol.SHL);
      }


    public void checkArgType() throws ConstraintException
      {
        checkArgType(Symbol.intLiteral);

        // check that the value is in the range 0..31
        int argValue = argToInt();
        if (argValue < 0 || argValue > 31)
          {
            Position errorPosition = getArg().getPosition();
            String   errorMessage  = "Shift amount must be be in the range 0..31";

            throw new ConstraintException(errorPosition, errorMessage);
          }
      }


    public int getArgSize()
      {
        return 1;   // 1 byte
      }


    @Override
    public void emit() throws IOException
      {
        emit(OpCode.SHL);
        emit(argToByte());
      }
  }
