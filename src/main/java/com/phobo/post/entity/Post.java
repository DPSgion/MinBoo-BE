package com.phobo.post.entity;

import jakarta.persistence.*;
import jdk.jfr.DataAmount;
import org.w3c.dom.Text;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_id")
    private UUID postId;

    //Tạo tạm để test
    @Column(name = "user_id")
    private UUID userId;

    //giúp text lớn hơn 255 kí tự
    @Column(columnDefinition = "TEXT", name = "content")
    private String content;

    @Column(name = "url_img")
    private String urlImg;

    @Column(name = "privacy")
    private String privacy;

    //tránh truyền dữ liệu vô
    @Column(name = "create_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "delete_at", insertable = false, updatable = false)
    private LocalDateTime deleteAt;

    @Column(name = "update_at", insertable = false, updatable = false)
    private LocalDateTime updateAt;

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public LocalDateTime getDeleteAt() {
        return deleteAt;
    }

    public void setDeleteAt(LocalDateTime deleteAt) {
        this.deleteAt = deleteAt;
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

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }
}
