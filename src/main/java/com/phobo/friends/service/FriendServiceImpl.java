package com.phobo.friends.service;

import com.phobo.common.exception.BusinessException;
import com.phobo.common.exception.ResourceNotFoundException;
import com.phobo.friends.dto.FriendRequest;
import com.phobo.friends.dto.FriendResponse;
import com.phobo.friends.entity.FriendShip;
import com.phobo.friends.repository.FriendRepository;
import com.phobo.user.entity.User;
import com.phobo.user.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FriendServiceImpl implements FriendService{

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    public FriendServiceImpl(UserRepository userRepository, FriendRepository friendRepository) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
    }

    @Override
    public FriendResponse sendFriendRequest(FriendRequest friendRequest, UUID receiver) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User requesterUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Sender data not found. ", username));

        if (requesterUser.getId().equals(receiver)) {
            throw new BusinessException(400, "You cannot send a friend request to yourself.");
        }

        User receiverUser = userRepository.findById(receiver)
                .orElseThrow(() -> new ResourceNotFoundException("The receiver does not exist.", receiver));

        if (friendRepository.existsFriendshipBetween(requesterUser.getId(), receiver)) {
            throw new BusinessException(400, "Friend request sent or already friends.");
        }

        FriendShip newFriend = new FriendShip();
        newFriend.setRequester(requesterUser);
        newFriend.setReceiver(receiverUser);
        newFriend.setStatus(FriendShip.FriendStatus.PENDING);
        newFriend.setMessage(friendRequest.message());

        FriendShip savedFriend = friendRepository.save(newFriend);

        return new FriendResponse(
                savedFriend.getFriendRequestId(),
                savedFriend.getRequester().getId(),
                savedFriend.getReceiver().getId(),
                savedFriend.getMessage(),
                savedFriend.getStatus().name(),
                savedFriend.getCreatedAt()
        );
    }
}
