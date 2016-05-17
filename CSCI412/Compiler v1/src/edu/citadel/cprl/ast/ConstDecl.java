package edu.citadel.cprl.ast;


import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.Position;
import edu.citadel.cprl.Symbol;
import edu.citadel.cprl.Token;
import edu.citadel.cprl.Type;


/**
 * The abstract syntax tree node for a constant declaration.
 */
public class ConstDecl extends InitialDecl
  {
    private Token literal;


    /**
     * Construct a ConstDecl with its identifier, type, and literal.
     */
    public ConstDecl(Token identifier, Type constType, Token literal)
      {
        super(identifier, constType);
        this.literal = literal;
      }


    public Token getLiteral()
      {
        return literal;
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
                catch (NumberFormatException e1)
                  {
                    Position errorPosition = literal.getPosition();
                    String errorMessage = "The number \"" + literal.getText()
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
  }
