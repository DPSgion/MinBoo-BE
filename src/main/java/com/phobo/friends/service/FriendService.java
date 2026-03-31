package com.phobo.friends.service;

import com.phobo.friends.dto.FriendRequest;
import com.phobo.friends.dto.FriendResponse;

import java.util.List;
import java.util.UUID;

public interface FriendService {

    public List<FriendResponse> getFriends();

    public List<FriendResponse> getSendFriendRequests();

    public FriendResponse sendFriendRequest(FriendRequest friendRequest, UUID receiver);

    public List<FriendResponse> getPendingRequests();

    public FriendResponse acceptFriendRequest(Integer requestId);

    public FriendResponse rejectFriendRequest(Integer requestId);

    public String unfriendRequest(UUID friendId);
}
