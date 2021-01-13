package edu.tarleton.drduplex;

import com.github.javaparser.JavaToken;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.Properties;

/**
 * The visitor that implements normalization, such as adding curly braces.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class NormalizingVisitor extends VoidVisitorAdapter<Void> {

    private final boolean ignoreParentheses;
    private final boolean ignoreAnnotations;
    private final boolean ignoreUnaryAtLiterals;

    public NormalizingVisitor(Properties conf) {
        ignoreParentheses = Boolean.parseBoolean(conf.getProperty("ignoreParentheses", "false"));
        ignoreUnaryAtLiterals = Boolean.parseBoolean(conf.getProperty("ignoreUnaryAtLiterals", "false"));
        ignoreAnnotations = Boolean.parseBoolean(conf.getProperty("ignoreAnnotations", "false"));
    }

    @Override
    public void visit(EnclosedExpr n, Void arg) {
        if (ignoreParentheses) {
            removeParentheses(n);
        }
        super.visit(n, arg);
    }

    private void removeParentheses(EnclosedExpr n) {
        TokenRange range = n.getTokenRange().get();
        JavaToken lparen = range.getBegin();
        lparen.deleteToken();
        JavaToken rparen = range.getEnd();
        rparen.deleteToken();
    }

    @Override
    public void visit(NormalAnnotationExpr n, Void arg) {
        if (ignoreAnnotations) {
            deleteAnnotation(n);
        }
        super.visit(n, arg);
    }

    private void deleteAnnotation(AnnotationExpr n) {
        TokenRange range = n.getTokenRange().get();
        JavaToken tok = range.getBegin();
        JavaToken end = range.getEnd();
        while (tok != end) {
            tok.setText("");
            tok = tok.getNextToken().get();
        }
        tok.setText("");
    }

    @Override
    public void visit(MarkerAnnotationExpr n, Void arg) {
        if (ignoreAnnotations) {
            deleteAnnotation(n);
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(SingleMemberAnnotationExpr n, Void arg) {
        if (ignoreAnnotations) {
            deleteAnnotation(n);
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(UnaryExpr n, Void arg) {
        if (ignoreUnaryAtLiterals) {
            removeUnary(n);
        }
        super.visit(n, arg);
    }

    private void removeUnary(UnaryExpr n) {
        switch (n.getOperator()) {
            case PLUS:
            case MINUS:
                Expression expr = n.getExpression();
                if (expr.isDoubleLiteralExpr()
                        || expr.isIntegerLiteralExpr()
                        || expr.isLongLiteralExpr()) {
                    TokenRange range = n.getTokenRange().get();
                    JavaToken tok = range.getBegin();
                    tok.deleteToken();
                }
        }
    }
}
