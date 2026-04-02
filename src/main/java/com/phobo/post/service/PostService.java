package com.phobo.post.service;

import com.phobo.post.dto.CreatePostRequest;
import com.phobo.post.dto.PostResponse;
import com.phobo.post.dto.ReportRequest;

import java.util.Map;
import java.util.UUID;

public interface PostService {

    public PostResponse createPost(CreatePostRequest request, UUID userId);

    public void deletePost(UUID postID);

    public PostResponse updatePost(UUID postId, CreatePostRequest request, UUID userId);

    public void deletePostImage(UUID postId);

    public Map<String, Object> getHomeFeed(UUID userId, int page, int limit);

    public Map<String, Object> getUserPosts(UUID viewerId, UUID profileOwnerId, int page, int limit);

    public void reportPost(UUID postId, UUID userId, ReportRequest request);
}
