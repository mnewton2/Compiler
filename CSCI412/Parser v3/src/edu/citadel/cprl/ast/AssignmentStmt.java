package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.Position;

import java.io.IOException;


/**
 * The abstract syntax tree node for an assignment statement.
 */
public class AssignmentStmt extends Statement
  {
    private Variable   var;
    private Expression expr;

    // position of the assignment operator (needed for error reporting)
    private Position stmtPosition;


    public AssignmentStmt(Variable var, Expression expr, Position stmtPosition)
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
