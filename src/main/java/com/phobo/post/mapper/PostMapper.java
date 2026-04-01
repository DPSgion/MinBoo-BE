package com.phobo.post.mapper;

import com.phobo.post.dto.PostResponse;
import com.phobo.post.entity.Post;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostMapper {

    private final String baseUrl = "https://objectstorage.ap-singapore-1.oraclecloud.com/n/axqv9e1of21u/b/minboo-storage/o/";

    public PostResponse toResponse(Post post) {
        if (post == null) return null;

        PostResponse response = new PostResponse();
        response.setPostId(post.getPostId());
        response.setContent(post.getContent());
        response.setPrivacy(post.getPrivacy());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdateAt(post.getUpdateAt());

        // Ghép link ảnh
        if (post.getUrlImg() != null && !post.getUrlImg().isEmpty()) {
            response.setUrlImg(post.getUrlImg());
        }

        // Bóc tách danh sách Tag
        if (post.getPostTags() != null) {
            List<String> tags = post.getPostTags().stream()
                    .map(postTag -> postTag.getTag().getTagName())
                    .collect(Collectors.toList());
            response.setTags(tags);
        }

        return response;
    }
}