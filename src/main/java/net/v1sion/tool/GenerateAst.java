package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

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
            "Unary    : Token operator, Expr right"
        ));
    }

    private static void defineAst(
            final String outputDir,
            final String baseName,
            final List<String> types) throws IOException {
        final String path = outputDir + "/" + baseName + ".java";
        final PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");
        for(final String type : types) {
            final String className = type.split(":")[0].trim();
            final String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }
        writer.println("}");
        writer.close();
    }

    private static void defineType(
            final PrintWriter writer,
            final String baseName,
            final String className,
            final String fieldList
    ) {
        writer.println(" ".repeat(INDENT) + "static class " + className + " extends " + baseName + " {");

        final String[] fields = fieldList.split(", ");

        for(final String field : fields) {
            writer.println(" ".repeat(INDENT * 2) + "final " + field + ";");
        }

        writer.println();
        writer.println(" ".repeat(INDENT * 2) + className + "(" + fieldList + ") {");
        for(final String field : fields) {
            final String name = field.split(" ")[1];
            writer.println(" ".repeat(INDENT * 3) + "this." + name + " = " + name + ";");
        }
        writer.println(" ".repeat(INDENT * 2) + "}");

        writer.println(" ".repeat(INDENT) + "}");
    }
}
