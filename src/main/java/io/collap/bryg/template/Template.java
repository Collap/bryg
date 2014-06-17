package io.collap.bryg.template;

import io.collap.bryg.model.Model;

import java.io.Writer;

public interface Template {

    public void render (Writer writer, Model model) throws InvalidInputParameterException;

}
