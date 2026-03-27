package com.phobo.post.service;

import com.phobo.post.dto.CreatePostRequest;
import com.phobo.post.dto.PostResponse;
import com.phobo.post.entity.Post;
import com.phobo.post.entity.PostTag;
import com.phobo.post.entity.Tag;
import com.phobo.post.repository.PostRepository;
import com.phobo.post.repository.PostTagRepository;
import com.phobo.post.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service

public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final FileUploadService fileUploadService;
    private final PostTagRepository postTagRepository;

    public PostService(PostRepository postRepository, TagRepository tagRepository, FileUploadService fileUploadService, PostTagRepository postTagRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.fileUploadService = fileUploadService;
        this.postTagRepository = postTagRepository;
    }

    @Transactional
    public Post createPost(CreatePostRequest request, UUID userId) {
        // Kiểm tra nội dung
        boolean hasContent = request.getContent() != null && !request.getContent().trim().isEmpty();
        boolean hasImage = request.getUrl_img() != null && !request.getUrl_img().isEmpty();

        if (!hasContent && !hasImage) {
            throw new IllegalArgumentException("Post phải có nội dung hoặc hình ảnh");
        }

        // Upload ảnh
        String imageUrl = null;
        if (hasImage) {
            imageUrl = fileUploadService.uploadImage(request.getUrl_img(), "posts");
        }

        // Tạo Post
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(hasContent ? request.getContent().trim() : null);
        post.setUrlImg(imageUrl);
        post.setPrivacy(request.getPrivacy());

        // Lưu Post để có ID
        Post savedPost = postRepository.save(post);

        // Gán tags (nếu có)
        if (request.getTag_ids() != null && !request.getTag_ids().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTag_ids());
            for (Tag tag : tags) {
                PostTag postTag = new PostTag();
                // Sử dụng id đã được khởi tạo trong PostTag
                postTag.getId().setPostId(savedPost.getPostId());
                postTag.getId().setTagId(tag.getTagId());
                postTag.setPost(savedPost);
                postTag.setTag(tag);
                postTagRepository.save(postTag);
            }
        }

        return savedPost;
    }
    //xóa bài viết
    public void deletePost(UUID postID){
        //kt xem postID có tồn tại không
        if(!postRepository.existsById(postID)){
            throw new RuntimeException("POST_NOT_FOUND");
        }
        postRepository.deleteById(postID);
    }

}
