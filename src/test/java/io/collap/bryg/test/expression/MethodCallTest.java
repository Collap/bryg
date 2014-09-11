package io.collap.bryg.test.expression;

import io.collap.bryg.model.Model;
import io.collap.bryg.test.TemplateTest;

public class MethodCallTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("obj", new TestObject ());
    }

    @Override
    protected String getTemplateName () {
        return "expression.MethodCall";
    }

}
