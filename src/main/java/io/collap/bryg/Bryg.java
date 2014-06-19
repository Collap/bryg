package io.collap.bryg;

import io.collap.bryg.environment.StandardEnvironment;
import io.collap.bryg.loader.FileSourceLoader;
import io.collap.bryg.model.BasicModel;
import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.model.Model;

import java.io.*;

public class Bryg {

    public static void main (String[] args) throws InvalidInputParameterException {
        StandardEnvironment environment = new StandardEnvironment ();
        environment.registerSourceLoader ("", new FileSourceLoader (new File ("example")));
        Template template = environment.getTemplate ("", "test.Simple");
        StringWriter stringWriter = null;
        final int renderIterations = 100000;
        long renderTime = System.nanoTime ();
        for (int i = 0; i < renderIterations; ++i) {
            stringWriter = new StringWriter ();
            Model model = new BasicModel ();
            model.setVariable ("test", "Hello Bryg!");
            model.setVariable ("number", 42);
            model.setVariable ("object", new TestObject ());
            template.render (stringWriter, model);
        }
        System.out.println ("Rendering took " + ((System.nanoTime () - renderTime) / renderIterations) + "ns on average.");
        System.out.println (stringWriter.toString ());
    }

}
