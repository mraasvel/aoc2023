package util;

import java.util.Objects;

public class PreconditionUtil {
    public static void assertTrue(boolean expr) {
        assertTrue("expected true, was: false", expr);
    }

    public static void assertTrue(String message, boolean expr) {
        if (!expr) {
            throw new RuntimeException(message);
        }
    }

    public static <T>
    void assertEquals(T a, T b) {
        assertEquals("expected equals", a, b);
    }

    public static <T>
    void assertEquals(String message, T a, T b) {
        if (!Objects.equals(a, b)) {
            throw new RuntimeException(message);
        }
    }

    public static void assertNotNull(Object obj) {
        if (obj == null) {
            throw new RuntimeException("Expected non-null, got: null");
        }
    }
}
