package edu.tarleton.drduplex.index.compressed;

import edu.tarleton.drduplex.clones.Pos;
import java.io.Serializable;
import java.util.List;

/**
 * The node of the compressed TRIE.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class CTrieNode implements Serializable {

    private static final long serialVersionUID = 1L;
    private static int count;
    private final int num;
    private CTrieEdge[] edges = new CTrieEdge[1];
    private int edgesCount;

    public CTrieNode() {
        num = count++;
    }

    public static int getCount() {
        return count;
    }

    public boolean isLeaf() {
        return edgesCount == 0;
    }

    public CTrieEdge[] getEdges() {
        if (edgesCount == edges.length) {
            return edges;
        }
        CTrieEdge[] ee = new CTrieEdge[edgesCount];
        System.arraycopy(edges, 0, ee, 0, edgesCount);
        return ee;
    }

    public CTrieEdge findEdge(String label) {
        for (int i = 0; i < edgesCount; i++) {
            List<String> elab = edges[i].getLabel();
            String s = elab.get(0);
            if (s.equals(label)) {
                return edges[i];
            }
        }
        return null;
    }

    public CTrieEdge addEdge(List<String> linearization, int ind) {
        CTrieNode dst = new CTrieNode();
        CTrieEdge e = new CTrieEdge(linearization, ind, ind + 1, dst);
        addEdge(e);
        return e;
    }

    public void addEdge(CTrieEdge e) {
        if (edgesCount == edges.length) {
            CTrieEdge[] ee = new CTrieEdge[edges.length * 2];
            System.arraycopy(edges, 0, ee, 0, edges.length);
            edges = ee;
        }
        edges[edgesCount] = e;
        edgesCount++;
    }

    public void addEdge(List<String> linearization, int start, int end, Pos pos) {
        CTrieEdge e = findEdge(linearization.get(start));
        if (e == null) {
            CTrieNode dest = new CTrieNode();
            CTrieEdge ee = new CTrieEdge(linearization, start, end, dest);
            ee.addPosition(pos);
            addEdge(ee);
        } else {
            List<String> lab = e.getLabel();
            List<String> subLin = linearization.subList(start, end);
            int prefix = commonPrefix(lab, subLin);
            if (prefix == lab.size()) {
                if (lab.size() == subLin.size()) {
                    e.addPosition(pos);
                } else {
                    CTrieNode p = e.getDestination();
                    p.addEdge(linearization, start + lab.size(), end, pos);
                }
            } else {
                CTrieNode ndest = new CTrieNode();
                CTrieEdge e2 = e.makeClone();
                e.setDestination(ndest);
                e.removePositions();
                int d = e.getStart() + prefix;
                e.setEnd(d);
                e2.setStart(d);
                ndest.addEdge(e2);
                if (prefix == subLin.size()) {
                    e.addPosition(pos);
                } else {
                    CTrieNode ndest2 = new CTrieNode();
                    CTrieEdge e3 = new CTrieEdge(linearization, start + prefix, end, ndest2);
                    e3.addPosition(pos);
                    ndest.addEdge(e3);
                }
            }
        }
    }

    private int commonPrefix(List<String> lin1, List<String> lin2) {
        int size = Math.min(lin1.size(), lin2.size());
        for (int i = 0; i < size; i++) {
            String s = lin1.get(i);
            String p = lin2.get(i);
            if (!s.equals(p)) {
                return i;
            }
        }
        return size;
    }

    public int getNum() {
        return num;
    }

    public void print() {
        System.out.printf("node %d%n", num);
        for (int i = 0; i < edgesCount; i++) {
            edges[i].print();
        }
        for (int i = 0; i < edgesCount; i++) {
            CTrieNode dst = edges[i].getDestination();
            dst.print();
        }
    }

    @Override
    public String toString() {
        return String.format("node %d", num);
    }
}
