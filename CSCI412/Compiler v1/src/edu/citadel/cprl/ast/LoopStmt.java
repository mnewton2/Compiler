package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.Position;
import edu.citadel.cprl.Type;

import java.util.LinkedList;
import java.util.List;
import java.io.IOException;


/**
 * The abstract syntax tree node for a loop statement.
 */
public class LoopStmt extends Statement
  {
    private Expression whileExpr;
    private List<Statement> statements;

    // labels used during code generation
    private String L1; // label for start of loop
    private String L2; // label for end of loop


    /**
     * Default constructor. Construct a loop statement with a null "while"
     * expression and an empty list of statements for the loop body.
     */
    public LoopStmt()
      {
        this(null, new LinkedList<Statement>());
      }


    /**
     * Construct a loop statement with its optional "while" expression (which
     * should be null if there is no "while" expression) and list of statements
     * in the loop body.
     */
    public LoopStmt(Expression whileExpr, List<Statement> statements)
      {
        this.whileExpr = whileExpr;
        this.statements = statements;
        L1 = getLabel();
        L2 = getLabel();
      }


    /**
     * Set the while expression for this loop statement.
     */
    public void setWhileExpr(Expression whileExpr)
      {
        this.whileExpr = whileExpr;
      }


    /**
     * Returns the list of statements for the body of this loop statement.
     */
    public List<Statement> getStatements()
      {
        return statements;
      }


    /**
     * Set the list of statements for the body of this loop statement.
     */
    public void setStatements(List<Statement> statements)
      {
        this.statements = statements;
      }


    /**
     * Returns the label for the end of the loop statement.
     */
    public String getExitLabel()
      {
        return L2;
      }


    /**
     * Returns the label for the beginning of the loop statement.
     */
    public String getStartLabel()
      {
        return L1;
      }


    @Override
    public void checkConstraints()
      {
        try
          {
            if (whileExpr != null)
              {
                whileExpr.checkConstraints();
                if (whileExpr.getType() != Type.Boolean)
                  {
                    Position errorPosition = whileExpr.getPosition();
                    String errorMessage = "The \"while\" expression should have type Boolean.";

                    throw new ConstraintException(errorPosition, errorMessage);
                  }
              }

            for (Statement stmt : statements)
              stmt.checkConstraints();
          }
        catch (ConstraintException e)
          {
            ErrorHandler.getInstance().reportError(e);
          }
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
        // L1:
        emitLabel(L1);

        if (whileExpr != null)
          whileExpr.emitBranch(false, L2);

        for (Statement stmt : statements)
          stmt.emit();

        emit("BR " + L1);

        // L2:
        emitLabel(L2);
      }
  }
