package edu.tarleton.drduplex.index.compressed;

import com.github.javaparser.JavaToken;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import edu.tarleton.drduplex.clones.Pos;
import edu.tarleton.drduplex.index.CompressedIndexBuilder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * The builder that builds the compressed method-level index.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class SimplifiedLexCompressedIndexBuilder extends CompressedIndexBuilder {

    private CTrie trie = new CTrie();
    private String srcFile;
    private final List<String> buffer = new ArrayList<>();

    public SimplifiedLexCompressedIndexBuilder(Properties conf, Path srcDir) {
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

    private void append(BodyDeclaration<?> decl) {
        Optional<TokenRange> opt = decl.getTokenRange();
        if (opt.isPresent()) {
            appendRange(opt.get(), pos(decl));
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

    @Override
    public void visit(CompilationUnit n, Void arg) {
        Path path = n.getStorage().get().getPath();
        Path rel = srcDir.relativize(path);
        srcFile = rel.toString();
        n.getTypes().forEach(p -> p.accept(this, arg));
        srcFile = null;
    }

    @Override
    public void visit(ConstructorDeclaration n, Void arg) {
        append(n);
        n.getBody().accept(this, arg);
    }

    @Override
    public void visit(InitializerDeclaration n, Void arg) {
        append(n);
        n.getBody().accept(this, arg);
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        if (!n.getBody().isPresent()) {
            return;
        }
        append(n);
        n.getBody().ifPresent(p -> p.accept(this, arg));
    }
}
