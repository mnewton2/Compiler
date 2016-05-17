package edu.citadel.cprl;


import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.InternalAssertion;
import edu.citadel.compiler.Position;
import edu.citadel.compiler.ScannerException;
import edu.citadel.compiler.Source;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;


public class Scanner
  {
    // maps strings to reserved word symbols
    private static Map<String, Symbol> rwMap = new HashMap<>(50);

    private Source source;
    private Token currentToken;
    private StringBuilder scanBuffer;


    /**
     * Initialize scanner with its associated source and advance to the first
     * token.
     */
    public Scanner(Source source) throws IOException
      {
        this.source = source;
        scanBuffer = new StringBuilder(100);
        currentToken = new Token();
        advance(); // advance to the first token
      }


    /**
     * Returns a copy (clone) of the current token in the source file.
     */
    public Token getToken()
      {
        return (Token) currentToken.clone();
      }


    /**
     * Returns a reference to the current symbol in the source file.
     */
    public Symbol getSymbol()
      {
        return currentToken.getSymbol();
      }


    /**
     * Returns a reference to the position of the current symbol in the source
     * file.
     */
    public Position getPosition()
      {
        return currentToken.getPosition();
      }


    /**
     * Advance to the next token in the source file.
     */
    public void advance() throws IOException
      {
        try
          {
            skipWhiteSpace();

            // currently at starting character of next token
            currentToken.setPosition(source.getCharPosition());
            currentToken.setText(null);

            if (source.getChar() == Source.EOF)
              {
                // set symbol but don't advance
                currentToken.setSymbol(Symbol.EOF);
              }
            else if (Character.isLetter((char) source.getChar()))
              {
                String idString = scanIdentifier();
                Symbol scannedSymbol = getIdentifierSymbol(idString);
                currentToken.setSymbol(scannedSymbol);

                if (scannedSymbol == Symbol.identifier)
                  currentToken.setText(idString);
              }
            else if (Character.isDigit((char) source.getChar()))
              {
                currentToken.setText(scanIntegerLiteral());
                currentToken.setSymbol(Symbol.intLiteral);
              }
            else
              {
                switch ((char) source.getChar())
                  {
                  case '+':
                    currentToken.setSymbol(Symbol.plus);
                    source.advance();
                    break;

                  case '-':
                    currentToken.setSymbol(Symbol.minus);
                    source.advance();
                    break;

                  case '*':
                    currentToken.setSymbol(Symbol.times);
                    source.advance();
                    break;

                  case '=':
                    currentToken.setSymbol(Symbol.equals);
                    source.advance();
                    break;

                  case '!':
                    source.advance();
                    if ((char) source.getChar() != '=')
                      {
                        throw error("Invalid character \'" + '!' + "\'");
                      }
                    else
                      {
                        currentToken.setSymbol(Symbol.notEqual);
                        source.advance();
                      }
                    break;


                  case ':':
                    source.advance();
                    if ((char) source.getChar() == '=')
                      {
                        currentToken.setSymbol(Symbol.assign);
                        source.advance();
                      }
                    else
                      currentToken.setSymbol(Symbol.colon);
                    break;

                  case '(':
                    currentToken.setSymbol(Symbol.leftParen);
                    source.advance();
                    break;

                  case ')':
                    currentToken.setSymbol(Symbol.rightParen);
                    source.advance();
                    break;

                  case '[':
                    currentToken.setSymbol(Symbol.leftBracket);
                    source.advance();
                    break;

                  case ']':
                    currentToken.setSymbol(Symbol.rightBracket);
                    source.advance();
                    break;

                  case ',':
                    currentToken.setSymbol(Symbol.comma);
                    source.advance();
                    break;

                  case ';':
                    currentToken.setSymbol(Symbol.semicolon);
                    source.advance();
                    break;

                  case '.':
                    currentToken.setSymbol(Symbol.dot);
                    source.advance();
                    break;

                  case '/':
                    source.advance();
                    if ((char) source.getChar() == '/')
                      {
                        skipComment();
                        advance();
                      }
                    else
                      currentToken.setSymbol(Symbol.divide);
                    break;

                  case '<':
                    source.advance();
                    if ((char) source.getChar() == '=')
                      {
                        currentToken.setSymbol(Symbol.lessOrEqual);
                        source.advance();
                      }
                    else
                      currentToken.setSymbol(Symbol.lessThan);
                    break;

                  case '>':
                    source.advance();
                    if ((char) source.getChar() == '=')
                      {
                        currentToken.setSymbol(Symbol.greaterOrEqual);
                        source.advance();
                      }
                    else
                      currentToken.setSymbol(Symbol.greaterThan);
                    break;

                  // handle string literals and character literals
                  case '"':
                    currentToken.setText(scanStringLiteral());
                    currentToken.setSymbol(Symbol.stringLiteral);
                    break;

                  case '\'':
                    currentToken.setText(scanCharLiteral());
                    currentToken.setSymbol(Symbol.charLiteral);
                    break;

                  default: // error: invalid character
                    {
                      String errorMsg = "Invalid character \'" + ((char) source.getChar())
                          + "\'";
                      source.advance();
                      throw error(errorMsg);
                    }
                  }
              }
          }
        catch (ScannerException e)
          {
            ErrorHandler.getInstance().reportError(e);

            // set token to either EOF or unknown
            if (source.getChar() == Source.EOF)
              {
                if (getSymbol() != Symbol.EOF)
                  currentToken.setSymbol(Symbol.EOF);
              }
            else
              currentToken.setSymbol(Symbol.unknown);
          }
      }


    /**
     * Returns the symbol associated with an identifier (Symbol.arrayRW,
     * Symbol.ifRW, Symbol.identifier, etc.)
     */
    private Symbol getIdentifierSymbol(String idString)
      {

        Symbol idSymbol = (Symbol) rwMap.get(idString);
        if (idSymbol != null)
          return idSymbol;
        else
          return Symbol.identifier;
      }


    /**
     * Skip over a comment.
     */
    private void skipComment() throws ScannerException, IOException
      {
        // assumes that source.getChar() is the second '/'

        InternalAssertion.check(((char) source.getChar()) == '/',
            "skipComment(): check for '/' as part of comment");

        skipToEndOfLine();
        source.advance();
      }


    /**
     * Advance until the symbol in the source file matches the symbol specified
     * in the parameter or until end of file is encountered.
     */
    public void advanceTo(Symbol symbol) throws IOException
      {
        while (true)
          {
            if (getSymbol() == symbol || source.getChar() == Source.EOF)
              return;
            else
              advance();
          }
      }


    /**
     * Advance until the symbol in the source file matches one of the symbols in
     * the given array or until end of file is encountered.
     */
    public void advanceTo(Symbol[] symbols) throws IOException
      {
        while (true)
          {
            if (search(symbols, currentToken.getSymbol()) >= 0
                || source.getChar() == Source.EOF)
              return;
            else
              advance();
          }

      }


    /**
     * Performs a linear search of the array for the given value.
     *
     * @return the index of the value in the array if found, otherwise -1.
     */
    private int search(Symbol[] symbols, Symbol value)
      {
        for (int i = 0; i < symbols.length; ++i)
          {
            if (symbols[i].equals(value))
              return i;
          }
        return -1;
      }


    /**
     * Clear the scan buffer (makes it empty).
     */
    private void clearScanBuffer()
      {
        scanBuffer.delete(0, scanBuffer.length());
      }


    /**
     * Scans characters in the source file for a valid identifier using the
     * lexical rule: identifier = letter ( letter | digit)* .
     *
     * @return the string of letters and digits for the identifier.
     */
    private String scanIdentifier() throws IOException
      {
        // assumes that source.getChar() is the first character of the
        // identifier

        InternalAssertion.check(Character.isLetter((char) source.getChar()),
            "scanIdentifier(): check identifier" + "start for letter at position "
                + getPosition());
        clearScanBuffer();

        do
          {
            scanBuffer.append((char) source.getChar());
            source.advance();
          }
        while (Character.isLetterOrDigit((char) source.getChar()));

        return scanBuffer.toString();
      }


    /**
     * Scans characters in the source file for a valid integer literal. Assumes
     * that source.getChar() is the first character of the Integer literal.
     *
     * @return the string of digits for the integer literal.
     */
    private String scanIntegerLiteral() throws ScannerException, IOException
      {
        // assumes that source.getChar() is the first digit of the integer
        // literal
        InternalAssertion.check(Character.isDigit((char) source.getChar()),
            "scanIntegerLiteral():  check integer literal start for digit at position "
                + getPosition());

        clearScanBuffer();

        do
          {
            scanBuffer.append((char) source.getChar());
            source.advance();
          }
        while (Character.isDigit((char) source.getChar()));

        return scanBuffer.toString();
      }


    /**
     * Scans characters in the source file for a String literal. Escaped
     * characters are converted; e.g., '\t' is converted to the tab character.
     * Assumes that source.getChar() is the opening quote (") of the String
     * literal.
     *
     * @return the string of characters for the string literal, including
     *         opening and closing quotes
     */
    private String scanStringLiteral() throws ScannerException, IOException
      {
        InternalAssertion.check(((char) source.getChar()) == '\"',
            "scanStringLiteral(): check for opening" + " quote (\") at position"
                + getPosition());

        clearScanBuffer();

        do
          {
            checkValidLiteralChar(source.getChar());
            char c = (char) source.getChar();

            if (c == '\\')
              {
                scanBuffer.append(c); // append the backslash character
                source.advance();
                checkValidLiteralChar(source.getChar());
                c = (char) source.getChar();
              }

            scanBuffer.append(c);
            source.advance();
          }
        while ((char) source.getChar() != '\"');

        scanBuffer.append('\"');
        source.advance();

        return scanBuffer.toString();

      }


    /**
     * Scans characters in the source file for a valid char literal. Escaped
     * characters are converted; e.g., '\t' is converted to the tab character.
     * Assumes that source.getChar() is the opening single quote (') of the Char
     * literal.
     *
     * @return the string of characters for the char literal, including opening
     *         and closing single quotes.
     */
    private String scanCharLiteral() throws ScannerException, IOException
      {
        InternalAssertion.check(((char) source.getChar()) == '\'',
            "scanCharLiteral():  check for opening quote (\') at position "
                + getPosition());

        clearScanBuffer();

        char c = (char) source.getChar(); // opening quote
        scanBuffer.append(c); // append the opening quote

        source.advance();
        c = (char) source.getChar();
        checkValidLiteralChar(c);

        if (c == '\\') // escaped character
          {
            scanBuffer.append(c); // append the backslash character
            source.advance();
            c = (char) source.getChar(); // get character following backslash
            checkValidLiteralChar(c);
          }
        else if (c == '\'') // check for empty char literal
          {
            source.advance();
            throw error("Char literal must contain exactly one character");
          }

        scanBuffer.append(c); // append the character literal

        source.advance();
        c = (char) source.getChar(); // should be the closing quote
        checkValidLiteralChar(c);

        if (c == '\'') // should be the closing quote
          {
            scanBuffer.append(c); // append the closing quote
            source.advance();
          }
        else
          throw error("Char literal not closed properly");

        return scanBuffer.toString();
      }


    /**
     * Fast skip over white space.
     */
    private void skipWhiteSpace() throws IOException
      {
        while (Character.isWhitespace((char) source.getChar()))
          {
            source.advance();
          }
      }


    /**
     * Advances over source characters to the end of the current line.
     */
    private void skipToEndOfLine() throws ScannerException, IOException
      {
        while ((char) source.getChar() != '\n')
          {
            source.advance();
            checkEOF();
          }
      }


    /**
     * Checks that the integer represents a valid character for a Char or String
     * literal. For example, a string can't contain end-of-line characters or
     * control characters.
     */
    private void checkValidLiteralChar(int n) throws ScannerException
      {
        if (n == Source.EOF)
          throw error(
              "End of file reached before closing quote for Char or String literal.");
        else
          {
            char c = (char) n;
            if (c == '\r' || c == '\n')
              throw error("Char and String literals can not extend past end of line.");
            else if (Character.isISOControl(c)) // Sorry. No ISO control
                                                // characters.
              throw error("ISO control character at " + source.getCharPosition()
                  + " is not allowed in Char or String literal.");
          }
      }


    /**
     * Throws a ScannerException with the specified error message.
     */
    private ScannerException error(String message)
      {
        return new ScannerException(getPosition(), message);
      }


    /**
     * Used to check for EOF in the middle of scanning tokens that require
     * closing characters such as strings and comments.
     *
     * @throws ScannerException
     *           if source is at end of file.
     */
    private void checkEOF() throws ScannerException
      {
        if (source.getChar() == Source.EOF)
          throw new ScannerException(getPosition(), "Unexpected end of file");
      }


    static
      {
        // initialize HashMap with reserve word symbols
        rwMap.put("Boolean", Symbol.BooleanRW);
        rwMap.put("Char", Symbol.CharRW);
        rwMap.put("Integer", Symbol.IntegerRW);
        rwMap.put("String", Symbol.StringRW);
        rwMap.put("and", Symbol.andRW);
        rwMap.put("array", Symbol.arrayRW);
        rwMap.put("begin", Symbol.beginRW);
        rwMap.put("class", Symbol.classRW);
        rwMap.put("const", Symbol.constRW);
        rwMap.put("declare", Symbol.declareRW);
        rwMap.put("else", Symbol.elseRW);
        rwMap.put("elsif", Symbol.elsifRW);
        rwMap.put("end", Symbol.endRW);
        rwMap.put("exit", Symbol.exitRW);
        rwMap.put("false", Symbol.falseRW);
        rwMap.put("for", Symbol.forRW);
        rwMap.put("function", Symbol.functionRW);
        rwMap.put("if", Symbol.ifRW);
        rwMap.put("in", Symbol.inRW);
        rwMap.put("is", Symbol.isRW);
        rwMap.put("loop", Symbol.loopRW);
        rwMap.put("mod", Symbol.modRW);
        rwMap.put("not", Symbol.notRW);
        rwMap.put("of", Symbol.ofRW);
        rwMap.put("or", Symbol.orRW);
        rwMap.put("private", Symbol.privateRW);
        rwMap.put("procedure", Symbol.procedureRW);
        rwMap.put("program", Symbol.programRW);
        rwMap.put("protected", Symbol.protectedRW);
        rwMap.put("public", Symbol.publicRW);
        rwMap.put("read", Symbol.readRW);
        rwMap.put("readln", Symbol.readlnRW);
        rwMap.put("return", Symbol.returnRW);
        rwMap.put("then", Symbol.thenRW);
        rwMap.put("true", Symbol.trueRW);
        rwMap.put("type", Symbol.typeRW);
        rwMap.put("var", Symbol.varRW);
        rwMap.put("when", Symbol.whenRW);
        rwMap.put("while", Symbol.whileRW);
        rwMap.put("write", Symbol.writeRW);
        rwMap.put("writeln", Symbol.writelnRW);
      }
  }
