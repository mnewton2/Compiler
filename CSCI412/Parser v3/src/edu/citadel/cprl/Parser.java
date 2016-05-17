/*
 * Michael Newton CSCI412 Parser v3 03/25/2016
 */

package edu.citadel.cprl;


import edu.citadel.compiler.Position;
import edu.citadel.compiler.ParserException;
import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.InternalAssertion;
import edu.citadel.compiler.InternalCompilerException;
import edu.citadel.cprl.IdTable;
import edu.citadel.cprl.ast.*;

import java.io.IOException;
import java.util.List;
import java.util.LinkedList;


/**
 * This class uses recursive descent to perform syntax analysis of the CPRL
 * source language and to generate an abstract syntax tree.
 */
public class Parser
  {
    /**
     * Symbols that can follow an initial declaration.
     */
    private static final Symbol[] initialDeclFollowers = { Symbol.typeRW,
        Symbol.procedureRW, Symbol.functionRW, Symbol.beginRW };


    /**
     * Symbols that can follow a subprogram declaration.
     */
    private static final Symbol[] subprogDeclFollowers = { Symbol.procedureRW,
        Symbol.functionRW, Symbol.beginRW };


    /**
     * Symbols that can follow a statement.
     */
    private static final Symbol[] stmtFollowers = { Symbol.endRW, Symbol.writeRW,
        Symbol.elsifRW, Symbol.ifRW, Symbol.elseRW, Symbol.loopRW, Symbol.exitRW,
        Symbol.identifier, Symbol.whileRW, Symbol.returnRW, Symbol.readRW,
        Symbol.writelnRW

    };


    /**
     * Symbols that can follow an expression.
     */
    private static final Symbol[] exprFollowers = { Symbol.semicolon, Symbol.loopRW,
        Symbol.thenRW, Symbol.rightParen, Symbol.andRW, Symbol.orRW, Symbol.equals,
        Symbol.notEqual, Symbol.lessThan, Symbol.lessOrEqual, Symbol.greaterThan,
        Symbol.greaterOrEqual, Symbol.plus, Symbol.minus, Symbol.times, Symbol.divide,
        Symbol.modRW, Symbol.rightBracket, Symbol.comma };


    private Scanner scanner;
    private IdTable idTable;
    private Context context;


    /**
     * Construct a parser with the specified scanner.
     */
    public Parser(Scanner scanner)
      {
        this.scanner = scanner;
        idTable = new IdTable();
        context = new Context();
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>program = ( declarativePart )? statementPart "." .</code>
     *
     * @return the parsed Program. Returns a program with empty declarative and
     *         statement parts if parsing fails.
     */
    public Program parseProgram() throws IOException
      {
        DeclarativePart declPart = null;
        StatementPart stmtPart = null;

        try
          {
            if (scanner.getSymbol() == Symbol.declareRW)
              declPart = parseDeclarativePart();
            else
              declPart = new DeclarativePart();

            stmtPart = parseStatementPart();
            match(Symbol.dot);
            match(Symbol.EOF);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            Symbol[] followers = { Symbol.EOF };
            recover(followers);
          }

        return new Program(declPart, stmtPart);
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>declarativePart = "declare" initialDecls subprogramDecls .</code>
     *
     * @return the parsed DeclarativePart. Returns an empty DeclarativePart if
     *         parsing fails.
     */
    public DeclarativePart parseDeclarativePart() throws IOException
      {
        DeclarativePart declPart = null;

        try
          {
            match(Symbol.declareRW);
            List<InitialDecl> initialDecls = parseInitialDecls();
            List<SubprogramDecl> subprogDecls = parseSubprogramDecls();

            declPart = new DeclarativePart(initialDecls, subprogDecls);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            Symbol[] followers = { Symbol.beginRW };
            recover(followers);
          }

        if (declPart == null)
          declPart = new DeclarativePart();

        return declPart;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>initialDecls = ( initialDecl )* .</code>
     *
     * @return the list of InitialDecl objects.
     */
    public List<InitialDecl> parseInitialDecls() throws IOException
      {
        List<InitialDecl> initialDecls = new LinkedList<InitialDecl>();

        while (SymbolUtil.isInitialDeclStarter(scanner.getSymbol()))
          {
            InitialDecl decl = parseInitialDecl();

            if (decl instanceof VarDecl)
              {
                // add the single variable declarations
                VarDecl varDecl = (VarDecl) decl;
                for (SingleVarDecl singleVarDecl : varDecl.getSingleVarDecls())
                  initialDecls.add(singleVarDecl);
              }
            else
              initialDecls.add(decl);
          }

        return initialDecls;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>initialDecl = constDecl | arrayTypeDecl | varDecl .</code>
     *
     * @return the parsed initial declaration. Returns null if parsing fails.
     */
    public InitialDecl parseInitialDecl() throws IOException
      {
        InitialDecl initialDecl = null;

        if (scanner.getSymbol() == Symbol.constRW)
          initialDecl = parseConstDecl();
        else if (scanner.getSymbol() == Symbol.varRW)
          initialDecl = parseVarDecl();
        else if (scanner.getSymbol() == Symbol.typeRW)
          initialDecl = parseArrayTypeDecl();
        else
          throw internalError("Invalid initial declaration.");

        return initialDecl;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>constDecl = "const" constId ":=" literal ";" .</code>
     *
     * @return the parsed ConstDecl. Returns null if parsing fails.
     */
    public ConstDecl parseConstDecl() throws IOException
      {
        ConstDecl constDecl = null;

        try
          {
            match(Symbol.constRW);
            check(Symbol.identifier);
            Token constId = scanner.getToken();
            matchCurrentSymbol();

            match(Symbol.assign);
            Token literal = parseLiteral();
            Type constType = Type.UNKNOWN;

            if (literal != null)
              constType = TypeUtil.getTypeOf(literal.getSymbol());

            constDecl = new ConstDecl(constId, constType, literal);

            match(Symbol.semicolon);
            idTable.add(constDecl);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(initialDeclFollowers);
          }
        return constDecl;
      }


    /**
     * Parse the following grammar rules:<br>
     * <code>literal = intLiteral | charLiteral | stringLiteral | booleanLiteral .<br>
     *    booleanLiteral = "true" | "false" .</code>
     *
     * @return the parsed literal Token. Returns null if parsing fails.
     */
    public Token parseLiteral() throws IOException
      {
        Token literal = null;

        try
          {
            if (SymbolUtil.isLiteral(scanner.getSymbol()))
              {
                literal = scanner.getToken();
                matchCurrentSymbol();
              }
            else
              throw error("Invalid literal expression.");
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(exprFollowers);
          }

        return literal;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>varDecl = "var" identifiers ":" typeName ";" .</code>
     *
     * @return the parsed VarDecl. Returns null if parsing fails.
     */
    public VarDecl parseVarDecl() throws IOException
      {
        VarDecl varDecl = null;

        try
          {
            match(Symbol.varRW);
            List<Token> identifiers = parseIdentifiers();
            match(Symbol.colon);
            Type varType = parseTypeName();
            match(Symbol.semicolon);

            int scopeLevel = idTable.getCurrentLevel();
            varDecl = new VarDecl(identifiers, varType, scopeLevel);

            for (SingleVarDecl decl : varDecl.getSingleVarDecls())
              idTable.add(decl);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(initialDeclFollowers);
          }

        return varDecl;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>identifiers = identifier ( "," identifier )* .</code>
     *
     * @return the list of identifier tokens. Returns an empty list if parsing
     *         fails.
     */
    public List<Token> parseIdentifiers() throws IOException
      {
        List<Token> identifiers = new LinkedList<Token>();

        try
          {
            check(Symbol.identifier);
            identifiers.add(scanner.getToken());
            matchCurrentSymbol();

            while (scanner.getSymbol() == Symbol.comma)
              {
                matchCurrentSymbol(); // match comma
                check(Symbol.identifier);
                identifiers.add(scanner.getToken());
                matchCurrentSymbol();
              }
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            Symbol[] followers = { Symbol.colon };
            recover(followers);
          }

        return identifiers;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>arrayTypeDecl = "type" typeId "=" "array" "[" intConstValue "]" "of" typeName ";" .</code>
     *
     * @return the parsed ArrayTypeDecl. Returns null if parsing fails.
     */
    public ArrayTypeDecl parseArrayTypeDecl() throws IOException
      {
        ArrayTypeDecl arrayTypeDecl = null;
        try
          {
            match(Symbol.typeRW);
            check(Symbol.identifier);
            Token typeId = scanner.getToken();
            matchCurrentSymbol();

            match(Symbol.equals);
            match(Symbol.arrayRW);
            match(Symbol.leftBracket);

            ConstValue intConstValue = parseConstValue();

            match(Symbol.rightBracket);
            match(Symbol.ofRW);
            Type elemType = parseTypeName();
            match(Symbol.semicolon);

            arrayTypeDecl = new ArrayTypeDecl(typeId, elemType, intConstValue);
            idTable.add(arrayTypeDecl);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(initialDeclFollowers);
          }
        return arrayTypeDecl;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>typeName = "Integer" | "Boolean" | "Char" | typeId .</code>
     *
     * @return the parsed Type object. Returns Type.UNKNOWN if parsing fails.
     */
    public Type parseTypeName() throws IOException
      {
        Type type = Type.UNKNOWN;

        try
          {
            if (scanner.getSymbol() == Symbol.IntegerRW)
              {
                type = Type.Integer;
                matchCurrentSymbol();
              }
            else if (scanner.getSymbol() == Symbol.BooleanRW)
              {
                type = Type.Boolean;
                matchCurrentSymbol();
              }
            else if (scanner.getSymbol() == Symbol.CharRW)
              {
                type = Type.Char;
                matchCurrentSymbol();
              }
            else if (scanner.getSymbol() == Symbol.identifier)
              {
                Token typeId = scanner.getToken();
                Declaration decl = idTable.get(typeId);

                if (decl != null)
                  {
                    if (decl instanceof ArrayTypeDecl)
                      {
                        type = decl.getType();
                        matchCurrentSymbol();
                      }
                    else
                      throw error("Identifier \"" + scanner.getToken()
                          + "\" is not a valid type name.");
                  }
                else
                  throw error(
                      "Identifier \"" + scanner.getToken() + "\" has not been declared.");
              }
            else
              throw error("Invalid type name.");
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            Symbol[] followers = { Symbol.semicolon, Symbol.comma, Symbol.rightParen,
                Symbol.isRW };
            recover(followers);
          }

        return type;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>subprogramDecls = ( subprogramDecl )* .</code>
     *
     * @return the list of SubprogramDecl objects.
     */
    public List<SubprogramDecl> parseSubprogramDecls() throws IOException
      {
        List<SubprogramDecl> subprogs = new LinkedList<SubprogramDecl>();

        while (SymbolUtil.isSubprogramDeclStarter(scanner.getSymbol()))
          {
            subprogs.add(parseSubprogramDecl());
          }
        return subprogs;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>subprogramDecl = procedureDecl | functionDecl .</code>
     *
     * @return the parsed SubprogramDecl. Returns null if parsing fails.
     */
    public SubprogramDecl parseSubprogramDecl() throws IOException
      {
        SubprogramDecl subprogramDecl = null;

        if (scanner.getSymbol() == Symbol.procedureRW)
          subprogramDecl = parseProcedureDecl();
        else if (scanner.getSymbol() == Symbol.functionRW)
          subprogramDecl = parseFunctionDecl();
        else
          throw internalError("Invalid declaration of subprogram.");

        return subprogramDecl;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>procedureDecl = "procedure" procId ( formalParameters )?
     *      "is" initialDecls statementPart procId ";" .</code>
     *
     * @return the parsed ProcedureDecl. Returns null if parsing fails.
     */
    public ProcedureDecl parseProcedureDecl() throws IOException
      {
        ProcedureDecl procDecl = null;

        try
          {
            match(Symbol.procedureRW);
            check(Symbol.identifier);
            Token procId = scanner.getToken();
            matchCurrentSymbol();

            procDecl = new ProcedureDecl(procId);
            context.beginSubprogramDecl(procDecl);

            // temporarily add procId to IdTable
            idTable.add(procDecl);
            idTable.openScope();

            if (scanner.getSymbol() == Symbol.leftParen)
              procDecl.setFormalParams(parseFormalParameters());

            match(Symbol.isRW);

            procDecl.setInitialDecls(parseInitialDecls());
            procDecl.setStatementPart(parseStatementPart());

            check(Symbol.identifier);
            Token procId2 = scanner.getToken();
            if (procId.getText().equals(procId2.getText()))
              matchCurrentSymbol();
            else
              throw error("Procedure name mismatch.");

            match(Symbol.semicolon);
            idTable.closeScope();
            idTable.replace(procDecl);
            context.endSubprogramDecl();
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(subprogDeclFollowers);
          }

        return procDecl;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>functionDecl = "function" funcId ( formalParameters )? "return" typeName
     *       "is" initialDecls statementPart funcId ";" .</code>
     *
     * @return the parsed FunctionDecl. Returns null if parsing fails.
     */
    public FunctionDecl parseFunctionDecl() throws IOException
      {
        FunctionDecl funcDecl = null;

        try
          {
            match(Symbol.functionRW);

            check(Symbol.identifier);
            Token funcId = scanner.getToken();
            matchCurrentSymbol();

            funcDecl = new FunctionDecl(funcId);
            context.beginSubprogramDecl(funcDecl);

            idTable.add(funcDecl);
            idTable.openScope();

            if (scanner.getSymbol() == Symbol.leftParen)
              funcDecl.setFormalParams(parseFormalParameters());

            match(Symbol.returnRW);

            funcDecl.setType(parseTypeName());

            match(Symbol.isRW);

            funcDecl.setInitialDecls(parseInitialDecls());
            funcDecl.setStatementPart(parseStatementPart());

            check(Symbol.identifier);
            Token funcId2 = scanner.getToken();

            if (funcId.getText().equals(funcId2.getText()))
              matchCurrentSymbol();
            else
              throw error("Function name mismatch.");

            match(Symbol.semicolon);
            idTable.closeScope();
            idTable.replace(funcDecl);
            context.endSubprogramDecl();
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(subprogDeclFollowers);
          }

        return funcDecl;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>formalParameters = "(" parameterDecl ( "," parameterDecl )* ")" .</code>
     *
     * @return a list of FormalParameters. Returns an empty list if parsing
     *         fails.
     */
    public List<ParameterDecl> parseFormalParameters() throws IOException
      {
        List<ParameterDecl> params = new LinkedList<ParameterDecl>();

        try
          {
            match(Symbol.leftParen);
            params.add(parseParameterDecl());
            while (scanner.getSymbol() == Symbol.comma)
              {
                matchCurrentSymbol();
                params.add(parseParameterDecl());
              }
            match(Symbol.rightParen);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            Symbol[] followers = { Symbol.returnRW, Symbol.isRW };
            recover(followers);
          }
        return params;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>parameterDecl = ( "var" )? paramId ":" typeName .</code>
     *
     * @return the parsed ParameterDecl. Returns null if parsing fails.
     */
    public ParameterDecl parseParameterDecl() throws IOException
      {
        ParameterDecl paramDecl = null;

        try
          {
            boolean isVarParam = false;
            if (scanner.getSymbol() == Symbol.varRW)
              {
                isVarParam = true;
                matchCurrentSymbol();
              }

            check(Symbol.identifier);
            Token paramId = scanner.getToken();
            matchCurrentSymbol();
            match(Symbol.colon);

            Type paramType = parseTypeName();
            int scopeLevel = idTable.getCurrentLevel();
            paramDecl = new ParameterDecl(paramId, paramType, scopeLevel, isVarParam);
            idTable.add(paramDecl);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            Symbol[] followers = { Symbol.comma, Symbol.rightParen };
            recover(followers);
          }

        return paramDecl;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>statementPart = "begin" statements "end" .</code>
     *
     * @return the parsed StatementPart. Returns a StatementPart with a null
     *         list of statements if parsing fails.
     */
    public StatementPart parseStatementPart() throws IOException
      {
        List<Statement> statements = null;

        try
          {
            match(Symbol.beginRW);
            statements = parseStatements();
            match(Symbol.endRW);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            Symbol[] followers = { Symbol.dot, Symbol.identifier };
            recover(followers);
          }

        return new StatementPart(statements);
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>statements = ( statement )* .</code>
     *
     * @return a list of Statements. Returns an empty list if parsing fails.
     */
    public List<Statement> parseStatements() throws IOException
      {
        List<Statement> statements = new LinkedList<Statement>();

        while (SymbolUtil.isStmtStarter(scanner.getSymbol()))
          statements.add(parseStatement());

        return statements;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>statement = assignmentStmt | ifStmt | loopStmt | exitStmt
     *     | inputStmt | outputStmt | procedureCallStmt | returnStmt .</code>
     *
     * @return the parsed Statement. Returns null if parsing fails.
     */
    public Statement parseStatement() throws IOException
      {
        Statement stmt = null;

        try
          {
            Symbol symbol = scanner.getSymbol();

            if (symbol == Symbol.identifier)
              {
                Declaration decl = idTable.get(scanner.getToken());

                if (decl != null)
                  {
                    if (decl instanceof NamedDecl)
                      stmt = parseAssignmentStmt();
                    else if (decl instanceof ProcedureDecl)
                      stmt = parseProcedureCallStmt();
                    else
                      {
                        throw error("Identifier \"" + scanner.getToken()
                            + "\" cannot start a statement.");
                      }
                  }
                else
                  throw error(
                      "Identifier \"" + scanner.getToken() + "\" has not been declared.");
              }
            else if (symbol == Symbol.ifRW)
              stmt = parseIfStmt();
            else if (symbol == Symbol.loopRW || symbol == Symbol.whileRW)
              stmt = parseLoopStmt();
            else if (symbol == Symbol.exitRW)
              stmt = parseExitStmt();
            else if (symbol == Symbol.readRW)
              stmt = parseReadStmt();
            else if (symbol == Symbol.writeRW | symbol == Symbol.writelnRW)
              stmt = parseOutputStmt();
            else if (symbol == Symbol.returnRW)
              stmt = parseReturnStmt();
            else
              throw internalError("Invalid statement.");
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);

            // Error recovery here is complicated for identifiers since they can
            // both
            // start a statement and appear elsewhere in the statement.
            // (Consider,
            // for example, an assignment statement or a procedure call
            // statement.)
            // Since the most common error is to declare or reference an
            // identifier
            // incorrectly, we will assume that this is the case and advance to
            // the
            // next semicolon, which hopefully ends the erroneous statement.
            if (scanner.getSymbol() == Symbol.identifier)
              scanner.advanceTo(Symbol.semicolon);

            recover(stmtFollowers);
          }

        return stmt;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>assignmentStmt = variable ":=" expression ";" .</code>
     *
     * @return the parsed AssignmentStmt. Returns null if parsing fails.
     */
    public AssignmentStmt parseAssignmentStmt() throws IOException
      {
        AssignmentStmt stmt = null;
        Position pos = null;

        try
          {
            Variable var = parseVariable();
            pos = scanner.getPosition();

            try
              {
                match(Symbol.assign);
              }
            catch (ParserException e)
              {
                if (scanner.getSymbol() == Symbol.equals)
                  {
                    ErrorHandler.getInstance().reportError(e);
                    matchCurrentSymbol(); // treat "=" as ":=" in this context
                  }
              }
            Expression expr = parseExpression();
            match(Symbol.semicolon);

            stmt = new AssignmentStmt(var, expr, pos);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(stmtFollowers);
          }

        return stmt;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>ifStmt = "if" booleanExpr "then" statements<br>
     *       ( "elsif" booleanExpr "then" statements )*<br>
     *       ( "else" statements )? "end" "if" ";" .</code>
     *
     * @return the parsed IfStmt. Returns null if parsing fails.
     */
    public IfStmt parseIfStmt() throws IOException
      {
        IfStmt stmt = null;

        try
          {
            match(Symbol.ifRW);

            Expression expr = parseExpression();

            match(Symbol.thenRW);
            List<Statement> stmts = parseStatements();
            stmt = new IfStmt(expr, stmts);

            while (scanner.getSymbol() == Symbol.elsifRW)
              {
                matchCurrentSymbol();
                expr = parseExpression();
                match(Symbol.thenRW);
                stmts = parseStatements();
                stmt.addElsifPart(expr, stmts);
              }

            if (scanner.getSymbol() == Symbol.elseRW)
              {
                matchCurrentSymbol();
                stmts = parseStatements();
                stmt.addElseStmts(stmts);
              }

            match(Symbol.endRW);
            match(Symbol.ifRW);
            match(Symbol.semicolon);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(stmtFollowers);
          }
        return stmt;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>loopStmt = ( "while" booleanExpr )? "loop" statements
     *       "end" "loop" ";" .</code>
     *
     * @return the parsed LoopStmt. Returns a LoopStmt with null while
     *         expression and empty list of statements if parsing fails.
     */
    public LoopStmt parseLoopStmt() throws IOException
      {
        LoopStmt stmt = new LoopStmt();

        try
          {
            
            if (scanner.getSymbol() == Symbol.whileRW)
              {
                matchCurrentSymbol();
                Expression expr = parseExpression();
                stmt.setWhileExpr(expr);
              }

            match(Symbol.loopRW);
            context.beginLoop(stmt);
            List<Statement> stmts = parseStatements();
            stmt.setStatements(stmts);
            context.endLoop();

            match(Symbol.endRW);
            match(Symbol.loopRW);
            match(Symbol.semicolon);

          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(stmtFollowers);
          }

        return stmt;

      }


    /**
     * Parse the following grammar rule:<br>
     * <code>exitStmt = "exit" ( "when" booleanExpr )? ";" .</code>
     *
     * @return the parsed ExitStmt. Returns null if parsing fails.
     */
    public ExitStmt parseExitStmt() throws IOException
      {
        ExitStmt stmt = null;
        Expression expr = null;

        try
          {
            check(Symbol.exitRW);
            Position exitPosition = scanner.getPosition();
            matchCurrentSymbol();

            if (scanner.getSymbol() == Symbol.whenRW)
              {
                matchCurrentSymbol();
                expr = parseExpression();
              }

            LoopStmt loopStmt = context.getLoopStmt();
            if (loopStmt == null)
              throw error(exitPosition, "Exit statement is not nested within a loop");

            stmt = new ExitStmt(expr, loopStmt);

            match(Symbol.semicolon);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(stmtFollowers);
          }
        return stmt;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>readStmt = "read" variable ";" .</code>
     *
     * @return the parsed ReadStmt. Returns null if parsing fails.
     */
    public ReadStmt parseReadStmt() throws IOException
      {
        ReadStmt stmt = null;

        try
          {
            match(Symbol.readRW);
            stmt = new ReadStmt(parseVariable());
            match(Symbol.semicolon);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(stmtFollowers);
          }
        return stmt;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>outputStmt = writeStmt | writelnStmt .</code>
     *
     * @return the parsed OutputStmt. Returns null if parsing fails.
     */
    public OutputStmt parseOutputStmt() throws IOException
      {
        OutputStmt stmt = null;

        if (scanner.getSymbol() == Symbol.writeRW)
          stmt = parseWriteStmt();
        else if (scanner.getSymbol() == Symbol.writelnRW)
          stmt = parseWritelnStmt();
        else
          throw internalError("Invalid statement.");

        return stmt;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>writeStmt = "write" expression ( "," expression )* ";" .</code>
     *
     * @return the parsed WriteStmt. Returns null if parsing fails.
     */
    public WriteStmt parseWriteStmt() throws IOException
      {
        WriteStmt stmt = null;
        List<Expression> expressions = new LinkedList<Expression>();

        try
          {
            match(Symbol.writeRW);

            expressions.add(parseExpression());

            while (scanner.getSymbol() == Symbol.comma)
              {
                matchCurrentSymbol();
                expressions.add(parseExpression());
              }


            stmt = new WriteStmt(expressions);
            match(Symbol.semicolon);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(stmtFollowers);
          }

        return stmt;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>writelnStmt = "writeln" ( expression ( "," expression )* )? ";" .</code>
     *
     * @return the parsed WritelnStmt. Returns null if parsing fails.
     */
    public WritelnStmt parseWritelnStmt() throws IOException
      {
        WritelnStmt stmt = null;
        List<Expression> expressions = new LinkedList<Expression>();

        try
          {
            match(Symbol.writelnRW);

            if (SymbolUtil.isExprStarter(scanner.getSymbol()))
              {
                expressions.add(parseExpression());

                while (scanner.getSymbol() == Symbol.comma)
                  {
                    matchCurrentSymbol();
                    expressions.add(parseExpression());
                  }
              }

            stmt = new WritelnStmt(expressions);
            match(Symbol.semicolon);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(stmtFollowers);
          }

        return stmt;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>procedureCallStmt = procId ( actualParameters )? ";" .</code>
     *
     * @return the parsed ProcedureCallStmt. Returns null if parsing fails.
     */
    public ProcedureCallStmt parseProcedureCallStmt() throws IOException
      {
        ProcedureCallStmt procCall = null;

        try
          {
            check(Symbol.identifier);
            Token procId = scanner.getToken();
            ProcedureDecl procDecl = (ProcedureDecl) idTable.get(procId);
            matchCurrentSymbol();

            List<Expression> actualParams = new LinkedList<>();
            if (scanner.getSymbol() == Symbol.leftParen)
              actualParams = parseActualParameters();

            procCall = new ProcedureCallStmt(procId, actualParams, procDecl);
            match(Symbol.semicolon);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(stmtFollowers);
          }
        return procCall;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>actualParameters = "(" expression ( "," expression )* ")" .</code>
     *
     * @return a list of Expressions. Returns an empty list if parsing fails.
     */
    public List<Expression> parseActualParameters() throws IOException
      {
        List<Expression> params = new LinkedList<Expression>();

        try
          {
            match(Symbol.leftParen);
            params.add(parseExpression());

            while (scanner.getSymbol() == Symbol.comma)
              {
                matchCurrentSymbol();
                params.add(parseExpression());
              }

            match(Symbol.rightParen);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(exprFollowers);
          }
        return params;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>returnStmt = "return" ( expression )? ";" .</code>
     *
     * @return the parsed ReturnStmt. Returns null if parsing fails.
     */
    public ReturnStmt parseReturnStmt() throws IOException
      {
        ReturnStmt stmt = null;

        try
          {
            check(Symbol.returnRW);
            // save position for error reporting
            Position returnPosition = scanner.getPosition();
            matchCurrentSymbol();

            Expression returnExpr = null;
            if (SymbolUtil.isExprStarter(scanner.getSymbol()))
              returnExpr = parseExpression();

            SubprogramDecl subprogDecl = context.getSubprogramDecl();
            if (subprogDecl == null)
              throw error(returnPosition,
                  "Return statement is not nested within a subprogram");

            stmt = new ReturnStmt(subprogDecl, returnExpr);
            match(Symbol.semicolon);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(stmtFollowers);
          }

        return stmt;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>variable = ( varId | varParamId | valueParamId ) ( "[" expression "]" )* .</code>
     *
     * @return the parsed Variable. Returns null if parsing fails.
     */
    public Variable parseVariable() throws IOException
      {
        Variable var = null;

        try
          {
            check(Symbol.identifier);
            Token token = scanner.getToken();
            matchCurrentSymbol();

            Declaration decl = idTable.get(token);

            if (decl == null)
              throw error("Identifier \"" + token + "\" has not been declared.");
            else if (!(decl instanceof NamedDecl))
              throw error("Identifier \"" + token + "\" is not a variable.");

            NamedDecl namedDecl = (NamedDecl) decl;

            // compute relative level of the declaration
            int relLevel = idTable.getCurrentLevel() - namedDecl.getScopeLevel();

            var = new Variable(namedDecl, relLevel, scanner.getPosition());

            List<Expression> indexExprs = new LinkedList<Expression>();
            while (scanner.getSymbol() == Symbol.leftBracket)
              {
                matchCurrentSymbol();
                indexExprs.add(parseExpression());
                match(Symbol.rightBracket);
              }

            var.setIndexExprs(indexExprs);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            Symbol[] followers = { Symbol.assign, Symbol.semicolon };
            recover(followers);
          }

        return var;
      }


    /**
     * Parse the following grammar rules:<br>
     * <code>expression = relation ( logicalOp relation )* .<br>
     *       logicalOp = "and" | "or" . </code>
     *
     * @return the parsed Expression. Returns null if parsing fails.
     */
    public Expression parseExpression() throws IOException
      {
        Expression expr = parseRelation();

        while (SymbolUtil.isLogicalOperator(scanner.getSymbol()))
          {
            Token operator = scanner.getToken();
            matchCurrentSymbol();
            Expression expr2 = parseRelation();
            expr = new LogicalExpr(expr, operator, expr2);
          }

        return expr;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>relation = simpleExpr ( relationalOp simpleExpr )? .<br>
     *   relationalOp = "=" | "!=" | "&lt;" | "&lt;=" | "&gt;" | "&gt;=" .</code>
     *
     * @return the parsed relation Expression. Returns null if parsing fails.
     */
    public Expression parseRelation() throws IOException
      {
        Expression expr = parseSimpleExpr();
        if (SymbolUtil.isRelationalOperator(scanner.getSymbol()))
          {
            Token operator = scanner.getToken();
            matchCurrentSymbol();
            Expression expr2 = parseSimpleExpr();
            expr = new RelationalExpr(expr, operator, expr2);
          }
        return expr;
      }


    /**
     * Parse the following grammar rules:<br>
     * <code>simpleExpr = ( addingOp )? term ( addingOp term )* .<br>
     *       addingOp = "+" | "-" .</code>
     *
     * @return the parsed simple Expression. Returns null if parsing fails.
     */
    public Expression parseSimpleExpr() throws IOException
      {
        Expression expr = null;
        Token operator = null;

        if (SymbolUtil.isAddingOperator(scanner.getSymbol()))
          {
            if (scanner.getSymbol() == Symbol.minus)
              operator = scanner.getToken();
            // leave operator as null if it is "+"

            matchCurrentSymbol();
          }

        expr = parseTerm();

        if (operator != null)
          expr = new NegationExpr(operator, expr);

        while (SymbolUtil.isAddingOperator(scanner.getSymbol()))
          {
            operator = scanner.getToken();
            matchCurrentSymbol();
            Expression expr2 = parseTerm();
            expr = new AddingExpr(expr, operator, expr2);
          }
        return expr;
      }


    /**
     * Parse the following grammar rules:<br>
     * <code>term = factor ( multiplyingOp factor )* .<br>
     *       multiplyingOp = "*" | "/" | "mod" .</code>
     *
     * @return the parsed term Expression. Returns null if parsing fails.
     */
    public Expression parseTerm() throws IOException
      {
        Expression expr = parseFactor();
        while (SymbolUtil.isMultiplyingOperator(scanner.getSymbol()))
          {
            Token operator = scanner.getToken();
            matchCurrentSymbol();
            Expression expr2 = parseFactor();
            expr = new MultiplyingExpr(expr, operator, expr2);
          }

        return expr;

      }


    /**
     * Parse the following grammar rule:<br>
     * <code>factor = "not" factor | constValue | namedValue | functionCall
     *              | "(" expression ")" .</code>
     *
     * @return the parsed factor Expression. Returns null if parsing fails.
     */
    public Expression parseFactor() throws IOException
      {
        Expression expr = null;

        try
          {
            if (scanner.getSymbol() == Symbol.notRW)
              {
                Token operator = scanner.getToken();
                matchCurrentSymbol();
                Expression factorExpr = parseFactor();
                expr = new NotExpr(operator, factorExpr);
              }
            else if (SymbolUtil.isLiteral(scanner.getSymbol()))
              {
                // Handle constant literals separately from constant
                // identifiers.
                expr = parseConstValue();
              }
            else if (scanner.getSymbol() == Symbol.identifier)
              {
                // Handle identifiers based on whether they are
                // declared as variables, constants, or functions.
                Token idToken = scanner.getToken();
                Declaration decl = idTable.get(idToken);

                if (decl != null)
                  {
                    if (decl instanceof ConstDecl)
                      expr = parseConstValue();
                    else if (decl instanceof NamedDecl)
                      expr = parseNamedValue();
                    else if (decl instanceof FunctionDecl)
                      expr = parseFunctionCall();
                    else
                      throw internalError(
                          "Invalid declaration for identifier \"" + idToken + "\".");
                  }
                else
                  throw error(
                      "Identifier \"" + scanner.getToken() + "\" has not been declared.");
              }
            else if (scanner.getSymbol() == Symbol.leftParen)
              {
                matchCurrentSymbol();
                expr = parseExpression();
                match(Symbol.rightParen);
              }
            else
              throw error("Invalid expression.");
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(exprFollowers);
          }

        return expr;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>constValue = literal | constId .</code>
     *
     * @return the parsed ConstValue. Returns null if parsing fails.
     */
    public ConstValue parseConstValue() throws IOException
      {
        ConstValue constVal = null;


        if (SymbolUtil.isLiteral(scanner.getSymbol()))
          {
            Token literal = parseLiteral();
            constVal = new ConstValue(literal);
          }
        else if (scanner.getSymbol() == Symbol.identifier)
          {
            Token constId = scanner.getToken();
            matchCurrentSymbol();
            Declaration decl = idTable.get(constId);
            ConstDecl constDecl = (ConstDecl) decl;
            InternalAssertion.check(decl != null && constDecl instanceof ConstDecl,
                "invalid declaration for constant " + constId.getText());

            constVal = new ConstValue(constId, constDecl);
          }
        else
          throw internalError("Invalid constant value.");

        return constVal;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>namedValue = variable .</code>
     *
     * @return the parsed NamedValue. Returns null if parsing fails.
     */
    public NamedValue parseNamedValue() throws IOException
      {

        // Rather than call parseVariable() here, the code for parseVariable()
        // is
        // essentially repeated. The way that variable and named value are used
        // in
        // CPRL, error recovery is different since they have different follow
        // sets.

        NamedValue namedValue = null;

        try
          {
            check(Symbol.identifier);
            Token idToken = scanner.getToken();
            matchCurrentSymbol();

            Declaration decl = idTable.get(idToken);

            InternalAssertion.check(decl != null && decl instanceof NamedDecl,
                "invalid declaration for function " + idToken.getText());

            NamedDecl namedDecl = (NamedDecl) decl;

            namedValue = new NamedValue(namedDecl, idToken.getPosition());

            List<Expression> indexExprs = new LinkedList<>();
            while (scanner.getSymbol() == Symbol.leftBracket)
              {
                matchCurrentSymbol();
                indexExprs.add(parseExpression());
                match(Symbol.rightBracket);
              }

            namedValue.setIndexExprs(indexExprs);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(exprFollowers);
          }

        return namedValue;
      }


    /**
     * Parse the following grammar rule:<br>
     * <code>functionCall = functId ( actualParameters )? .</code>
     *
     * @return the parsed FunctionCall. Returns null if parsing fails.
     */
    public FunctionCall parseFunctionCall() throws IOException
      {
        FunctionCall funcCall = null;

        try
          {
            check(Symbol.identifier);
            Token funcId = scanner.getToken();
            FunctionDecl funcDecl = (FunctionDecl) idTable.get(funcId);
            matchCurrentSymbol();

            List<Expression> actualParams = new LinkedList<>();
            if (scanner.getSymbol() == Symbol.leftParen)
              actualParams = parseActualParameters();

            funcCall = new FunctionCall(funcId, actualParams, funcDecl);
          }
        catch (ParserException e)
          {
            ErrorHandler.getInstance().reportError(e);
            recover(exprFollowers);
          }

        return funcCall;
      }


    // Utility parsing methods


    /**
     * Check that the current scanner symbol is the expected symbol and throw a
     * ParserException object if it isn't. The scanner is not advanced.
     */
    private void check(Symbol expectedSymbol) throws ParserException
      {
        if (scanner.getSymbol() != expectedSymbol)
          {
            String errorMsg = "Expecting \"" + expectedSymbol + "\" but found \""
                + scanner.getSymbol() + "\" instead.";
            throw error(errorMsg);
          }
      }


    /**
     * Check that the current scanner symbol is the expected symbol. If it is,
     * then advance the scanner. Otherwise, throw a ParserException object.
     */
    private void match(Symbol expectedSymbol) throws IOException, ParserException
      {
        if (scanner.getSymbol() == expectedSymbol)
          scanner.advance();
        else
          {
            String errorMsg = "Expecting \"" + expectedSymbol + "\" but found \""
                + scanner.getSymbol() + "\" instead.";
            throw error(errorMsg);
          }
      }


    /**
     * Advance the scanner. This method represents an unconditional match with
     * the current scanner symbol.
     */
    private void matchCurrentSymbol() throws IOException
      {
        scanner.advance();
      }


    /**
     * Advance the scanner until the current symbol is one of the symbols in the
     * specified array of follows.
     */
    private void recover(Symbol[] followers) throws IOException
      {
        scanner.advanceTo(followers);
      }


    /**
     * Create a ParserException with the specified message and the current
     * scanner position.
     */
    private ParserException error(String message)
      {
        Position errorPosition = scanner.getToken().getPosition();
        return new ParserException(errorPosition, message);
      }


    /**
     * Create a ParserException with the specified error position and message.
     */
    public ParserException error(Position errorPosition, String message)
      {
        return new ParserException(errorPosition, message);
      }


    /**
     * Create an InternalCompilerException with the specified message and the
     * current scanner position.
     */
    public InternalCompilerException internalError(String message)
      {
        Position errorPosition = scanner.getToken().getPosition();
        return new InternalCompilerException(errorPosition, message);
      }
  }
