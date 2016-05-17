package edu.citadel.cprl;


/**
 * A utility class for working with CPRL symbols.
 */
public class SymbolUtil
  {
    /**
     * Returns true if the symbol can start an initial declaration.
     */
    public static boolean isInitialDeclStarter(Symbol s)
      {
        return s == Symbol.constRW
            || s == Symbol.varRW
            || s == Symbol.typeRW;
      }


    /**
     * Returns true if the symbol can start a subprogram declaration.
     */
    public static boolean isSubprogramDeclStarter(Symbol s)
      {
        return s == Symbol.procedureRW
            || s == Symbol.functionRW;
      }


    /**
     * Returns true if the symbol can start a statement.
     */
    public static boolean isStmtStarter(Symbol s)
      {
        return s == Symbol.exitRW
            || s == Symbol.identifier
            || s == Symbol.ifRW
            || s == Symbol.loopRW
            || s == Symbol.whileRW
            || s == Symbol.readRW
            || s == Symbol.writeRW
            || s == Symbol.writelnRW
            || s == Symbol.returnRW;
      }


    /**
     * Returns true if the symbol is a logical operator.
     */
    public static boolean isLogicalOperator(Symbol s)
      {
        return s == Symbol.andRW
            || s == Symbol.orRW;
      }


    /**
     * Returns true if the symbol is a relational operator.
     */
    public static boolean isRelationalOperator(Symbol s)
      {
        return s.ordinal() >= Symbol.equals.ordinal()
            && s.ordinal() <= Symbol.greaterOrEqual.ordinal();
      }


    /**
     * Returns true if the symbol is an adding operator.
     */
    public static boolean isAddingOperator(Symbol s)
      {
        return s == Symbol.plus
            || s == Symbol.minus;
      }


    /**
     * Returns true if the symbol is a multiplying operator.
     */
    public static boolean isMultiplyingOperator(Symbol s)
      {
        return s == Symbol.times
            || s == Symbol.divide
            || s == Symbol.modRW;
      }


    /**
     * Returns true if the symbol is a literal.
     */
    public static boolean isLiteral(Symbol s)
      {
        return s == Symbol.intLiteral
            || s == Symbol.charLiteral
            || s == Symbol.stringLiteral
            || s == Symbol.trueRW
            || s == Symbol.falseRW;
      }


    /**
     * Returns true if the symbol can start an expression.
     */
    public static boolean isExprStarter(Symbol s)
      {
        return isLiteral(s)
            || s == Symbol.identifier
            || s == Symbol.leftParen
            || s == Symbol.plus
            || s == Symbol.minus
            || s == Symbol.notRW;
      }
  }
