package com.phobo.tag.service;

import com.phobo.tag.dto.TagRequest;
import com.phobo.tag.entity.Tag;

import java.util.List;

public interface TagService {
    public Tag createTag(TagRequest tagRequest);
    public List<Tag> getAll();
}
