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
 * The abstract syntax tree node for a multiplying expression. A multiplying
 * expression is a binary expression where the operator is a multiplying
 * operator such as "*", "/", or "mod". A simple example would be "5*x".
 */

public class MultiplyingExpr extends BinaryExpr
  {
    /**
     * Construct a multiplying expression with the operator ("*", "/", or "mod")
     * and the two operands.
     */
    public MultiplyingExpr(Expression leftOperand, Token operator,
        Expression rightOperand)
      {
        super(leftOperand, operator, rightOperand);
      }


    @Override
    public void checkConstraints()
      {
        try
          {
            Expression leftOperand = getLeftOperand();
            Expression rightOperand = getRightOperand();

            leftOperand.checkConstraints();
            rightOperand.checkConstraints();

            // both operands must have type Integer
            if (leftOperand.getType() != Type.Integer)
              {
                Position errorPosition = leftOperand.getPosition();
                String errorMessage = "Left operand for expression should have type Integer.";

                throw new ConstraintException(errorPosition, errorMessage);
              }

            if (rightOperand.getType() != Type.Integer)
              {
                Position errorPosition = rightOperand.getPosition();
                String errorMessage = "Right operand for expression should have type Integer.";

                throw new ConstraintException(errorPosition, errorMessage);
              }
          }
        catch (ConstraintException e)
          {
            ErrorHandler.getInstance().reportError(e);
          }

        // Result must be Integer type
        setType(Type.Integer);
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
        Expression leftOperand = getLeftOperand();
        Expression rightOperand = getRightOperand();
        Symbol operatorSym = getOperator().getSymbol();

        leftOperand.emit();
        rightOperand.emit();

        InternalAssertion.check(SymbolUtil.isMultiplyingOperator(operatorSym),
            "Invalid operator for a multiplying expression.");

        if (operatorSym == Symbol.times)
          emit("MUL");
        else if (operatorSym == Symbol.divide)
          emit("DIV");
        else if (operatorSym == Symbol.modRW)
          emit("MOD");
      }
  }
