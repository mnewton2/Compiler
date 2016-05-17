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
    private Variable var;
    private Expression expr;

    // position of the assignment operator (needed for error reporting)
    private Position stmtPosition;


    public AssignmentStmt(Variable var, Expression expr, Position stmtPosition)
      {
        this.var = var;
        this.expr = expr;
        this.stmtPosition = stmtPosition;
      }


    @Override
    public void checkConstraints()
      {
        try
          {
            expr.checkConstraints();
            var.checkConstraints();

            if (!matchTypes(var.getType(), expr.getType()))
              {
                Position errorPosition = stmtPosition;
                String errorMessage = "Type mismatch for assignment statement.";
                throw new ConstraintException(errorPosition, errorMessage);
              }
          }
        catch (ConstraintException e)
          {
            ErrorHandler.getInstance().reportError(e);
          }
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
        var.emit();
        expr.emit();

        emitStoreInst(expr.getType());
      }
  }
