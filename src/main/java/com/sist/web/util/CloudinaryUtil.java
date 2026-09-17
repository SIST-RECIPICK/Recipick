package com.sist.web.util;


import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CloudinaryUtil {

    private final com.cloudinary.Cloudinary cloudinary;
    private static final long MAX_FILE_SIZE = 8 * 1024 * 1024; // 8MB

    public Map<String, Object> uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 용량은 8MB를 초과할 수 없습니다.");
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("url", (String) uploadResult.get("secure_url"));
        
        double sizeInMB = Math.round((file.getSize() / (1024.0 * 1024.0)) * 100.0) / 100.0;
        resultMap.put("size", sizeInMB);

        return resultMap;
    }
}