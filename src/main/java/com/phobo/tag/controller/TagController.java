package com.phobo.tag.controller;

import com.phobo.tag.dto.TagRequest;
import com.phobo.tag.entity.Tag;
import com.phobo.tag.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {
    @Autowired
    private TagService tagService;

    @PostMapping
    Tag createTag(@RequestBody TagRequest request){
        return tagService.createTag(request);
    }

    @GetMapping
    List<Tag> getAll(){
        return tagService.getAll();
    }
}
