package edu.citadel.cprlvm.assembler.ast;


import java.io.IOException;
import java.io.OutputStream;


/**
 * Base class for all abstract syntax trees
 */
public abstract class AST
  {
    private static OutputStream out = null;


    /**
     * Default constructor.
     */
    public AST()
      {
        super();
      }


    /**
     * Set the OutputStream to be used for code generation.
     */
    public void setOutputStream(OutputStream out)
      {
        AST.out = out;
      }


    /**
     * Returns the OutputStream to be used for code generation
     */
    public OutputStream getOutputStream()
      {
        return out;
      }


    /**
     * check semantic/contextual constraints
     */
    public abstract void checkConstraints();


    /**
     * emit the object code for the AST
     */
    public abstract void emit() throws IOException;
  }
