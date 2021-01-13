package edu.tarleton.drduplex.index.compressed;

import com.github.javaparser.JavaToken;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.AssertStmt;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import edu.tarleton.drduplex.clones.Pos;
import edu.tarleton.drduplex.index.CompressedIndexBuilder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * The builder that builds the compressed statement-level index.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class SimplifiedLexCompressedIndexStmtBuilder extends CompressedIndexBuilder {

    private CTrie trie = new CTrie();
    private String srcFile;
    private final List<String> buffer = new ArrayList<>();
    private Pos prevStmt;

    public SimplifiedLexCompressedIndexStmtBuilder(Properties conf, Path srcDir) {
        super(conf, srcDir);
    }

    @Override
    public CTrie getTrie() {
        return trie;
    }

    @Override
    public List<String> getBuffer() {
        return buffer;
    }

    @Override
    public void reset() {
        trie = new CTrie();
    }

    private void appendStmt(Statement stmt) {
        Optional<TokenRange> opt = stmt.getTokenRange();
        if (opt.isPresent()) {
            appendRange(opt.get(), pos(stmt));
        }
    }

    private void appendRange(TokenRange range, Pos pos) {
        JavaToken token = range.getBegin();
        JavaToken end = range.getEnd();
        int start = buffer.size();
        while (token != end) {
            if ("".equals(token.getText())) {
                token = token.getNextToken().orElse(null);
                continue;
            }
            switch (token.getCategory()) {
                case COMMENT:
                case EOL:
                case WHITESPACE_NO_EOL:
                    break;
                case IDENTIFIER:
                    buffer.add("id");
                    break;
                case LITERAL:
                    buffer.add("literal");
                    break;
                case KEYWORD: {
                    String s = token.getText();
                    if (isPrimitiveType(s)
                            || (treatSuperThisAsIdentifier && isSuperThis(s))) {
                        buffer.add("id");
                    } else if (isLiteral(s)) {
                        buffer.add("literal");
                    } else {
                        buffer.add(s);
                    }
                    break;
                }
                default:
                    buffer.add(token.getText());
            }
            token = token.getNextToken().orElse(null);
        }
        buffer.add(token.getText());
        CTrieNode p = trie.getRoot();
        p.addEdge(buffer, start, buffer.size(), pos);
    }

    private boolean isSuperThis(String token) {
        return token.equals("super") || token.equals("this");
    }

    private boolean isPrimitiveType(String token) {
        switch (token) {
            case "boolean":
            case "byte":
            case "char":
            case "double":
            case "float":
            case "int":
            case "long":
            case "short":
                return true;
            default:
                return false;
        }
    }

    private boolean isLiteral(String token) {
        switch (token) {
            case "false":
            case "true":
                return true;
            case "null":
                return treatNullAsLiteral;
            default:
                return false;
        }
    }

    private Pos pos(Node n) {
        Range r = n.getRange().orElse(null);
        Position begin = (r == null) ? null : r.begin;
        Position end = (r == null) ? null : r.end;
        return new Pos(srcFile, begin, end);
    }

    private void nextStmt(Statement stmt) {
        Pos pos = pos(stmt);
        if (prevStmt != null) {
            trie.nextStmt(prevStmt, pos);
        }
        prevStmt = pos;
    }

    private void endStmts() {
        prevStmt = null;
    }

    @Override
    public void visit(AssertStmt n, Void arg) {
        appendStmt(n);
        n.getCheck().accept(this, arg);
        n.getMessage().ifPresent(p -> p.accept(this, arg));
    }

    @Override
    public void visit(BlockStmt n, Void arg) {
        if (mergeClones) {
            n.getStatements().forEach(p -> nextStmt(p));
            endStmts();
        }
        n.getStatements().forEach(p -> p.accept(this, arg));
    }

    @Override
    public void visit(BreakStmt n, Void arg) {
        appendStmt(n);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        n.getMembers().forEach(p -> p.accept(this, arg));
    }

    @Override
    public void visit(CompilationUnit n, Void arg) {
        Path path = n.getStorage().get().getPath();
        Path rel = srcDir.relativize(path);
        srcFile = rel.toString();
        n.getTypes().forEach(p -> p.accept(this, arg));
        srcFile = null;
    }

    @Override
    public void visit(ContinueStmt n, Void arg) {
        appendStmt(n);
    }

    @Override
    public void visit(DoStmt n, Void arg) {
        appendStmt(n);
        n.getBody().accept(this, arg);
        n.getCondition().accept(this, arg);
    }

    @Override
    public void visit(EmptyStmt n, Void arg) {
        appendStmt(n);
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt n, Void arg) {
        appendStmt(n);
        n.getExpression().ifPresent(p -> p.accept(this, arg));
        n.getArguments().forEach(p -> p.accept(this, arg));
    }

    @Override
    public void visit(ExpressionStmt n, Void arg) {
        appendStmt(n);
        n.getExpression().accept(this, arg);
    }

    @Override
    public void visit(ForEachStmt n, Void arg) {
        appendStmt(n);
        n.getVariable().accept(this, arg);
        n.getIterable().accept(this, arg);
        n.getBody().accept(this, arg);
    }

    @Override
    public void visit(ForStmt n, Void arg) {
        appendStmt(n);
        n.getInitialization().forEach(p -> p.accept(this, arg));
        n.getCompare().ifPresent(p -> p.accept(this, arg));
        n.getUpdate().forEach(p -> p.accept(this, arg));
        n.getBody().accept(this, arg);
    }

    @Override
    public void visit(IfStmt n, Void arg) {
        appendStmt(n);
        n.getCondition().accept(this, arg);
        n.getThenStmt().accept(this, arg);
        n.getElseStmt().ifPresent(p -> p.accept(this, arg));
    }

    @Override
    public void visit(LabeledStmt n, Void arg) {
        appendStmt(n);
        n.getStatement().accept(this, arg);
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        if (!n.getBody().isPresent()) {
            return;
        }
        n.getBody().ifPresent(p -> p.accept(this, arg));
    }

    @Override
    public void visit(ReturnStmt n, Void arg) {
        appendStmt(n);
        n.getExpression().ifPresent(p -> p.accept(this, arg));
    }

    @Override
    public void visit(SwitchEntry n, Void arg) {
        n.getLabels().forEach(p -> p.accept(this, arg));
        if (mergeClones) {
            n.getStatements().forEach(p -> nextStmt(p));
            endStmts();
        }
        n.getStatements().forEach(p -> p.accept(this, arg));
    }

    @Override
    public void visit(SwitchStmt n, Void arg) {
        appendStmt(n);
        n.getSelector().accept(this, arg);
        n.getEntries().forEach(p -> p.accept(this, arg));
    }

    @Override
    public void visit(SynchronizedStmt n, Void arg) {
        appendStmt(n);
        n.getExpression().accept(this, arg);
        n.getBody().accept(this, arg);
    }

    @Override
    public void visit(ThrowStmt n, Void arg) {
        appendStmt(n);
        n.getExpression().accept(this, arg);
    }

    @Override
    public void visit(TryStmt n, Void arg) {
        appendStmt(n);
        n.getResources().forEach(p -> p.accept(this, arg));
        n.getTryBlock().accept(this, arg);
        n.getCatchClauses().forEach(p -> p.accept(this, arg));
        n.getFinallyBlock().ifPresent(p -> p.accept(this, arg));
    }

    @Override
    public void visit(WhileStmt n, Void arg) {
        appendStmt(n);
        n.getCondition().accept(this, arg);
        n.getBody().accept(this, arg);
    }
}
