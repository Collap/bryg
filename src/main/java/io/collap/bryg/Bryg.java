package io.collap.bryg;

import io.collap.bryg.compiler.resolver.ClassResolver;
import io.collap.bryg.compiler.loader.FileTemplateLoader;
import io.collap.bryg.compiler.TemplateClassLoader;
import io.collap.bryg.compiler.TestCompiler;
import io.collap.bryg.model.BasicModel;
import io.collap.bryg.template.InvalidInputParameterException;
import io.collap.bryg.model.Model;
import io.collap.bryg.template.Template;

import java.io.*;

public class Bryg {

    public static void main (String[] args) throws InvalidInputParameterException {
        TemplateClassLoader loader = new TemplateClassLoader (new TestCompiler (new ClassResolver ()), new FileTemplateLoader (new File ("example")));
        try {
            Class<?> testTemplateClass = loader.loadClass ("Simple");
            Template template = (Template) testTemplateClass.newInstance ();
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
        } catch (InstantiationException e) {
            e.printStackTrace ();
        } catch (IllegalAccessException e) {
            e.printStackTrace ();
        } catch (ClassNotFoundException e) {
            e.printStackTrace ();
        }
    }

}
