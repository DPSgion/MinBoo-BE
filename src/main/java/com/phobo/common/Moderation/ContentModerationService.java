package com.phobo.common.Moderation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phobo.common.exception.BusinessException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class ContentModerationService {

    // API KEY
    @Value("${openai.api.key}")
    private String openAiApiKey;
    private static final String OPENAI_URL = "https://api.openai.com/v1/moderations";

    @Value("${sightengine.api.user}")
    private String sightengineApiUser;

    @Value("${sightengine.api.secret}")
    private String sightengineApiSecret;
    private static final String SIGHTENGINE_URL = "https://api.sightengine.com/1.0/check.json";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==========================================
    // KIỂM DUYỆT CHỮ
    // ==========================================
    public void moderateText(String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        try {
            // 1. Tạo Header...
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            // 2. Tạo Body...
            String requestBody = "{\"input\": \"" + content.replace("\"", "\\\"") + "\"}";
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            // 3. Gọi API...
            ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_URL, request, String.class);

            // 4. Đọc kết quả...
            JsonNode root = objectMapper.readTree(response.getBody());
            boolean isFlagged = root.path("results").get(0).path("flagged").asBoolean();

            // Nếu AI báo vi phạm
            if (isFlagged) {
                throw new BusinessException(400, "Bài viết bị từ chối vì vi phạm tiêu chuẩn cộng đồng.");
            }

        } catch (BusinessException e) {
            //ném  ra ngoài để chặn lưu DB!
            throw e;
        } catch (Exception e) {
            System.err.println("Lỗi hệ thống khi kết nối OpenAI: " + e.getMessage());
        }
    }

    // ==========================================
    // KIỂM DUYỆT ẢNH
    // ==========================================
    public void moderateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return;
        }
        System.out.println("=== TEST SIGHTENGINE: User=[" + sightengineApiUser + "] | Secret=[" + sightengineApiSecret + "] ===");

        // CHẶN FILE FAKE
        try {
            byte[] bytes = image.getBytes();
            if (!isRealImage(bytes)) {
                throw new BusinessException(400, "File tải lên không phải là định dạng ảnh hợp lệ.");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "Lỗi đọc file ảnh.");
        }

        //  KIỂM DUYỆT BẰNG AI
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // models muốn AI quét. Ở đây chọn quét: Nudity (18+), Vũ khí, Ma túy, và Ảnh phản cảm
            body.add("models", "nudity-2.0,wad,offensive,gore");
            body.add("api_user", sightengineApiUser);
            body.add("api_secret", sightengineApiSecret);

            //Chuyển MultipartFile thành ByteArrayResource để RestTemplate có thể gửi đi được
            ByteArrayResource fileAsResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename(); // Bắt buộc phải có để API nhận diện đuôi file
                }
            };
            body.add("media", fileAsResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Gửi ảnh sang máy chủ Sightengine
            ResponseEntity<String> response = restTemplate.postForEntity(SIGHTENGINE_URL, requestEntity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            // ĐỌC ĐIỂM SỐ AI TRẢ VỀ

            // 1. Điểm nhạy cảm (18+)
            double nudityScore = root.path("nudity").path("sexual_activity").asDouble(0) +
                    root.path("nudity").path("sexual_display").asDouble(0) +
                    root.path("nudity").path("erotica").asDouble(0);

            // 2. Điểm bạo lực/vũ khí
            double weaponScore = root.path("weapon").asDouble(0);
            double drugsScore = root.path("drugs").asDouble(0);

            // 3. Điểm máu me bạo lực (gore)
            double goreScore = root.path("gore").path("prob").asDouble(0);

            // Đặt ngưỡng chặn (Threshold). Trên 50% (0.5) chắc chắn là chặn.
            double threshold = 0.5;

            if (nudityScore > threshold || weaponScore > threshold || drugsScore > threshold || goreScore > threshold) {
                throw new BusinessException(400, "Hình ảnh vi phạm tiêu chuẩn cộng đồng do Chứa nội dung nhạy cảm, bạo lực hoặc chất cấm,...");
            }

        } catch (BusinessException e) {
            throw e; // Ném lỗi văng ra ngoài cho Controller bắt
        } catch (Exception e) {
            System.err.println("Lỗi kết nối AI Sightengine: " + e.getMessage());
        }
    }

    // Hàm phụ trợ soi Magic Bytes (Tránh user đổi tên file)
    private boolean isRealImage(byte[] bytes) {
        if (bytes == null || bytes.length < 4) return false;
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        String hexHeader = hexString.toString().toUpperCase();
        return hexHeader.startsWith("FFD8FF") || hexHeader.startsWith("89504E47") ||
                hexHeader.startsWith("47494638") || hexHeader.startsWith("52494646") ||
                hexHeader.startsWith("00000020");
    }
}