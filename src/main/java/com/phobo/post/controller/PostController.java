package com.phobo.post.controller;

import com.phobo.post.dto.CreatePostRequest;
import com.phobo.post.dto.PostResponse;
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

    //test
    private final UUID HARDCODED_USER_ID = UUID.fromString("7d99d30e-caeb-4e3a-aa81-cc5a7c58a1d2");

    //Tạo bài viết
    //@RequestBody khi FE gửi JSON thì rqbody sẽ biến nó thành object để thao tác
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> createPost(@ModelAttribute CreatePostRequest request) {

        PostResponse post = postService.createPost(request, HARDCODED_USER_ID);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", post);
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
            @PathVariable("post_id") UUID postId,
            @ModelAttribute CreatePostRequest request) {

        PostResponse postResponse = postService.updatePost(postId, request, HARDCODED_USER_ID);

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
}
