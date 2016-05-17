package save;

import edu.citadel.cprlvm.assembler.Symbol;


public class SymbolUtil
  {
    // arg = intLiteral | labelId | strLiteral | identifier .
    public static boolean isArgType(Symbol s)
      {
        return s == Symbol.intLiteral
            || s == Symbol.labelId
            || s == Symbol.stringLiteral
            || s == Symbol.charLiteral
            || s == Symbol.identifier;
      }
  }
