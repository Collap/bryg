package io.collap.bryg.test.control;

import io.collap.bryg.Model;
import io.collap.bryg.test.TemplateTest;

public class WhileTest extends TemplateTest {

    @Override
    protected void configureModel(Model model) {
        model.setVariable("iterations", 10);
        model.setVariable("closure", null);
    }

    @Override
    protected String getTemplateName() {
        return "control.While";
    }

}
