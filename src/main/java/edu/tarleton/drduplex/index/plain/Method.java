package edu.tarleton.drduplex.index.plain;

import edu.tarleton.drduplex.clones.Pos;
import java.util.List;

/**
 * The representation of a method.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class Method {

    private static int count;
    private final int id;
    private final Pos[] pos;
    private final String[] tokens;

    public Method(Pos[] pos, List<String> tt) {
        this.pos = pos;
        tokens = new String[tt.size()];
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tt.get(i);
        }
        id = count;
        count++;
    }

    public int getId() {
        return id;
    }

    public Pos[] getPos() {
        return pos;
    }

    public String[] getTokens() {
        return tokens;
    }

    public void print() {
        System.out.printf("method [id: %d, pos:", id);
        for (Pos p : pos) {
            System.out.printf(" %s", p);
        }
        System.out.println();
    }
}
