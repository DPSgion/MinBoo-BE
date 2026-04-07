package com.phobo.comment.mapper;

import com.phobo.comment.dto.CommentDto;
import com.phobo.comment.entity.Comment;
import com.phobo.user.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    private final UserRepository userRepository;

    public CommentMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CommentDto toDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setCommentId(comment.getCommentId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());

        // tìm Author
        userRepository.findById(comment.getUserId()).ifPresent(user -> {
            CommentDto.AuthorDto authorDto = new CommentDto.AuthorDto();
            authorDto.setUserId(user.getId());
            authorDto.setName(user.getName());
            authorDto.setUrlAvt(user.getAvatar());
            dto.setAuthor(authorDto);
        });

        return dto;
    }
}