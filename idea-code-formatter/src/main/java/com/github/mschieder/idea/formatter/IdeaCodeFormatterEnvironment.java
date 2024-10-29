package com.github.mschieder.idea.formatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class IdeaCodeFormatterEnvironment implements AutoCloseable {
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
        return this.format(args, outputLines -> outputLines.forEach(line -> Log.info(IdeaCodeFormatterEnvironment.class, line)));
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
                Log.error(IdeaCodeFormatterEnvironment.class, line);
                validationOk = false;
            } else if (line.contains("...Formatted well")) {
                Log.debug(IdeaCodeFormatterEnvironment.class, line);
            } else {
                Log.info(IdeaCodeFormatterEnvironment.class, line);
            }
        }

        if (returnCode == 0) {
            return validationOk ? 0 : -1;
        }
        return returnCode;
    }

    private int doFormat(final Path formatterRoot, final String[] args, final List<String> outputLines) throws Exception {
        final String javaBin = System.getProperty("java.home") + "/bin/java";

        final Path ideHome = formatterRoot.resolve("ide");
        final String classpath;
        try (var files = Files.list(ideHome.resolve("lib"))) {
            classpath = files
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .collect(Collectors.joining(":"));
        }

        final List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-cp");
        command.add(classpath);

        command.add("-Djava.system.class.loader=com.intellij.util.lang.PathClassLoader");

        final List<String> addOpens = Files.readAllLines(ideHome.resolve("bin/idea.bat")).stream()
                .filter(line -> line.contains("--add-opens"))
                .flatMap(line -> Arrays.stream(line.split(" ")))
                .filter(token -> token.startsWith("--add-opens"))
                .toList();
        command.addAll(addOpens);

        command.add("com.intellij.idea.Main");
        command.add("format");
        command.addAll(Arrays.asList(args));

        final ProcessBuilder builder = new ProcessBuilder(command)
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.to(formatterRoot.resolve("error.log").toFile()));

        final long now = System.nanoTime();
        Process process = builder
                .start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.lines()
                    .forEach(outputLines::add);
        }

        process.waitFor();
        Log.debug(IdeaCodeFormatterEnvironment.class, "process finished after " + NANOSECONDS.toMillis(System.nanoTime() - now) + " ms");
        return process.exitValue();
    }
}
