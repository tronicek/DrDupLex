package p1;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class A {

    List<?> createSet() {
        return Collections.singletonList(null);
    }

    Set<?> instantiateSet() {
        return Collections.singleton(null);
    }
}
