package io.collap.bryg.test;

import io.collap.bryg.model.BasicModel;
import io.collap.bryg.template.Template;
import io.collap.bryg.model.Model;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

public abstract class TemplateTest {

    protected Template template;

    @Before
    public void init () {
        template = Domain.getEnvironment ().getTemplate (getTemplateName ());
    }

    @Test
    public void render () throws IOException {
        Model model = createModel ();

        StringWriter writer = new StringWriter ();
        template.render (writer, model);
        System.out.println ("Result:");
        System.out.println (writer.toString ());
        System.out.println ();

        File outputFile = new File ("build/test/" + getTemplateName ().replace ('.', '/') + ".html");
        outputFile.getParentFile ().mkdirs ();
        FileWriter fileWriter = new FileWriter (outputFile);
        fileWriter.write (writer.toString ());
        fileWriter.close ();
    }

    // @Test
    public void benchmark () throws IOException {
        final int iterations = 10000;
        final Model model = createModel ();

        StringWriter stringWriter;
        for (int i = 0; i < iterations; ++i) {
            stringWriter = new StringWriter ();
            template.render (stringWriter, model);
        }

        final long renderTime = System.nanoTime ();
        for (int i = 0; i < iterations; ++i) {
            stringWriter = new StringWriter ();
            template.render (stringWriter, model);
        }

        final long timeNs = System.nanoTime () - renderTime;
        System.out.println ("Rendering " + template.getClass ().getName ()
                + " took " + (timeNs / iterations) + "ns on average.");
        System.out.println ("That are " + (timeNs / 1.0e9) + "s in total.");
    }

    private Model createModel () {
        Model model = new BasicModel ();
        configureModel (model);
        return model;
    }

    protected abstract void configureModel (Model model);

    protected abstract String getTemplateName ();

}
