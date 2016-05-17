package edu.citadel.cprl.ast;


import edu.citadel.compiler.CodeGenException;

import java.util.List;
import java.util.LinkedList;
import java.io.IOException;


/**
 * The abstract syntax tree node for the declarative part of a program.
 */
public class DeclarativePart extends AST
  {
    private List<InitialDecl> initialDecls;
    private List<SubprogramDecl> subprogDecls;

    private int varLength; // # bytes of all declared variables


    /**
     * Construct a DeclarativePart with empty lists of initial and subprogram
     * declarations.
     */
    public DeclarativePart()
      {
        initialDecls = new LinkedList<>();
        subprogDecls = new LinkedList<>();
      }


    /**
     * Construct a DeclarativePart with the lists of initial and subprogram
     * declarations.
     */
    public DeclarativePart(List<InitialDecl> initialDecls,
        List<SubprogramDecl> subprogramDecls)
      {
        this.initialDecls = initialDecls;
        this.subprogDecls = subprogramDecls;
      }


    /**
     * Returns the list of initial declarations.
     */
    public List<InitialDecl> getInitialDecls()
      {
        return initialDecls;
      }


    /**
     * Returns the list of subprogram declarations.
     */
    public List<SubprogramDecl> getSubprogramDecls()
      {
        return subprogDecls;
      }


    /**
     * Returns the number of bytes required for all variables in the initial
     * declarations.
     */
    public int getVarLength()
      {
        return varLength;
      }


    @Override
    public void checkConstraints()
      {
        // initial relative address is 0 for a program
        int currentAddr = 0;

        for (InitialDecl decl : initialDecls)
          {
            decl.checkConstraints();

            // set relative address for single variable declarations
            if (decl instanceof SingleVarDecl)
              {
                SingleVarDecl singleVarDecl = (SingleVarDecl) decl;
                singleVarDecl.setRelAddr(currentAddr);
                currentAddr = currentAddr + singleVarDecl.getSize();
              }
          }
        // compute length of all variables
        varLength = currentAddr;

        for (SubprogramDecl decl : subprogDecls)
          decl.checkConstraints();
      }


    @Override
    public void emit() throws CodeGenException, IOException
      {

        for(SubprogramDecl decl: subprogDecls)
          decl.emit();
      }
  }
