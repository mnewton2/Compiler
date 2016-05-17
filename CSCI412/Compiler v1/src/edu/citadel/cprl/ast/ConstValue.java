package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.Position;
import edu.citadel.cprl.Symbol;
import edu.citadel.cprl.Token;
import edu.citadel.cprl.Type;
import edu.citadel.cprl.TypeUtil;

import java.io.IOException;


/**
 * The abstract syntax tree node for a constant value expression, which is
 * either a literal or a declared constant identifier representing a literal.
 */
public class ConstValue extends Expression
  {
    /**
     * The literal token for the constant.  If the const value is an
     * identifier, then the literal is the one associated with the
     * identifier in its declaration.
     */
    Token literal;


    /**
     * Construct a ConstValue from a literal token.
     */
    public ConstValue(Token literal)
      {
        super(TypeUtil.getTypeOf(literal.getSymbol()), literal.getPosition());

        this.literal = literal;
      }


    /**
     * Construct a ConstValue from a constant identifier
     * token and its corresponding constant declaration.
     */
    public ConstValue(Token identifier, ConstDecl decl)
      {
        super(decl.getType(), identifier.getPosition());

        this.literal = decl.getLiteral();
      }


    /**
     * Returns an integer value for the declaration literal.  For an integer
     * literal, this method simply returns its integer value.  For a char
     * literal, this method returns the underlying integer value for the
     * character.  For a boolean literal, this method returns 0 for false
     * and 1 for true.  For any other literal, the method returns 0.
     */
    public int getLiteralIntValue()
      {
        if (literal.getSymbol() == Symbol.intLiteral)
            return Integer.parseInt(literal.getText());
        else if (literal.getSymbol() == Symbol.trueRW)
            return 1;
        else if (literal.getSymbol() == Symbol.charLiteral)
          {
            char ch = literal.getText().charAt(1);
            return (int) ch;
          }
        else
            return 0;
      }


    @Override
    public void checkConstraints()
      {
        try
          {
            // check that an intLiteral can actually be converted to an integer
            if (literal.getSymbol() == Symbol.intLiteral)
              {
                try
                  {
                    Integer.parseInt(literal.getText());
                  }
                catch(NumberFormatException e1)
                  {
                    Position errorPosition = literal.getPosition();
                    String   errorMessage  = "The number \"" + literal.getText()
                        + "\" cannot be converted to an integer in CPRL.";

                    throw error(errorPosition, errorMessage);
                  }
              }
          }
        catch (ConstraintException e)
          {
            ErrorHandler.getInstance().reportError(e);
          }
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
        Type exprType = getType();

        if (exprType == Type.Integer)
            emit("LDCINT " + getLiteralIntValue());
        else if (exprType == Type.Boolean)
            emit("LOADCB " + getLiteralIntValue());
        else if (exprType == Type.Char)
            emit("LDCCH " + literal.getText());
        else if (exprType == Type.String)
            emit("LDCSTR " + literal.getText());
        else
          {
            String errorMsg = "Invalid type for constant value.";
            throw new CodeGenException(literal.getPosition(), errorMsg);
          }
      }
  }
