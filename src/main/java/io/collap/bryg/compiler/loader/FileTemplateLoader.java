package io.collap.bryg.compiler.loader;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileTemplateLoader implements TemplateLoader {

    private File templateDirectory;
    private String fileExtension = ".bryg";

    public FileTemplateLoader (File templateDirectory) {
        if (!templateDirectory.exists () || !templateDirectory.isDirectory ()) {
            throw new IllegalArgumentException ("The template directory must be existing and qualified as a directory!");
        }
        this.templateDirectory = templateDirectory;
    }

    @Override
    public String getTemplateSource (String name) {
        File sourceFile = new File (templateDirectory, name + fileExtension);

        String source = null;
        try {
            InputStream stream = new FileInputStream (sourceFile);
            byte[] data = new byte[(int) sourceFile.length ()];
            stream.read (data);
            source = new String (data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace ();
        }
        return source;
    }

    public String getFileExtension () {
        return fileExtension;
    }

    /**
     * @param fileExtension Needs to be prefixed with a dot if you want a proper file extension.
     */
    public void setFileExtension (String fileExtension) {
        this.fileExtension = fileExtension;
    }

}
