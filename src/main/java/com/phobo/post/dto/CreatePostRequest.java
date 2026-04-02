package com.phobo.post.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class CreatePostRequest {
    private String content;
    private String privacy;


    private MultipartFile url_img;
    private List<Integer> tag_ids;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public MultipartFile getUrl_img() {
        return url_img;
    }

    public void setUrl_img(MultipartFile url_img) {
        this.url_img = url_img;
    }

    public List<Integer> getTag_ids() {
        return tag_ids;
    }

    public void setTag_ids(List<Integer> tag_ids) {
        this.tag_ids = tag_ids;
    }
}
