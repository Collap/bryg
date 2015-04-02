package io.collap.bryg.test.expression;

import io.collap.bryg.Model;
import io.collap.bryg.test.TemplateTest;

public class TemplateCallTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("n", 3);
    }

    @Override
    protected String getTemplateName () {
        return "expression.TemplateCall";
    }

}
