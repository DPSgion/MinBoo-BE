package com.phobo.tag.service;

import com.phobo.tag.dto.TagRequest;
import com.phobo.tag.entity.Tag;
import com.phobo.tag.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService{
    @Autowired
    private TagRepository tagRepository;

    //tao tag moi
    public Tag createTag(TagRequest tagRequest) {
        Tag tag = new Tag();

        tag.setTagName(tagRequest.getTagName());

        return tagRepository.save(tag);
    }

    //lay tat ca tag
    public List<Tag> getAll(){
        return tagRepository.findAll();
    }
}
