package com.lostark.backend.friend.service;

import com.lostark.backend.discord.DiscordBotClient;
import com.lostark.backend.friend.dto.*;
import com.lostark.backend.friend.entity.Friendship;
import com.lostark.backend.friend.entity.FriendshipStatus;
import com.lostark.backend.friend.repository.FriendshipRepository;
import com.lostark.backend.user.entity.AppUser;
import com.lostark.backend.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final AppUserRepository appUserRepository;
    private final DiscordBotClient discordBotClient;

    @Transactional
    public FriendRequestResponse createFriendRequest(Long requesterUserId, String addresseeDiscordUserId) {
        if (addresseeDiscordUserId == null || addresseeDiscordUserId.isBlank()) {
            throw new IllegalArgumentException("discordUserId가 필요합니다.");
        }

        AppUser requester = appUserRepository.findById(requesterUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (requester.getDiscordId() == null || requester.getDiscordId().isBlank()) {
            throw new IllegalArgumentException("Discord 로그인 사용자만 친구 요청을 보낼 수 있습니다.");
        }

        if (requester.getDiscordId().equals(addresseeDiscordUserId)) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        AppUser addressee = appUserRepository.findByDiscordId(addresseeDiscordUserId)
                .orElseGet(() -> {
                    AppUser stub = new AppUser();
                    stub.setDiscordId(addresseeDiscordUserId);
                    return appUserRepository.save(stub);
                });

        Optional<Friendship> existingActive = friendshipRepository.findLatestBetweenUsersWithStatuses(
                requester.getId(),
                addressee.getId(),
                List.of(FriendshipStatus.PENDING, FriendshipStatus.ACCEPTED)
        );

        if (existingActive.isPresent()) {
            Friendship f = existingActive.get();
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new IllegalArgumentException("이미 친구입니다.");
            }
            // 상대가 이미 나에게 요청을 보낸 상태면 자동 수락 처리
            if (f.getRequester().getId().equals(addressee.getId()) && f.getAddressee().getId().equals(requester.getId())) {
                return acceptRequestInternal(f, requester, true);
            }
            return toRequestResponse(f);
        }

        Friendship request = new Friendship();
        request.setRequester(requester);
        request.setAddressee(addressee);
        request.setStatus(FriendshipStatus.PENDING);
        Friendship saved = friendshipRepository.save(request);

        if (discordBotClient.isAvailable()) {
            try {
                String requesterName = displayName(requester);
                String messageId = discordBotClient.sendFriendRequestDm(addresseeDiscordUserId, saved.getId(), requesterName)
                        .join();
                saved.setDiscordDmMessageId(messageId);
                friendshipRepository.save(saved);
            } catch (Exception e) {
                log.warn("친구 요청 DM 발송 실패: requestId={}, addresseeDiscordId={}", saved.getId(), addresseeDiscordUserId, e);
            }
        }

        return toRequestResponse(saved);
    }

    @Transactional(readOnly = true)
    public FriendRequestsResponse getMyRequests(Long userId) {
        List<FriendRequestResponse> outgoing = friendshipRepository.findByRequesterIdAndStatusOrderByCreatedAtDesc(userId, FriendshipStatus.PENDING)
                .stream()
                .map(this::toRequestResponse)
                .collect(Collectors.toList());

        List<FriendRequestResponse> incoming = friendshipRepository.findByAddresseeIdAndStatusOrderByCreatedAtDesc(userId, FriendshipStatus.PENDING)
                .stream()
                .map(this::toRequestResponse)
                .collect(Collectors.toList());

        return FriendRequestsResponse.builder()
                .outgoing(outgoing)
                .incoming(incoming)
                .build();
    }

    @Transactional(readOnly = true)
    public List<FriendResponse> getMyFriends(Long userId) {
        List<Friendship> asRequester = friendshipRepository.findByRequesterIdAndStatusOrderByCreatedAtDesc(userId, FriendshipStatus.ACCEPTED);
        List<Friendship> asAddressee = friendshipRepository.findByAddresseeIdAndStatusOrderByCreatedAtDesc(userId, FriendshipStatus.ACCEPTED);

        return List.of(asRequester, asAddressee).stream()
                .flatMap(List::stream)
                .map(f -> toFriendResponse(f, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public FriendRequestResponse acceptRequest(Long userId, Long requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new IllegalArgumentException("받은 요청만 수락할 수 있습니다.");
        }

        return acceptRequestInternal(friendship, friendship.getAddressee(), false);
    }

    @Transactional
    public FriendRequestResponse declineRequest(Long userId, Long requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new IllegalArgumentException("받은 요청만 거절할 수 있습니다.");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            return toRequestResponse(friendship);
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
        friendship.setRespondedAt(LocalDateTime.now());
        Friendship saved = friendshipRepository.save(friendship);

        notifyRequester(saved, "DECLINED");
        return toRequestResponse(saved);
    }

    @Transactional
    public void cancelRequest(Long userId, Long requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        if (!friendship.getRequester().getId().equals(userId)) {
            throw new IllegalArgumentException("보낸 요청만 취소할 수 있습니다.");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            return;
        }

        friendship.setStatus(FriendshipStatus.CANCELLED);
        friendship.setRespondedAt(LocalDateTime.now());
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void removeFriend(Long userId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("친구 관계를 찾을 수 없습니다."));

        boolean meIsRequester = friendship.getRequester().getId().equals(userId);
        boolean meIsAddressee = friendship.getAddressee().getId().equals(userId);
        if (!meIsRequester && !meIsAddressee) {
            throw new IllegalArgumentException("본인의 친구만 삭제할 수 있습니다.");
        }

        friendshipRepository.delete(friendship);
    }

    @Transactional
    public FriendRequestResponse respondToRequestFromDiscord(Long requestId, String discordUserId, boolean accept) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("친구 요청을 찾을 수 없습니다."));

        String addresseeDiscordId = friendship.getAddressee().getDiscordId();
        if (addresseeDiscordId == null || !addresseeDiscordId.equals(discordUserId)) {
            throw new IllegalArgumentException("이 요청을 처리할 권한이 없습니다.");
        }

        if (accept) {
            return acceptRequestInternal(friendship, friendship.getAddressee(), false);
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            return toRequestResponse(friendship);
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
        friendship.setRespondedAt(LocalDateTime.now());
        Friendship saved = friendshipRepository.save(friendship);
        notifyRequester(saved, "DECLINED");
        return toRequestResponse(saved);
    }

    private FriendRequestResponse acceptRequestInternal(Friendship friendship, AppUser addressee, boolean autoAccepted) {
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            return toRequestResponse(friendship);
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setRespondedAt(LocalDateTime.now());
        Friendship saved = friendshipRepository.save(friendship);

        notifyRequester(saved, autoAccepted ? "ACCEPTED_AUTO" : "ACCEPTED");
        return toRequestResponse(saved);
    }

    private void notifyRequester(Friendship saved, String status) {
        if (!discordBotClient.isAvailable()) return;
        AppUser requester = saved.getRequester();
        if (requester.getDiscordId() == null || requester.getDiscordId().isBlank()) return;
        String addresseeName = displayName(saved.getAddressee());
        try {
            discordBotClient.sendFriendResponseNotification(requester.getDiscordId(), addresseeName, status).join();
        } catch (Exception e) {
            log.warn("친구 요청 응답 알림 실패: requestId={}", saved.getId(), e);
        }
    }

    private FriendRequestResponse toRequestResponse(Friendship friendship) {
        return FriendRequestResponse.builder()
                .requestId(friendship.getId())
                .requester(toUserResponse(friendship.getRequester()))
                .addressee(toUserResponse(friendship.getAddressee()))
                .status(friendship.getStatus())
                .createdAt(friendship.getCreatedAt())
                .respondedAt(friendship.getRespondedAt())
                .build();
    }

    private FriendResponse toFriendResponse(Friendship friendship, Long meUserId) {
        AppUser other = friendship.getRequester().getId().equals(meUserId)
                ? friendship.getAddressee()
                : friendship.getRequester();
        return FriendResponse.builder()
                .friendshipId(friendship.getId())
                .friend(toUserResponse(other))
                .build();
    }

    private FriendUserResponse toUserResponse(AppUser user) {
        return FriendUserResponse.builder()
                .userId(user.getId())
                .discordUserId(user.getDiscordId())
                .discordUsername(user.getDiscordUsername())
                .build();
    }

    private String displayName(AppUser user) {
        if (user.getDiscordUsername() != null && !user.getDiscordUsername().isBlank()) return user.getDiscordUsername();
        if (user.getKakaoNickname() != null && !user.getKakaoNickname().isBlank()) return user.getKakaoNickname();
        if (user.getDiscordId() != null && !user.getDiscordId().isBlank()) return "User#" + user.getDiscordId();
        return "User#" + user.getId();
    }
}
