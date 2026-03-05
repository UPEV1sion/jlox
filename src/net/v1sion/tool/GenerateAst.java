package net.v1sion.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Emanuel Schäffer
 * at 21.02.26
 **/

public class GenerateAst {
    private static final int TAB_SIZE = 4;

    public static void main(final String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        final String outputDir = args[0];
        defineAst(outputDir, "Expr", List.of(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Logical  : Expr left, Token operator, Expr right",
                "Unary    : Token operator, Expr right",
                "Ternary  : Expr cond, Expr thenBranch, Expr elseBranch",
                "Variable : Token name"
        ));

        defineAst(outputDir, "Stmt", List.of(
                "Block      : List<Stmt> statements",
                "Expression : Expr expression",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt body"
        ));
    }

    private static void defineAst(
            final String outputDir,
            final String baseName,
            final List<String> types) throws IOException {
        final String path = outputDir + "/" + baseName + ".java";

        try (final PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8)) {
            writer.println("package net.v1sion.lox;");
            writer.println();
            writer.println("import java.util.List;");
            writer.println();
            defineHeader(writer);
            writer.println();
            writer.println("public interface " + baseName + " {");

            writer.println(indent(1) + "<R> R accept(final Visitor<R> visitor);");
            writer.println();

            defineVisitor(writer, baseName, types);

            for (final String type : types) {
                final String className = type.split(":")[0].trim();
                final String fields = type.split(":")[1].trim();
                defineType(writer, baseName, className, fields);
            }

            writer.println("}");
        }
    }

    private static void defineHeader(final PrintWriter writer) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        writer.println("/**");
        writer.println(" * @author Emanuel Schäffer");
        writer.println(" * at 21.02.26, revision " + LocalDate.now().format(formatter));
        writer.println(" **/");
    }

    private static void defineVisitor(final PrintWriter writer, final String baseName, final List<String> types) {
        writer.println(indent(1) + "interface Visitor<R> {");
        for (final String type : types) {
            final String typeName = type.split(":")[0].trim();
            writer.println(indent(2) + "R visit" + typeName + baseName +
                           "(final " + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println(indent(1) + "}");
    }

    private static void defineType(
            final PrintWriter writer,
            final String baseName,
            final String className,
            final String fieldList
    ) {
        writer.println();
        writer.println(indent(1) + "record " + className + "(" + fieldList + ")"
                + " implements " + baseName + " {");

        writer.println();
        writer.println(indent(2) + "@Override");
        writer.println(indent(2) + "public <R> R accept (final Visitor<R> visitor) {");
        writer.println(indent(3) + "return visitor.visit" + className + baseName + "(this);");
        writer.println(indent(2) + "}");


        writer.println(indent(1) + "}");
    }

    private static String indent(final int tabs) {
        return " ".repeat(TAB_SIZE * tabs);
    }
}
