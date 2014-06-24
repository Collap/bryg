package io.collap.bryg;

import io.collap.bryg.environment.Environment;
import io.collap.bryg.environment.StandardEnvironment;
import io.collap.bryg.loader.FileSourceLoader;
import io.collap.bryg.model.BasicModel;
import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.model.Model;

import java.io.*;

public class Bryg {

    public static void main (String[] args) throws InvalidInputParameterException {
        Environment environment = new StandardEnvironment (new FileSourceLoader (new File ("example")));
        {
            Template template = environment.getTemplate ("test.Simple");
            Model model = new BasicModel ();
            model.setVariable ("test", "Hello Bryg!");
            model.setVariable ("number", 42);
            model.setVariable ("object", new TestObject ());
            benchmarkTemplate (template, model);
        }

        /* Render test.Parameters */
        {
            Template template = environment.getTemplate ("test.Parameters");
            Model model = new BasicModel ();
            model.setVariable ("booleanValue", true);
            model.setVariable ("charValue", 'H');
            model.setVariable ("byteValue", (byte) 42);
            model.setVariable ("shortValue", (short) 500);
            model.setVariable ("intValue", 72000);
            model.setVariable ("longValue", 10000000000L);
            model.setVariable ("floatValue", 0.5656f);
            model.setVariable ("doubleValue", 0.5656d);
            benchmarkTemplate (template, model);
        }
    }

    private static void benchmarkTemplate (Template template, Model model) throws InvalidInputParameterException {
        StringWriter stringWriter = null;
        final int renderIterations = 100000;
        long renderTime = System.nanoTime ();
        for (int i = 0; i < renderIterations; ++i) {
            stringWriter = new StringWriter ();
            template.render (stringWriter, model);
        }
        System.out.println ("Rendering took " + ((System.nanoTime () - renderTime) / renderIterations) + "ns on average.");
        System.out.println (stringWriter.toString ());
    }

}
