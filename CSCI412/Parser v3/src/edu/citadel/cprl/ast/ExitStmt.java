package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.InternalAssertion;
import edu.citadel.compiler.Position;
import edu.citadel.cprl.Type;

import java.io.IOException;


/**
 * The abstract syntax tree node for an exit statement.
 */
public class ExitStmt extends Statement
  {
    private Expression whenExpr;
    private LoopStmt   loopStmt;


    /**
     * Construct an exit statement with its optional "when"
     * expression (which should be null if there is no "when"
     * expression) and a reference to the enclosing loop statement.
     */
    public ExitStmt(Expression whenExpr, LoopStmt loopStmt)
      {
// ...
      }


    @Override
    public void checkConstraints()
      {
// ...
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
// ...
      }
  }
