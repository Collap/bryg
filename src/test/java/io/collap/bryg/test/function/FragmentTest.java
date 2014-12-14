package io.collap.bryg.test.function;

import io.collap.bryg.model.BasicModel;
import io.collap.bryg.model.Model;
import io.collap.bryg.template.Template;
import io.collap.bryg.test.TemplateTest;

import java.io.IOException;
import java.io.StringWriter;

public class FragmentTest extends TemplateTest{

    @Override
    protected void render (Template template, Model model) throws IOException {
        super.render (template, model);

        Model headModel = new BasicModel ();
        headModel.setVariable ("name", "Marco");

        StringWriter writer = new StringWriter ();
        template.call ("head", writer, headModel);
        System.out.println ("Called fragment 'head' from Java with name='Marco':");
        System.out.println (writer.toString ());
        System.out.println ();
    }

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("name", "Marco");
    }

    @Override
    protected String getTemplateName () {
        return "function.Fragment";
    }

}
