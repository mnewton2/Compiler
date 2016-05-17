package edu.citadel.cprl;


import edu.citadel.compiler.Position;
import edu.citadel.compiler.AbstractToken;


public class Token extends AbstractToken<Symbol>
  {
    /**
     * Constructs a new Token with symbol = Symbol.unknown.
     * Position and text are initialized to null.
     */
    public Token()
      {
        super(Symbol.unknown, null, null);
      }


    /**
     * Constructs a new Token with the given symbol, position, and text.
     */
    public Token(Symbol symbol, Position position, String text)
      {
        super(symbol, position, text);
      }
  }
