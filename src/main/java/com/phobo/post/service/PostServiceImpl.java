package com.phobo.post.service;

import com.phobo.common.Moderation.ContentModerationService;
import com.phobo.common.exception.BusinessException;
import com.phobo.post.dto.ReportRequest;
import com.phobo.post.entity.Report;
import com.phobo.post.repository.ReportRepository;
import com.phobo.tag.entity.Tag;
import com.phobo.tag.repository.TagRepository;
import com.phobo.user.entity.User;
import com.phobo.user.repository.UserRepository;
import com.phobo.common.oci.ImageStorageService;
import com.phobo.post.dto.CreatePostRequest;
import com.phobo.post.dto.FeedPostDto;
import com.phobo.post.dto.PostResponse;
import com.phobo.post.entity.Post;
import com.phobo.post.entity.PostTag;
import com.phobo.post.mapper.PostMapper;
import com.phobo.post.repository.PostRepository;
import com.phobo.post.repository.PostTagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final ImageStorageService imageStorageService;
    private final PostMapper postMapper;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ContentModerationService contentModerationService;

    public PostServiceImpl(PostRepository postRepository, TagRepository tagRepository, PostTagRepository postTagRepository, ImageStorageService imageStorageService, PostMapper postMapper, UserRepository userRepository, ReportRepository reportRepository, ContentModerationService contentModerationService) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
        this.imageStorageService = imageStorageService;
        this.postMapper = postMapper;
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.contentModerationService = contentModerationService;
    }

    @Transactional
    public PostResponse createPost(CreatePostRequest request, String username) {

        // lay userId
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND"));

        UUID userId = user.getId();

        // Kiểm tra nội dung
        boolean hasContent = request.getContent() != null && !request.getContent().trim().isEmpty();
        boolean hasImage = request.getUrl_img() != null && !request.getUrl_img().isEmpty();

        if (!hasContent && !hasImage) {
            throw new BusinessException(400, "BAD_REQUEST_NO_CONTENT");
        }

        //Kiểm duyệt
        contentModerationService.moderateText(request.getContent());
        contentModerationService.moderateImage(request.getUrl_img());

        // Upload ảnh lên oracle
        String imageUrl = null;
        if (hasImage) {
            try {
                imageUrl = imageStorageService.uploadImage(request.getUrl_img(), "posts");
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
    @Override
    @Transactional
    public void deletePost(UUID postId, String username) {
        // 1. Lấy thông tin User từ Database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND"));

        // 2. Kiểm tra xem bài viết có tồn tại không
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(404, "POST_NOT_FOUND"));

        // 3. KIỂM TRA QUYỀN: Bắt buộc phải là chủ bài viết
        if (!post.getUserId().equals(user.getId())) {
            throw new BusinessException(403, "FORBIDDEN");
        }

        // 4. Xóa file ảnh trên Oracle (nếu có)
        if (post.getUrlImg() != null) {
            try {
                imageStorageService.deleteImageByUrl(post.getUrlImg());
            } catch (Exception e) {
                System.err.println("Lỗi khi xóa ảnh bài viết trên Cloud: " + e.getMessage());
            }
        }

        // 5. Xóa bài viết trong Database
        postRepository.delete(post);
    }

    // Hàm update
    @Transactional
    public PostResponse updatePost(UUID postId, CreatePostRequest request, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND"));

        UUID userId = user.getId();

        // Tìm bài viết xem có tồn tại không
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(404, "POST_NOT_FOUND"));

        // Kiểm tra quyền (Chỉ người tạo mới được phép sửa bài của mình)
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "FORBIDDEN");
        }

        // Cập nhật Nội dung & Quyền riêng tư (Nếu người dùng có gửi lên thì mới sửa)
        if (request.getContent() != null) {
            contentModerationService.moderateText(request.getContent()); //kiểm duyệt
            post.setContent(request.getContent().trim());
        }
        if (request.getPrivacy() != null) {
            post.setPrivacy(request.getPrivacy().trim().toLowerCase());
        }

        //XỬ LÝ ẢNH
        boolean hasNewImage = request.getUrl_img() != null && !request.getUrl_img().isEmpty();
        if (hasNewImage) {
            contentModerationService.moderateImage(request.getUrl_img()); // kiểm duyệt trước

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
                String newImageUrl = imageStorageService.uploadImage(request.getUrl_img(), "posts");
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
    @Override
    @Transactional
    public void deletePostImage(UUID postId, String username) {
        // 1. Lấy thông tin User từ Database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND"));

        // 2. Tìm bài viết trong Database
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(404, "POST_NOT_FOUND"));

        // 3. KIỂM TRA QUYỀN: Bắt buộc phải là chủ bài viết
        if (!post.getUserId().equals(user.getId())) {
            throw new BusinessException(403, "FORBIDDEN");
        }

        // 4. Nếu bài viết có ảnh thì tiến hành xóa
        if (post.getUrlImg() != null) {
            try {
                // Xóa ảnh vật lý trên Oracle Cloud
                imageStorageService.deleteImageByUrl(post.getUrlImg());
            } catch (Exception e) {
                // In ra log nếu Cloud báo lỗi, tiếp tục chạy để xóa link trong DB
                System.err.println("Lỗi khi xóa riêng ảnh trên Cloud: " + e.getMessage());
            }

            // Xóa link ảnh trong Entity và lưu lại xuống DB
            post.setUrlImg(null);
            postRepository.save(post);
        }
    }


    public Map<String, Object> getHomeFeed(String username, int page, int limit) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND"));

        UUID userId = user.getId();

        // Trừ 1 vì Page trong Spring Boot bắt đầu từ số 0
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Post> postPage = postRepository.getFeedPosts(userId, pageable);

        List<FeedPostDto> feedList = new ArrayList<>();
        String baseUrl = "https://objectstorage.ap-singapore-1.oraclecloud.com/n/axqv9e1of21u/b/minboo-storage/o/";

        for (Post post : postPage.getContent()) {
            FeedPostDto dto = new FeedPostDto();
            dto.setPostId(post.getPostId());
            dto.setContent(post.getContent());
            dto.setPrivacy(post.getPrivacy());
            dto.setCreatedAt(post.getCreatedAt());
            dto.setUpdatedAt(post.getUpdateAt());

            // Link ảnh bài viết
            if (post.getUrlImg() != null && !post.getUrlImg().isEmpty()) {
                dto.setUrlImg(post.getUrlImg().startsWith("http") ? post.getUrlImg() : baseUrl + post.getUrlImg());
            }

            // 1. Tác giả
            // (Đảm bảo bạn đã khai báo UserRepository trong class PostService)
            userRepository.findById(post.getUserId()).ifPresent(author -> {
                FeedPostDto.AuthorDto authorDto = new FeedPostDto.AuthorDto();
                authorDto.setUserId(author.getId());
                authorDto.setName(author.getName());
                authorDto.setUrlAvt(author.getAvatar());
                dto.setAuthor(authorDto);
            });

            // 2. Tags
            if (post.getPostTags() != null) {
                List<FeedPostDto.TagDto> tagDtos = post.getPostTags().stream().map(pt -> {
                    FeedPostDto.TagDto t = new FeedPostDto.TagDto();
                    t.setTagId(pt.getTag().getTagId());
                    t.setTagName(pt.getTag().getTagName());
                    return t;
                }).collect(Collectors.toList());
                dto.setTags(tagDtos);
            }

            // 3. Comment Count
            dto.setCommentsCount(postRepository.countCommentsByPostId(post.getPostId()));

            // 4. Reaction Count
            List<Object[]> reactionData = postRepository.countReactionsByPostId(post.getPostId());
            FeedPostDto.ReactionCountDto rCount = new FeedPostDto.ReactionCountDto();
            long totalReacts = 0;
            for (Object[] row : reactionData) {
                String type = (String) row[0];
                long count = ((Number) row[1]).longValue();
                totalReacts += count;
                switch (type.toLowerCase()) {
                    case "like": rCount.setLike(count); break;
                    case "love": rCount.setLove(count); break;
                    case "haha": rCount.setHaha(count); break;
                    case "sad": rCount.setSad(count); break;
                    case "angry": rCount.setAngry(count); break;
                }
            }
            rCount.setTotal(totalReacts);
            dto.setReactionsCount(rCount);

            // 5. My Reaction
            String myReact = postRepository.getMyReaction(post.getPostId(), userId);
            dto.setMyReaction(myReact);

            feedList.add(dto);
        }

        // Đóng gói JSON Pagination
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("total", postPage.getTotalElements());

        Map<String, Object> data = new HashMap<>();
        data.put("posts", feedList);
        data.put("pagination", pagination);

        return data;
    }

    // Lấy bài viết cho Trang cá nhân
    public Map<String, Object> getUserPosts(String viewername, UUID profileOwnerId, int page, int limit) {

        User user = userRepository.findByUsername(viewername)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND"));

        UUID viewerId = user.getId();

        Pageable pageable = PageRequest.of(page - 1, limit);

        Page<Post> postPage = postRepository.getUserProfilePosts(viewerId, profileOwnerId, pageable);

        List<FeedPostDto> postList = new ArrayList<>();

        String baseUrl = "https://objectstorage.ap-singapore-1.oraclecloud.com/n/axqv9e1of21u/b/minboo-storage/o/";

        for (Post post : postPage.getContent()) {
            FeedPostDto dto = new FeedPostDto();
            dto.setPostId(post.getPostId());
            dto.setContent(post.getContent());
            dto.setPrivacy(post.getPrivacy());
            dto.setCreatedAt(post.getCreatedAt());
            dto.setUpdatedAt(post.getUpdateAt());

            // Link ảnh bài viết
            if (post.getUrlImg() != null && !post.getUrlImg().isEmpty()) {
                dto.setUrlImg(post.getUrlImg().startsWith("http") ? post.getUrlImg() : baseUrl + post.getUrlImg());
            }

            // 1. Tác giả
            userRepository.findById(post.getUserId()).ifPresent(author -> {
                FeedPostDto.AuthorDto authorDto = new FeedPostDto.AuthorDto();
                authorDto.setUserId(author.getId());
                authorDto.setName(author.getName());
                authorDto.setUrlAvt(author.getAvatar());
                dto.setAuthor(authorDto);
            });

            // 2. Tags
            if (post.getPostTags() != null) {
                List<FeedPostDto.TagDto> tagDtos = post.getPostTags().stream().map(pt -> {
                    FeedPostDto.TagDto t = new FeedPostDto.TagDto();
                    t.setTagId(pt.getTag().getTagId());
                    t.setTagName(pt.getTag().getTagName());
                    return t;
                }).collect(Collectors.toList());
                dto.setTags(tagDtos);
            }

            // 3. Comment Count
            dto.setCommentsCount(postRepository.countCommentsByPostId(post.getPostId()));

            // 4. Reaction Count
            List<Object[]> reactionData = postRepository.countReactionsByPostId(post.getPostId());
            FeedPostDto.ReactionCountDto rCount = new FeedPostDto.ReactionCountDto();
            long totalReacts = 0;
            for (Object[] row : reactionData) {
                String type = (String) row[0];
                long count = ((Number) row[1]).longValue();
                totalReacts += count;
                switch (type.toLowerCase()) {
                    case "like": rCount.setLike(count); break;
                    case "love": rCount.setLove(count); break;
                    case "haha": rCount.setHaha(count); break;
                    case "sad": rCount.setSad(count); break;
                    case "angry": rCount.setAngry(count); break;
                }
            }
            rCount.setTotal(totalReacts);
            dto.setReactionsCount(rCount);

            String myReact = postRepository.getMyReaction(post.getPostId(), viewerId);
            dto.setMyReaction(myReact);

            postList.add(dto);
        }

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("total", postPage.getTotalElements());

        Map<String, Object> data = new HashMap<>();
        data.put("posts", postList);
        data.put("pagination", pagination);

        return data;
    }

    //Report
    @Transactional
    public void reportPost(UUID postId, String username, ReportRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND"));

        UUID userId = user.getId();

        // 1. Kiểm tra bài viết có tồn tại không
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(404, "POST_NOT_FOUND"));

        // 2. Kiểm tra tính hợp lệ của reason (Phòng trường hợp Hacker gửi sai định dạng)
        String reason = request.getReason();
        if (reason == null || (!reason.equals("inappropriate_content") && !reason.equals("spam") && !reason.equals("other"))) {
            throw new BusinessException(400, "INVALID_REPORT_REASON");
        }

        // 3. Tạo mới Report
        Report report = new Report();
        report.setPostId(post.getPostId()); // Hoặc report.setPost(post) tùy cách bạn khai báo Entity
        report.setReportedBy(userId);       // Hoặc report.setUser(user)
        report.setReason(reason);
        report.setDescription(request.getDescription());
        report.setStatus("pending");        // Mặc định trạng thái là chờ xử lý theo chuẩn DB

        // 4. Lưu xuống Database
        reportRepository.save(report);
    }

}
