package io.collap.bryg.test.example;

import io.collap.bryg.Model;
import io.collap.bryg.test.TemplateTest;

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
