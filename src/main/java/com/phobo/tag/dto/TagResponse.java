package com.phobo.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import java.time.ZonedDateTime;

@Builder
public class TagResponse {

    @JsonProperty("tag_id")
    private Integer tagId;

    @JsonProperty("tag_name")
    private String tagName;

    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    public Integer getTagId() { return tagId; }
    public void setTagId(Integer tagId) { this.tagId = tagId; }

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}