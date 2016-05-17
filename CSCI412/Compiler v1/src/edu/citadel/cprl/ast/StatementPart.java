package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;

import java.util.List;
import java.util.LinkedList;
import java.io.IOException;


/**
 * The abstract syntax tree node for the statement part of a program.
 */
public class StatementPart extends AST
  {
    private List<Statement> statements;


    /**
     * Construct a StatementPart with empty list of statements. and
     * ProcedureDeclPart.
     */
    public StatementPart()
      {
        statements = new LinkedList<>();
      }


    /**
     * Construct a statement part with the specified list of statements.
     */
    public StatementPart(List<Statement> statements)
      {
        this.statements = statements;
      }


    public List<Statement> getStatements()
      {
        return statements;

      }


    @Override
    public void checkConstraints()
      {
        for (Statement stmt : statements)
          stmt.checkConstraints();
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
        for (Statement stmt : statements)
          stmt.emit();
      }
  }
