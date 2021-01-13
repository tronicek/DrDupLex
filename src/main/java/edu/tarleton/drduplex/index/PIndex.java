package edu.tarleton.drduplex.index;

import edu.tarleton.drduplex.Histogram;
import edu.tarleton.drduplex.clones.CloneSet;

/**
 * The AST index.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public interface PIndex {

    public void print() throws Exception;

    public CloneSet detectClonesType2(String level, int minSize, int maxSize) throws Exception;

    public Histogram createHistogram() throws Exception;

}
