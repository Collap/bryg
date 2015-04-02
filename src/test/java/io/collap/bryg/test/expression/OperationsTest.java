package io.collap.bryg.test.expression;

import io.collap.bryg.Model;
import io.collap.bryg.test.object.Format;
import io.collap.bryg.test.TemplateTest;

public class OperationsTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("format", new Format ());
        model.setVariable ("a", 15);
        model.setVariable ("b", -5);
    }

    @Override
    protected String getTemplateName () {
        return "expression.Operations";
    }

}
