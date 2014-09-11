package io.collap.bryg.test.function;

import io.collap.bryg.model.Model;
import io.collap.bryg.test.TemplateTest;

/**
 * @author Marco Pennekamp
 */
public class ParametersTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("z", true);
        model.setVariable ("c", 'H');
        model.setVariable ("b", (byte) 42);
        model.setVariable ("s", (short) 500);
        model.setVariable ("i", 72000);
        model.setVariable ("l", 10000000000L);
        model.setVariable ("f", 0.5656f);
        model.setVariable ("d", 0.5656d);
    }

    @Override
    protected String getTemplateName () {
        return "function.Parameters";
    }

}
