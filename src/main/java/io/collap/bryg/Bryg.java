package io.collap.bryg;

import io.collap.bryg.environment.Environment;
import io.collap.bryg.environment.StandardEnvironment;
import io.collap.bryg.example.Post;
import io.collap.bryg.loader.FileSourceLoader;
import io.collap.bryg.model.BasicModel;
import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.model.Model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Bryg {

    public static void main (String[] args) throws InvalidInputParameterException {
        Environment environment = new StandardEnvironment (new FileSourceLoader (new File ("example")));

        /* test.Simple */
        {
            Template template = environment.getTemplate ("test.Simple");
            Model model = new BasicModel ();
            model.setVariable ("test", "Hello Bryg!");
            model.setVariable ("number", 42);
            model.setVariable ("object", new TestObject ());
            benchmarkTemplate (template, model);
        }

        /* test.Parameters */
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

        /* test.Each */
        {
            Template template = environment.getTemplate ("test.Each");
            Model model = new BasicModel ();
            List<String> names = new ArrayList<> ();
            names.add ("Robert");
            names.add ("Dany");
            names.add ("Tyrion");
            model.setVariable ("names", names);
            benchmarkTemplate (template, model);
        }

        /* post.Edit */
        {
            Template template = environment.getTemplate ("post.Edit");
            Model model = new BasicModel ();
            Post post = new Post ();
            post.setId (1);
            post.setTitle ("Test Post");
            post.setContent ("This is a test post!");

            List<String> categories = post.getCategories ();
            categories.add ("Test");
            categories.add ("Discussion");
            categories.add ("Computer Science");
            model.setVariable ("post", post);
            benchmarkTemplate (template, model);
        }

        /* test.Item */
        {
            Template template = environment.getTemplate ("test.Item");
            Model model = new BasicModel ();
            model.setVariable ("id", 12345);
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
