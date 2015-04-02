package io.collap.bryg.test;

import io.collap.bryg.MapModel;
import io.collap.bryg.Template;
import io.collap.bryg.Model;
import io.collap.bryg.TemplateFactory;
import io.collap.bryg.test.object.TestController;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

public abstract class TemplateTest {

    protected TemplateFactory templateFactory;

    @Before
    public void init () {
        templateFactory = Domain.getEnvironment ().getTemplateFactory (getTemplateName ());
    }

    @Test
    public void render () throws IOException {
        TestController testController = new TestController ();

        Model model = createModel ();
        model.setVariable ("test", testController);
        Template template = templateFactory.create (model);
        render (template, model);
    }

    protected void render (Template template, Model model) throws IOException {
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

        Template template = templateFactory.create (model);

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
        Model model = new MapModel();
        configureModel (model);
        return model;
    }

    protected abstract void configureModel (Model model);

    protected abstract String getTemplateName ();

}
