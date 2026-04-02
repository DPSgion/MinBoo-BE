package com.phobo.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class FeedPostDto {
    @JsonProperty("post_id")
    private UUID postId;
    private String content;
    @JsonProperty("url_img")
    private String urlImg;
    private String privacy;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    private AuthorDto author;
    private List<TagDto> tags;
    @JsonProperty("reactions_count")
    private ReactionCountDto reactionsCount;
    @JsonProperty("my_reaction")
    private String myReaction;
    @JsonProperty("comments_count")
    private long commentsCount;

    // Getters và Setters
    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrlImg() {
        return urlImg;
    }

    public void setUrlImg(String urlImg) {
        this.urlImg = urlImg;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public AuthorDto getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDto author) {
        this.author = author;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }

    public ReactionCountDto getReactionsCount() {
        return reactionsCount;
    }

    public void setReactionsCount(ReactionCountDto reactionsCount) {
        this.reactionsCount = reactionsCount;
    }

    public String getMyReaction() {
        return myReaction;
    }

    public void setMyReaction(String myReaction) {
        this.myReaction = myReaction;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }

    // --- CÁC CLASS CON BÊN TRONG ---
    public static class AuthorDto {
        @JsonProperty("user_id")
        private UUID userId;
        private String name;
        @JsonProperty("url_avt")
        private String urlAvt;
        // Getters & Setters

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrlAvt() {
            return urlAvt;
        }

        public void setUrlAvt(String urlAvt) {
            this.urlAvt = urlAvt;
        }
    }

    public static class TagDto {
        @JsonProperty("tag_id")
        private Integer tagId;
        @JsonProperty("tag_name")
        private String tagName;

        public Integer getTagId() {
            return tagId;
        }

        public void setTagId(Integer tagId) {
            this.tagId = tagId;
        }

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }
    }

    public static class ReactionCountDto {
        private long total = 0;
        private long like = 0;
        private long love = 0;
        private long haha = 0;
        private long sad = 0;
        private long angry = 0;
        // Getters & Setters

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getLike() {
            return like;
        }

        public void setLike(long like) {
            this.like = like;
        }

        public long getLove() {
            return love;
        }

        public void setLove(long love) {
            this.love = love;
        }

        public long getHaha() {
            return haha;
        }

        public void setHaha(long haha) {
            this.haha = haha;
        }

        public long getSad() {
            return sad;
        }

        public void setSad(long sad) {
            this.sad = sad;
        }

        public long getAngry() {
            return angry;
        }

        public void setAngry(long angry) {
            this.angry = angry;
        }
    }
}