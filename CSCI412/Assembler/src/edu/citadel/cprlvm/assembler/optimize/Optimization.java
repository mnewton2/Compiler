package edu.citadel.cprlvm.assembler.optimize;


import edu.citadel.cprlvm.assembler.ast.Instruction;

import java.util.List;


/**
 * Perform peephole optimization on the list of instructions starting with
 * the instruction at the specified position in the list and looking at
 * that instruction plus the next several instructions.  Any class that
 * implements this interface can remove/replace instructions in the list. 
 */
public interface Optimization
  {
    public void optimize(List<Instruction> instructions, int instNum);
  }
