package com.phobo.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class TagRequest {
    @NotBlank(message = "Tên tag không được để trống")
    @JsonProperty("tag_name")
    private String tagName;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
