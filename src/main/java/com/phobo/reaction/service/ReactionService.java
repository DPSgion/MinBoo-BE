package com.phobo.reaction.service;

import com.phobo.reaction.dto.ReactionRequest;
import com.phobo.reaction.dto.ReactionResponse;
import java.util.UUID;

public interface ReactionService {
    ReactionResponse toggleReaction(UUID postId, String username, ReactionRequest request);
}