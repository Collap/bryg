package io.collap.bryg.test.control;

import io.collap.bryg.model.Model;
import io.collap.bryg.test.TemplateTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Pennekamp
 */
public class EachTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        List<String> names = new ArrayList<> ();
        names.add ("Robert");
        names.add ("Dany");
        names.add ("Tyrion");
        model.setVariable ("names", names);
    }

    @Override
    protected String getTemplateName () {
        return "control.Each";
    }

}
