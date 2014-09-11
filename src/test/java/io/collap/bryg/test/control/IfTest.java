package io.collap.bryg.test.control;

import io.collap.bryg.model.Model;
import io.collap.bryg.test.TemplateTest;

public class IfTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("number", 42);
    }

    @Override
    protected String getTemplateName () {
        return "control.If";
    }

}
