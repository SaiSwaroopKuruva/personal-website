package finadvisor.service.impl;

import finadvisor.config.UploadProperties;
import finadvisor.exception.InvalidFileException;
import finadvisor.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/webp", ".webp");
    private static final Set<String> ALLOWED_TYPES = ALLOWED_CONTENT_TYPES.keySet();
    private static final String PROFILE_PHOTOS_SUBDIR = "profile-photos";

    private final UploadProperties uploadProperties;

    @Override
    public String storeProfilePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("A photo file is required");
        }
        if (file.getSize() > uploadProperties.getMaxFileSizeBytes()) {
            throw new InvalidFileException("Photo must be smaller than "
                    + (uploadProperties.getMaxFileSizeBytes() / (1024 * 1024)) + "MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new InvalidFileException("Photo must be a PNG, JPEG or WEBP image");
        }

        try {
            Path targetDir = Path.of(uploadProperties.getDir(), PROFILE_PHOTOS_SUBDIR).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);
            String fileName = UUID.randomUUID() + ALLOWED_CONTENT_TYPES.get(contentType);
            Path targetFile = targetDir.resolve(fileName);
            file.transferTo(targetFile);
            return "/uploads/" + PROFILE_PHOTOS_SUBDIR + "/" + fileName;
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to store uploaded photo", ex);
        }
    }
}
