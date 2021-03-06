package edu.tarleton.drduplex.index.compressed;

import edu.tarleton.drduplex.Histogram;
import edu.tarleton.drduplex.clones.Clone;
import edu.tarleton.drduplex.clones.CloneSet;
import edu.tarleton.drduplex.clones.Pos;
import edu.tarleton.drduplex.index.Index;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The implementation of the compressed TRIE.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class CTrie implements Index, Serializable {

    private static final long serialVersionUID = 1L;
    private final CTrieNode root = new CTrieNode();
    private final List<String> buffer = new ArrayList<>();
    private final Map<Pos, Pos> nextStmtMap = new HashMap<>();

    public CTrieNode getRoot() {
        return root;
    }

    public Map<Pos, Pos> getNextStmtMap() {
        return nextStmtMap;
    }

    public List<String> getBuffer() {
        return buffer;
    }

    @Override
    public void nextStmt(Pos prev, Pos curr) {
        nextStmtMap.put(prev, curr);
    }

    @Override
    public void print() {
        root.print();
    }

    @Override
    public CloneSet detectClonesType2(String level, int minSize, int maxSize) {
        switch (level) {
            case "method":
            case "statement":
                return detectClones(minSize, maxSize);
            case "statements":
                return detectMergedStmtClonesType2(minSize, maxSize);
            default:
                throw new AssertionError("invalid level: " + level);
        }
    }

    private CloneSet detectClones(int minSize, int maxSize) {
        CloneSet clones = new CloneSet();
        List<CTrieNode> nodes = new ArrayList<>();
        List<Pos[]> positions = new ArrayList<>();
        for (CTrieEdge edge : root.getEdges()) {
            nodes.add(edge.getDestination());
            positions.add(edge.getPositions());
        }
        while (!nodes.isEmpty()) {
            CTrieNode node = nodes.remove(0);
            Pos[] pp = positions.remove(0);
            Pos[] rr = positions(pp, minSize, maxSize);
            if (rr.length > 1) {
                Clone clone = new Clone(100, rr);
                clones.addClone(clone);
            }
            for (CTrieEdge e : node.getEdges()) {
                nodes.add(e.getDestination());
                positions.add(e.getPositions());
            }
        }
        return clones;
    }

    private Pos[] positions(Pos[] positions, int minSize, int maxSize) {
        List<Pos> pp = new ArrayList<>();
        for (Pos p : positions) {
            long size = p.getLines();
            if (minSize <= size && size <= maxSize) {
                pp.add(p);
            }
        }
        Pos[] rr = new Pos[pp.size()];
        for (int i = 0; i < rr.length; i++) {
            rr[i] = pp.get(i);
        }
        return rr;
    }

    private CloneSet detectMergedStmtClonesType2(int minSize, int maxSize) {
        List<Clone> cls = new ArrayList<>();
        List<CTrieNode> nodes = new ArrayList<>();
        List<Pos[]> positions = new ArrayList<>();
        for (CTrieEdge edge : root.getEdges()) {
            nodes.add(edge.getDestination());
            positions.add(edge.getPositions());
        }
        while (!nodes.isEmpty()) {
            CTrieNode node = nodes.remove(0);
            Pos[] pp = positions.remove(0);
            if (pp.length > 1) {
                Clone clone = new Clone(100, pp);
                cls.add(clone);
            }
            for (CTrieEdge e : node.getEdges()) {
                nodes.add(e.getDestination());
                positions.add(e.getPositions());
            }
        }
        return merge(cls, minSize, maxSize);
    }

    private CloneSet merge(List<Clone> cls, int minSize, int maxSize) {
        List<Clone> cls2 = new ArrayList<>();
        Map<Pos, Set<Pos>> posMap = createPosMap(cls);
        for (Clone cl : cls) {
            Pos[] pp = cl.getPositions();
            Set<Integer> merged = new TreeSet<>();
            for (int i = 0; i < pp.length; i++) {
                if (merged.contains(i)) {
                    continue;
                }
                Pos next = nextStmtMap.get(pp[i]);
                Set<Pos> set = posMap.get(next);
                if (set == null) {
                    continue;
                }
                Set<Integer> inds = new TreeSet<>();
                for (int j = i + 1; j < pp.length; j++) {
                    Pos next2 = nextStmtMap.get(pp[j]);
                    if (set.contains(next2)) {
                        inds.add(j);
                    }
                }
                if (inds.isEmpty()) {
                    continue;
                }
                Pos[] np = new Pos[inds.size() + 1];
                np[0] = new Pos(pp[i].getFile(), pp[i].getStart(), next.getEnd());
                int k = 1;
                for (Integer j : inds) {
                    Pos next2 = nextStmtMap.get(pp[j]);
                    np[k] = new Pos(pp[j].getFile(), pp[j].getStart(), next2.getEnd());
                    k++;
                }
                Clone clone = new Clone(100, np);
                cls2.add(clone);
                merged.add(i);
                merged.addAll(inds);
            }
            if (merged.size() < pp.length) {
                cls2.add(cl);
            }
        }
        List<Clone> cls3 = new ArrayList<>();
        for (Clone cl : cls2) {
            Pos[] pp = cl.getPositions();
            Pos[] rr = positions(pp, minSize, maxSize);
            if (rr.length > 1) {
                Clone clone = new Clone(100, rr);
                cls3.add(clone);
            }
        }
        List<Clone> cls4 = removeDuplicates(cls3);
        CloneSet clones = new CloneSet();
        clones.addClones(cls4);
        return clones;
    }

    private Map<Pos, Set<Pos>> createPosMap(List<Clone> clones) {
        Map<Pos, Set<Pos>> map = new HashMap<>();
        for (Clone cl : clones) {
            for (Pos p : cl.getPositions()) {
                Set<Pos> pset = map.get(p);
                if (pset == null) {
                    pset = new HashSet<>();
                    map.put(p, pset);
                }
                for (Pos p2 : cl.getPositions()) {
                    if (p2 == p) {
                        continue;
                    }
                    pset.add(p2);
                }
            }
        }
        return map;
    }

    private List<Clone> removeDuplicates(List<Clone> clones) {
        Map<Clone, Integer> numMap = new HashMap<>();
        Map<Integer, Clone> revNumMap = new HashMap<>();
        for (int i = 0; i < clones.size(); i++) {
            Clone cl = clones.get(i);
            numMap.put(cl, i);
            revNumMap.put(i, cl);
        }
        Map<String, Set<Integer>> fileMap = new HashMap<>();
        for (Clone cl : clones) {
            Integer i = numMap.get(cl);
            for (Pos pos : cl.getPositions()) {
                String file = pos.getFile();
                Set<Integer> cls = fileMap.get(file);
                if (cls == null) {
                    cls = new HashSet<>();
                    fileMap.put(file, cls);
                }
                cls.add(i);
            }
        }
        List<Clone> subs = new ArrayList<>();
        for (Clone cl : clones) {
            Pos[] pp = cl.getPositions();
            String file = pp[0].getFile();
            Set<Integer> cls = new HashSet<>();
            Set<Integer> cc = fileMap.get(file);
            cls.addAll(cc);
            for (int i = 1; i < pp.length; i++) {
                String file2 = pp[i].getFile();
                Set<Integer> cls2 = fileMap.get(file2);
                cls.retainAll(cls2);
            }
            Integer i = numMap.get(cl);
            for (Integer j : cls) {
                if (i.equals(j)) {
                    continue;
                }
                Clone cl2 = revNumMap.get(j);
                if (cl.subsetOf(cl2)) {
                    subs.add(cl);
                    break;
                }
            }
        }
        List<Clone> clones2 = new ArrayList<>();
        for (Clone cl : clones) {
            if (!subs.contains(cl)) {
                clones2.add(cl);
            }
        }
        return clones2;
    }

    @Override
    public Histogram createHistogram() {
        Histogram hist = new Histogram();
        Deque<CTrieNode> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            CTrieNode p = queue.remove();
            CTrieEdge[] edges = p.getEdges();
            for (CTrieEdge e : edges) {
                Pos[] pp = e.getPositions();
                hist.storeEdge(pp.length);
            }
            hist.storeNode(edges.length);
        }
        return hist;
    }
}
