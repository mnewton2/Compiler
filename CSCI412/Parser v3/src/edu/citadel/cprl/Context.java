package edu.citadel.cprl;


import edu.citadel.cprl.ast.LoopStmt;
import edu.citadel.cprl.ast.SubprogramDecl;

import java.util.Stack;


/**
 * The Context provides access to an enclosing context required for
 * proper code generation by certain statements.  For example, an exit
 * statement needs to know which loop it belongs to.  Similarly, a
 * return statement needs to know which procedure it is returning from
 * so that it can get the parameter length to remove from the stack.
 */
public final class Context
  {
    private Stack<LoopStmt> loopStack;
    private SubprogramDecl  subprogDecl;


    public Context()
      {
        loopStack   = new Stack<LoopStmt>();
        subprogDecl = null;
      }


    /**
     * Returns the loop statement currently being parsed.
     * Returns null if no such loop statement exists.
     */
    public LoopStmt getLoopStmt()
      {
        if (!loopStack.empty())
            return (LoopStmt) loopStack.peek();
        else
            return null;
      }


    /**
     * Called when starting to parse a loop statement.
     */
    public void beginLoop(LoopStmt stmt)
      {
        loopStack.push(stmt);
      }


    /**
     * Called when finished parsing a loop statement.
     */
    public void endLoop()
      {
        if (!loopStack.empty())
            loopStack.pop();
      }


    /**
     * Returns the subprogram declaration currently being parsed.
     * Returns null if no such procedure exists.
     */
    public SubprogramDecl getSubprogramDecl()
      {
        return subprogDecl;
      }


    /**
     * Called when starting to parse a subprogram declaration.
     */
    public void beginSubprogramDecl(SubprogramDecl subprogDecl)
      {
        this.subprogDecl = subprogDecl;
      }


    /**
     * Called when finished parsing a procedure declaration.
     */
    public void endSubprogramDecl()
      {
        this.subprogDecl = null;
      }
  }
