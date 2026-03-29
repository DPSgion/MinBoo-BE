package com.phobo.friends.controller;

import com.phobo.friends.dto.FriendRequest;
import com.phobo.friends.dto.FriendResponse;
import com.phobo.friends.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PostMapping("/request/{id}")
    public ResponseEntity<FriendResponse> sendFriendRequest(
            @PathVariable("id") UUID receiverId,
            @RequestBody FriendRequest friendRequest
    ){
        return ResponseEntity.ok(friendService.sendFriendRequest(friendRequest, receiverId));
    }

}
