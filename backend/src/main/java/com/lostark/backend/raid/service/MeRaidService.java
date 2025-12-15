package com.lostark.backend.raid.service;

import com.lostark.backend.discord.DiscordBotClient;
import com.lostark.backend.friend.repository.FriendshipRepository;
import com.lostark.backend.raid.dto.RaidScheduleCreateRequest;
import com.lostark.backend.raid.dto.RaidScheduleResponse;
import com.lostark.backend.raid.entity.ParticipantStatus;
import com.lostark.backend.raid.entity.RaidParticipant;
import com.lostark.backend.raid.entity.RaidSchedule;
import com.lostark.backend.raid.repository.RaidParticipantRepository;
import com.lostark.backend.raid.repository.RaidScheduleRepository;
import com.lostark.backend.user.entity.AppUser;
import com.lostark.backend.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeRaidService {

    private final RaidScheduleService raidScheduleService;
    private final RaidScheduleRepository raidScheduleRepository;
    private final RaidParticipantRepository raidParticipantRepository;
    private final AppUserRepository appUserRepository;
    private final FriendshipRepository friendshipRepository;
    private final DiscordBotClient discordBotClient;

    @Transactional
    public RaidScheduleResponse createRaid(Long creatorId, RaidScheduleCreateRequest request) {
        if (request.getInvitedUserIds() != null && !request.getInvitedUserIds().isEmpty()) {
            validateFriendAccess(creatorId, request.getInvitedUserIds());
        }
        return raidScheduleService.createRaidSchedule(creatorId, request);
    }

    @Transactional(readOnly = true)
    public List<RaidScheduleResponse> getMyRaids(Long userId) {
        return raidScheduleService.getMyRaids(userId);
    }

    @Transactional(readOnly = true)
    public RaidScheduleResponse getMyRaid(Long userId, Long raidId) {
        RaidSchedule schedule = raidScheduleRepository.findById(raidId)
                .orElseThrow(() -> new IllegalArgumentException("레이드 일정을 찾을 수 없습니다."));
        boolean isCreator = schedule.getCreator() != null && Objects.equals(schedule.getCreator().getId(), userId);
        boolean isParticipant = raidParticipantRepository.existsByRaidScheduleIdAndUserId(raidId, userId);
        if (!isCreator && !isParticipant) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        return raidScheduleService.getRaidSchedule(raidId);
    }

    @Transactional
    public RaidScheduleResponse addMembers(Long creatorId, Long raidId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return raidScheduleService.getRaidSchedule(raidId);
        }

        RaidSchedule schedule = raidScheduleRepository.findById(raidId)
                .orElseThrow(() -> new IllegalArgumentException("레이드 일정을 찾을 수 없습니다."));

        if (schedule.getCreator() == null || !Objects.equals(schedule.getCreator().getId(), creatorId)) {
            throw new IllegalArgumentException("본인이 생성한 레이드만 멤버를 추가할 수 있습니다.");
        }

        validateFriendAccess(creatorId, userIds);

        for (Long userId : userIds) {
            if (userId == null) continue;
            if (raidParticipantRepository.existsByRaidScheduleIdAndUserId(raidId, userId)) continue;

            AppUser user = appUserRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            RaidParticipant participant = new RaidParticipant();
            participant.setRaidSchedule(schedule);
            participant.setUser(user);
            participant.setCharacterName(user.getMainCharacterName());
            participant.setStatus(Objects.equals(user.getId(), creatorId) ? ParticipantStatus.ACCEPTED : ParticipantStatus.PENDING);
            raidParticipantRepository.save(participant);

            if (!Objects.equals(user.getId(), creatorId) && user.getDiscordId() != null && discordBotClient.isAvailable()) {
                discordBotClient.sendRaidInviteDm(
                        user.getDiscordId(),
                        schedule.getId(),
                        schedule.getRaidName(),
                        schedule.getDifficulty(),
                        schedule.getScheduledAt(),
                        schedule.getCreator().getDiscordUsername() != null ? schedule.getCreator().getDiscordUsername() : schedule.getCreator().getKakaoNickname(),
                        schedule.getDescription()
                ).thenAccept(messageId -> {
                    participant.setDmMessageId(messageId);
                    raidParticipantRepository.save(participant);
                }).exceptionally(e -> {
                    log.warn("레이드 초대 DM 발송 실패: raidId={}, userId={}", raidId, userId, e);
                    return null;
                });
            }
        }

        schedule.setUpdatedAt(LocalDateTime.now());
        raidScheduleRepository.save(schedule);

        return raidScheduleService.getRaidSchedule(raidId);
    }

    @Transactional
    public RaidScheduleResponse removeMember(Long requesterId, Long raidId, Long participantId) {
        RaidSchedule schedule = raidScheduleRepository.findById(raidId)
                .orElseThrow(() -> new IllegalArgumentException("레이드 일정을 찾을 수 없습니다."));

        RaidParticipant participant = raidParticipantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("참가자 정보를 찾을 수 없습니다."));

        if (participant.getRaidSchedule() == null || !Objects.equals(participant.getRaidSchedule().getId(), raidId)) {
            throw new IllegalArgumentException("참가자 정보가 올바르지 않습니다.");
        }

        boolean isCreator = schedule.getCreator() != null && Objects.equals(schedule.getCreator().getId(), requesterId);
        boolean isSelf = participant.getUser() != null && Objects.equals(participant.getUser().getId(), requesterId);
        if (!isCreator && !isSelf) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        raidParticipantRepository.delete(participant);
        return raidScheduleService.getRaidSchedule(raidId);
    }

    private void validateFriendAccess(Long creatorId, List<Long> userIds) {
        for (Long userId : userIds) {
            if (userId == null) continue;
            if (Objects.equals(userId, creatorId)) continue;
            boolean ok = friendshipRepository.existsAcceptedBetweenUsers(creatorId, userId);
            if (!ok) {
                throw new IllegalArgumentException("친구가 아닌 사용자는 초대할 수 없습니다.");
            }
        }
    }
}

