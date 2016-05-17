package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.InternalAssertion;

import java.util.List;
import java.io.IOException;


/**
 * The abstract syntax tree node for a write statement.
 */
public class WriteStmt extends OutputStmt
  {
    public WriteStmt(List<Expression> expressions)
      {
        super(expressions);
      }


    @Override
    public void checkConstraints()
      {
        super.checkConstraints();

        InternalAssertion.check(getExpressions().size() > 0,
            "A \"write\" statement must have an expression.");
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
        super.emit();
      }
  }
