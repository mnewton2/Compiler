package edu.citadel.cprlvm.assembler.optimize;


import edu.citadel.cprlvm.assembler.Symbol;
import edu.citadel.cprlvm.assembler.Token;
import edu.citadel.cprlvm.assembler.ast.Instruction;
import edu.citadel.cprlvm.assembler.ast.InstructionSHL;

import java.util.List;
import java.util.LinkedList;


/*
 * Replaces multiplication by a power of 2 times a variable with left
 * shift.  Basically, this class looks for patterns of the form
 * "LDCINT 2**n, LDADDR x, LOADW, MUL" and replaces it with
 * "LDADDR x, LOADW, SHL n".  Note that the analogous replacement
 * for division will not work since division is not commutative.
 */
public class ShiftLeft implements Optimization
  {
    @Override
    public void optimize(List<Instruction> instructions, int instNum)
      {
        // quick check that there are at least four instructions remaining
        if (instNum > instructions.size() - 4)
            return;

        Instruction inst0 = instructions.get(instNum);
        Instruction inst1 = instructions.get(instNum + 1);
        Instruction inst2 = instructions.get(instNum + 2);
        Instruction inst3 = instructions.get(instNum + 3);
        
        // quick check that we are dealing with a constant and a variable
        Symbol symbol0 = inst0.getOpCode().getSymbol();
        Symbol symbol1 = inst1.getOpCode().getSymbol();
        Symbol symbol2 = inst2.getOpCode().getSymbol();

        if (OptimizationUtil.isConstAndVar(symbol0, symbol1, symbol2))
          {
            String arg0 = inst0.getArg().getText();
            int shiftAmount = OptimizationUtil.getShiftAmount(Integer.parseInt(arg0));

            if (shiftAmount > 0)
              {
                Symbol symbol3 = inst3.getOpCode().getSymbol();

                if (symbol3 == Symbol.MUL)
                  {
                    // replace MUL by SHL
                    Token shlToken = new Token(Symbol.SHL);
                    List<Token> labels = inst3.getLabels();
                    String argStr = Integer.toString(shiftAmount);
                    Token argToken = new Token(Symbol.intLiteral, argStr);
                    Instruction shlInst = new InstructionSHL(labels, shlToken, argToken);
                    instructions.set(instNum + 3, shlInst);
                    inst3 = shlInst;
                  }
                else
                    return;

                // copy labels from inst0 to inst1 before removing it
                List<Token> inst0Labels = inst0.getLabels();

                if (inst0Labels != null)
                  {
                    List<Token> inst1Labels = inst1.getLabels();
                    if (inst1Labels == null)
                        inst1Labels = new LinkedList<>();

                    inst1Labels.addAll(inst0Labels);
                  }

                // remove the LDCINT instruction
                instructions.remove(instNum);
              }
          }
      }
  }
