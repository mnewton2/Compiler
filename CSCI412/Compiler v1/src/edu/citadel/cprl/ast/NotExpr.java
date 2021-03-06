package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.InternalAssertion;
import edu.citadel.compiler.Position;
import edu.citadel.cprl.Symbol;
import edu.citadel.cprl.Token;
import edu.citadel.cprl.Type;

import java.io.IOException;


/**
 * The abstract syntax tree node for a not expression. A not expression is a
 * unary expression of the form "not expr". A simple example would be
 * "not isEmtpy()".
 */
public class NotExpr extends UnaryExpr
  {
    public NotExpr(Token operator, Expression operand)
      {
        super(operator, operand);

        InternalAssertion.check(operator.getSymbol() == Symbol.notRW,
            "NotExpr: operator is not the reserved word \"not\".");
      }


    @Override
    public void checkConstraints()
      {
        try
          {
            Expression operand = getOperand();
            operand.checkConstraints();

            // the operand for a not expression must have type boolean
            if (operand.getType() != Type.Boolean)
              {
                Position errorPosition = operand.getPosition();
                String errorMessage = "Expression following \"not\" operator"
                    + " is not a Boolean expression.";

                throw new ConstraintException(errorPosition, errorMessage);
              }

          }
        catch (ConstraintException e)
          {
            ErrorHandler.getInstance().reportError(e);
          }
        // the result has type boolean
        setType(Type.Boolean);
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
        Expression operand = getOperand();
        operand.emit();

        emit("NOT");
      }
  }
