package edu.citadel.cprlvm.assembler.ast;


import edu.citadel.compiler.ConstraintException;
import edu.citadel.compiler.InternalAssertion;
import edu.citadel.compiler.Position;
import edu.citadel.compiler.util.ByteUtil;
import edu.citadel.cprlvm.Constants;
import edu.citadel.cprlvm.assembler.Symbol;
import edu.citadel.cprlvm.assembler.Token;

import java.util.*;
import java.io.*;


/**
 * This abstract class implements common methods for the abstract
 * syntax tree of a single assembly language instruction.
 */
public abstract class Instruction extends AST
  {
    // maps label text (type String) to an address (type Integer)
    // note that the label text always includes the colon (:) at the end
    private static Map<String, Integer> labelMap = new HashMap<String, Integer>();

    // maps identifier text (type String) to a stack address (type Integer)
    private static Map<String, Integer> idMap = new HashMap<String, Integer>();

    // initialize address for identifiers (e.g., used in DEFINT)
    private static int idAddress = Constants.BYTES_PER_FRAME;

    private List<Token> labels;
    private Token opCode;
    private Token arg;

    private int address;


    public Instruction(List<Token> labels, Token opCode, Token arg)
      {
        this.labels = labels;
        this.opCode = opCode;
        this.arg    = arg;
      }


    public List<Token> getLabels()
      {
        return labels;
      }


    public Token getOpCode()
      {
        return opCode;
      }


    public Token getArg()
      {
        return arg;
      }


    /**
     * Sets the memory address for an instruction.  Also defines
     * the label values if the instruction has labels.
     */
    public void setAddress(int address) throws ConstraintException
      {
        this.address = address;

        // define addresses for labels
        if (labels != null)
          {
            for (Token label : labels)
              {
                if (labelMap.containsKey(label.getText()))
                  {
                    Position errorPosition = label.getPosition();
                    String   errorMessage  = "This label has already been defined.";

                    throw new ConstraintException(errorPosition, errorMessage);
                  }
                else
                    labelMap.put(label.getText(), Integer.valueOf(address));
              }
          }
      }


    /**
     * Returns the address of this instruction. 
     */
    public int getAddress()
      {
        return address;
      }


    /**
     * map the text of the identifier token to an address on the stack
     */
    public void defineIdAddress(Token identifier, int size) throws ConstraintException
      {
        InternalAssertion.check(identifier != null, "id can't be null");

        InternalAssertion.check(identifier.getSymbol() == Symbol.identifier,
            "expecting an identifier but found " + identifier.getSymbol());

        if (idMap.containsKey(identifier.getText()))
          {
            Position errorPosition = identifier.getPosition();
            String   errorMessage  = "This identifier has already been defined.";

            throw new ConstraintException(errorPosition, errorMessage);
          }
        else
          {
            idMap.put(identifier.getText(), Integer.valueOf(idAddress));
            idAddress = idAddress + size;
          }
      }


    /**
     * Returns the stack address associated with an identifier.
     */
    protected int getIdAddress(Token identifier)
      {
        InternalAssertion.check(identifier != null, "id can't be null");

        InternalAssertion.check(identifier.getSymbol() == Symbol.identifier,
            "expecting an identifier but found " + identifier.getSymbol());

        InternalAssertion.check(idMap.containsKey(identifier.getText()),
            "identifier " + identifier.getText() + " not found");

        Integer idAddress = (Integer) idMap.get(identifier.getText());

        return idAddress.intValue();
      }


    /**
     * Returns the number of bytes in memory occupied by the argument.
     */
    protected abstract int getArgSize();


    /**
     * Returns the number of bytes in memory occupied by the
     * instruction, computed as 1 (for the opcode) plus
     * the sizes of the operands.
     */
    protected int getSize()
      {
        return Constants.BYTES_PER_OPCODE + getArgSize();
      }


    /**
     * If this instruction has labels, this method checks that each
     * label has a value defined in the label map.  This method should
     * not be called for an instruction before method setAddress().
     * @throws ConstraintException if the instruction has a label that
     *         is not defined in the label map.
     */
    protected void checkLabels() throws ConstraintException
      {
        if (labels != null)
          {
            for (Token label : labels)
              {
                if (!labelMap.containsKey(label.getText()))
                  {
                    Position errorPosition = label.getPosition();
                    String   errorMessage  = "label \"" + label.getText()
                                           + "\" has not been defined.";

                    throw new ConstraintException(errorPosition, errorMessage);
                  }
              }
          }
      }


    /**
     * This method is called by instructions that have an argument that
     * references a label.  It verifies that the referenced label exists.
     */
    protected void checkLabelArgDefined() throws ConstraintException
      {
        InternalAssertion.check(arg != null, "label argument can't be null");

        if (arg.getSymbol() != Symbol.identifier)
          {
            Position errorPosition = arg.getPosition();
            String   errorMessage  = "expecting a label identifier but found "
                                    + arg.getSymbol();

            throw new ConstraintException(errorPosition, errorMessage);
          }

        String label = arg.getText() + ":";
        if (!labelMap.containsKey(label))
          {
            Position errorPosition = arg.getPosition();
            String   errorMessage  = "label \"" + arg.getText() + "\" has not been defined.";

            throw new ConstraintException(errorPosition, errorMessage);
          }
      }


    /**
     * This method is called by instructions that have an argument that references
     * an identifier.  It verifies that the referenced identifier exists.
     */
    protected void checkIdArgDefined() throws ConstraintException
      {
        InternalAssertion.check(arg != null, "argument can't be null");

        InternalAssertion.check(arg.getSymbol() == Symbol.identifier,
            "expecting an identifier but found " + arg.getSymbol());

        if (!idMap.containsKey(arg.getText()))
          {
            Position errorPosition = arg.getPosition();
            String   errorMessage  = "identifier \"" + arg.getText()
                                   + "\" has not been defined.";

            throw new ConstraintException(errorPosition, errorMessage);
          }
      }


    /**
     * This method is called by instructions to verify the type of its argument.  
     */
    protected void checkArgType(Symbol argType) throws ConstraintException
      {
        if (arg == null)
          {
            Position errorPosition = opCode.getPosition();
            String   errorMessage  = "This opcode requires an argument.";

            throw new ConstraintException(errorPosition, errorMessage);
          }
        else if (arg.getSymbol() != argType)
          {
            Position errorPosition = arg.getPosition();
            String   errorMessage  = "Invalid type for argument -- should be " + argType;

            throw new ConstraintException(errorPosition, errorMessage);
          }
      }


    /**
     * Calculates the displacement between an instruction's address and
     * a label (computed as label's address - instruction's address).
     * This method is used by branching and call instructions.
     */
    protected int getDisplacement(Token labelArg)
      {
        String labelId = labelArg.getText() + ":";

        InternalAssertion.check(labelMap.containsKey(labelId),
            "label " + labelArg.getText() + " not found");

        Integer labelAddress = (Integer) labelMap.get(labelId);

        return (labelAddress.intValue() - address);
      }


    /**
     * Returns the argument as converted to an integer.  Valid
     * only for instructions with arguments of type intLiteral.
     */
    public int argToInt()
      {
        InternalAssertion.check(getArg().getSymbol() == Symbol.intLiteral,
            "can't convert argument to an integer");
        return Integer.parseInt(getArg().getText());
      }


    /**
     * Returns the argument as converted to a byte.  Valid
     * only for instructions with arguments of type intLigeral.
     */
    public byte argToByte()
      {
        InternalAssertion.check(getArg().getSymbol() == Symbol.intLiteral,
            "can't convert argument to a byte");
        return Byte.parseByte(getArg().getText());
      }


    /**
     * Asserts that the opCode token of the instruction has
     * the correct Symbol.  Implemented in each instruction
     * by calling the method assertOpCode(Symbol).
     */
    protected abstract void assertOpCode();


    protected void assertOpCode(Symbol opCode)
      {
        InternalAssertion.check(this.opCode != null, "null opCode");
        InternalAssertion.check(this.opCode.getSymbol() == opCode, "wrong opCode");
      }


    public String toString()
      {
        StringBuffer buffer = new StringBuffer(100);

        // for now simply print the instruction
        if (labels != null)
          {
            for (Token label : labels)
               buffer.append(label.getText() + "\n");
          }

        buffer.append("   " + opCode.getText());

        if (arg != null)
            buffer.append(" " + arg.getText());

        return buffer.toString();
      }


     /**
      * emit the opCode for the instruction
      */
    protected void emit(byte opCode) throws IOException
      {
        getOutputStream().write(opCode);
      }


     /**
      * emit an integer argument for the instruction
      */
    protected void emit(int arg) throws IOException
      {
        getOutputStream().write(ByteUtil.intToBytes(arg));
      }


     /**
      * emit a character argument for the instruction
      */
    protected void emit(char arg) throws IOException
      {
        getOutputStream().write(ByteUtil.charToBytes(arg));
      }
  }
