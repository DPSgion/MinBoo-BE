package com.phobo.tag.controller;

import com.phobo.tag.dto.TagRequest;
import com.phobo.tag.dto.TagResponse;
import com.phobo.tag.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {
    @Autowired
    private TagService tagService;

    @PostMapping
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagRequest request){
        TagResponse tagResponse = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tagResponse);
    }

    @GetMapping
    public ResponseEntity<List<TagResponse>> getAll(){
        List<TagResponse> tags = tagService.getAll();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{tagId}")
    public ResponseEntity<TagResponse> getByTagId(@PathVariable Integer tagId){
        TagResponse tag = tagService.getByIdTag(tagId);
        return ResponseEntity.ok(tag);
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable Integer tagId,
            @Valid @RequestBody TagRequest tagRequest){

        TagResponse tagResponse = tagService.updateTag(tagId, tagRequest);
        return ResponseEntity.ok(tagResponse);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Integer tagId){
        tagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }
}
