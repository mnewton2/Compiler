package edu.citadel.cprl.ast;


import java.io.IOException;

import edu.citadel.compiler.CodeGenException;
import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.Position;
import edu.citadel.cprl.Type;


/**
 * The abstract syntax tree node for a read statement.
 */
public class ReadStmt extends Statement
  {
    Variable var;


    /**
     * Construct an input statement with the specified variable for storing the
     * input.
     */
    public ReadStmt(Variable var)
      {
        this.var = var;
      }


    @Override
    public void checkConstraints()
      {
        try
          {
            var.checkConstraints();

            // var must have either type Integer or type Char
            if (var.getType() != Type.Integer)
              {
                if (var.getType() != Type.Char)
                  {
                    String errorMessage = "Invalid type for variable.";
                    Position errorPosition = var.getPosition();

                    throw new ConstraintException(errorPosition, errorMessage);
                  }
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
        var.emit();

        if (var.getType() == Type.Integer)
          {
            emit("GETINT");
            emitStoreInst(Type.Integer);
          }
        else if (var.getType() == Type.Char)
          {
            emit("GETCH");
            emitStoreInst(Type.Char);
          }
      }
  }
