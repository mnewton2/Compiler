package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.Position;
import edu.citadel.cprl.Type;

import java.io.IOException;


/**
 * The abstract syntax tree node for a read statement.
 */
public class ReadStmt extends Statement
  {
    Variable var;


    /**
     * Construct an input statement with the specified
     * variable for storing the input.
     */
    public ReadStmt(Variable var)
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
