package io.collap.bryg.test.function;

import io.collap.bryg.model.Model;
import io.collap.bryg.test.TemplateTest;

public class FragmentTest extends TemplateTest{

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("name", "Marco");
    }

    @Override
    protected String getTemplateName () {
        return "function.Fragment";
    }

}
