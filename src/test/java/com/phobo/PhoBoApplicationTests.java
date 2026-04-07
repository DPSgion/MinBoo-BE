package com.phobo;

import com.phobo.common.oci.ImageStorageService;
import com.phobo.common.oci.PrivateChatStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class PhoBoApplicationTests {

    // Đổi thành @MockitoBean cho chuẩn với Spring Boot 4.x
    @MockitoBean
    private ImageStorageService imageStorageService;

    @MockitoBean
    private PrivateChatStorageService privateChatStorageService;

    @Test
    void contextLoads() {
    }
}