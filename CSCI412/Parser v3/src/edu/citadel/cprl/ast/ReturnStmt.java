package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.InternalAssertion;
import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.Position;

import java.io.IOException;


/**
 * The abstract syntax tree node for a return statement.
 */
public class ReturnStmt extends Statement
  {
    Expression     returnExpr;    // may be null
    SubprogramDecl subprogramDecl;

    // position of the return token (needed for error reporting)
    private Position returnPosition;


    /**
     * Construct a return statement with a reference to the enclosing subprogram
     * and the expression for the value being returned, which may be null.
     */
    public ReturnStmt(SubprogramDecl subprogramDecl, Expression returnExpr, Position returnPosition)
      {
// ...
      }


    /**
     * Returns the expression associated with this return statement.  Returns
     * null if no expression has been associated with this return statement.
     */
    public Expression getReturnExpr()
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
