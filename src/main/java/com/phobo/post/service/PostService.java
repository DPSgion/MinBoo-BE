package com.phobo.post.service;

import com.phobo.post.dto.CreatePostRequest;
import com.phobo.post.entity.Post;
import com.phobo.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepo;

    //Tạo bài viết
    public Post createPost(CreatePostRequest request){
        Post newPost = new Post();
        newPost.setContent(request.getContent());
        newPost.setPrivacy(request.getPrivacy());

        //hardcode test truoc
        newPost.setUserId(UUID.fromString("038e47d6-7ada-437e-b803-c753a9d740e4"));
        return postRepo.save(newPost);
    }

    //xóa bài viết
    public void deletePost(UUID postID){
        //kt xem postID có tồn tại không
        if(!postRepo.existsById(postID)){
            throw new RuntimeException("POST_NOT_FOUND");
        }
        postRepo.deleteById(postID);
    }

}
