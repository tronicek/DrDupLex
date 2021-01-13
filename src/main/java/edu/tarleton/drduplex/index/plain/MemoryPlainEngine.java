package edu.tarleton.drduplex.index.plain;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import edu.tarleton.drduplex.Engine;
import edu.tarleton.drduplex.Histogram;
import edu.tarleton.drduplex.NormalizingVisitor;
import edu.tarleton.drduplex.clones.Pos;
import edu.tarleton.drduplex.index.IndexBuilder;
import edu.tarleton.drduplex.nicad.NiCadConvertor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * The class that builds the index and finds the clones.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class MemoryPlainEngine extends Engine {

    public MemoryPlainEngine(Properties conf) {
        super(conf);
    }

    @Override
    public void findClones() throws Exception {
        IndexBuilder builder = createBuilder();
        processDir(builder, sourceDir);
        if (printStatistics) {
            statistics.print(false);
        }
        Trie trie = builder.getTrie();
        if (printTrie) {
            trie.print();
        }
        if (printHistogram) {
            Histogram hist = trie.createHistogram();
            hist.print();
        }
        if (verbose) {
            System.out.println("searching for clones...");
        }
        cloneSet = trie.detectClonesType2(level, minSize, maxSize);
        if (outputFileName == null) {
            cloneSet.print();
        } else {
            NiCadConvertor conv = new NiCadConvertor(conf);
            conv.convert(cloneSet, outputFileName);
        }
    }

    private IndexBuilder createBuilder() {
        boolean methodLevel = level.equals("method");
        Path dir = Paths.get(sourceDir).toAbsolutePath();
        switch (index) {
            case "simplified":
                return methodLevel
                        ? new SimplifiedLexIndexBuilder(conf, dir)
                        : new SimplifiedLexIndexStmtBuilder(conf, dir);
            default:
                throw new AssertionError("invalid index: " + index);
        }
    }

    void processDir(IndexBuilder builder, String srcDir) throws IOException {
        Path path = Paths.get(srcDir);
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        processFile(builder, srcDir, p);
                    });
        }
    }

    void processFile(IndexBuilder builder, String srcDir, Path path) {
        String fn = path.toString().substring(srcDir.length());
        if (fn.startsWith("/") || fn.startsWith("\\")) {
            fn = fn.substring(1);
        }
        if (verbose) {
            System.out.printf("processing %s...%n", fn);
        }
        NormalizingVisitor normVisitor = new NormalizingVisitor(conf);
        Path root = Paths.get(srcDir);
        SourceRoot sourceRoot = new SourceRoot(root, parserConfiguration);
        try {
            CompilationUnit cu = sourceRoot.parse("", fn);
            if (printStatistics) {
                cu.accept(countingVisitor, null);
            }
            cu.accept(normVisitor, null);
            cu.accept(builder, null);
            if (printStatistics) {
                statistics.store(countingVisitor.getLines(),
                        countingVisitor.getNodes(), TrieNode.getCount(),
                        TrieEdge.getCount(), Pos.getCount());
            }
            fileCount++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printTrie() throws Exception {
        throw new AssertionError("Not supported.");
    }
}
