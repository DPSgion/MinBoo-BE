package com.phobo.post.controller;

import com.phobo.post.dto.CreatePostRequest;
import com.phobo.post.dto.PostResponse;
import com.phobo.post.dto.ReportRequest;
import com.phobo.post.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
            @ModelAttribute CreatePostRequest request) {

        // 1. Tự động lấy username từ Token
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Truyền username xuống Service
        PostResponse postResponse = postService.createPost(request, username);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", postResponse);
        response.put("message", "Post created successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //Xóa bài viết
    @DeleteMapping("/{post_id}")
    public ResponseEntity<?> deletePost(@PathVariable("post_id") UUID postId) {

        // 1. Lấy username của người đang thực hiện request
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Truyền xuống Service
        postService.deletePost(postId, username);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("message", "Post deleted successfully");

        return ResponseEntity.ok(responseBody);
    }

    //Sửa bài viết
    @PatchMapping(value = "/{post_id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> updatePost(
            @PathVariable("post_id") UUID postId,
            @ModelAttribute CreatePostRequest request) {

        // 1. Tự động lấy username từ Token
        String username = SecurityContextHolder.getContext().getAuthentication().getName();


        PostResponse postResponse = postService.updatePost(postId,request, username);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", postResponse);
        response.put("message", "Post updated successfully");

        return ResponseEntity.ok(response);
    }

    // Xóa riêng tấm ảnh của một bài viết
    @DeleteMapping("/{post_id}/image")
    public ResponseEntity<?> deletePostImage(@PathVariable("post_id") UUID postId) {

        // 1. Lấy username của người đang thực hiện request
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Truyền xuống Service
        postService.deletePostImage(postId, username);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("message", "Post image deleted successfully");

        return ResponseEntity.ok(responseBody);
    }

    //Bảng feed
    @GetMapping("/feed")
    public ResponseEntity<Map<String, Object>> getHomeFeed(

            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {

        //Tự động lấy username từ Token
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> data = postService.getHomeFeed(username, page, limit);


        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    //báo cáo bài viết
    @PostMapping("/{post_id}/report")
    public ResponseEntity<Map<String, Object>> reportPost(

            @PathVariable("post_id") UUID postId,
            @RequestBody ReportRequest request) {

        // 1. Tự động lấy username từ Token
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Truyền username xuống Service
        postService.reportPost(postId, username, request);

        // Trả về JSON đúng chuẩn 200 OK
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Report submitted successfully");

        return ResponseEntity.ok(response);
    }


    // TÌM KIẾM BÀI VIẾT THEO CONTENT VÀ TAG
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPosts(
            @RequestParam(value = "keyword", required = false) String keyword,
            // SỬA Ở ĐÂY: Nhận vào một List thay vì 1 số
            @RequestParam(value = "tag_id", required = false) List<Integer> tagIds,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Truyền List xuống Service
        Map<String, Object> data = postService.searchPosts(username, keyword, tagIds, page, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
