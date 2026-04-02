package com.phobo.tag.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagRequest {
    @NotBlank(message = "Tên tag không được để trống")
    @JsonProperty("tag_name")
    private String tagName;
}
