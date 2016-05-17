package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;

import java.io.IOException;
import java.util.List;


/**
 * The abstract syntax tree node for a CPRL program.
 */
public class Program extends AST
  {
    private DeclarativePart declPart;
    private StatementPart   stmtPart;

    // label for first program statement (used during code generation)
    private String L1;


    public Program(DeclarativePart declPart, StatementPart stmtPart)
      {
        this.declPart = declPart;
        this.stmtPart = stmtPart;

        L1 = getLabel();
      }


    @Override
    public void checkConstraints()
      {
        if (declPart != null)
            declPart.checkConstraints();

        stmtPart.checkConstraints();
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {
        int varLength = (declPart == null ? 0 : declPart.getVarLength());
        emit("PROGRAM " + varLength);

        // emit branch over procedures only if necessary
        List<SubprogramDecl> subprogDecls = declPart.getSubprogramDecls();
        if (!subprogDecls.isEmpty())
          {
            // jump over code for subprograms
            emit("BR " + L1);
            declPart.emit();            
            emitLabel(L1);
          }
        else
            declPart.emit();

        stmtPart.emit();
        emit("HALT");
      }
  }
