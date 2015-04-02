package io.collap.bryg;

public class TemplateInstantiationException extends RuntimeException {

    public TemplateInstantiationException(String name, Throwable cause) {
        super("The template '" + name + "' could not be instantiated.", cause);
    }

}
