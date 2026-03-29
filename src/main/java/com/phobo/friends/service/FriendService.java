package com.phobo.friends.service;

import com.phobo.friends.dto.FriendRequest;
import com.phobo.friends.dto.FriendResponse;

import java.util.UUID;

public interface FriendService {

    public FriendResponse sendFriendRequest(FriendRequest friendRequest, UUID receiver);

}
