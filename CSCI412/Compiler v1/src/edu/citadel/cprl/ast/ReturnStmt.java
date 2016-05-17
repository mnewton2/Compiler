package edu.citadel.cprl.ast;


import java.io.IOException;

import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.Position;


/**
 * The abstract syntax tree node for a return statement.
 */
public class ReturnStmt extends Statement
  {
    Expression returnExpr; // may be null
    SubprogramDecl subprogramDecl;
    Position returnPositon;


    /**
     * Construct a return statement with a reference to the enclosing subprogram
     * and the expression for the value being returned, which may be null.
     * 
     * @param returnPosition
     */
    public ReturnStmt(SubprogramDecl subprogramDecl, Expression returnExpr,
        Position returnPosition)
      {
        this.subprogramDecl = subprogramDecl;
        this.returnExpr = returnExpr;
        this.returnPositon = returnPosition;
      }


    /**
     * Returns the expression associated with this return statement. Returns
     * null if no expression has been associated with this return statement.
     */
    public Expression getReturnExpr()
      {
        return returnExpr;
      }


    @Override
    public void checkConstraints()
      {
        try
          {

            if (returnExpr != null)
              {
                returnExpr.checkConstraints();

                if (!(subprogramDecl instanceof FunctionDecl))
                  {
                    Position errorPosition = subprogramDecl.getPosition();
                    String errorMessage = "Procedures can not contain return expression.";

                    throw new ConstraintException(errorPosition, errorMessage);

                  }
                FunctionDecl funcDecl = (FunctionDecl) subprogramDecl;

                /*
                 * if the statement returns a value for a function the
                 * expression must match that of the function return type
                 */

                if (!matchTypes(funcDecl.getType(), getReturnExpr().getType()))
                  {
                    Position errorPosition = returnExpr.getPosition();
                    String errorMessage = "Value being returned must match that of the function return type.";

                    throw new ConstraintException(errorPosition, errorMessage);
                  }
              }
            else
              {
                if (subprogramDecl instanceof FunctionDecl)
                  {
                    Position errorPosition = subprogramDecl.getPosition();
                    String errorMessage = "A return statement nested within a function must return a value.";
                    throw new ConstraintException(errorPosition, errorMessage);
                  }
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

        if (subprogramDecl instanceof FunctionDecl)
          {
            // load address on the function
            FunctionDecl funcDecl = (FunctionDecl) subprogramDecl;
            emit("LDADDR " + funcDecl.getRelAddr());
          }

        if (returnExpr != null)
          returnExpr.emit();

        emitStoreInst(subprogramDecl.getType());

        emit("RET " + subprogramDecl.getParamLength());
      }
  }
