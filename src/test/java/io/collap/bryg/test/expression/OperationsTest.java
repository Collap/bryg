package io.collap.bryg.test.expression;

import io.collap.bryg.model.Model;
import io.collap.bryg.test.TemplateTest;

public class OperationsTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("a", 15);
        model.setVariable ("b", -5);
    }

    @Override
    protected String getTemplateName () {
        return "expression.Operations";
    }

}
