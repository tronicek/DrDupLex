package edu.tarleton.drduplex.index.compressed;

import edu.tarleton.drduplex.clones.Pos;

/**
 * The stack node.
 * 
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class EStackNode {

    private final CTrieEdge edge;
    private final int current;
    private final Pos pos;

    public EStackNode(CTrieEdge edge, int current, Pos pos) {
        this.edge = edge;
        this.current = current;
        this.pos = pos;
        assert current <= edge.getEnd();
    }

    public CTrieEdge getEdge() {
        return edge;
    }

    public int getCurrent() {
        return current;
    }

    public Pos getPos() {
        return pos;
    }
    
    @Override
    public String toString() {
        return String.format("%s:%d", edge, current);
    }
}
