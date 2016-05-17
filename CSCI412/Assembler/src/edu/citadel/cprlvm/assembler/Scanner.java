package edu.citadel.cprlvm.assembler;


import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.InternalAssertion;
import edu.citadel.compiler.Position;
import edu.citadel.compiler.ScannerException;
import edu.citadel.compiler.Source;

import java.io.*;
import java.util.HashMap;


public class Scanner
  {
    /** maps strings to opcode symbols */
    private static HashMap<String, Symbol> opCodeMap = new HashMap<String, Symbol>(50);

    private Source source;
    private Token  currentToken;
    private StringBuilder scanBuffer;


    /**
     * Initialize scanner with its associated source and advance
     * to the first token.
     */
    public Scanner(Source source) throws IOException
      {
        this.source  = source;
        scanBuffer   = new StringBuilder(100);
        currentToken = new Token();
        advance();           // advance to the first token
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
     * Returns a reference to the position of the current
     * symbol in the source file.
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
                // set symbol but don't advance
                currentToken.setSymbol(Symbol.EOF);
            else if (Character.isLetter((char) source.getChar()))
              {
                // opcode symbol, identifier, or label
                String idString = scanIdentifier();
                Symbol scannedSymbol = getIdentifierSymbol(idString);
                currentToken.setSymbol(scannedSymbol);

                if (scannedSymbol == Symbol.identifier)
                  {
                    // check to see if we have a label
                    if (source.getChar() == ':')
                      {
                        currentToken.setSymbol(Symbol.labelId);
                        currentToken.setText(idString + ":");
                        source.advance();
                      }
                    else
                        currentToken.setText(idString);
                  }
              }
            else if (Character.isDigit((char) source.getChar()))     // integer literal
              {
                currentToken.setText(scanIntegerLiteral());
                currentToken.setSymbol(Symbol.intLiteral);
              }
            else
                switch((char) source.getChar())
                  {
                    case ';':
                        skipComment();
                        advance();   // continue scanning for next token
                        break;
                    case '\'':
                        currentToken.setText(replaceEscapedChars(scanCharLiteral()));
                        currentToken.setSymbol(Symbol.charLiteral);
                        break;
                    case '\"':
                        currentToken.setText(replaceEscapedChars(scanStringLiteral()));
                        currentToken.setSymbol(Symbol.stringLiteral);
                        break;
                    case '-':
                        // should be a negative integer literal
                        source.advance();
                        if (Character.isDigit((char) source.getChar()))     // integer literal
                          {
                            currentToken.setText("-" + scanIntegerLiteral());
                            currentToken.setSymbol(Symbol.intLiteral);
                          }
                        else
                          {
                            throw error("Expecting an integer literal");
                          }
                        break;
                    default:
                        // error:  invalid token
                        source.advance();
                        throw error("Invalid Token");
                  }
          }
        catch (ScannerException e)
          {
            ErrorHandler.getInstance().reportError(e);
            System.exit(1);
          }
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
        // assumes that source.getChar() is the first character of the identifier

        InternalAssertion.check(Character.isLetter((char) source.getChar()),
            "scanIdentifier():  check identifier start for letter at position "
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
     * Scans characters in the source file for a valid integer literal.
     * Assumes that source.getChar() is the first character of the Integer literal.
     *
     * @return the string of digits for the integer literal.
     */
    private String scanIntegerLiteral() throws ScannerException, IOException
      {
        // assumes that source.getChar() is the first digit of the integer literal
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


    private void skipComment() throws ScannerException, IOException
      {
        // assumes that source.getChar() is the leading ';'

        InternalAssertion.check(((char) source.getChar()) == ';',
            "skipComment():  check for ';' to start comment");

        skipToEndOfLine();
        source.advance();
      }


    /**
     * Scans characters in the source file for a String literal.
     * Escaped characters are converted; e.g., '\t' is converted to
     * the tab character.  Assumes that source.getChar() is the
     * opening quote (") of the String literal.
     *
     * @return the string of characters for the string literal, including
     *         opening and closing quotes
     */
    private String scanStringLiteral() throws ScannerException, IOException
      {
        InternalAssertion.check(((char) source.getChar()) == '\"',
            "scanStringLiteral():  check for opening quote (\") at position "
            + getPosition());

        clearScanBuffer();

        do
          {
            char c = (char) source.getChar();
            checkValidLiteralChar(c);

            if (c == '\\')
                scanBuffer.append(scanEscapedChar());
            else
                scanBuffer.append(c);

            source.advance();
          }
        while ((char) source.getChar() != '\"');

        scanBuffer.append('\"');     // append closing quote
        source.advance();

        return scanBuffer.toString();
      }


    /**
     * Scans characters in the source file for a valid char literal.
     * Escaped characters are converted; e.g., '\t' is converted to the
     * tab character.  Assumes that source.getChar() is the opening
     * single quote (') of the Char literal.
     *
     * @return the string of characters for the char literal, including
     *         opening and closing single quotes.
     */
    private String scanCharLiteral() throws ScannerException, IOException
      {
        InternalAssertion.check(((char) source.getChar()) == '\'',
            "scanCharLiteral():  check for opening quote (\') at position "
            + getPosition());

        clearScanBuffer();

        char c = (char) source.getChar();   // opening quote
        scanBuffer.append(c);               // append the opening quote

        source.advance();
        c = (char) source.getChar();        // the character literal
        checkValidLiteralChar(c);

        if (c == '\\')                      // escaped character
            c = scanEscapedChar();
        else if (c == '\'')                 // check for empty char literal
          {
            source.advance();
            throw error("Char literal must contain exactly one character");
          }

        scanBuffer.append(c);               // append the character literal

        source.advance();
        c = (char) source.getChar();        // should be the closing quote
        checkValidLiteralChar(c);

        if (c == '\'')                      // should be the closing quote
          {
            scanBuffer.append(c);           // append the closing quote
            source.advance();
          }
        else
            throw error("Char literal not closed properly");

        return scanBuffer.toString();
      }


    /**
     * Returns the value for an escaped character (one preceded by a backslash).
     * For example, if parameter src is "\t", then the tab character is returned.
     * This method handles escape characters \b, \t, \n, \r, \", and \'.  If the
     * second character (the one following a backslash) is anything other than
     * 'b', 't', 'n', 'r', '"', or ''', then that character is returned unmodified.
     *
     * @param src a 2-character string, where the first character is the backslash
     */
    private char getEscapedChar(String src)
      {
        InternalAssertion.check(src.length() == 2,
            "getEscapedChar():  parameter must contain exactly two characters");

        InternalAssertion.check(src.charAt(0) == '\\',
            "getEscapedChar():  check for backslash as first character");

        char c = src.charAt(1);

        switch (c)
          {
            case 'b':  return '\b';   // backspace
            case 't':  return '\t';   // tab
            case 'n':  return '\n';   // linefeed (a.k.a. newline)
            case 'r':  return '\r';   // carriage return
            case '\"': return '\"';   // double quote
            case '\'': return '\'';   // single quote
            default:   return c;
          }
      }


    /**
     * Scans characters in the source file for a escaped character; i.e.,
     * a character preceded by a backslash.  This method handles escape
     * characters \b, \t, \n, \r, \", and \'.  If the second character
     * (the one following a backslash) is anything other than one of
     * these characters, then that character is returned unmodified.
     * Assumes that source.getChar() is the escape character (\).
     *
     * @return the value for an escaped character.
     */
    private char scanEscapedChar() throws ScannerException, IOException
      {
        InternalAssertion.check(((char) source.getChar()) == '\\',
            "scanEscapedChar():  check for escape character ('\\') at position "
            + getPosition());

        source.advance();
        checkValidLiteralChar(source.getChar());

        char c = (char) source.getChar();

        switch (c)
          {
            case 'b' : return '\b';   // backspace
            case 't' : return '\t';   // tab
            case 'n' : return '\n';   // linefeed (a.k.a. newline)
            case 'r' : return '\r';   // carriage return
            case '\"': return '\"';   // double quote
            case '\'': return '\'';   // single quote
            default:   return c;
          }
      }


    /**
     * Replaces each substring of the form \x with the corresponding escape
     * character and returns the resulting string.
     */
    public String replaceEscapedChars(String src)
      {
        if (src == null || src.length() <= 1)
            return src;

        // quick check to see if the string contains a backslash
        boolean hasBackslash = false;
        for (int i = 0;  i < src.length();  ++i)
          {
            if (src.charAt(i) == '\\')
              {
                hasBackslash = true;
                break;
              }
          }

        if (hasBackslash)
          {
            StringBuffer buffer = new StringBuffer(src.length());

            for (int i = 0;  i < src.length();  ++i)
              {
                char c = src.charAt(i);

                if (i == '\\')
                  {
                    buffer.append(getEscapedChar(src.substring(i, i + 1)));
                    ++i;
                  }
                else
                    buffer.append(c);
              }

            return buffer.toString();
          }
        else
            return src;
      }


    /**
     * Returns the symbol associated with an identifier
     * (Symbol.ADD, Symbol.AND, Symbol.identifier, etc.)
     */
    private Symbol getIdentifierSymbol(String idString)
      {
        idString = idString.toUpperCase();
        int idLength = idString.length();

        if (idLength < 2 || idLength > 7)       // quick check based on string length
            return Symbol.identifier;

        Symbol idSymbol = (Symbol) opCodeMap.get(idString);
        if (idSymbol != null)
            return idSymbol;
        else
            return Symbol.identifier;
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
     * Performs a linear search of the array for the given value.
     *
     * @return the index of the value in the array if found, otherwise -1.
     */
    private int search(Symbol[] symbols, Symbol value)
      {
        for (int i = 0;  i < symbols.length;  ++i)
          {
            if (symbols[i].equals(value))
                return i;
          }
        return -1;
      }


    /**
     * Advance until the symbol in the source file matches the symbol
     * specified in the parameter or until end of file is encountered.
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
     * Advance until the symbol in the source file matches one of the
     * symbols in the given array or until end of file is encountered.
     */
    public void advanceTo(Symbol[] symbols) throws IOException
      {
        while (true)
          {
            if (search(symbols, getSymbol()) >= 0
                || source.getChar() == Source.EOF)
                return;
            else
                advance();
          }

      }


    /**
     * Checks that the integer represents a valid character for a Char or String literal.
     * For example, a string can't contain end-of-line characters or control characters.
     */
    private void checkValidLiteralChar(int n) throws ScannerException
      {
        if (n == Source.EOF)
            throw error("End of file reached before closing quote for Char or String literal.");
        else
          {
            char c = (char) n;
            if (c == '\r' || c == '\n')
                throw error("Char and String literals can not extend past end of line.");
            else if (Character.isISOControl(c))    // Sorry.  No ISO control characters.
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
     * Used to check for EOF in the middle of scanning tokens that
     * require closing characters such as strings and comments.
     *
     * @throws ScannerException if source is at end of file.
     */
    private void checkEOF() throws ScannerException
      {
        if (source.getChar() == Source.EOF)
            throw new ScannerException(getPosition(), "Unexpected end of file");
      }


    static   // static initialization block
      {
        // initialize HashMap with opcode symbols
        opCodeMap.put("HALT",    Symbol.HALT);
        opCodeMap.put("LOAD",    Symbol.LOAD);
        opCodeMap.put("LOADB",   Symbol.LOADB);
        opCodeMap.put("LOAD2B",  Symbol.LOAD2B);
        opCodeMap.put("LOADW",   Symbol.LOADW);
        opCodeMap.put("LOADCB",  Symbol.LOADCB);
        opCodeMap.put("LDCCH",   Symbol.LDCCH);
        opCodeMap.put("LDCINT",  Symbol.LDCINT);
        opCodeMap.put("LDCSTR",  Symbol.LDCSTR);
        opCodeMap.put("LDADDR",  Symbol.LDADDR);
        opCodeMap.put("LDGADDR", Symbol.LDGADDR);
        opCodeMap.put("LDMEM",   Symbol.LDMEM);
        opCodeMap.put("STORE",   Symbol.STORE);
        opCodeMap.put("STOREB",  Symbol.STOREB);
        opCodeMap.put("STORE2B", Symbol.STORE2B);
        opCodeMap.put("STOREW",  Symbol.STOREW);
        opCodeMap.put("CMP",     Symbol.CMP);
        opCodeMap.put("BR",      Symbol.BR);
        opCodeMap.put("BNZ",     Symbol.BNZ);
        opCodeMap.put("BZ",      Symbol.BZ);
        opCodeMap.put("BG",      Symbol.BG);
        opCodeMap.put("BGE",     Symbol.BGE);
        opCodeMap.put("BL",      Symbol.BL);
        opCodeMap.put("BLE",     Symbol.BLE);
        opCodeMap.put("SHL",     Symbol.SHL);
        opCodeMap.put("SHR",     Symbol.SHR);
        opCodeMap.put("NOT",     Symbol.NOT);
        opCodeMap.put("ADD",     Symbol.ADD);
        opCodeMap.put("SUB",     Symbol.SUB);
        opCodeMap.put("MUL",     Symbol.MUL);
        opCodeMap.put("DIV",     Symbol.DIV);
        opCodeMap.put("MOD",     Symbol.MOD);
        opCodeMap.put("NEG",     Symbol.NEG);
        opCodeMap.put("INC",     Symbol.INC);
        opCodeMap.put("DEC",     Symbol.DEC);
        opCodeMap.put("GETCH",   Symbol.GETCH);
        opCodeMap.put("GETINT",  Symbol.GETINT);
        opCodeMap.put("PUTBYTE", Symbol.PUTBYTE);
        opCodeMap.put("PUTCH",   Symbol.PUTCH);
        opCodeMap.put("PUTINT",  Symbol.PUTINT);
        opCodeMap.put("PUTEOL",  Symbol.PUTEOL);
        opCodeMap.put("PUTSTR",  Symbol.PUTSTR);
        opCodeMap.put("PROGRAM", Symbol.PROGRAM);
        opCodeMap.put("PROC",    Symbol.PROC);
        opCodeMap.put("CALL",    Symbol.CALL);
        opCodeMap.put("RET",     Symbol.RET);
        opCodeMap.put("ALLOC",   Symbol.ALLOC);
      }
  }
