package io.collap.bryg.test.control;

import io.collap.bryg.FragmentCallException;
import io.collap.bryg.internal.StandardClosure;
import io.collap.bryg.Model;
import io.collap.bryg.internal.StandardEnvironment;
import io.collap.bryg.test.Domain;
import io.collap.bryg.test.TemplateTest;

import java.io.IOException;
import java.io.Writer;

public class WhileTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("iterations", 10);
        // TODO: Add an "instantiate closure" method to the environment that takes a function as an argument?
        // Note: The following is a "hack" that uses internal information! Do not use this right now!
        model.setVariable ("closure", new StandardClosure (((StandardEnvironment) Domain.getEnvironment())) {
            @Override
            public void call(String name, Writer writer, Model model) throws FragmentCallException {
                try {
                    writer.append ("Hello Closure!");
                } catch (IOException e) {
                    throw new FragmentCallException("IOException occurred: ", e);
                }
            }
        });
    }

    @Override
    protected String getTemplateName () {
        return "control.While";
    }

}
