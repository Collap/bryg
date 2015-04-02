package io.collap.bryg.test.example;

import io.collap.bryg.Model;
import io.collap.bryg.test.TemplateTest;
import io.collap.bryg.test.object.Stock;

public class StocksTest extends TemplateTest {

    @Override
    protected void configureModel (Model model) {
        model.setVariable ("items", Stock.dummyItems ());
    }

    @Override
    protected String getTemplateName () {
        return "example.Stocks";
    }

}
