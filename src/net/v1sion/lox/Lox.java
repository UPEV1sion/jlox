package net.v1sion.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Emanuel Schäffer
 * at 04.02.26
 **/

public class Lox {
    static boolean hadError = false;

    public static void main(final String[] args) throws IOException {
        if (args.length > 1) {
            System.err.println("[Usage] jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runRepl();
        }
    }

    private static void runFile(final String path) throws IOException {
        final byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
    }

    private static void runRepl() throws IOException {
        final InputStreamReader input = new InputStreamReader(System.in);
        final BufferedReader reader = new BufferedReader(input);
        for (; ; ) {
            System.out.print("> ");
            final String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(final String source) {
        final Lexer scanner = new Lexer(source);
        final List<Token> tokens = scanner.scanTokens();
        final Parser parser = new Parser(tokens);
        final Expr expr = parser.parse();

        if (hadError) return;

        System.out.println(new AstPrinter().print(expr));

//        for(final Token token : tokens) {
//            System.out.println(token);
//        }
    }

    static void error(final int line, final String message) {
        report(line, "", message);
    }

    private static void report(final int line, final String where, final String message) {
        System.err.println("[line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }

    static void error(final Token token, final String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at'" + token.lexeme + "'", message);
        }
    }
}
