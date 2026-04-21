package com.phobo.friends.service;

import com.phobo.common.exception.BusinessException;
import com.phobo.common.exception.ResourceNotFoundException;
import com.phobo.friends.dto.FriendRequest;
import com.phobo.friends.dto.FriendResponse;
import com.phobo.friends.entity.Friend;
import com.phobo.friends.entity.FriendRequestEntity;
import com.phobo.friends.repository.FriendRepository;
import com.phobo.friends.repository.FriendRequestRepository;
import com.phobo.notification.entity.NotificationType;
import com.phobo.notification.service.NotificationService;
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
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final NotificationService notificationService;

    public FriendServiceImpl(UserRepository userRepository, FriendRepository friendRepository, FriendRequestRepository friendRequestRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<FriendResponse> getFriends() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: ", username));

        List<Friend> friends = friendRepository.getFriendList(currentUser);

        return friends.stream()
                .map(f -> new FriendResponse(
                        f.getFriendId(),
                        f.getUser1().getId(),
                        f.getUser1().getName(),
                        f.getUser2().getId(),
                        f.getUser2().getName(),
                        "Đã là bạn bè", // Không có message ở bảng Friends
                        "ACCEPTED",
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

        List<FriendRequestEntity> sendRequests = friendRequestRepository.findAllByRequesterAndStatusOrderByCreatedAtDesc(
                currentUser, FriendRequestEntity.RequestStatus.PENDING);

        return sendRequests.stream()
                .map(this::mapToResponse)
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

        // 1. Kiểm tra bảng Friends (Đã là bạn chưa?)
        if (friendRepository.existsFriendshipBetween(requesterUser.getId(), receiver)) {
            throw new BusinessException(400, "Both of you were friends !");
        }

        // 2. Kiểm tra bảng FriendRequest (Có lời mời nào đang kẹt không?)
        Optional<FriendRequestEntity> existingRequest = friendRequestRepository.findRequestBetween(requesterUser.getId(), receiver);

        if (existingRequest.isPresent()) {
            FriendRequestEntity requestEntity = existingRequest.get();

            if (requestEntity.getStatus() == FriendRequestEntity.RequestStatus.PENDING) {
                throw new BusinessException(400, "Request is pending !");
            }

            if (requestEntity.getStatus() == FriendRequestEntity.RequestStatus.REJECTED) {
                // Kiểm tra Cooldown 3 ngày
                LocalDateTime lastInteraction = requestEntity.getUpdatedAt() != null ? requestEntity.getUpdatedAt() : requestEntity.getCreatedAt();
                long daysSinceRejected = java.time.temporal.ChronoUnit.DAYS.between(lastInteraction, LocalDateTime.now());

                if (daysSinceRejected < 3) {
                    long daysLeft = 3 - daysSinceRejected;
                    throw new BusinessException(400, "You have been rejected, or you recently rejected this request ! Please try again after " + daysLeft + " more days !");
                }

                // Tái chế (Update dòng cũ)
                requestEntity.setRequester(requesterUser);
                requestEntity.setReceiver(receiverUser);
                requestEntity.setMessage(friendRequest.message());
                requestEntity.setStatus(FriendRequestEntity.RequestStatus.PENDING);

                FriendRequestEntity savedRequest = friendRequestRepository.save(requestEntity);

                // [THÊM LOGIC THÔNG BÁO] - Trường hợp tái chế request cũ
                notificationService.createNotification(
                        receiverUser,
                        requesterUser,
                        NotificationType.friend_request,
                        requesterUser.getName() + " đã gửi cho bạn một lời mời kết bạn",
                        requesterUser.getId().toString(), // Target ID là ID của người gửi (để FE bấm vào thì bay ra Profile)
                        "user" // Cột entity_type chuẩn theo DB của bạn
                );

                return mapToResponse(savedRequest);
            }
        }

        // 3. Nếu qua hết các vòng kiểm tra -> Tạo Request mới
        FriendRequestEntity newRequest = new FriendRequestEntity();
        newRequest.setRequester(requesterUser);
        newRequest.setReceiver(receiverUser);
        newRequest.setStatus(FriendRequestEntity.RequestStatus.PENDING);
        newRequest.setMessage(friendRequest.message());

        FriendRequestEntity savedRequest = friendRequestRepository.save(newRequest);

        // [THÊM LOGIC THÔNG BÁO] - Trường hợp request mới hoàn toàn
        notificationService.createNotification(
                receiverUser,
                requesterUser,
                NotificationType.friend_request,
                requesterUser.getName() + " đã gửi cho bạn một lời mời kết bạn",
                requesterUser.getId().toString(),
                "user"
        );

        return mapToResponse(savedRequest);
    }

    @Override
    public List<FriendResponse> getPendingRequests() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: ", username));

        List<FriendRequestEntity> requests = friendRequestRepository.findAllByReceiverAndStatusOrderByCreatedAtDesc(
                currentUser, FriendRequestEntity.RequestStatus.PENDING);

        return requests.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @Override
    public FriendResponse acceptFriendRequest(Integer requestId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found ", username));

        // 1. Lấy lời mời từ bảng Request
        FriendRequestEntity pendingRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: ", requestId));

        if (!currentUser.getUsername().equals(pendingRequest.getReceiver().getUsername())) {
            throw new BusinessException(403, "You are not the receiver ! You don't have permission to accept or reject this invitation.");
        }

        if (pendingRequest.getStatus() != FriendRequestEntity.RequestStatus.PENDING) {
            throw new BusinessException(400, "This invitation has already been processed or is no longer valid.");
        }

        // 2. Xóa khỏi bảng Request
        friendRequestRepository.delete(pendingRequest);

        // 3. Chèn vào bảng Friends (Ép sang String để so sánh chuẩn xác với Database)
        Friend friend = new Friend();
        String requesterIdStr = pendingRequest.getRequester().getId().toString();
        String receiverIdStr = pendingRequest.getReceiver().getId().toString();

        // Ép sang toString() để so sánh theo bảng chữ cái
        if (requesterIdStr.compareTo(receiverIdStr) < 0) {
            friend.setUser1(pendingRequest.getRequester());
            friend.setUser2(pendingRequest.getReceiver());
        } else {
            friend.setUser1(pendingRequest.getReceiver());
            friend.setUser2(pendingRequest.getRequester());
        }

        Friend savedFriend = friendRepository.save(friend);

        // [THÊM LOGIC THÔNG BÁO] - Báo cho người gửi là mình đã đồng ý
        notificationService.createNotification(
                pendingRequest.getRequester(), // Người nhận thông báo là người ngày xưa gửi lời mời
                currentUser,                   // Người kích hoạt là mình
                NotificationType.friend_accepted,
                currentUser.getName() + " đã chấp nhận lời mời kết bạn của bạn",
                currentUser.getId().toString(),
                "user"
        );

        // 4. Trả về DTO như cũ để Frontend không báo lỗi
        return new FriendResponse(
                pendingRequest.getFriendRequestId(),
                pendingRequest.getRequester().getId(),
                pendingRequest.getRequester().getName(),
                pendingRequest.getReceiver().getId(),
                pendingRequest.getReceiver().getName(),
                pendingRequest.getMessage(),
                "ACCEPTED",
                savedFriend.getCreatedAt(),
                savedFriend.getUpdatedAt() != null ? savedFriend.getUpdatedAt() : savedFriend.getCreatedAt()
        );
    }

    @Transactional
    @Override
    public FriendResponse rejectFriendRequest(Integer requestId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found ", username));

        FriendRequestEntity pendingRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: ", requestId));

        if (!currentUser.getUsername().equals(pendingRequest.getReceiver().getUsername())) {
            throw new BusinessException(403, "You are not the receiver ! You don't have permission to accept or reject this invitation.");
        }

        if (pendingRequest.getStatus() != FriendRequestEntity.RequestStatus.PENDING) {
            throw new BusinessException(400, "This invitation has already been processed or is no longer valid.");
        }

        // Cập nhật trạng thái thành REJECTED thay vì xóa để giữ thời gian Cooldown
        pendingRequest.setStatus(FriendRequestEntity.RequestStatus.REJECTED);
        FriendRequestEntity savedRequest = friendRequestRepository.save(pendingRequest);

        return mapToResponse(savedRequest);
    }

    @Transactional
    @Override
    public String unfriendRequest(UUID friendId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: ", username));

        // Rất nhàn: Chỉ cần dò trong bảng Friends, không cần check Enum nữa
        Friend friendShip = friendRepository.findFriendshipBetween(currentUser.getId(), friendId)
                .orElseThrow(() -> new BusinessException(400, "Relationship not found"));

        String friendName = friendShip.getUser1().getId().equals(friendId)
                ? friendShip.getUser1().getName()
                : friendShip.getUser2().getName();

        friendRepository.delete(friendShip);

        return "Unfriend with " + friendName + " successfully !";
    }

    // --- Hàm tiện ích gom logic map ra DTO để code Controller sạch đẹp ---
    private FriendResponse mapToResponse(FriendRequestEntity f) {
        return new FriendResponse(
                f.getFriendRequestId(),
                f.getRequester().getId(),
                f.getRequester().getName(),
                f.getReceiver().getId(),
                f.getReceiver().getName(),
                f.getMessage(),
                f.getStatus().name(),
                f.getCreatedAt(),
                f.getUpdatedAt() != null ? f.getUpdatedAt() : (f.getCreatedAt() != null ? f.getCreatedAt() : LocalDateTime.now())
        );
    }
}