package edu.citadel.cprlvm.assembler.optimize;


import edu.citadel.cprlvm.assembler.Symbol;
import edu.citadel.cprlvm.assembler.Token;
import edu.citadel.cprlvm.assembler.ast.Instruction;
import edu.citadel.cprlvm.assembler.ast.InstructionBZ;
import edu.citadel.cprlvm.assembler.ast.InstructionBNZ;
import edu.citadel.cprlvm.assembler.ast.InstructionBG;
import edu.citadel.cprlvm.assembler.ast.InstructionBGE;
import edu.citadel.cprlvm.assembler.ast.InstructionBL;
import edu.citadel.cprlvm.assembler.ast.InstructionBLE;

import edu.citadel.compiler.InternalCompilerException;

import java.util.List;



/**
 * Improves branching instructions where a nonconditional branch is used
 * to branch over a conditional branch (which can occur when an exit
 * statement appears at the end of a loop).  For example, the following code 
 * <code>
     BZ L1
     BR L0
  L1: ...
 * </code>
 * can be improved as follows: 
 * <code>
     BNZ L0
  L1: ...
 * </code>
 */
public class BranchingReduction implements Optimization
  {
    @Override
    public void optimize(List<Instruction> instructions, int instNum)
      {
        // quick check that there are at least 3 instructions remaining
        if (instNum > instructions.size() - 3)
            return;

        Instruction inst0 = instructions.get(instNum);
        Instruction inst1 = instructions.get(instNum + 1);
        Instruction inst2 = instructions.get(instNum + 2);
        
        Symbol symbol0 = inst0.getOpCode().getSymbol();
        Symbol symbol1 = inst1.getOpCode().getSymbol();
        
        // make sure that we have a conditional branch followed by BR
        // instruction, and that the label argument for the conditional
        // branch immediately follows the BR instruction.
        if (isConditionalBranch(symbol0) && symbol1 == Symbol.BR
            && containsLabel(inst2.getLabels(), inst0.getArg()))
          {
            // combine labels for instructions 0 and 1
            List<Token> labels = combineLabels(inst0.getLabels(), inst1.getLabels());
                
            // get argument label from inst1
            Token arg = inst1.getArg();

            // make the new branch instruction
            Instruction branchInst = makeDualBranchInst(labels, symbol0, arg);
            instructions.set(instNum, branchInst);
                
            // remove the unconditional branch instruction
            instructions.remove(instNum + 1);
          }
      }


    /**
     * Combines the lists of labels into a single list.
     */
    private static List<Token> combineLabels(List<Token> labels1, List<Token> labels2)
      {
        if (labels1 == null)
            return labels2;
        else 
          {
            labels1.addAll(labels2);
            return labels1;
          }
      }
    
    
    /**
     * Returns true if the symbol is a conditional branch; that is,
     * if the symbol is one of BNZ, BZ, BG, BGE, BL, or BLE.  
     */
    private static boolean isConditionalBranch(Symbol s)
      {
        return s == Symbol.BNZ
             | s == Symbol.BZ
             | s == Symbol.BG
             | s == Symbol.BGE
             | s == Symbol.BL
             | s == Symbol.BLE;
      }


    /**
     * Returns dual branch conditional branch instruction with the specified
     * instruction labels and label argument.  For example, if parameter s has
     * the value Symbol.BG, then an instruction of type InstructionBLE is returned.
     */
    private static Instruction makeDualBranchInst(List<Token> labels, Symbol s, Token labelArg)
      {
        if (s == Symbol.BNZ)
            return new InstructionBZ(labels, new Token(Symbol.BZ), labelArg);
        else if (s == Symbol.BZ)
            return new InstructionBNZ(labels,  new Token(Symbol.BNZ), labelArg);
        else if (s == Symbol.BG)
            return new InstructionBLE(labels,  new Token(Symbol.BLE), labelArg);
        else if (s == Symbol.BGE)
            return new InstructionBL(labels, new Token(Symbol.BL), labelArg);
        else if (s == Symbol.BL)
            return new InstructionBGE(labels,  new Token(Symbol.BGE), labelArg);
        else if (s == Symbol.BLE)
            return new InstructionBG(labels, new Token(Symbol.BG), labelArg);
        else
            throw new InternalCompilerException("");
      }


    /**
     * Returns true if the text of the second parameter label equals the
     * text of one of the labels in the list.  Returns false otherwise. 
     */
    private static boolean containsLabel(List<Token> labels, Token label)
      {
        String labelStr = label.getText() + ":";
        for (Token l : labels)
          {
            if (l.getText().equals(labelStr))
                return true;
          }

        return false;
      }
  }
