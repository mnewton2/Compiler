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
 * The abstract syntax tree node for an adding expression.  An adding expression
 * is a binary expression where the operator is an adding operator, "+" or "-".
 * A simple example would be "x + 5".
 */
public class AddingExpr extends BinaryExpr
  {
    /**
     * Construct an adding expression with the operator ("+" or "-")
     * and the two operands.
     */
    public AddingExpr(Expression leftOperand, Token operator, Expression rightOperand)
      {
        super(leftOperand, operator, rightOperand);
        
        InternalAssertion.check(SymbolUtil.isAddingOperator(operator.getSymbol()),
            "AddingExpr: operator is not an adding operator.");
      }


    @Override
    public void checkConstraints()
      {
        try
          {
            Expression leftOperand  = getLeftOperand();
            Expression rightOperand = getRightOperand();

            leftOperand.checkConstraints();
            rightOperand.checkConstraints();

            // can add/subtract only integers

            if (leftOperand.getType() != Type.Integer)
              {
                Position errorPosition = leftOperand.getPosition();
                String   errorMessage  = "Left operand for expression should have type Integer.";

                throw new ConstraintException(errorPosition, errorMessage);
              }

            if (rightOperand.getType() != Type.Integer)
              {
                Position errorPosition = rightOperand.getPosition();
                String   errorMessage  = "Right operand for expression should have type Integer.";

                throw new ConstraintException(errorPosition, errorMessage);
              }
          }
        catch (ConstraintException ex)
          {
            ErrorHandler.getInstance().reportError(ex);
          }

        setType(Type.Integer);
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
        Expression leftOperand  = getLeftOperand();
        Expression rightOperand = getRightOperand();
        Symbol     operatorSym  = getOperator().getSymbol();

        leftOperand.emit();
        rightOperand.emit();

        InternalAssertion.check(operatorSym == Symbol.plus || operatorSym == Symbol.minus,
            "Invalid adding operator symbol: " + operatorSym);

        if (operatorSym == Symbol.plus)
            emit("ADD");
        else if (operatorSym == Symbol.minus)
            emit("SUB");
      }
  }
