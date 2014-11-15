package io.collap.bryg.test;

public class Format {

    public String i2b (int value) {
        return Integer.toBinaryString (value);
    }

    public String i2b (byte value) {
        String str = Integer.toBinaryString (value);
        if (str.length () > 8) {
            str = str.substring (str.length () - 8);
        }else {
            for (int i = str.length (); i < 8; ++i) {
                str = "0" + str;
            }
        }
        return str;
    }

}
