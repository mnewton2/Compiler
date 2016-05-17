package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.InternalAssertion;
import edu.citadel.compiler.Position;
import edu.citadel.cprl.Symbol;
import edu.citadel.cprl.SymbolUtil;
import edu.citadel.cprl.Token;
import edu.citadel.cprl.Type;

import java.io.IOException;


/**
 * The abstract syntax tree node for a multiplying expression.  A multiplying
 * expression is a binary expression where the operator is a multiplying operator
 * such as "*", "/", or "mod".  A simple example would be "5*x".
 */
public class MultiplyingExpr extends BinaryExpr
  {
    /**
     * Construct a multiplying expression with the operator ("*", "/", or "mod")
     * and the two operands.
     */
    public MultiplyingExpr(Expression leftOperand, Token operator, Expression rightOperand)
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
