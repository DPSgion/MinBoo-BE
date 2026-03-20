package com.phobo.post.controller;

import com.phobo.post.dto.CreatePostRequest;
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

    @Autowired //kết nối, ko cần khởi tạo new
    private PostService postService;

    //Tạo bài viết
    //@RequestBody khi FE gửi JSON thì rqbody sẽ biến nó thành object để thao tác
    @PostMapping
    public ResponseEntity<?> createNewPost(@RequestBody CreatePostRequest request){
        Post savedPost = postService.createPost(request);

        //cấu trúc respone
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("data", savedPost);
        responseBody.put("message", "OK");

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
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
}
