package com.whatsappclone.utils;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileUtils {

    private FileUtils() {}

    public static byte[] readBytesFromFile(String fileUrl) {

        if(StringUtils.isBlank(fileUrl)) {
            return new byte[0];
        }

        try {
            Path filePath = new File(fileUrl).toPath();
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            log.warn("File was not read", ex);
        }
        return new byte[0];
    }
}
