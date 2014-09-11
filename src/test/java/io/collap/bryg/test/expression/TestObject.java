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

}
