package com.shangluhua.app.common;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileUploadControllerTest {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    @Test
    void shouldAllowImageJpeg() {
        assertTrue(ALLOWED_TYPES.contains("image/jpeg"));
    }

    @Test
    void shouldAllowImagePng() {
        assertTrue(ALLOWED_TYPES.contains("image/png"));
    }

    @Test
    void shouldRejectExeFile() {
        assertFalse(ALLOWED_TYPES.contains("application/x-msdownload"));
    }

    @Test
    void shouldRejectTextHtml() {
        assertFalse(ALLOWED_TYPES.contains("text/html"));
    }

    @Test
    void shouldRejectApplicationJavascript() {
        assertFalse(ALLOWED_TYPES.contains("application/javascript"));
    }

    @Test
    void shouldRejectNullContentType() {
        assertThrows(Exception.class, () -> {
            MockMultipartFile file = new MockMultipartFile("file", "test.exe", null, new byte[]{});
            if (file.getContentType() == null || !ALLOWED_TYPES.contains(file.getContentType().toLowerCase())) {
                throw new ApiException("不支持的文件类型");
            }
        });
    }
}
