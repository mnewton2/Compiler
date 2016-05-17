package edu.citadel.cprl.ast;


import edu.citadel.cprl.Token;
import edu.citadel.cprlvm.Constants;

import java.util.*;


/**
 * Base class for CPRL procedures and functions.
 */
public abstract class SubprogramDecl extends Declaration
  {
    private List<ParameterDecl> formalParams;
    private List<InitialDecl>   initialDecls;
    private StatementPart stmtPart;

    private int varLength;   // # bytes of all declared variables

    private String L1;       // label of address of first statement
                             // (used during code generation)


    /**
     * Construct a subprogram declaration with the specified subprogram identifier.
     */
    public SubprogramDecl(Token subprogId)
      {
        super(subprogId);

        this.formalParams = new LinkedList<>();
        this.initialDecls = null;
        this.stmtPart     = null;
        this.varLength    = 0;

        L1 = getLabel();
      }


    /**
     * Set the list of initial declarations for this subprogram.
     */
    public void setInitialDecls(List<InitialDecl> initialDecls)
      {
        this.initialDecls = initialDecls;
      }


    /**
     * Returns the list of formal parameter declarations for this subprogram.
     */
    public List<ParameterDecl> getFormalParams()
      {
        return formalParams;
      }


    /**
     * Set the list of formal parameter declarations for this subprogram.
     */
    public void setFormalParams(List<ParameterDecl> formalParams)
      {
        this.formalParams = formalParams;
      }


    /**
     * Returns the statement part for this subprogram.
     */
    public StatementPart getStatementPart()
      {
        return stmtPart;
      }


    /**
     * Set the statement part for this subprogram.
     */
    public void setStatementPart(StatementPart stmtPart)
      {
        this.stmtPart = stmtPart;
      }


    /**
     * Returns the number of bytes required for all
     * variables in the initial declarations.
     */
    public int getVarLength()
      {
        return varLength;
      }


    /**
     * Returns the label associated with the first statement of the subprogram.
     */
    public String getSubprogramLabel()
      {
        return L1;
      }


    /**
     * Returns the number of bytes for all parameters
     */
    public int getParamLength()
      {
        int paramLength = 0;

        for (ParameterDecl decl : formalParams)
            paramLength += decl.getSize();

        return paramLength;
      }


    @Override
    public void checkConstraints()
      {
        // initial relative address for a subprogram
        int currentAddr = Constants.BYTES_PER_FRAME;

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

        // compute length of all variables by subtracting initial relative address
        varLength = currentAddr - Constants.BYTES_PER_FRAME;

        stmtPart.checkConstraints();

        // set relative address for parameters
        if (formalParams.size() > 0)
          {
            // first lets copy the parameters to a stack so
            // that we can process them in reverse order
            Stack<ParameterDecl> paramStack = new Stack<ParameterDecl>();

            for (ParameterDecl decl : formalParams)
                paramStack.push(decl);

            currentAddr = 0;

            while (!paramStack.empty())
              {
                ParameterDecl decl = (ParameterDecl) paramStack.pop();
                currentAddr = currentAddr - decl.getSize();
                decl.setRelAddr(currentAddr);
              }
          }
      }
  }
