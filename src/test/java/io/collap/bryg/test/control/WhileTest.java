package io.collap.bryg.test.control;

import io.collap.bryg.internal.StandardClosure;
import io.collap.bryg.Model;
import io.collap.bryg.test.Domain;
import io.collap.bryg.test.TemplateTest;

import java.io.IOException;
import java.io.Writer;

public class WhileTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("iterations", 10);
        model.setVariable ("closure", new StandardClosure (Domain.getEnvironment ()) {
            @Override
            public void render (Writer writer, Model model) throws IOException {
                writer.append ("Hello Closure!");
            }
        });
    }

    @Override
    protected String getTemplateName () {
        return "control.While";
    }

}
