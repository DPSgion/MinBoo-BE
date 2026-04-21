package com.phobo.notification.controller;

import com.phobo.common.exception.BusinessException;
import com.phobo.notification.dto.NotificationResponse;
import com.phobo.notification.service.NotificationService;
import com.phobo.user.entity.User;
import com.phobo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // Bê nguyên hàm lấy ID thần thánh từ ChatController sang dùng lại cho tiện
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Vui lòng đăng nhập để sử dụng tính năng này!");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Lỗi xác thực: Không tìm thấy tài khoản trong hệ thống"));

        return user.getId();
    }

    // 1. Lấy danh sách thông báo của tôi (Load lúc vừa mở app)
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        UUID currentUserId = getCurrentUserId();
        List<NotificationResponse> notifications = notificationService.getMyNotifications(currentUserId);
        return ResponseEntity.ok(notifications);
    }

    // 2. Đánh dấu 1 thông báo là đã xem (Khi user click vào)
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable UUID notificationId) {
        UUID currentUserId = getCurrentUserId();
        notificationService.markAsRead(notificationId, currentUserId);
        return ResponseEntity.ok().build();
    }
}