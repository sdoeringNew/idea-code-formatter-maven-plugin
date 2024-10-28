package com.github.mschieder.idea.formatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.zip.ZipInputStream;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class.getName());

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

        log.info("unzipped in {} ms", NANOSECONDS.toMillis(System.nanoTime() - now));
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

    public static void unzipFromStream(final InputStream is, final File outputDir) throws IOException {
        final long now = System.nanoTime();
        try (ZipInputStream zipInputstream = new ZipInputStream(new BufferedInputStream(is))) {
            ZipEntry entry;
            while ((entry = zipInputstream.getNextEntry()) != null) {
                final File entryDestination = new File(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    try (OutputStream out = new FileOutputStream(entryDestination)) {
                        copy(zipInputstream, out);
                    }
                }
            }
        }
        log.info("unzipped in {] ms", NANOSECONDS.toMillis(System.nanoTime() - now));
    }

    private static void copy(final InputStream source, final OutputStream target) throws IOException {
        final int bufferSize = 4 * 1024;
        final byte[] buffer = new byte[bufferSize];

        int nextCount;
        while ((nextCount = source.read(buffer)) >= 0) {
            target.write(buffer, 0, nextCount);
        }
    }


    public static String getJarName() {
        return getJarName(Utils.class);
    }

    public static boolean isPackagedInJar() {
        return getJarName().endsWith("jar");
    }


    public static String getJarName(final Class theClass) {
        return new File(theClass.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
    }

    public static String getJarPath(final Class theClass) {
        return theClass.getProtectionDomain().getCodeSource().getLocation().getPath();
    }
}
