package com.github.mschieder.idea.formatter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

class Utils {
    public static void deleteDir(final Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    public static void unzipZippedFileFromResource(final InputStream is, final File outputDir) throws IOException {
        final long now = System.nanoTime();
        final File zippedFile = new File(outputDir, "ide.zip");
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(zippedFile))) {
            is.transferTo(os);
        }
        unzipFromFile(zippedFile, outputDir);

        Log.debug(Utils.class, "unzipped in " + NANOSECONDS.toMillis(System.nanoTime() - now) + " ms");
    }

    public static void unzipFromFile(final File zippedFile, final File outputDir) throws IOException {
        try (ZipFile zipFile = new ZipFile(zippedFile)) {
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                final File entryDestination = new File(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    try (
                            InputStream in = new BufferedInputStream(zipFile.getInputStream(entry));
                            OutputStream out = new BufferedOutputStream(new FileOutputStream(entryDestination))
                    ) {
                        in.transferTo(out);
                    }
                }
            }
        }
    }
}
