package com.github.mschieder.idea.formatter;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class IdeaCodeFormatterEnvironment implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(IdeaCodeFormatterEnvironment.class);
    private final Path tmpFormatterRoot;

    public IdeaCodeFormatterEnvironment() throws IOException {
        tmpFormatterRoot = extractPortableIde();
    }

    @Override
    public void close() throws Exception {
        Utils.deleteDir(tmpFormatterRoot);
    }

    private Path extractPortableIde() throws IOException {
        final Path tmpFormatterRoot = Files.createTempDirectory("formatterRoot");
        final InputStream ideStream = IdeaCodeFormatterMain.class.getResourceAsStream("/ide.zip");
        Utils.unzipZippedFileFromResource(ideStream, tmpFormatterRoot.toFile());
        return tmpFormatterRoot;
    }

    public int format(final String[] args) throws Exception {
        return this.format(args, outputLines -> outputLines.forEach(log::info));
    }

    public int format(final String[] args, final Consumer<List<String>> outputLinePrinter) throws Exception {
        final List<String> outputLines = new ArrayList<>();
        final int returnCode = doFormat(tmpFormatterRoot, args, outputLines);
        outputLinePrinter.accept(outputLines);
        return returnCode;
    }

    public int validate(final String[] args) throws Exception {
        final List<String> argsList = new ArrayList<>(Arrays.asList(args));
        if (!argsList.contains("-d") && !argsList.contains("-dry")) {
            argsList.add(0, "-dry");
        }

        final List<String> outputLines = new ArrayList<>();
        final int returnCode = doFormat(tmpFormatterRoot, argsList.toArray(new String[0]), outputLines);

        boolean validationOk = true;
        for (String line : outputLines) {
            if (line.contains("...Needs reformatting")) {
                log.error(line);
                validationOk = false;
            } else {
                log.info(line);
            }
        }

        if (returnCode == 0) {
            return validationOk ? 0 : -1;
        }
        return returnCode;
    }

    private int doFormat(final Path formatterRoot, final String[] args, final List<String> outputLines) throws Exception {

        final String javaBin = System.getProperty("java.home") + "/bin/java";

        final String ideHome = formatterRoot.resolve("ide").toString();
        final String appdata = formatterRoot.resolve("appdata").toString();
        final String localAppdata = formatterRoot.resolve("localAppdata").toString();

        final String classpath;
        if (Utils.isPackagedInJar()) {
            classpath = new File(Utils.getJarPath(Utils.class)).toString();
        } else {
            // add the current classpath (for unit testing)
            classpath = System.getProperty("java.class.path");
        }

        final List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-cp");
        command.add(classpath);

        command.add("-Didea.home.path=" + ideHome);
        command.add("-Djava.system.class.loader=com.intellij.util.lang.PathClassLoader");
        command.add("-Didea.vendor.name=JetBrains");
        command.add("-Didea.paths.selector=IdeaIC2023.1");
        command.add("-Djna.nosys=true");
        command.add("-Djna.noclasspath=true");
        command.add("-Didea.platform.prefix=Idea");
        command.add("-Dsplash=false");


        command.add("--add-opens=java.base/java.io=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.lang.ref=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.lang.reflect=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.net=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.nio=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.nio.charset=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.text=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.time=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.util=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.util.concurrent=ALL-UNNAMED");
        command.add("--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED");
        command.add("--add-opens=java.base/jdk.internal.vm=ALL-UNNAMED");
        command.add("--add-opens=java.base/sun.nio.ch=ALL-UNNAMED");
        command.add("--add-opens=java.base/sun.nio.fs=ALL-UNNAMED");
        command.add("--add-opens=java.base/sun.security.ssl=ALL-UNNAMED");
        command.add("--add-opens=java.base/sun.security.util=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/java.awt=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/java.awt.dnd.peer=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/java.awt.event=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/java.awt.image=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/java.awt.peer=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/java.awt.font=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/javax.swing=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.awt.datatransfer=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.awt.image=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.awt.windows=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.awt=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.font=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.java2d=ALL-UNNAMED");
        command.add("--add-opens=java.desktop/sun.swing=ALL-UNNAMED");
        command.add("--add-opens=jdk.attach/sun.tools.attach=ALL-UNNAMED");
        command.add("--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED");
        command.add("--add-opens=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED");
        command.add("--add-opens=jdk.jdi/com.sun.tools.jdi=ALL-UNNAMED");


        command.add("com.intellij.idea.Main");
        command.add("format");
        command.addAll(Arrays.asList(args));

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.environment().put("APPDATA", appdata);
        builder.environment().put("LOCALAPPDATA", localAppdata);

        final Stopwatch sw = Stopwatch.createStarted();
        Process process = builder
                //       .inheritIO()
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.to(formatterRoot.resolve("error.log").toFile()))
                .start();

        outputLines.addAll(IOUtils.readLines(process.getInputStream()));

        process.waitFor();
        sw.stop();
        log.info("process finished after {} ms", sw.elapsed().toMillis());
        return process.exitValue();
    }
}
