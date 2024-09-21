package com.ben.file.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ben.file.Constants.MICROSERVICE_NAME;

@Slf4j
public class DiskFileStorage {

    private final ByteArrayOutputStream byteArrayOutputStream;

    public DiskFileStorage() {
        this.byteArrayOutputStream = new ByteArrayOutputStream();
    }


    public ByteArrayOutputStream getStream() {
        return this.byteArrayOutputStream;
    }

    public void write(String fileNameWithExtension) throws IOException {
        String DEFAULT_PATH = "output/";
        createNewFolder(Path.of("output"));
        try (FileOutputStream fileOutputStream = new FileOutputStream(DEFAULT_PATH.concat(fileNameWithExtension))) {
            this.byteArrayOutputStream.writeTo(fileOutputStream);
        }
    }

    public void close() throws IOException {
        this.byteArrayOutputStream.close();
    }

    private void createNewFolder(Path folderPath) {
        try {
            Files.createDirectories(folderPath);

        } catch (IOException e) {
            log.error("[{}]: Can not create folder: {}", MICROSERVICE_NAME, folderPath);
        }
    }

}