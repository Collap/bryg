package io.collap.bryg.test.object;

public class TestController {

    public void fail (String message) {
        throw new RuntimeException ("Test failed in template: " + message);
    }

}
