package edu.tarleton.drduplex.index;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import edu.tarleton.drduplex.index.compressed.CTrie;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/**
 * The visitor that builds compressed index.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public abstract class CompressedIndexBuilder extends VoidVisitorAdapter<Void> {

    protected final Logger logger = Logger.getInstance();
    protected final Path srcDir;
    protected final boolean mergeClones;
    protected final boolean treatNullAsLiteral;
    protected final boolean treatSuperThisAsIdentifier;

    public CompressedIndexBuilder(Properties conf, Path srcDir) {
        this.srcDir = srcDir;
        mergeClones = "statements".equals(conf.getProperty("level"));
        treatNullAsLiteral = Boolean.parseBoolean(conf.getProperty("treatNullAsLiteral", "false"));
        treatSuperThisAsIdentifier = Boolean.parseBoolean(conf.getProperty("treatSuperThisAsIdentifier", "false"));
    }

    public abstract CTrie getTrie();

    public abstract List<String> getBuffer();

    public abstract void reset();
}
