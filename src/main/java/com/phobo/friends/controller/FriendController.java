package com.phobo.friends.controller;

import com.phobo.friends.dto.FriendRequest;
import com.phobo.friends.dto.FriendResponse;
import com.phobo.friends.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<List<FriendResponse>> getFriends(){
        return ResponseEntity.ok(friendService.getFriends());
    }

    @GetMapping("/requests/send")
    public ResponseEntity<List<FriendResponse>> getSendFriendRequests(){
        return ResponseEntity.ok(friendService.getSendFriendRequests());
    }

    @PostMapping("/request/{id}")
    public ResponseEntity<FriendResponse> sendFriendRequest(
            @PathVariable("id") UUID receiverId,
            @RequestBody FriendRequest friendRequest
    ){
        return ResponseEntity.ok(friendService.sendFriendRequest(friendRequest, receiverId));
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<FriendResponse>> getPendingRequests(){
        return ResponseEntity.ok(friendService.getPendingRequests());
    }

    @PostMapping("/request/accept/{requestId}")
    public ResponseEntity<FriendResponse> acceptFriendRequest(@PathVariable("requestId") Integer requestId){
        return ResponseEntity.ok(friendService.acceptFriendRequest(requestId));
    }

    @PostMapping("/request/reject/{requestId}")
    public ResponseEntity<FriendResponse> rejectFriendRequest(@PathVariable("requestId") Integer requestId){
        return ResponseEntity.ok(friendService.rejectFriendRequest(requestId));
    }

    @DeleteMapping("/unfriend/{friendId}")
    public ResponseEntity<Map<String, String>> unfriend(@PathVariable("friendId") UUID friendId){
        String message = friendService.unfriendRequest(friendId);
        return ResponseEntity.ok(Map.of("message", message));
    }

}
