package edu.tarleton.drduplex;

import edu.tarleton.drduplex.clones.Clone;
import edu.tarleton.drduplex.clones.CloneSet;
import edu.tarleton.drduplex.index.plain.persistent.PEdge;
import edu.tarleton.drduplex.index.plain.persistent.PNode;
import edu.tarleton.drduplex.index.plain.persistent.PPos;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * The unit tests.
 *
 * @author Zdenek Tronicek, tronicek@tarleton.edu
 */
public class StmtsTest {

    private final Random rand = new Random();
    private final Set<String> names = new HashSet<>();

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() throws Exception {
    }

    @AfterClass
    public static void tearDownAll() throws Exception {
        // GC must collect the mapped buffers
        Thread.sleep(500);
        System.gc();
        Thread.sleep(500);
    }

    private int perform(String srcDir) throws Exception {
        Properties conf = new Properties();
        return performWith(srcDir, conf);
    }

    private int performWith(String srcDir, Properties conf) throws Exception {
        conf.setProperty("command", "findClones");
        conf.setProperty("index", "simplified");
        conf.setProperty("level", "statements");
        conf.setProperty("sourceDir", srcDir);
        // simplified
        conf.setProperty("compressed", "false");
        conf.setProperty("persistent", "false");
        int size = test(conf);
        // simplified compressed
        conf.setProperty("compressed", "true");
        conf.setProperty("persistent", "false");
        int size2 = test(conf);
        assertEquals(size, size2);
        // simplified persistent
        conf.setProperty("compressed", "false");
        conf.setProperty("persistent", "true");
        int size3 = testPersistent(conf);
        assertEquals(size, size3);
        // simplified compressed persistent
        conf.setProperty("compressed", "true");
        conf.setProperty("persistent", "true");
        int size4 = testPersistent(conf);
        assertEquals(size, size4);
        return size;
    }

    private int test(Properties conf) throws Exception {
        Engine eng = Engine.instance(conf);
        eng.findClones();
        CloneSet set = eng.getCloneSet();
        List<Clone> cc = set.getClones();
        return cc.size();
    }

    private int testPersistent(Properties conf) throws Exception {
        String nodeFileName = generateFileName("data", "nodes");
        String edgeFileName = generateFileName("data", "edges");
        String posFileName = generateFileName("data", "positions");
        String pathFileName = generateFileName("data", "paths");
        String labelFileName = generateFileName("data", "labels");
        String linearizationFileName = generateFileName("data", "linearizations");
        String nextStmtMapFile = generateFileName("data", "nextstmtmap");
        conf.setProperty("nodeFile", nodeFileName);
        conf.setProperty("nodeFilePageSize", Integer.toString(PNode.LENGTH * 1024 * 64));
        conf.setProperty("edgeFile", edgeFileName);
        conf.setProperty("edgeFilePageSize", Integer.toString(PEdge.LENGTH * 1024 * 64));
        conf.setProperty("posFile", posFileName);
        conf.setProperty("posFilePageSize", Integer.toString(PPos.LENGTH * 1024 * 64));
        conf.setProperty("pathFile", pathFileName);
        conf.setProperty("labelFile", labelFileName);
        conf.setProperty("linearizationFile", linearizationFileName);
        conf.setProperty("nextStmtMapFile", nextStmtMapFile);
        return test(conf);
    }

    private String generateFileName(String dir, String prefix) {
        String fn;
        do {
            fn = dir + "/" + prefix + randomString(8);
        } while (names.contains(fn));
        names.add(fn);
        File file = new File(fn);
        file.deleteOnExit();
        return fn;
    }

    private String randomString(int len) {
        String chars = "0123456789abcdefghijklmnopqrstuvwxyz";
        String str = "";
        for (; len > 0; len--) {
            int i = Math.abs(rand.nextInt() % chars.length());
            str += chars.charAt(i);
        }
        return str;
    }

    @Test
    public void test1() throws Exception {
        int c = perform("src/test/src3/1");
        assertEquals(1, c);
    }

    @Test
    public void test2() throws Exception {
        int c = perform("src/test/src3/2");
        assertEquals(1, c);
    }

    @Test
    public void test3() throws Exception {
        int c = perform("src/test/src3/3");
        assertEquals(1, c);
    }

    @Test
    public void test4() throws Exception {
        int c = perform("src/test/src3/4");
        assertEquals(1, c);
    }

    @Test
    public void test5() throws Exception {
        int c = perform("src/test/src3/5");
        assertEquals(1, c);
    }
}
