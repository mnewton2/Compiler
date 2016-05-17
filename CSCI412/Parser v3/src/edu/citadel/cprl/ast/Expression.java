package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.Position;
import edu.citadel.compiler.InternalAssertion;
import edu.citadel.cprl.Type;

import java.io.IOException;


/**
 * Base class for all CPRL expressions.
 */
public abstract class Expression extends AST
  {
    /** constant for false */
    public static final String FALSE = "0";

    /** constant for true */
    public static final String TRUE = "1";

    private Type     exprType;
    private Position exprPosition;   // position of the start of the expression


    /**
     * Default constructor.  Initializes the type of the expression
     * to UNKNOWN and the position to the default position.
     */
    public Expression()
      {
        this.exprType = Type.UNKNOWN;
        this.exprPosition = new Position();
      }


    /**
     * Construct an expression with the specified type and position.
     */
    public Expression(Type exprType, Position exprPosition)
      {
        this.exprType     = exprType;
        this.exprPosition = exprPosition;
      }


    /**
     * Construct an expression with the specified position.  Initializes
     * the type of the expression to UNKNOWN.
     */
    public Expression(Position exprPosition)
      {
        this.exprType = Type.UNKNOWN;
        this.exprPosition = exprPosition;
      }


    /**
     * Returns the type of this expression.
     */
    public Type getType()
      {
        return exprType;
      }


    /**
     * Sets the type of this expression.
     */
    public void setType(Type exprType)
      {
        this.exprType = exprType;
      }


    /**
     * Returns the position of this expression.
     */
    public Position getPosition()
      {
        return exprPosition;
      }


    /**
     * Sets the position of this expression.
     */
    public void setPosition(Position exprPosition)
      {
        this.exprPosition = exprPosition;
      }


    /**
     * For Boolean expressions, the method emits the appropriate branch opcode
     * based on the condition.  For example, if the expression is a "&lt;"
     * relational expression and the condition is false, then the opcode "BGE"
     * is emitted.  The default behavior will throw an InternalCompilerException
     * if the type of the expression is not Boolean.  The method defined in this
     * class works correctly for Boolean constants, Boolean named values, and
     * "not" expressions.  It should be overridden for other Boolean expression
     * ASTs (e.g., RelationalExpr).
     * 
     * @param condition  the condition that determines the branch to be emitted.
     * @param label      the label for the branch destination.
     * 
     * @throws InternalCompilerException  if the type of the expression is not Boolean.
     * @throws IOException                if there is a problem writing to the target file.
     * @throws CodeGenException           if the method is unable to generate appropriate
     *                                    target code. 
     */
    public void emitBranch(boolean condition, String label) throws CodeGenException, IOException
      {
        // default behavior unless overridden; correct for constants and named values
        checkBooleanType();
        emit();  // leaves boolean value on top of stack
        emit(condition ? "BNZ " + label : "BZ " + label);
      }


    /**
     * Throws an InternalCompilerException if the type of the expression is not Boolean.
     */
    protected void checkBooleanType()
      {
        String errorMsg = "Expression type is not Boolean";
        InternalAssertion.check(exprType == Type.Boolean, errorMsg);
      }
  }
