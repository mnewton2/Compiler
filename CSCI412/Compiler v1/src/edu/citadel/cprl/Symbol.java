package edu.citadel.cprl;


/**
 * This class encapsulates the symbols (also known as token types)
 * of the programming language CPRL.
 */
public enum Symbol
  {
    // reserved words
    BooleanRW("Reserved word: Boolean"),
    CharRW("Reserved word: Char"),
    IntegerRW("Reserved word: Integer"),
    StringRW("Reserved word: String"),
    andRW("Reserved word: and"),
    arrayRW("Reserved word: array"),
    beginRW("Reserved word: begin"),
    classRW("Reserved word: class"),
    constRW("Reserved word: const"),
    declareRW("Reserved word: declare"),
    elseRW("Reserved word: else"),
    elsifRW("Reserved word: elsif"),
    endRW("Reserved word: end"),
    exitRW("Reserved word: exit"),
    falseRW("Reserved word: false"),
    forRW("Reserved word: for"),
    functionRW("Reserved word: function"),
    ifRW("Reserved word: if"),
    inRW("Reserved word: in"),
    isRW("Reserved word: is"),
    loopRW("Reserved word: loop"),
    modRW("Reserved word: mod"),
    notRW("Reserved word: not"),
    ofRW("Reserved word: of"),
    orRW("Reserved word: or"),
    privateRW("Reserved word: private"),
    procedureRW("Reserved word: procedure"),
    programRW("Reserved word: program"),
    protectedRW("Reserved word: protected"),
    publicRW("Reserved word: public"),
    readRW("Reserved word: read"),
    readlnRW("Reserved word: readln"),
    returnRW("Reserved word: return"),
    thenRW("Reserved word: then"),
    trueRW("Reserved word: true"),
    typeRW("Reserved word: type"),
    varRW("Reserved word: var"),
    whenRW("Reserved word: when"),
    whileRW("Reserved word: while"),
    writeRW("Reserved word: write"),
    writelnRW("Reserved word: writeln"),

    // arithmetic operator symbols
    plus("+"),
    minus("-"),
    times("*"),
    divide("/"),

    // relational operator symbols
    equals("="),
    notEqual("!="),
    lessThan("<"),
    lessOrEqual("<="),
    greaterThan(">"),
    greaterOrEqual(">="),

    // assignment, punctuation, and grouping symbols
    assign(":="),
    leftParen("("),
    rightParen(")"),
    leftBracket("["),
    rightBracket("]"),
    comma(","),
    colon(":"),
    semicolon(";"),
    dot("."),

    // literal values and identifier symbols
    intLiteral("Integer Literal"),
    charLiteral("Character Literal"),
    stringLiteral("String Literal"),
    identifier("Identifier"),

    // special scanning symbols
    EOF("End-of-File"),
    unknown("Unknown");


    // instance fields
    private final String label;

    
    /**
     * Constructs a new Symbol with its label.
     */
    private Symbol(String label)
      {
        this.label = label;
      }


    /**
     * Returns the label for this Symbol.
     */
    public String toString()
      {
        return label;
      }
  }
