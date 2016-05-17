package edu.citadel.compiler;

/**
 * Superclass for all compiler exceptions.
 */
public abstract class CompilerException extends Exception
  {
    private static final long serialVersionUID = -6999301636930707946L;

    private String   errorType;   // type of error based on compilation phase
    private Position position;    // error position in the source file


    /**
     * Returns the error type associated with the exception based on the compilation
     * phase in which the exception was detected.  Examples include "Lexical",
     * "Syntax", "Constraint", and "Code Generation".
     */
    public String getErrorType()
      {
        return errorType;
      }


    /**
     * Returns position of the character in the source file where the exception
     * was detected.
     */
    public Position getPosition()
      {
        return position;
      }


    /**
     * Construct an exception with information about the nature and position
     * of the error.
     * 
     * @param errorType the name of compilation phase in which the error was detected.
     * @param position  the position in the source file where the error was detected.
     * @param message   a brief message about the nature of the error.
     */
    public CompilerException(String errorType, Position position, String message)
      {
        super("*** " + errorType
            + " error detected near line " + position.getLineNumber()
            + ", character " + position.getCharNumber() + ":\n    " + message);
        this.errorType = errorType;
        this.position = position;
      }


    /**
     * Construct an exception with information about the nature of the error
     * but not its position.
     * 
     * @param errorType the name of compilation phase in which the error was detected.
     * @param message   a brief message about the nature of the error.
     */
    public CompilerException(String errorType, String message)
      {
        super("*** " + errorType + " error detected:  " + message);
        this.errorType = errorType;
      }
  }
