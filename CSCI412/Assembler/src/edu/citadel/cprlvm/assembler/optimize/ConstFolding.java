package edu.citadel.cprlvm.assembler.optimize;


import edu.citadel.cprlvm.assembler.Symbol;
import edu.citadel.cprlvm.assembler.Token;
import edu.citadel.cprlvm.assembler.ast.*;

import java.util.List;
import java.util.LinkedList;


/**
 * Replaces runtime arithmetic on constants with compile-time arithmetic. 
 */
public class ConstFolding implements Optimization
  {
    @Override
    public void optimize(List<Instruction> instructions, int instNum)
      {
        // quick check that there are at least three instructions remaining
        if (instNum > instructions.size() - 3)
            return;

        Instruction inst0 = instructions.get(instNum);
        Instruction inst1 = instructions.get(instNum + 1);
        Instruction inst2 = instructions.get(instNum + 2);
        
        // quick check that we are dealing with two constants
        Symbol symbol0 = inst0.getOpCode().getSymbol();
        Symbol symbol1 = inst1.getOpCode().getSymbol();
        Symbol symbol2 = inst2.getOpCode().getSymbol();

        if (!OptimizationUtil.isTwoConstInts(symbol0, symbol1))
            return;

        // we are dealing with two constant integers
        int value0 = inst0.argToInt();
        int value1 = inst1.argToInt();

        switch (symbol2)
          {
            case ADD:
                int sum = value0 + value1;
                inst0.getArg().setText(Integer.toString(sum));
                break;
            case SUB:
                int difference = value0 - value1;
                inst0.getArg().setText(Integer.toString(difference));
                break;
            case MUL:
                int product = value0*value1;
                inst0.getArg().setText(Integer.toString(product));
                break;
            case DIV:
                int quotient = value0/value1;
                inst0.getArg().setText(Integer.toString(quotient));
                break;
            case MOD:
                int remainder = value0%value1;
                inst0.getArg().setText(Integer.toString(remainder));
                break;
            default:
                return;
          }

        // copy labels from inst1 and inst2 to inst0 before removing them
        List<Token> inst1Labels = inst1.getLabels();
        List<Token> inst2Labels = inst2.getLabels();

        if (inst1Labels != null || inst2Labels != null)
          {
            List<Token> inst0Labels = inst0.getLabels();
            if (inst0Labels == null)
                inst0Labels = new LinkedList<>();

            if (inst1Labels != null)
                inst0Labels.addAll(inst1Labels);

            if (inst2Labels != null)
                inst0Labels.addAll(inst2Labels);
          }

        // modify the list of instructions to reflect the optimization
        instructions.remove(instNum + 1);
        instructions.remove(instNum + 1);
      }
  }
