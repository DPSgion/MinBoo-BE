package com.phobo.post.controller;

import com.phobo.post.dto.CreatePostRequest;
import com.phobo.post.dto.PostResponse;
import com.phobo.post.dto.ReportRequest;
import com.phobo.post.entity.Post;
import com.phobo.post.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController //dữ liệu trả về JSON
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }



    //Tạo bài viết
    //@RequestBody khi FE gửi JSON thì rqbody sẽ biến nó thành object để thao tác
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> createPost(
            @RequestHeader("user-id") UUID userId,
            @ModelAttribute CreatePostRequest request) {

        // Truyền userId nhận được vào Service
        PostResponse postResponse = postService.createPost(request, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", postResponse);
        response.put("message", "Post created successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //Xóa bài viết
    @DeleteMapping("{post_id}")
    public ResponseEntity<?> deletePost(@PathVariable("post_id")UUID postID){
        postService.deletePost(postID);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("message", "Post deleted successfully");

        return ResponseEntity.ok(responseBody);

    }

    //Sửa bài viết
    @PatchMapping(value = "/{post_id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> updatePost(
            @RequestHeader("user-id") UUID userId,
            @PathVariable("post_id") UUID postId,
            @ModelAttribute CreatePostRequest request) {

        PostResponse postResponse = postService.updatePost(postId, request, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", postResponse);
        response.put("message", "Post updated successfully");

        return ResponseEntity.ok(response);
    }

    // Xóa riêng tấm ảnh của một bài viết
    @DeleteMapping("/{post_id}/image")
    public ResponseEntity<?> deletePostImage(@PathVariable("post_id") UUID postId) {

        // Giao toàn bộ việc nặng nhọc cho Service xử lý
        postService.deletePostImage(postId);

        // Trả kết quả về cho Frontend/Postman
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("message", "Post delete images successfully");

        return ResponseEntity.ok(responseBody);
    }

    //Bảng feed
    @GetMapping("/feed")
    public ResponseEntity<Map<String, Object>> getHomeFeed(
            @RequestHeader("user-id") UUID userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {

        Map<String, Object> data = postService.getHomeFeed(userId, page, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    //Lấy bài viết của 1 user cụ thể
    @GetMapping("/{user_id}/posts")
    public ResponseEntity<Map<String, Object>> getUserPosts(
            @RequestHeader("user-id") UUID viewerId, // Người đang xem (Lấy từ Header)
            @PathVariable("user_id") UUID profileOwnerId, // ID của trang cá nhân đang được xem (Lấy từ URL)
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {

        // Truyền cả 2 ID vào cho Service xử lý
        Map<String, Object> data = postService.getUserPosts(viewerId, profileOwnerId, page, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    //báo cáo bài viết
    @PostMapping("/{post_id}/report")
    public ResponseEntity<Map<String, Object>> reportPost(
            @RequestHeader("user-id") UUID userId,
            @PathVariable("post_id") UUID postId,
            @RequestBody ReportRequest request) {

        // Gọi Service xử lý lưu báo cáo
        postService.reportPost(postId, userId, request);

        // Trả về JSON đúng chuẩn 200 OK
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Report submitted successfully");

        return ResponseEntity.ok(response);
    }
}
