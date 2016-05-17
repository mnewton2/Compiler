package edu.citadel.cprl;


/**
 * A utility class for working with CPRL types.
 */
public class TypeUtil
  {
    /**
     * Returns the type of a literal symbol.  For example, if the
     * symbol is an intLiteral, then Type.Integer is returned.
     *
     * @throws IllegalArgumentException if the symbol is not a literal
     */
    public static Type getTypeOf(Symbol literal)
      {
        if (!SymbolUtil.isLiteral(literal))
            throw new IllegalArgumentException("Symbol is not a literal symbol");

        if (literal == Symbol.intLiteral)
            return Type.Integer;
        else if (literal == Symbol.stringLiteral)
            return Type.String;
        else if (literal == Symbol.charLiteral)
            return Type.Char;
        else if (literal == Symbol.trueRW || literal == Symbol.falseRW)
            return Type.Boolean;
        else
            return Type.UNKNOWN;
      }
  }
