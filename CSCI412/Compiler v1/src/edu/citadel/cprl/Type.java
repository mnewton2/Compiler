package edu.citadel.cprl;


import edu.citadel.cprlvm.Constants;


/**
 * This class encapsulates the language types for the programming language CPRL.
 * Type sizes are initialized to values appropriate for the CPRL virtual machine.
 */
public class Type
  {
    private String name;
    private int    size;

    // predefined types
    public static final Type Boolean = new Type("Boolean", Constants.BYTES_PER_BOOLEAN);
    public static final Type Integer = new Type("Integer", Constants.BYTES_PER_INTEGER);
    public static final Type Char    = new Type("Char"   , Constants.BYTES_PER_CHAR);
    public static final Type String  = new Type("String");   // actual size not needed in CPRL

    // an address of the target machine
    public static final Type Address = new Type("Address", Constants.BYTES_PER_ADDRESS);

    // compiler-internal types
    public static final Type UNKNOWN = new Type("UNKNOWN");
    public static final Type none    = new Type("none");

    
    /**
     * Construct a new Type with the specified type name and size.
     */
    protected Type(String name, int size)
      {
        this.name = name;
        this.size = size;
      }


    /**
     * Construct a new Type with the specified type name.
     * Size is initially set to 0.
     */
    protected Type(String name)
      {
        this(name, 0);
      }


    /**
     * Returns the name for this Type.
     */
    public String toString()
      {
        return name;
      }


    /**
     * Returns the number of machine addressable units
     * (e.g., bytes or words) for this type.
     */
    public int getSize()
      {
        return size;
      }


    /**
     * Sets the number of machine addressable units
     * (e.g., bytes or words) for this type.
     */
    public void setSize(int size)
      {
        this.size = size;
      }


    /**
     * Returns true if and only if this type is a scalar type.
     * The scalar types in CPTL are Integer, Boolean, and Char. 
     */
    public boolean isScalar()
      {
        return equals(Integer) || equals(Boolean) || equals(Char);
      }


    @Override
    public int hashCode()
      {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
      }


    @Override
    public boolean equals(Object obj)
      {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        Type other = (Type) obj;
        if (name == null)
          {
            if (other.name != null)
                return false;
          }
        else if (!name.equals(other.name))
            return false;

        return true;
      }
  }
