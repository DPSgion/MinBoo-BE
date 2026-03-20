package com.phobo.post.service;

import com.phobo.post.dto.PostRequest;
import com.phobo.post.entity.Post;
import com.phobo.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepo;

    public Post createPost(PostRequest request){
        Post newPost = new Post();
        newPost.setContent(request.getContent());
        newPost.setPrivacy(request.getPrivacy());

        //hardcode test truoc
        newPost.setUserId(UUID.fromString("038e47d6-7ada-437e-b803-c753a9d740e4"));
        return postRepo.save(newPost);
    }
}
