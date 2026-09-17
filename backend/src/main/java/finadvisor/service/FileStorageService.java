package finadvisor.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /** Validates and stores the file, returning a public URL path (e.g. {@code /uploads/profile-photos/<name>}). */
    String storeProfilePhoto(MultipartFile file);
}
