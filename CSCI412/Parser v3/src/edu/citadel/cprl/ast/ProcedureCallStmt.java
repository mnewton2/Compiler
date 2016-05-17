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


    /*
     * Construct a procedure call statement with the token for the name of the
     * procedure, the list of actual parameters being passed as part of the call,
     * and a reference to the declaration of the procedure being called.
     */
    public ProcedureCallStmt(Token procId,
                             List<Expression> actualParams,
                             ProcedureDecl procDecl)
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
