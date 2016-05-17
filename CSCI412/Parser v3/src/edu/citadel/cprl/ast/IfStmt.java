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
 * The abstract syntax tree node for an if statement.
 */
public class IfStmt extends Statement
  {
    private Expression booleanExpr;
    private List<Statement> thenStmts;      // List of Statement objects following "then"
    private List<ElsifPart> elsifParts;     // List of ElsifPart objects
    private List<Statement> elseStmts;      // List of Statement objects following "else"

    // labels used during code generation
    private String L1;   // label of address at end of then statements
    private String L2;   // label of address at end of if statement


    /**
     * Construct an if statement with the specified boolean expression
     * and list of "then" statements.
     *
     * @param booleanExpr the boolean expression that, if true, will result
     *                    in the execution of the list of "then" statements.
     * @param thenStmts   the statements to be executed when the boolean
     *                    expression evaluates to true.
     */
    public IfStmt(Expression booleanExpr, List<Statement> thenStmts)
      {
        this.booleanExpr = booleanExpr;
        this.thenStmts   = thenStmts;
        this.elsifParts  = new LinkedList<>();
        this.elseStmts   = new LinkedList<>();

        L1 = getLabel();
        L2 = getLabel();
      }


    /**
     * Returns the list of "then" statements for this if statement.
     */
    public List<Statement> getThenStmts()
      {
        return thenStmts;
      }


    /**
     * Add an ElsifPart to this if statement.
     *
     * @param booleanExpr the boolean expression
     * @param thenStmts
     */
    public void addElsifPart(Expression booleanExpr, List<Statement> thenStmts)
      {
        ElsifPart elsifPart = new ElsifPart(booleanExpr, thenStmts);
        elsifParts.add(elsifPart);
      }


    /**
     * Returns the list of elsif parts for this if statement.
     */
    public List<ElsifPart> getElsifParts()
      {
        return elsifParts;
      }


    /**
     * Add a list of "else" statements to this if statement.  These are
     * the statements that get executed if boolean expressions associated
     * with the if statement and all elsif parts evaluate to false.
     *
     * @param elseStmts  the list of else statements for this if statement.
     */
    public void addElseStmts(List<Statement> elseStmts)
      {
        this.elseStmts = elseStmts;
      }


    /**
     * Returns the list of "else" statements for this if statement.
     */
    public List<Statement> getElseStmts()
      {
        return elseStmts;
      }


    @Override
    public void checkConstraints()
      {
        try
          {
            booleanExpr.checkConstraints();

            for (Statement stmt : thenStmts)
                stmt.checkConstraints();

            for (ElsifPart part : elsifParts)
                part.checkConstraints();

            for (Statement stmt : elseStmts)
                stmt.checkConstraints();

            if (booleanExpr.getType() != Type.Boolean)
              {
                Position errorPosition = booleanExpr.getPosition();
                String   errorMessage  = "An \"if\" condition should have type Boolean.";

                throw new ConstraintException(errorPosition, errorMessage);
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
        // if expression evaluates to false, branch to L1
        booleanExpr.emitBranch(false, L1);

        // emit code for then statements
        for (Statement stmt : thenStmts)
            stmt.emit();

        // if there are elsif parts or an else part, branch to end of if statement
        if (elsifParts.size() > 0 || elseStmts.size() > 0)
            emit("BR " + L2);

        // L1:
        emitLabel(L1);

        // emit code for elsif statements
        for (ElsifPart part : elsifParts)
          {
            part.setEndIfLabel(L2);
            part.emit();
          }

        // emit code for else statements
        for (Statement stmt : elseStmts)
            stmt.emit();

        // L2:
        emitLabel(L2);
      }
  }
