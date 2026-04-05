package com.phobo.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {

    @JsonProperty("tag_id")
    private Integer tagId;

    @JsonProperty("tag_name")
    private String tagName;

    @JsonProperty("created_at")
    private ZonedDateTime createdAt;
}