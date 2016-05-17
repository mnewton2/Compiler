package edu.citadel.cprl.ast;


import java.io.IOException;

import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.Position;
import edu.citadel.cprl.Token;
import edu.citadel.cprl.Type;


/**
 * The abstract syntax tree node for a negation expression. A negation
 * expression is a unary expression where the operator is either "-" or "+". A
 * simple example would be "-x".
 */
public class NegationExpr extends UnaryExpr
  {
    /**
     * Construct a negation expression with the specified operator ("-") and
     * operand.
     */
    public NegationExpr(Token operator, Expression operand)
      {
        super(operator, operand);
      }


    @Override
    public void checkConstraints()
      {
        try
          {
            Expression operand = getOperand();
            operand.checkConstraints();

            // the operand must have type Integer
            if (operand.getType() != Type.Integer)
              {
                Position errorPosition = operand.getPosition();
                String errorMessage = "The operand must have type Integer.";

                throw new ConstraintException(errorPosition, errorMessage);
              }
          }
        catch (ConstraintException e)
          {
            ErrorHandler.getInstance().reportError(e);
          }
        // the result has type integer
        setType(Type.Integer);
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
        Expression operand = getOperand();
        operand.emit();

        emit("NEG");
      }
  }
