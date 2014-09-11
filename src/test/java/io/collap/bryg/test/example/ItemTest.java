package io.collap.bryg.test.example;

import io.collap.bryg.model.Model;
import io.collap.bryg.test.TemplateTest;

/**
 * @author Marco Pennekamp
 */
public class ItemTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("id", 12345);
    }

    @Override
    protected String getTemplateName () {
        return "example.Item";
    }

}
