package com.github.mschieder.idea.formatter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

class Utils {
    public static void deleteDir(final Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    public static void unzipZippedFileFromResource(final InputStream inputStream, final Path outputDir) throws IOException {
        final long now = System.nanoTime();
        try (ZipInputStream zipStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                final Path entryDestination = outputDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryDestination);
                } else {
                    Files.createDirectories(entryDestination.getParent());
                    Files.copy(zipStream, entryDestination);
                }
                zipStream.closeEntry();
            }
        }
        Log.debug(Utils.class, "unzipped in " + NANOSECONDS.toMillis(System.nanoTime() - now) + " ms");
    }
}
