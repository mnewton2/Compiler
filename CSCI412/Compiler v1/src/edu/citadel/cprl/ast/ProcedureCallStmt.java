package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.cprl.Token;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.IOException;


/**
 * The abstract syntax tree node for a procedure call statement.
 */
public class ProcedureCallStmt extends Statement
  {
    Token procId;
    List<Expression> actualParams;
    ProcedureDecl procDecl;
    List<ParameterDecl> formalParams;


    /*
     * Construct a procedure call statement with the token for the name of the
     * procedure, the list of actual parameters being passed as part of the
     * call, and a reference to the declaration of the procedure being called.
     */
    public ProcedureCallStmt(Token procId, List<Expression> actualParams,
        ProcedureDecl procDecl)
      {
        this.procId = procId;
        this.actualParams = actualParams;
        this.procDecl = procDecl;
      }


    @Override
    public void checkConstraints()
      {
        try
          {
            formalParams = procDecl.getFormalParams();

            // check that numbers of parameters match
            if (actualParams.size() != formalParams.size())
              {
                throw error(procId.getPosition(),
                    "Incorrect number of actual parameters.");
              }

            // call checkConstraints for each actual parameter
            for (Expression expr : actualParams)
              expr.checkConstraints();


            Iterator<Expression> iterActual = actualParams.iterator();
            Iterator<ParameterDecl> iterFormal = formalParams.iterator();

            List<Expression> newActualParams = new LinkedList<Expression>();

            while (iterActual.hasNext())
              {
                // check types
                Expression expr = iterActual.next();
                ParameterDecl param = iterFormal.next();

                if (!matchTypes(expr.getType(), param.getType()))
                  throw error(expr.getPosition(), "Parameter type mismatch.");

                // check that named values are being passed for var parameters
                if (param.isVarParam())
                  {
                    if (expr instanceof NamedValue)
                      {
                        // convert named value to a variable
                        expr = new Variable((NamedValue) expr);
                      }
                    else
                      {
                        throw error(expr.getPosition(),
                            "Expression for a var parameter must be a variable.");
                      }
                  }
                newActualParams.add(expr);
              }
            actualParams = newActualParams;

          }
        catch (ConstraintException e)
          {
            ErrorHandler.getInstance().reportError(e);
          }
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
        // emit code for actual parameters
        for (Expression expr : actualParams)
          expr.emit();

        emit("CALL " + procDecl.getSubprogramLabel());
      }
  }
