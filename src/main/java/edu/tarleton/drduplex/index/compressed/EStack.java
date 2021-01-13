package edu.tarleton.drduplex.index.compressed;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * The implementation of stack.
 * 
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class EStack implements Iterable<EStackNode> {

    private final Deque<EStackNode> nodes = new ArrayDeque<>();

    public void push(EStackNode node) {
        nodes.addLast(node);
    }

    public EStackNode pop() {
        return nodes.removeLast();
    }
    
    @Override
    public Iterator<EStackNode> iterator() {
        return nodes.iterator();
    }
    
    @Override
    public String toString() {
        return nodes.toString();
    }
}
