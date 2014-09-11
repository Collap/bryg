package io.collap.bryg.test.example;

import io.collap.bryg.model.Model;
import io.collap.bryg.test.TemplateTest;

/**
 * @author Marco Pennekamp
 */
public class GetSetTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("stock", new Stock ("", "", "", "", 0.0, 0.0, 0.0));
    }

    @Override
    protected String getTemplateName () {
        return "example.GetSet";
    }

}
