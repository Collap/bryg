package io.collap.bryg.test.expression;

import io.collap.bryg.model.Model;
import io.collap.bryg.test.TemplateTest;

/**
 * @author Marco Pennekamp
 */
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
