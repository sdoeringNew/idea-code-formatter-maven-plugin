package com.github.mschieder.idea.formatter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE)
public class CheckMojo extends AbstractMojo {
    /**
     * Scan directories recursively.
     */
    @Parameter(property = "validate.recursive", defaultValue = "true")
    private boolean recursive;

    @Parameter(property = "validate.masks", defaultValue = "*.java")
    private List<String> masks;

    @Parameter(property = "validate.codestyleSettingsFile")
    private File codestyleSettingsFile;

    @Parameter(property = "validate.directories", defaultValue = "src/main/java,src/test/java")
    private List<File> directories;

    /**
     * Force charset to use when reading and writing files.
     */
    @Parameter(property = "validate.charset")
    private String charset;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().debug("codestyleSettingsFile: " + codestyleSettingsFile);
        getLog().debug("directories: " + directories);
        getLog().debug("charset: " + charset);
        getLog().debug("masks: " + masks);

        var returnCode = -1;
        try (IdeaCodeFormatterEnvironment environment = new IdeaCodeFormatterEnvironment()) {
            returnCode = environment.validate(new IdeaFormatterArgsBuilder().recursive(recursive).charset(charset).masks(masks)
                    .dryRun(true).directories(directories).codestyleSettingsFile(codestyleSettingsFile).build());
        } catch (final Exception e) {
            throw new MojoExecutionException(e);
        }
        if (returnCode == 0) {
            Log.info(CheckMojo.class, "All files are well formatted.");
        } else {
            throw new MojoExecutionException("Some file(s) need reformatting.");
        }
    }
}