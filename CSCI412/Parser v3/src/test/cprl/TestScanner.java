package test.cprl;


import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.Source;
import edu.citadel.cprl.Scanner;
import edu.citadel.cprl.Symbol;
import edu.citadel.cprl.Token;

import java.io.*;


public class TestScanner
  {
    public static void main(String[] args)
      {
        try
          {
            System.out.println("initializing...");
            System.out.println();

            String fileName = args[0];
            FileReader fileReader = new FileReader(fileName);

            Source  source  = new Source(fileReader);
            Scanner scanner = new Scanner(source);
            
            // write error messages to System.out
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.setPrintWriter(new PrintWriter(System.out, true));

            Token token;

            System.out.println("starting main loop...");
            System.out.println();

            do
              {
                token = scanner.getToken();
                printToken(token);
                scanner.advance();
              }
            while (token.getSymbol() != Symbol.EOF);

            System.out.println();
            System.out.println("...done");
          }
        catch (Exception e)
          {
            e.printStackTrace();
          }
      }


    public static void printToken(Token token)
      {
        System.out.printf("line: %2d   char: %2d   token: ", 
            token.getPosition().getLineNumber(),
            token.getPosition().getCharNumber());
        if (   token.getSymbol() == Symbol.identifier
            || token.getSymbol() == Symbol.intLiteral
            || token.getSymbol() == Symbol.stringLiteral
            || token.getSymbol() == Symbol.charLiteral)
            System.out.print(token.getSymbol().toString() + " -> ");
        System.out.println(token.getText());
      }
  }
