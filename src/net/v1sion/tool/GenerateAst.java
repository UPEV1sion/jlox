package net.v1sion.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * @author Emanuel Schäffer
 * at 21.02.26
 **/

public class GenerateAst {
    private static final int INDENT = 4;

    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        final String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Unary    : Token operator, Expr right",
            "Ternary  : Expr cond, Expr thenBranch, Expr elseBranch"
        ));
    }

    private static void defineAst(
            final String outputDir,
            final String baseName,
            final List<String> types) throws IOException {
        final String path = outputDir + "/" + baseName + ".java";
        final PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package net.v1sion.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        writer.println(indent(1) + "abstract <R> R accept(final Visitor<R> visitor);");
        writer.println();

        defineVisitor(writer, baseName, types);

        for(final String type : types) {
            final String className = type.split(":")[0].trim();
            final String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(final PrintWriter writer, final String baseName, final List<String> types) {
        writer.println(indent(1) + "interface Visitor<R> {");
        for(final String type : types) {
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
        writer.println(indent(1) + "static class " + className + " extends " + baseName + " {");

        final String[] fields = fieldList.split(", ");

        for(final String field : fields) {
            writer.println(indent(2) + "final " + field + ";");
        }

        writer.println();
        writer.println(indent(2) + className + "(" + fieldList + ") {");
        for(final String field : fields) {
            final String name = field.split(" ")[1];
            writer.println(indent(3) + "this." + name + " = " + name + ";");
        }
        writer.println(indent(2) + "}");

        writer.println();
        writer.println(indent(2) + "@Override");
        writer.println(indent(2) + "<R> R accept (final Visitor<R> visitor) {");
        writer.println(indent(3) + "return visitor.visit" + className + baseName + "(this);");
        writer.println(indent(2) + "}");


        writer.println(indent(1) + "}");
    }

    private static String indent(final int tabs) {
        return " ".repeat(INDENT * tabs);
    }
}
