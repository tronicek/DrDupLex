package edu.tarleton.drduplex.index.plain;

import edu.tarleton.drduplex.clones.Pos;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * The implementation of stack.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class Stack implements Iterable<StackNode> {

    private final Deque<StackNode> nodes = new ArrayDeque<>();

    public void push(TrieNode node, Pos pos) {
        StackNode p = new StackNode(node, pos);
        nodes.addLast(p);
    }

    public void push(StackNode node) {
        nodes.addLast(node);
    }

    public StackNode pop() {
        return nodes.removeLast();
    }

    @Override
    public Iterator<StackNode> iterator() {
        return nodes.iterator();
    }

    @Override
    public String toString() {
        return nodes.toString();
    }
}
