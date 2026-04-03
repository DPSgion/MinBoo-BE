package com.phobo.common.oci;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ImageStorageService {

    private final String namespace = "axqv9e1of21u";
    private final String bucketName = "minboo-storage";
    private final String region = "ap-singapore-1";
    private final ObjectStorage client;

    public ImageStorageService() throws Exception {
        ConfigFileReader.ConfigFile configFile = ConfigFileReader.parseDefault();
        AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);
        this.client = ObjectStorageClient.builder().build(provider);
    }

    public String uploadImage(MultipartFile file, String folderName) throws Exception {
        String originalFilename = file.getOriginalFilename();

        String safeFileName = sanitizeFileName(originalFilename);

        String rawFileName = UUID.randomUUID().toString() + "_" + safeFileName;

        String objectName = folderName + "/" + rawFileName;

        String contentType = file.getContentType();

        // Ép kiểu dữ liệu chuẩn xác để trình duyệt cho phép xem ảnh
        if (contentType == null || contentType.equals("application/octet-stream")) {
            if (originalFilename != null && originalFilename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (originalFilename != null && (originalFilename.toLowerCase().endsWith(".jpg") || originalFilename.toLowerCase().endsWith(".jpeg"))) {
                contentType = "image/jpeg";
            } else {
                contentType = "application/octet-stream";
            }
        }

        // Đẩy file lên Cloud
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(objectName)
                .contentType(contentType)
                .putObjectBody(file.getInputStream())
                .contentLength(file.getSize())
                .build();

        client.putObject(putObjectRequest);

        return getPublicImageUrl(objectName);
    }
    public String getPublicImageUrl(String objectName) {

        return String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                region, namespace, bucketName, objectName);
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (this.client != null) {
                System.out.println("Cleaning and disconnect with Oracle Cloud...");
                this.client.close();
                System.out.println("Disconnected successfully !");
            }
        } catch (Exception e) {
            System.err.println("There are errors when close Oracle Client: " + e.getMessage());
        }
    }


    // Hàm xóa ảnh từ URL
    public void deleteImageByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // 1. Dùng mẹo cắt chuỗi để lấy objectName từ URL dài
            // URL luôn có đoạn "/o/" đứng trước tên file
            String marker = "/o/";
            int index = imageUrl.indexOf(marker);
            if (index == -1) {
                return; // Link không đúng chuẩn Oracle, bỏ qua
            }

            // Lấy phần đuôi sau chữ "/o/" (chính là posts/tên-file.png)
            String objectName = imageUrl.substring(index + marker.length());

            // 2. Kết nối với Oracle
            ConfigFileReader.ConfigFile configFile = ConfigFileReader.parseDefault();
            AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);
            ObjectStorage client = ObjectStorageClient.builder().build(provider);

            // 3. Gửi lệnh "Trảm"
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .objectName(objectName)
                    .build();

            client.deleteObject(deleteObjectRequest);

            System.out.println("Đã dọn dẹp ảnh trên Oracle: " + objectName);

        } catch (Exception e) {
            // Dùng try-catch để lỡ ảnh đã bị xóa tay trên web Oracle rồi thì code không bị văng lỗi sập server
            System.err.println("Lỗi khi xóa ảnh trên Oracle (có thể file không còn tồn tại): " + e.getMessage());
        }
    }


    // Hàm phụ trợ giúp làm sạch tên file
    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "unknown_file";
        }

        // 1. Gỡ bỏ dấu tiếng Việt (Ví dụ: "Ảnh mèo" -> "Anh meo")
        String temp = Normalizer.normalize(fileName, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccent = pattern.matcher(temp).replaceAll("");

        // Chữ Đ/đ trong tiếng Việt Normalizer không tự xử lý được nên phải tự đổi
        noAccent = noAccent.replace("Đ", "D").replace("đ", "d");

        // 2. Thay thế khoảng trắng bằng dấu gạch ngang
        noAccent = noAccent.replaceAll("\\s+", "-");

        // 3. Quét sạch mọi ký tự lạ, chỉ giữ lại chữ cái, số, dấu gạch ngang, gạch dưới và dấu chấm (cho phần mở rộng .png, .jpg)
        noAccent = noAccent.replaceAll("[^a-zA-Z0-9.\\-_]", "");

        // Chuyển tất cả thành chữ thường cho đẹp URL
        return noAccent.toLowerCase();
    }
}