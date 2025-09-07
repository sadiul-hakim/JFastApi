package com.jFastApi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class FileUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtility.class);
    private static final int BUFFER_SIZE = 8192;

    private FileUtility() {
    }

    /**
     * Simplest & fast: reads entire file into a string (UTF-8).
     */
    public static String quickRead(String fullPath) throws IOException {
        return Files.readString(Path.of(fullPath), StandardCharsets.UTF_8);
    }

    public static String quickRead(URI fullPath) throws IOException {
        return Files.readString(Path.of(fullPath), StandardCharsets.UTF_8);
    }

    /**
     * Reads entire file using FileChannel + direct buffer.
     * Safer than the first approach for big files.
     */
    public static String readEntireFile(String fullPath) {
        return readEntireFile(fullPath, StandardCharsets.UTF_8);
    }

    public static String readEntireFile(String fullPath, Charset charset) {
        Path path = Path.of(fullPath);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = channel.size();
            if (fileSize > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("File too large to read into memory: " + fileSize + " bytes");
            }

            ByteBuffer buffer = ByteBuffer.allocateDirect((int) fileSize);
            while (buffer.hasRemaining()) {
                if (channel.read(buffer) == -1) break; // EOF safety
            }
            buffer.flip();
            return charset.decode(buffer).toString();

        } catch (Exception ex) {
            LOGGER.error("Failed to read file {}", fullPath, ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Reads the entire InputStream into a string (UTF-8).
     */
    public static String readStream(InputStream inputStream) {
        return readStream(inputStream, StandardCharsets.UTF_8);
    }

    public static String readStream(InputStream inputStream, Charset charset) {
        try (InputStream in = inputStream;
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            return out.toString(charset);

        } catch (IOException ex) {
            LOGGER.error("Failed to read from InputStream", ex);
            throw new RuntimeException(ex);
        }
    }
}

