package edu.citadel.cprl.ast;


import edu.citadel.compiler.InternalAssertion;
import edu.citadel.cprl.ArrayType;
import edu.citadel.cprl.Token;
import edu.citadel.cprl.Type;


/**
 * Base class for all initial declarations.
 */
public abstract class InitialDecl extends Declaration
  {
    /**
     * Construct an InitialDecl with its identifier and type.
     */
    public InitialDecl(Token identifier, Type declType)
      {
        super(identifier, declType);
      }


    @Override
    public void checkConstraints()
      {
        InternalAssertion.check(getType() == Type.Boolean
                             || getType() == Type.Integer
                             || getType() == Type.Char
                             || getType() instanceof ArrayType,
                             "Invalid CPRL type in var declaration.");
      }
  }
