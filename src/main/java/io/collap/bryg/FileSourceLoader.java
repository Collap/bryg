package io.collap.bryg;

import io.collap.bryg.internal.StandardClassLoader;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileSourceLoader implements SourceLoader {

    private File templateDirectory;
    private String fileExtension;

    public FileSourceLoader(File templateDirectory) {
        this(templateDirectory, ".bryg");
    }

    /**
     * @param fileExtension Needs to be prefixed with a dot.
     */
    public FileSourceLoader(File templateDirectory, String fileExtension) {
        if (!templateDirectory.exists()) {
            throw new IllegalArgumentException("The template directory must be existing!");
        }

        if (!templateDirectory.isDirectory()) {
            throw new IllegalArgumentException("The template directory must be qualified as a directory!");
        }

        this.templateDirectory = templateDirectory;
        this.fileExtension = fileExtension;
    }

    @Override
    public @Nullable String getTemplateSource(String name) {
        String path = name.replace('.', File.separatorChar) + fileExtension;
        File sourceFile = new File(templateDirectory, path);
        if (!sourceFile.exists()) {
            return null;
        }

        String source;
        try (InputStream stream = new FileInputStream(sourceFile)) {
            byte[] data = new byte[(int) sourceFile.length()];
            stream.read(data);
            source = new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Template '" + name + "' could not be loaded from '" + path + "'.", e);
        }
        return source;
    }

}
