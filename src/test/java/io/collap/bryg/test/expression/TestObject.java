package io.collap.bryg.test.expression;

public class TestObject {

    private String name = "Hello Object!";

    public String getName () {
        return name;
    }

    public String testString (int n) {
        String str = "";
        for (int i = 0; i < n; ++i) {
            str += "test;";
        }
        return str;
    }

    /**
     * This method is supposed to be called with any class except Object to
     * reference widening coercion.
     */
    public String objectToString (Object obj) {
        return obj.toString ();
    }

}
