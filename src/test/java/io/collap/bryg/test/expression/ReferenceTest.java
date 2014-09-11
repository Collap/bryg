package io.collap.bryg.test.expression;

import io.collap.bryg.model.Model;
import io.collap.bryg.test.TemplateTest;

public class ReferenceTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("a", new Object ());
        model.setVariable ("b", null);
    }

    @Override
    protected String getTemplateName () {
        return "expression.Reference";
    }

}
