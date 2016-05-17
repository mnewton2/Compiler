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
 * The abstract syntax tree node for a relational expression. A relational
 * expression is a binary expression where the operator is a relational operator
 * such as "&lt;=" or "&gt;". A simple example would be "x &lt; 5".
 */
public class RelationalExpr extends BinaryExpr
  {
    // labels used during code generation
    private String L1; // label at start of right operand
    private String L2; // label at end of the relational expression


    /**
     * Construct a relational expression with the operator ("=", "&lt;=", etc.)
     * and the two operands.
     */
    public RelationalExpr(Expression leftOperand, Token operator, Expression rightOperand)
      {
        super(leftOperand, operator, rightOperand);

        InternalAssertion.check(SymbolUtil.isRelationalOperator(operator.getSymbol()),
            "RelationalExpr: operator is not a relational operator");

        L1 = getLabel();
        L2 = getLabel();
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


            // only types Integer, Char, or Boolean are allowed for operands
            if (leftOperand.getType() != Type.Boolean)
              {
                if (leftOperand.getType() != Type.Integer)
                  {
                    if (leftOperand.getType() != Type.Char)
                      {

                        String errorMessage = "Invalid type for relational expression.";
                        Position errorPosition = leftOperand.getPosition();

                        throw new ConstraintException(errorPosition, errorMessage);
                      }
                  }
              }
            if (rightOperand.getType() != Type.Boolean)
              {
                if (rightOperand.getType() != Type.Integer)
                  {
                    if (rightOperand.getType() != Type.Char)
                      {

                        String errorMessage = "Invalid type for relational expression.";
                        Position errorPosition = rightOperand.getPosition();

                        throw new ConstraintException(errorPosition, errorMessage);
                      }
                  }
              }

            // both operands must have the same type
            if (!matchTypes(rightOperand.getType(), leftOperand.getType()))
              {
                Position errorPosition = getPosition();
                String errorMessage = "Both operands in a relational expression must have the same type.";

                throw new ConstraintException(errorPosition, errorMessage);
              }

          }
        catch (ConstraintException e)
          {
            ErrorHandler.getInstance().reportError(e);
          }
        // result has type boolean
        setType(Type.Boolean);
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
        emitBranch(false, L1);

        // emit true
        emit("LOADCB " + TRUE);

        // jump over code to emit false
        emit("BR " + L2);

        // L1:
        emitLabel(L1);

        // emit false
        emit("LOADCB " + FALSE);

        // L2:
        emitLabel(L2);
      }


    @Override
    public void emitBranch(boolean condition, String label)
        throws CodeGenException, IOException
      {
        Token operator = getOperator();

        emitOperands();
        emit("CMP");

        Symbol operatorSym = operator.getSymbol();

        if (operatorSym == Symbol.equals)
          emit(condition ? "BZ " + label : "BNZ " + label);
        else if (operatorSym == Symbol.notEqual)
          emit(condition ? "BNZ " + label : "BZ " + label);
        else if (operatorSym == Symbol.lessThan)
          emit(condition ? "BL " + label : "BGE " + label);
        else if (operatorSym == Symbol.lessOrEqual)
          emit(condition ? "BLE " + label : "BG " + label);
        else if (operatorSym == Symbol.greaterThan)
          emit(condition ? "BG " + label : "BLE " + label);
        else if (operatorSym == Symbol.greaterOrEqual)
          emit(condition ? "BGE " + label : "BL " + label);
        else
          throw new CodeGenException(operator.getPosition(),
              "Invalid relational operator.");
      }


    private void emitOperands() throws CodeGenException, IOException
      {
        Expression leftOperand = getLeftOperand();
        Expression rightOperand = getRightOperand();

        // Relational operators compare integers only, so we need to make sure
        // that we have enough bytes on the stack. Pad with zero bytes.
        int leftOperandSize = leftOperand.getType().getSize();
        for (int n = 1; n <= (Type.Integer.getSize() - leftOperandSize); ++n)
          emit("LOADCB " + 0);

        leftOperand.emit();

        int rightOperandSize = rightOperand.getType().getSize();
        for (int n = 1; n <= (Type.Integer.getSize() - rightOperandSize); ++n)
          emit("LOADCB " + 0);

        rightOperand.emit();
      }
  }
