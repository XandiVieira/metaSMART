package com.relyon.metasmart.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.relyon.metasmart.config.CloudinaryConfig;
import com.relyon.metasmart.exception.ImageUploadException;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/webp"
    };

    @Setter(onMethod_ = {@Autowired(required = false)})
    private Cloudinary cloudinary;

    private final CloudinaryConfig cloudinaryConfig;

    public String uploadProfilePicture(MultipartFile file, Long userId) {
        validateFile(file);

        if (cloudinary == null) {
            throw new ImageUploadException("Cloudinary is not configured");
        }

        try {
            var publicId = cloudinaryConfig.getFolder() + "/user_" + userId;

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", publicId,
                    "overwrite", true,
                    "resource_type", "image",
                    "transformation", ObjectUtils.asMap(
                            "width", 400,
                            "height", 400,
                            "crop", "fill",
                            "gravity", "face",
                            "quality", "auto",
                            "fetch_format", "auto"
                    )
            ));

            var secureUrl = (String) uploadResult.get("secure_url");
            log.info("Profile picture uploaded for user ID: {}. URL: {}", userId, secureUrl);

            return secureUrl;

        } catch (IOException e) {
            log.error("Failed to upload profile picture for user ID: {}", userId, e);
            throw new ImageUploadException("Failed to upload image: " + e.getMessage());
        }
    }

    public void deleteProfilePicture(Long userId) {
        if (cloudinary == null) {
            log.warn("Cloudinary is not configured. Cannot delete profile picture.");
            return;
        }

        try {
            var publicId = cloudinaryConfig.getFolder() + "/user_" + userId;
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Profile picture deleted for user ID: {}", userId);

        } catch (IOException e) {
            log.error("Failed to delete profile picture for user ID: {}", userId, e);
        }
    }

    public boolean isConfigured() {
        return cloudinaryConfig.isConfigured();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageUploadException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImageUploadException("File size exceeds maximum allowed (10MB)");
        }

        var contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new ImageUploadException("Invalid file type. Allowed types: JPEG, PNG, GIF, WebP");
        }
    }

    private boolean isAllowedContentType(String contentType) {
        for (var allowed : ALLOWED_CONTENT_TYPES) {
            if (allowed.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}
