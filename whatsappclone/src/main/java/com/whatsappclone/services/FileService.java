package com.whatsappclone.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private static final String USER_DIRECTORY_PREFIX = "users";
    private static final String EMPTY_STRING = "";
    private static final char DOT = '.';

    @Value("${application.file.uploads.media-output-path}")
    private String fileUploadPath;

    public String saveFile(@NonNull MultipartFile sourceFile, @NonNull String userId) {
        final String fileUploadSubPath = buildUserDirectoryPath(userId);
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    // -------------------------- PRIVATE METHODS -----------------------------

    private String buildUserDirectoryPath(String userId) {
        return USER_DIRECTORY_PREFIX + File.separator + userId;
    }

    private String uploadFile(@NonNull MultipartFile sourceFile, @NonNull String fileUploadSubPath) {
        final String finalUploadPath = buildFinalUploadPath(fileUploadSubPath);

        if (!ensureDirectoryExists(finalUploadPath)) {
            return null;
        }
        final String targetFilePath = buildTargetFilePath(finalUploadPath, sourceFile);
        return writeFileToPath(sourceFile, targetFilePath);
    }

    private String buildFinalUploadPath(String fileUploadSubPath) {
        return fileUploadPath + File.separator + fileUploadSubPath;
    }

    private boolean ensureDirectoryExists(String directoryPath) {
        File targetFolder = new File(directoryPath);

        if (targetFolder.exists()) {
            return true;
        }
        boolean folderCreated = targetFolder.mkdirs();

        if (!folderCreated) {
            log.warn("Failed to create folder: {}", targetFolder.getAbsolutePath());
            return false;
        }
        return true;
    }

    private String buildTargetFilePath(String finalUploadPath, MultipartFile sourceFile) {
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        return finalUploadPath + File.separator + System.currentTimeMillis() + fileExtension;
    }

    private String writeFileToPath(MultipartFile sourceFile, String targetFilePath) {
        Path targetPath = Paths.get(targetFilePath);

        try {
            Files.write(targetPath, sourceFile.getBytes());
            log.info("File saved at {}", targetPath);
            return targetFilePath;
        } catch (IOException ex) {
            log.error("File was not saved", ex);
            return null;
        }
    }

    private String getFileExtension(String originalFilename) {

        if (isNullOrEmpty(originalFilename)) {
            return EMPTY_STRING;
        }
        int lastDotIndex = originalFilename.lastIndexOf(DOT);

        if (lastDotIndex == -1) {
            return EMPTY_STRING;
        }
        return DOT + originalFilename.substring(lastDotIndex + 1).toLowerCase();
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
