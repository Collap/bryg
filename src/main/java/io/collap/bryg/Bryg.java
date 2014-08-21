package io.collap.bryg;

import io.collap.bryg.environment.Environment;
import io.collap.bryg.environment.StandardEnvironment;
import io.collap.bryg.example.Post;
import io.collap.bryg.example.Stock;
import io.collap.bryg.example.TestObject;
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

        /* Pre-compile templates. */
        environment.getTemplate ("test.Simple");
        environment.getTemplate ("test.Parameters");
        environment.getTemplate ("test.Each");
        environment.getTemplate ("post.Edit");
        environment.getTemplate ("test.Item");
        environment.getTemplate ("test.Stocks");
        environment.getTemplate ("test.GetSet");
        environment.getTemplate ("test.Arithmetic");
        System.gc ();

        /* test.Simple */
        if (true) {
            Template template = environment.getTemplate ("test.Simple");
            Model model = new BasicModel ();
            model.setVariable ("test", "Hello Bryg!");
            model.setVariable ("number", 42);
            model.setVariable ("object", new TestObject ());
            benchmarkTemplate (template, model);
        }

        /* test.Parameters */
        if (true) {
            Template template = environment.getTemplate ("test.Parameters");
            Model model = new BasicModel ();
            model.setVariable ("z", true);
            model.setVariable ("c", 'H');
            model.setVariable ("b", (byte) 42);
            model.setVariable ("s", (short) 500);
            model.setVariable ("i", 72000);
            model.setVariable ("l", 10000000000L);
            model.setVariable ("f", 0.5656f);
            model.setVariable ("d", 0.5656d);
            benchmarkTemplate (template, model);
        }

        /* test.Each */
        if (true) {
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
        if (true) {
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
            model.setVariable ("basePath", ""); // TODO: basePath will be part of a global variable context!
            benchmarkTemplate (template, model);
        }

        /* test.Item */
        if (true) {
            Template template = environment.getTemplate ("test.Item");
            Model model = new BasicModel ();
            model.setVariable ("id", 12345);
            benchmarkTemplate (template, model);
        }

        /* test.Stocks */
        if (true) {
            Template template = environment.getTemplate ("test.Stocks");
            Model model = new BasicModel ();
            model.setVariable ("items", Stock.dummyItems ());
            benchmarkTemplate (template, model);
        }

        /* test.GetSet */
        if (true) {
            Template template = environment.getTemplate ("test.GetSet");
            Model model = new BasicModel ();
            Post post = new Post ();
            post.setContent ("Hello Alice");
            model.setVariable ("post", post);
            benchmarkTemplate (template, model);
        }

        /* test.Arithmetic */
        if (true) {
            Template template = environment.getTemplate ("test.Arithmetic");
            Model model = new BasicModel ();
            model.setVariable ("a", 15);
            model.setVariable ("b", -5);
            benchmarkTemplate (template, model);
        }
    }

    private static void benchmarkTemplate (Template template, Model model) throws InvalidInputParameterException {
        benchmarkTemplate (template, model, 25000);
    }

    private static void benchmarkTemplate (Template template, Model model, int iterations) throws InvalidInputParameterException {
        StringWriter stringWriter = null;
        final int warmUpLoops = iterations;
        final int renderIterations = iterations;
        for (int i = 0; i < warmUpLoops; ++i) {
            stringWriter = new StringWriter ();
            template.render (stringWriter, model);
        }

        File file = new File ("out.html");
        try {
            FileWriter writer = new FileWriter (file, false);
            long timeMillis = System.currentTimeMillis ();
            long renderTime = System.nanoTime ();
            for (int i = 0; i < renderIterations; ++i) {
                stringWriter = new StringWriter ();
                template.render (stringWriter, model);
            }
            System.out.println ("Rendering took " + ((System.nanoTime () - renderTime) / renderIterations) + "ns on average.");
            System.out.println ("That are " + (System.currentTimeMillis () - timeMillis) + "ms in total.");

            writer.write (stringWriter.toString ());
            writer.close ();
        } catch (IOException e) {
            e.printStackTrace ();
        }

        System.out.println (stringWriter.toString ());
        System.out.println ();
    }

}
