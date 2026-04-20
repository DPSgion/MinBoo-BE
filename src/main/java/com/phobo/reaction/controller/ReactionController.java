package com.phobo.reaction.controller;

import com.phobo.reaction.dto.ReactionRequest;
import com.phobo.reaction.dto.ReactionResponse;
import com.phobo.reaction.service.ReactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/posts/{post_id}/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ReactionResponse toggleReaction(
            @PathVariable("post_id") UUID postId,
            @Valid @RequestBody ReactionRequest request) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return reactionService.toggleReaction(postId, username, request);
    }
}