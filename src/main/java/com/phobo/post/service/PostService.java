package com.phobo.post.service;

import com.phobo.common.oci.ImageStorageService;
import com.phobo.post.dto.CreatePostRequest;
import com.phobo.post.dto.PostResponse;
import com.phobo.post.entity.Post;
import com.phobo.post.entity.PostTag;
import com.phobo.post.entity.Tag;
import com.phobo.post.mapper.PostMapper;
import com.phobo.post.repository.PostRepository;
import com.phobo.post.repository.PostTagRepository;
import com.phobo.post.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final ImageStorageService imageStorageService;
    private final PostMapper postMapper;

    public PostService(PostRepository postRepository, TagRepository tagRepository, PostTagRepository postTagRepository, ImageStorageService imageStorageService, PostMapper postMapper) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
        this.imageStorageService = imageStorageService;
        this.postMapper = postMapper;
    }

    @Transactional
    public PostResponse createPost(CreatePostRequest request, UUID userId) {
        // Kiểm tra nội dung
        boolean hasContent = request.getContent() != null && !request.getContent().trim().isEmpty();
        boolean hasImage = request.getUrl_img() != null && !request.getUrl_img().isEmpty();

        if (!hasContent && !hasImage) {
            throw new IllegalArgumentException("Post phải có nội dung hoặc hình ảnh");
        }

        // Upload ảnh lên oracle
        String imageUrl = null;
        if (hasImage) {
            try {
                imageUrl = imageStorageService.uploadImage(request.getUrl_img());
            }catch (Exception e){
                throw new RuntimeException("Upload ảnh lên oracle thất bại: " + e.getMessage(), e);
            }
        }

        // Tạo Post
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(hasContent ? request.getContent().trim() : null);
        post.setUrlImg(imageUrl);
        if (request.getPrivacy() != null) {
            post.setPrivacy(request.getPrivacy().trim().toLowerCase());
        }

        // Lưu Post để có ID
        Post savedPost = postRepository.save(post);

        // Gán tags
        if (request.getTag_ids() != null && !request.getTag_ids().isEmpty()) {

            // 1. Lọc bỏ các tag_id bị trùng lặp từ Postman (Ví dụ gửi [1, 1, 2] sẽ tự gộp thành [1, 2])
            List<Integer> uniqueTagIds = request.getTag_ids().stream()
                    .distinct()
                    .collect(Collectors.toList());

            List<Tag> tags = tagRepository.findAllById(uniqueTagIds);

            for (Tag tag : tags) {
                PostTag postTag = new PostTag();
                // Sử dụng id đã được khởi tạo trong PostTag
                postTag.getId().setPostId(savedPost.getPostId());
                postTag.getId().setTagId(tag.getTagId());
                postTag.setPost(savedPost);
                postTag.setTag(tag);

                savedPost.getPostTags().add(postTag);
            }

            savedPost = postRepository.save(savedPost);
        }

        return postMapper.toResponse(savedPost);
    }

    //xóa bài viết
    @Transactional
    public void deletePost(UUID postID){
        //kt xem postID có tồn tại không
        Post post = postRepository.findById(postID)
                .orElseThrow(()-> new RuntimeException("POST_NOT_FOUND"));
        //Xóa file ảnh trên Oracle
        if (post.getUrlImg() != null) {
            imageStorageService.deleteImageByUrl(post.getUrlImg());
        }
        //Xóa bài viết trong Database
        postRepository.delete(post);
    }

    // Hàm update
    @Transactional
    public PostResponse updatePost(UUID postId, CreatePostRequest request, UUID userId) {
        // Tìm bài viết xem có tồn tại không
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết để sửa"));

        // Kiểm tra quyền (Chỉ người tạo mới được phép sửa bài của mình)
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài viết này");
        }

        // Cập nhật Nội dung & Quyền riêng tư (Nếu người dùng có gửi lên thì mới sửa)
        if (request.getContent() != null) {
            post.setContent(request.getContent().trim());
        }
        if (request.getPrivacy() != null) {
            post.setPrivacy(request.getPrivacy().trim().toLowerCase());
        }

        //XỬ LÝ ẢNH
        boolean hasNewImage = request.getUrl_img() != null && !request.getUrl_img().isEmpty();
        if (hasNewImage) {

            if (post.getUrlImg() != null) {
                try {
                    imageStorageService.deleteImageByUrl(post.getUrlImg());
                    System.out.println("Đã xóa ảnh cũ trên Cloud: " + post.getUrlImg());
                } catch (Exception e) {
                    System.err.println("Lỗi khi xóa ảnh cũ (có thể file không tồn tại): " + e.getMessage());
                }
            }

            //Upload ảnh mới và lưu link mới vào DB
            try {
                String newImageUrl = imageStorageService.uploadImage(request.getUrl_img());
                post.setUrlImg(newImageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Upload ảnh mới thất bại: " + e.getMessage(), e);
            }
        }

        // Cập nhật Tags
        if (request.getTag_ids() != null) {
            // Xóa các liên kết tag cũ trong DB
            if (post.getPostTags() != null && !post.getPostTags().isEmpty()) {
                postTagRepository.deleteAll(post.getPostTags());
                post.getPostTags().clear(); // Xóa tạm trong bộ nhớ
            }

            // Thêm tags mới
            if (!request.getTag_ids().isEmpty()) {
                List<Tag> tags = tagRepository.findAllById(request.getTag_ids());
                for (Tag tag : tags) {
                    PostTag postTag = new PostTag();
                    postTag.getId().setPostId(post.getPostId());
                    postTag.getId().setTagId(tag.getTagId());
                    postTag.setPost(post);
                    postTag.setTag(tag);
                    postTagRepository.save(postTag);
                }
            }
        }
        //Cập nhập thời gian
        post.setUpdateAt(java.time.LocalDateTime.now());

        //Lưu thay đổi và dùng Mapper trả về
        Post updatedPost = postRepository.save(post);
        return postMapper.toResponse(updatedPost);
    }

    //Hàm xóa riêng ảnh
    @Transactional
    public void deletePostImage(UUID postId) {
        // 1. Tìm bài viết trong Database
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết để xóa ảnh"));

        // 2. Nếu bài viết có ảnh thì mới tiến hành xóa
        if (post.getUrlImg() != null) {
            try {
                // Xóa ảnh vật lý trên Oracle Cloud
                imageStorageService.deleteImageByUrl(post.getUrlImg());
            } catch (Exception e) {
                // In ra log nếu Cloud báo lỗi, nhưng vẫn tiếp tục chạy để xóa link trong DB
                System.err.println("Lỗi khi xóa ảnh trên Cloud: " + e.getMessage());
            }

            // Xóa link ảnh trong Entity và lưu lại xuống DB
            post.setUrlImg(null);
            postRepository.save(post);
        }
    }

}
