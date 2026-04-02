package com.phobo.tag.service;

import com.phobo.tag.dto.TagRequest;
import com.phobo.tag.dto.TagResponse;

import java.util.List;

public interface TagService {
    public TagResponse createTag(TagRequest tagRequest);

    public List<TagResponse> getAll();

    public TagResponse getByIdTag(Integer tagId);

    public TagResponse updateTag(Integer tagId, TagRequest tagRequest);

    public void deleteTag(Integer tagId);
}
