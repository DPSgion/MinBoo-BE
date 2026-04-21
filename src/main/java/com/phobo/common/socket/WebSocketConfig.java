package com.phobo.common.socket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Định nghĩa Endpoint để Frontend kết nối vào
        // setAllowedOriginPatterns("*") giúp test ở localhost không bị lỗi CORS
        // withSockJS() hỗ trợ các trình duyệt cũ không có WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. Kênh để Frontend LẮNG NGHE (Nhận tin nhắn từ Server)
        // Dùng "/user" cho tin nhắn cá nhân, "/topic" cho tin nhắn group/thông báo chung
        registry.enableSimpleBroker("/user", "/topic");

        // 3. Kênh để Frontend GỬI tin nhắn lên Server (Nếu dùng WS để gửi)
        registry.setApplicationDestinationPrefixes("/app");

        // 4. Prefix dành riêng cho tin nhắn cá nhân (1-1)
        registry.setUserDestinationPrefix("/user");
    }
}
