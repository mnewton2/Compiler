package edu.citadel.compiler;


/**
 * InternalAssertion is based loosely on Java's assert mechanism and the
 * ASSERT macro in C/C++.  If the condition is not true, an internal compiler
 * exception (runtime exception) with the given error message will be thrown.
 */
public class InternalAssertion
  {
    private static final boolean DEBUG = true;


    /**
     *  Check the condition and throw an internal compiler exception if the condition
     *  is false.
     *  
     *  @param condition the condition to check.
     *  @param message   a message to include with the exception if the condition fails.
     *  
     *  @throws InternalCompilerException  if the condition evaluates to false.
     */
    public static void check(boolean condition, String message) throws InternalCompilerException
      {
        if(DEBUG)
          {
            if (!condition)
                throw new InternalCompilerException(message);
          }
      }
  }
