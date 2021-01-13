package edu.tarleton.drduplex.index;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import edu.tarleton.drduplex.index.plain.Trie;
import java.nio.file.Path;
import java.util.Properties;

/**
 * The visitor that builds plain (not compressed) index.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public abstract class IndexBuilder extends VoidVisitorAdapter<Void> {

    protected final Logger logger = Logger.getInstance();
    protected final Path srcDir;
    protected final boolean mergeClones;
    protected final boolean treatNullAsLiteral;
    protected final boolean treatSuperThisAsIdentifier;

    protected IndexBuilder(Properties conf, Path srcDir) {
        this.srcDir = srcDir;
        mergeClones = "statements".equals(conf.getProperty("level"));
        treatNullAsLiteral = Boolean.parseBoolean(conf.getProperty("treatNullAsLiteral", "false"));
        treatSuperThisAsIdentifier = Boolean.parseBoolean(conf.getProperty("treatSuperThisAsIdentifier", "false"));
    }

    public abstract Trie getTrie();

    public abstract void reset();
}
