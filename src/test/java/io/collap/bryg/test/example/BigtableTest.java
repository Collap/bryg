package io.collap.bryg.test.example;

import io.collap.bryg.Model;
import io.collap.bryg.test.TemplateTest;

public class BigtableTest extends TemplateTest {

    @Override
    protected void configureModel(Model model) {

    }

    @Override
    protected String getTemplateName() {
        return "example.Bigtable";
    }

}
