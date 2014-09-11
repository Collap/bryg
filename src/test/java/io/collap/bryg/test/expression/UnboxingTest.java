package io.collap.bryg.test.expression;

import io.collap.bryg.model.Model;
import io.collap.bryg.test.TemplateTest;

public class UnboxingTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("a", 10);
        model.setVariable ("b", 20);
    }

    @Override
    protected String getTemplateName () {
        return "expression.Unboxing";
    }

}
