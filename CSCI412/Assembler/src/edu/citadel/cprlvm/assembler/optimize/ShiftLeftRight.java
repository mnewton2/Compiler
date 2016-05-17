package edu.citadel.cprlvm.assembler.optimize;


import java.util.LinkedList;
import java.util.List;

import edu.citadel.cprlvm.assembler.Symbol;
import edu.citadel.cprlvm.assembler.Token;
import edu.citadel.cprlvm.assembler.ast.Instruction;
import edu.citadel.cprlvm.assembler.ast.InstructionSHL;
import edu.citadel.cprlvm.assembler.ast.InstructionSHR;


/**
 * Replaces multiplication by a power of 2 with left shift and
 * division by a power of two with right shift where possible.  
 */
public class ShiftLeftRight implements Optimization
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
            int shiftAmount = OptimizationUtil.getShiftAmount(Integer.parseInt(arg0));

            if (shiftAmount > 0)
              {
                Symbol symbol1 = inst1.getOpCode().getSymbol();
                
                if (symbol1 == Symbol.MUL)
                  {
                    // replace LDCINT with SHL 
                    Token shlToken = new Token(Symbol.SHL);
                    List<Token> labels = inst0.getLabels();
                    String argStr = Integer.toString(shiftAmount);
                    Token argToken = new Token(Symbol.intLiteral, argStr);
                    Instruction shlInst = new InstructionSHL(labels, shlToken, argToken);
                    instructions.set(instNum, shlInst);
                    inst0 = shlInst;
                  }
                else if (symbol1 == Symbol.DIV)
                  {
                    // replace LDCINT by SHR
                    Token shrToken = new Token(Symbol.SHR);
                    List<Token> labels = inst0.getLabels();
                    String argStr = Integer.toString(shiftAmount);
                    Token argToken = new Token(Symbol.intLiteral, argStr);
                    Instruction shrInst = new InstructionSHR(labels, shrToken, argToken);
                    instructions.set(instNum, shrInst);
                    inst0 = shrInst;
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
