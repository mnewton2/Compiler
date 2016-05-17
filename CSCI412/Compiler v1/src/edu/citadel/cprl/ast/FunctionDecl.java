package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.cprl.Token;

import java.util.*;
import java.io.IOException;


/**
 * The abstract syntax tree node for a function declaration.
 */
public class FunctionDecl extends SubprogramDecl
  {
    /**
     * Construct a function declaration with its name (an identifier).
     */
    public FunctionDecl(Token funcId)
      {
        super(funcId);
      }


    /**
     * Computes the relative address of the function return value. <br>
     * Note: This method assumes that the relative addresses of all formal
     * parameters have been set.
     */
    public int getRelAddr()
      {
        int firstParamAddr = 0;

        List<ParameterDecl> params = getFormalParams();
        if (params.size() > 0)
          {
            ParameterDecl firstParamDecl = params.get(0);
            firstParamAddr = firstParamDecl.getRelAddr();
          }

        return firstParamAddr - getType().getSize();
      }


    @Override
    public void checkConstraints()
      {
        try
          {
            List<ParameterDecl> formalParams = getFormalParams();
            List<Statement> stmts = getStatementPart().getStatements();

            // should be no var parameters
            for (ParameterDecl paramDecl : formalParams)
              {
                if (paramDecl.isVarParam())
                  {
                    String errorMessage = "A function cannot have var parameters.";
                    throw new ConstraintException(paramDecl.getPosition(), errorMessage);
                  }
              }

            // should be at least one return statement
            if (!hasReturnStmt(stmts))
              {
                String errorMessage = "A CPRL function must contain at least one return statement.";
                throw new ConstraintException(getPosition(), errorMessage);
              }
            super.checkConstraints();

          }
        catch (ConstraintException e)
          {
            ErrorHandler.getInstance().reportError(e);
          }
      }


    /**
     * Returns true if the specified list of statements contains at least one
     * return statement. Also reports an error for any return statement that
     * does not return a value.
     *
     * @param statements
     *          the list of statements to check for a return statement. If any
     *          of the statements in the list contains nested statements (e.g.,
     *          an if statement or a loop statement), then the nested statements
     *          are also checked for a return statement.
     */
    private boolean hasReturnStmt(List<Statement> statements)
      {
        for (Statement stmt : statements)
          {
            if (stmt instanceof ReturnStmt)
              return true;
            else
              {
                if (stmt instanceof IfStmt)
                  {
                    IfStmt ifstmt = (IfStmt) stmt;

                    // check then stmts
                    if (hasReturnStmt(ifstmt.getThenStmts()))
                      return true;

                    // check elsif stmts
                    for (ElsifPart elsif : ifstmt.getElsifParts())
                      {
                        statements = elsif.getThenStmts();
                        if (hasReturnStmt(statements))
                          return true;
                      }

                    // check else stmts
                    if (hasReturnStmt(ifstmt.getElseStmts()))
                      return true;

                  }
                else if (stmt instanceof LoopStmt)
                  {
                    LoopStmt loop = (LoopStmt) stmt;
                    statements = loop.getStatements();
                    if (hasReturnStmt(statements))
                      return true;
                  }
              }
          }

        return false;
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
        // get relative addr of function decl
        emitLabel(getSubprogramLabel());

        emit("PROC " + getVarLength());

        getStatementPart().emit();

      }
  }
