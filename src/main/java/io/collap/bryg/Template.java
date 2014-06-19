package io.collap.bryg;

import io.collap.bryg.exception.InvalidInputParameterException;
import io.collap.bryg.model.Model;

import java.io.Writer;

public interface Template {

    public void render (Writer writer, Model model) throws InvalidInputParameterException;

}
