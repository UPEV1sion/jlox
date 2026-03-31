package net.v1sion.lox;

import java.util.List;

import static net.v1sion.lox.Expr.*;

/**
 * @author Emanuel Schäffer
 * at 22.02.26
 **/

// TODO
public class AstPrinter implements Stmt.Visitor<String>, Expr.Visitor<String> {
    public static void main(final String[] args) {
        final Expr comma = new Binary(
                new Binary(
                        new Literal(1),
                        new Token(TokenType.PLUS, "+", null, 1),
                        new Literal(2)
                ),
                new Token(TokenType.COMMA, ",", null, 1),
                new Binary(
                        new Literal(4),
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Literal(3)
                )
        );

        final Expr expr = new Binary(
                new Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Literal(123)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Grouping(new Literal(45.67))
        );

        final Expr logical = new Logical(
                new Literal(false),
                new Token(TokenType.AND, "and", null, -1),
                new Literal(true)
        );

        final Expr ast = new Assign(
                new Token(TokenType.IDENTIFIER, "a", null, 1),
                new Binary(
                        new Grouping(
                                new Binary(
                                        new Literal(1),
                                        new Token(TokenType.PLUS, "+", null, 1),
                                        new Literal(2)
                                )
                        ),
                        new Token(TokenType.STAR, "*", null, 1),
                        new Literal(3)
                )
        );

        System.out.println(new AstPrinter().print(ast));
    }

    String print(final Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitAssignExpr(final Assign expr) {
        return parenthesize("assign " + expr.name().lexeme(), expr.value());
    }

    @Override
    public String visitBinaryExpr(final Binary expr) {
        return parenthesize(expr.operator().lexeme(), expr.left(), expr.right());
    }

    @Override
    public String visitCallExpr(final Call expr) {
        return parenthesize("call", concat(expr.callee(), expr.arguments()));
    }

    @Override
    public String visitGetExpr(final Get expr) {
        return "";
    }

    @Override
    public String visitSetExpr(final Set expr) {
        return "";
    }

    @Override
    public String visitThisExpr(This expr) {
        return "";
    }

    @Override
    public String visitGroupingExpr(final Grouping expr) {
        return parenthesize("group", expr.expression());
    }

    @Override
    public String visitLiteralExpr(final Literal expr) {
        if (expr.value() == null) return "nil";
        return expr.value().toString();
    }

    @Override
    public String visitLogicalExpr(final Logical expr) {
        return parenthesize(expr.operator().lexeme(), expr.left(), expr.right());
    }

    @Override
    public String visitUnaryExpr(final Unary expr) {
        return parenthesize(expr.operator().lexeme(), expr.right());
    }

    @Override
    public String visitTernaryExpr(final Ternary expr) {
        return parenthesize("if", expr.cond(), expr.thenBranch(), expr.elseBranch());
    }

    @Override
    public String visitVariableExpr(final Variable expr) {
        return expr.name().lexeme();
    }

    @Override
    public String visitFunctionExpr(final Function expr) {
        return "<fn>";
    }

    private String parenthesize(final String name, final Expr... exprs) {
        final StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (final Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    private Expr[] concat(final Expr first, final List<Expr> rest) {
        final Expr[] result = new Expr[rest.size() + 1];
        result[0] = first;
        for (int i = 0; i < rest.size(); i++) {
            result[i + 1] = rest.get(i);
        }
        return result;
    }

    @Override
    public String visitBlockStmt(final Stmt.Block stmt) {
        return "";
    }

    @Override
    public String visitClassStmt(final Stmt.Class stmt) {
        return "";
    }

    @Override
    public String visitExpressionStmt(final Stmt.Expression stmt) {
        return "";
    }

    @Override
    public String visitFunctionStmt(final Stmt.Function stmt) {
        return "";
    }

    @Override
    public String visitIfStmt(final Stmt.If stmt) {
        return "";
    }

    @Override
    public String visitPrintStmt(final Stmt.Print stmt) {
        return "";
    }

    @Override
    public String visitReturnStmt(final Stmt.Return stmt) {
        return "";
    }

    @Override
    public String visitVarStmt(final Stmt.Var stmt) {
        return "";
    }

    @Override
    public String visitWhileStmt(final Stmt.While stmt) {
        return "";
    }

    @Override
    public String visitBreakStmt(final Stmt.Break stmt) {
        return "";
    }

    @Override
    public String visitContinueStmt(final Stmt.Continue stmt) {
        return "";
    }
}
