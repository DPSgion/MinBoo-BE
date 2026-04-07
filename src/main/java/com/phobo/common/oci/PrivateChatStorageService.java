package com.phobo.common.oci;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PrivateChatStorageService {
    // Thông tin Bucket Private của bạn
    private final String namespace = "axhjvdpvhq0q";
    private final String bucketName = "minboo-chat-storage";
    private final String region = "ap-singapore-1";
    private final String profileName = "ACCOUNT_2";

    private final ObjectStorage client;

    public PrivateChatStorageService() throws IOException {
        // Load cấu hình từ Profile riêng để không đụng hàng với Phương
        ConfigFileReader.ConfigFile configFile = ConfigFileReader.parseDefault(profileName);
        AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);

        this.client = ObjectStorageClient.builder()
                .region(region)
                .build(provider);
    }

    /**
     * Hàm Upload ảnh vào Bucket Private
     * Trả về: Object Name (Tên file trên Cloud) để lưu vào Database
     */
    public String uploadPrivateImage(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String safeFileName = sanitizeFileName(originalFilename);

        // Tạo đường dẫn file trong bucket: chats/uuid_ten-file.jpg
        String objectName = "chats/" + UUID.randomUUID().toString() + "_" + safeFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(objectName)
                .contentType(file.getContentType())
                .putObjectBody(file.getInputStream())
                .contentLength(file.getSize())
                .build();

        client.putObject(putObjectRequest);

        // Lưu ý: Chỉ trả về objectName, không trả về link URL trực tiếp vì link đó sẽ bị chặn
        return objectName;
    }

    /**
     * Hàm tạo "Vé mời" (Pre-Authenticated Request) để xem ảnh
     * Dùng hàm này mỗi khi load tin nhắn để lấy link hiển thị cho Frontend
     */
    public String getTemporaryUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) return null;

        // Link có tác dụng trong 7 ngày (Bạn có thể chỉnh ngắn hơn tùy độ bảo mật)
        Date expiration = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

        CreatePreauthenticatedRequestDetails details = CreatePreauthenticatedRequestDetails.builder()
                .name("ChatRead_" + UUID.randomUUID())
                .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectRead)
                .objectName(objectName)
                .timeExpires(expiration)
                .build();

        CreatePreauthenticatedRequestRequest request = CreatePreauthenticatedRequestRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .createPreauthenticatedRequestDetails(details)
                .build();

        CreatePreauthenticatedRequestResponse response = client.createPreauthenticatedRequest(request);

        // Ghép nối để tạo link hoàn chỉnh có chứa mã xác thực (Access URI)
        return String.format("https://objectstorage.%s.oraclecloud.com%s",
                region, response.getPreauthenticatedRequest().getAccessUri());
    }

    @PreDestroy
    public void cleanup() throws Exception {
        if (this.client != null) {
            this.client.close();
            System.out.println("PrivateChatStorageService: Đã đóng kết nối Oracle an toàn.");
        }
    }

    // Hàm phụ trợ làm sạch tên file
    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "unknown";
        String temp = Normalizer.normalize(fileName, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccent = pattern.matcher(temp).replaceAll("");
        noAccent = noAccent.replace("Đ", "D").replace("đ", "d");
        noAccent = noAccent.replaceAll("\\s+", "-");
        noAccent = noAccent.replaceAll("[^a-zA-Z0-9.\\-_]", "");
        return noAccent.toLowerCase();
    }
}
