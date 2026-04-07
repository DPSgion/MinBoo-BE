package com.phobo.post.service;

import com.phobo.post.dto.CreatePostRequest;
import com.phobo.post.dto.PostResponse;
import com.phobo.post.dto.ReportRequest;

import java.util.Map;
import java.util.UUID;

public interface PostService {

    public PostResponse createPost(CreatePostRequest request, String username);

    void deletePost(UUID postId, String username);

    public PostResponse updatePost(UUID postId, CreatePostRequest request, String username);

    void deletePostImage(UUID postId, String username);

    public Map<String, Object> getHomeFeed(String username, int page, int limit);

    public Map<String, Object> getUserPosts(String viewername, UUID profileOwnerId, int page, int limit);

    public void reportPost(UUID postId, String username, ReportRequest request);
}
