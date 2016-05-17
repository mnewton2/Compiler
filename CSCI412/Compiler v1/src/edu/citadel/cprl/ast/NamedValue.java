package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.Position;

import java.util.LinkedList;
import java.util.List;
import java.io.IOException;


/**
 * The abstract syntax tree node for a named value.  A named value is similar
 * to a variable except that it generates different code.  For example, consider
 * the assignment statement "x := y;"  The identifier "x" represents a variable,
 * and the identifier "y" represents a named value.  Code generation for "x"
 * would leave its address on the top of the stack, while code generation for
 * "y" would leave its value on the top of the stack.
 */
public class NamedValue extends Variable
  {
    /**
     * Construct a named value with a reference to its declaration,
     * its position, and a list of index expressions. 
     */
    public NamedValue(NamedDecl decl, Position position, List<Expression> indexExprs)
      {
        super(decl, position, indexExprs);
      }
    
    
    /**
     * Construct a named value with a reference to its declaration,
     * position, and an empty list of index expressions. 
     */
    public NamedValue(NamedDecl decl, Position position)
      {
        this(decl, position, new LinkedList<Expression>());
      }


    public void emit() throws CodeGenException, IOException
      {
        super.emit();    // leaves address on top of stack
        emitLoadInst(getType());
      }
  }

