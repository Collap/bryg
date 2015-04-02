package io.collap.bryg.test.expression;

import io.collap.bryg.Model;
import io.collap.bryg.test.TemplateTest;
import io.collap.bryg.test.object.TestObject;

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
