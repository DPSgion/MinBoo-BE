package com.phobo.friends.service;

import com.phobo.common.exception.BusinessException;
import com.phobo.common.exception.ResourceNotFoundException;
import com.phobo.friends.dto.FriendRequest;
import com.phobo.friends.dto.FriendResponse;
import com.phobo.friends.entity.FriendShip;
import com.phobo.friends.repository.FriendRepository;
import com.phobo.user.entity.User;
import com.phobo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    public List<FriendResponse> getFriends() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: ", username));

        List<FriendShip> friendShips = friendRepository.getFriendList(currentUser);

        return friendShips.stream()
                .map(f -> new FriendResponse(
                        f.getFriendRequestId(),
                        f.getRequester().getId(),
                        f.getRequester().getName(),
                        f.getReceiver().getId(),
                        f.getReceiver().getName(),
                        f.getMessage(),
                        f.getStatus().name(),
                        f.getCreatedAt(),
                        f.getUpdatedAt()
                ))
                .toList();

    }

    @Override
    public List<FriendResponse> getSendFriendRequests() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", username));

        List<FriendShip> sendRequests = friendRepository.findAllByRequesterAndStatusOrderByCreatedAtDesc(
                currentUser, FriendShip.FriendStatus.PENDING);

        return sendRequests.stream()
                .map(f -> new FriendResponse(
                        f.getFriendRequestId(),
                        f.getRequester().getId(),
                        f.getRequester().getName(),
                        f.getReceiver().getId(),
                        f.getReceiver().getName(),
                        f.getMessage(),
                        f.getStatus().name(),
                        f.getCreatedAt(),
                        f.getUpdatedAt()
                ))
                .toList();
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


        Optional<FriendShip> existingFriendship = friendRepository.findFriendshipBetween(requesterUser.getId(), receiver);

        if (existingFriendship.isPresent()) {
            FriendShip friendship = existingFriendship.get();

            // Scenario 1: If both were friends
            if (friendship.getStatus() == FriendShip.FriendStatus.ACCEPTED) {
                throw new BusinessException(400, "Both of you were friends !");
            }

            // Scenario 2: Pending
            if (friendship.getStatus() == FriendShip.FriendStatus.PENDING) {
                throw new BusinessException(400, "Request is pending !");
            }

            // Scenario 3: Request Again after rejected or was rejected
            if (friendship.getStatus() == FriendShip.FriendStatus.REJECTED) {

                // 1. Check Cooldown (3 days)
                // Get lastest update (when rejected), if not, get createdAt
                LocalDateTime lastInteraction = friendship.getUpdatedAt() != null ? friendship.getUpdatedAt() : friendship.getCreatedAt();
                long daysSinceRejected = java.time.temporal.ChronoUnit.DAYS.between(lastInteraction, LocalDateTime.now());

                if (daysSinceRejected < 3) {
                    long daysLeft = 3 - daysSinceRejected;
                    throw new BusinessException(400, "You have been rejected, or you recently rejected this request ! Please try again after " + daysLeft + " more days !");
                }

                // 2. If pass through 3 days -> Allow request again
                friendship.setRequester(requesterUser);
                friendship.setReceiver(receiverUser);
                friendship.setMessage(friendRequest.message());
                friendship.setStatus(FriendShip.FriendStatus.PENDING);

                friendRepository.save(friendship);

                return new FriendResponse(
                        friendship.getFriendRequestId(),
                        friendship.getRequester().getId(),
                        friendship.getRequester().getName(),
                        friendship.getReceiver().getId(),
                        friendship.getReceiver().getName(),
                        friendship.getMessage(),
                        friendship.getStatus().name(),
                        friendship.getCreatedAt(),
                        friendship.getUpdatedAt()
                );
            }
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
                savedFriend.getRequester().getName(),
                savedFriend.getReceiver().getId(),
                savedFriend.getReceiver().getName(),
                savedFriend.getMessage(),
                savedFriend.getStatus().name(),
                savedFriend.getCreatedAt(),
                savedFriend.getCreatedAt() != null ? savedFriend.getCreatedAt() : LocalDateTime.now()
        );
    }

    @Override
    public List<FriendResponse> getPendingRequests() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: ", username));

        List<FriendShip> requests = friendRepository.findAllByReceiverAndStatusOrderByCreatedAtDesc(currentUser, FriendShip.FriendStatus.PENDING);

        return requests.stream()
                .map(f -> new FriendResponse(
                        f.getFriendRequestId(),
                        f.getRequester().getId(),
                        f.getRequester().getName(),
                        f.getReceiver().getId(),
                        f.getReceiver().getName(),
                        f.getMessage(),
                        f.getStatus().name(),
                        f.getCreatedAt(),
                        f.getUpdatedAt()
                ))
                .toList();
    }

    @Transactional
    @Override
    public FriendResponse acceptFriendRequest(Integer requestId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found ", username));

        FriendShip pendingFriendship = friendRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: ", requestId));

        if (!currentUser.getUsername().equals(pendingFriendship.getReceiver().getUsername())){
            throw new BusinessException(403, "You are not the receiver ! You don't have permission to accept or reject this invitation.");
        }

        if (pendingFriendship.getStatus() != FriendShip.FriendStatus.PENDING) {
            throw new BusinessException(400, "This invitation has already been processed or is no longer valid.");
        }

        pendingFriendship.setStatus(FriendShip.FriendStatus.ACCEPTED);

        friendRepository.save(pendingFriendship);

        return new FriendResponse(
                pendingFriendship.getFriendRequestId(),
                pendingFriendship.getRequester().getId(),
                pendingFriendship.getRequester().getName(),
                pendingFriendship.getReceiver().getId(),
                pendingFriendship.getReceiver().getName(),
                pendingFriendship.getMessage(),
                pendingFriendship.getStatus().name(),
                pendingFriendship.getCreatedAt(),
                pendingFriendship.getUpdatedAt()
        );
    }

    @Transactional
    @Override
    public FriendResponse rejectFriendRequest(Integer requestId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found ", username));

        FriendShip pendingFriendship = friendRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: ", requestId));

        if (!currentUser.getUsername().equals(pendingFriendship.getReceiver().getUsername())){
            throw new BusinessException(403, "You are not the receiver ! You don't have permission to accept or reject this invitation.");
        }

        if (pendingFriendship.getStatus() != FriendShip.FriendStatus.PENDING) {
            throw new BusinessException(400, "This invitation has already been processed or is no longer valid.");
        }

        pendingFriendship.setStatus(FriendShip.FriendStatus.REJECTED);

        friendRepository.save(pendingFriendship);

        return new FriendResponse(
                pendingFriendship.getFriendRequestId(),
                pendingFriendship.getRequester().getId(),
                pendingFriendship.getRequester().getName(),
                pendingFriendship.getReceiver().getId(),
                pendingFriendship.getReceiver().getName(),
                pendingFriendship.getMessage(),
                pendingFriendship.getStatus().name(),
                pendingFriendship.getCreatedAt(),
                pendingFriendship.getUpdatedAt()
        );
    }

    @Transactional
    @Override
    public String unfriendRequest(UUID friendId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: ", username));

        FriendShip friendShip = friendRepository.findFriendshipBetween(currentUser.getId(), friendId)
                .orElseThrow(() -> new BusinessException(400, "Relationship not found"));

        if (friendShip.getStatus() != FriendShip.FriendStatus.ACCEPTED){
            throw new BusinessException(403, "Both of you aren't friends so you can't unfriend");
        }

        String friendName = friendShip.getRequester().getId().equals(friendId)
                ? friendShip.getRequester().getName()
                : friendShip.getReceiver().getName();

        friendRepository.delete(friendShip);

        return "Unfriend with " + friendName + " successfully !";
    }


}
