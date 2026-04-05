package com.phobo.tag.service;

import com.phobo.common.exception.BusinessException;
import com.phobo.tag.dto.TagRequest;
import com.phobo.tag.dto.TagResponse;
import com.phobo.tag.entity.Tag;
import com.phobo.tag.repository.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService{
    @Autowired
    private TagRepository tagRepository;

    //1 - create
    public TagResponse createTag(TagRequest tagRequest) {
        Tag tag = new Tag();
        tag.setTagName(tagRequest.getTagName());

        Tag tagFinal = tagRepository.save(tag);

        return TagResponse.builder()
                .tagId(tagFinal.getTagId())
                .tagName(tagFinal.getTagName())
                .createdAt(tag.getCreatedAt())
                .build();
    }

    //2 - getAll ko phan trang
    public List<TagResponse> getAll(){
        List<Tag> tags = tagRepository.findAll();

        return tags.stream()
                .map(tag -> new TagResponse(
                        tag.getTagId(),
                        tag.getTagName(),
                        tag.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    //3 - getById
    public TagResponse getByIdTag(Integer tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy tag với ID này !"));

        return TagResponse.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName())
                .createdAt(tag.getCreatedAt())
                .build();
    }

    //4 - update
    @Transactional
    public TagResponse updateTag(Integer tagId, TagRequest tagRequest) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy tag với ID này để Update!"));

        String newTagName = tagRequest.getTagName();

        if (!tag.getTagName().equals(newTagName) && tagRepository.existsByTagName((newTagName))) {
            throw new BusinessException(HttpStatus.CONFLICT.value(), "Tên tag '" + newTagName + "' đã tồn tại. Vui lòng chọn tên khác !");
        }

        tag.setTagName(tagRequest.getTagName());

        return TagResponse.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName())
                .createdAt(tag.getCreatedAt())
                .build();
    }

    //5 - delete
    @Transactional
    public void deleteTag(Integer tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy tag với ID này để Delete!"));

        tagRepository.delete(tag);
    }
}
