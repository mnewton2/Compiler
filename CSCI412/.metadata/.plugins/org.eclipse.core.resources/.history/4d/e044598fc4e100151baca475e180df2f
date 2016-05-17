package edu.citadel.compiler;


/**
 * This class encapsulates the properties of a language token.  A token consist
 * of a Symbol (a.k.a., the Token type), a Position, and a String that contains
 * the text of the token.
 */
public abstract class AbstractToken<Symbol extends Enum<Symbol>> implements Cloneable
  {
    private Symbol   symbol;
    private Position position;
    private String   text;


    /**
     * Constructs a new Token with the given symbol, position, and text.
     */
    public AbstractToken(Symbol symbol, Position position, String text)
      {
        this.symbol   = symbol;
        this.position = position;
        this.text     = text;
      }


    @Override
    public Object clone()
      {
        Object token = null;

        try
          {
            token = super.clone();
          }
        catch (CloneNotSupportedException ex)
          {
            return null;   // will never happen
          }

        return token;
      }


    /**
     * Returns the token's symbol.
     */
    public Symbol getSymbol()
      {
        return symbol;
      }


    /**
     * Set the token's symbol.
     */
    public void setSymbol(Symbol symbol)
      {
        this.symbol = symbol;
      }


    /**
     * Returns the token's position within the source file.
     */
    public Position getPosition()
      {
        return position;
      }


    /**
     * Set the token's position within the source file.
     */
    public void setPosition(Position position)
      {
        this.position = position;
      }


    /**
     * Returns the string representation for the token.
     */
    public String getText()
      {
        return text != null ? text : symbol.toString();
      }


    /**
     * Set the string representation for the token.
     */
    public void setText(String text)
      {
        this.text = text;
      }


    @Override
    public String toString()
      {
        return getText();
      }
  }
