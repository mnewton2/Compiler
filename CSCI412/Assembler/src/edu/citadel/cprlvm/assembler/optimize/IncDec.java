package edu.citadel.cprlvm.assembler.optimize;

import edu.citadel.cprlvm.assembler.Symbol;
import edu.citadel.cprlvm.assembler.Token;
import edu.citadel.cprlvm.assembler.ast.Instruction;
import edu.citadel.cprlvm.assembler.ast.InstructionDEC;
import edu.citadel.cprlvm.assembler.ast.InstructionINC;

import java.util.List;
import java.util.LinkedList;


/*
 * Replaces addition of 1 with increment and subtraction of 1 with decrement.
 */
public class IncDec implements Optimization
  {
    @Override
    public void optimize(List<Instruction> instructions, int instNum)
      {
        // quick check that there are at least 2 instructions remaining
        if (instNum > instructions.size() - 2)
            return;

        Instruction inst0 = instructions.get(instNum);
        Instruction inst1 = instructions.get(instNum + 1);
        
        Symbol symbol0 = inst0.getOpCode().getSymbol();

        if (symbol0 == Symbol.LDCINT)
          {
            String arg0 = inst0.getArg().getText();

            if (arg0.equals("1"))
              {
                Symbol symbol1 = inst1.getOpCode().getSymbol();
                
                if (symbol1 == Symbol.ADD)
                  {
                    // replace LDCINT by INC
                    Token incToken = new Token(Symbol.INC);
                    List<Token> labels = inst0.getLabels();
                    Instruction incInst = new InstructionINC(labels, incToken);
                    instructions.set(instNum, incInst);
                    inst0 = incInst;
                  }
                else if (symbol1 == Symbol.SUB)
                  {
                    // replace LDCINT 1 by DEC
                    Token decToken = new Token(Symbol.DEC);
                    List<Token> labels = inst0.getLabels();
                    Instruction decInst = new InstructionDEC(labels, decToken);
                    instructions.set(instNum, decInst);
                    inst0 = decInst;
                  }
                else
                    return;

                // copy labels from inst1 to inst0 before removing it
                List<Token> inst1Labels = inst1.getLabels();

                if (inst1Labels != null)
                  {
                    List<Token> inst0Labels = inst0.getLabels();
                    if (inst0Labels == null)
                        inst0Labels = new LinkedList<>();

                    inst0Labels.addAll(inst1Labels);
                  }

                // remove the LDCINT instruction
                instructions.remove(instNum + 1);
              }
          }
      }
  }
