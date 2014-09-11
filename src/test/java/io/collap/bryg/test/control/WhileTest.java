package io.collap.bryg.test.control;

import io.collap.bryg.model.Model;
import io.collap.bryg.test.TemplateTest;

public class WhileTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("iterations", 10);
    }

    @Override
    protected String getTemplateName () {
        return "control.While";
    }

}
