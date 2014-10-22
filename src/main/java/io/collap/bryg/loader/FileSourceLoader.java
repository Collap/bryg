package io.collap.bryg.loader;

import io.collap.bryg.unit.UnitClassLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileSourceLoader implements SourceLoader {

    private File templateDirectory;
    private String fileExtension = ".bryg";

    public FileSourceLoader (File templateDirectory) {
        if (!templateDirectory.exists () || !templateDirectory.isDirectory ()) {
            throw new IllegalArgumentException ("The template directory must be existing and qualified as a directory!");
        }
        this.templateDirectory = templateDirectory;
    }

    @Override
    public String getTemplateSource (String name) {
        String prefixlessName = UnitClassLoader.getPrefixlessName (name);
        String path = prefixlessName.replace ('.', File.separatorChar) + fileExtension;

        File sourceFile = new File (templateDirectory, path);

        String source = null;
        try {
            InputStream stream = new FileInputStream (sourceFile);
            byte[] data = new byte[(int) sourceFile.length ()];
            stream.read (data);
            source = new String (data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException ("Template '" + name + "' not found at '" + path + "'.", e);
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
