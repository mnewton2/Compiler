package edu.citadel.cprl.ast;


import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.Position;
import edu.citadel.cprl.ArrayType;
import edu.citadel.cprl.Token;
import edu.citadel.cprl.Type;


/**
 * The abstract syntax tree node for an array type declaration.
 */
public class ArrayTypeDecl extends InitialDecl
  {
    ConstValue numElements;
    Type       elemType;     // type of elements in the array


    /**
     * Construct an ArrayTypeDecl with its identifier and element type.
     * Note that the index type is always Integer in CPRL.
     */
    public ArrayTypeDecl(Token typeId, Type elemType, ConstValue numElements)
      {
        super(typeId, new ArrayType(typeId.getText(), numElements.getLiteralIntValue(), elemType));

        this.elemType    = elemType;
        this.numElements = numElements;
      }


    /**
     * Returns the number of elements in the array type definition.
     */
    public ConstValue getNumElements()
      {
        return numElements;
      }


    /**
     * Returns the type of the elements in the array.
     */
    public Type getElementType()
      {
        return elemType;
      }


    @Override
    public void checkConstraints()
      {
        try
          {
            numElements.checkConstraints();
            
            /* constant value specifying the number of items in the array
             must have type Integer */
            if(numElements.getType() != Type.Integer )
              {
                Position errorPosition = numElements.getPosition();
                String errorMessage = "The constant value specifying array size must have type Integer.";
                
                throw new ConstraintException(errorPosition, errorMessage);
              }
            
            // the constant value must be a positive number
            if(numElements.getLiteralIntValue() < 0)
              {
                Position errorPosition = numElements.getPosition();
                String errorMessage = "The constant value specifying array size must be a positive number";
                
                throw new ConstraintException(errorPosition, errorMessage);
              }
            
            
          }
        catch (ConstraintException e)
          {
            ErrorHandler.getInstance().reportError(e);
          }
      }
  }
