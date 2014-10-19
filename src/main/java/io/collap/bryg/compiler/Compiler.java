package io.collap.bryg.compiler;

import io.collap.bryg.TemplateType;

public interface Compiler {

    /**
     * Parses the file and initializes the TemplateType, but
     * does not compile the code yet.
     */
    public TemplateType parse (String name, String source);

    public byte[] compile (TemplateType templateType);

}
