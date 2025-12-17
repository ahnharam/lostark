package com.lostark.backend.raid.service;

import com.lostark.backend.discord.DiscordBotClient;
import com.lostark.backend.friend.repository.FriendshipRepository;
import com.lostark.backend.raid.dto.RaidScheduleCreateRequest;
import com.lostark.backend.raid.dto.ExpeditionCharacterResponse;
import com.lostark.backend.raid.dto.RaidScheduleResponse;
import com.lostark.backend.dto.SiblingCharacterDto;
import com.lostark.backend.raid.entity.ParticipantStatus;
import com.lostark.backend.raid.entity.RaidParticipant;
import com.lostark.backend.raid.entity.RaidSchedule;
import com.lostark.backend.raid.repository.RaidParticipantRepository;
import com.lostark.backend.raid.repository.RaidScheduleRepository;
import com.lostark.backend.user.entity.AppUser;
import com.lostark.backend.user.repository.AppUserRepository;
import com.lostark.backend.lostark.domain.LostArkProfileDomainService;
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
    private final LostArkProfileDomainService lostArkProfileDomainService;
    private final RaidWeeklyLockoutService raidWeeklyLockoutService;

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
            boolean acceptImmediately = Objects.equals(user.getId(), creatorId);
            if (acceptImmediately) {
                raidWeeklyLockoutService.validateCanAccept(schedule, participant.getCharacterName());
            }
            participant.setStatus(acceptImmediately ? ParticipantStatus.ACCEPTED : ParticipantStatus.PENDING);
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
        raidParticipantRepository.flush();
        if (schedule.getParticipants() != null) {
            schedule.getParticipants().removeIf(p -> Objects.equals(p.getId(), participantId));
        }
        return raidScheduleService.getRaidSchedule(raidId);
    }

    @Transactional
    public void deleteRaid(Long requesterId, Long raidId) {
        RaidSchedule schedule = raidScheduleRepository.findById(raidId)
                .orElseThrow(() -> new IllegalArgumentException("레이드 일정을 찾을 수 없습니다."));

        if (schedule.getCreator() == null || !Objects.equals(schedule.getCreator().getId(), requesterId)) {
            throw new IllegalArgumentException("본인이 생성한 레이드만 삭제할 수 있습니다.");
        }

        raidScheduleRepository.delete(schedule);
    }

    @Transactional(readOnly = true)
    public List<ExpeditionCharacterResponse> getMemberExpeditionCharacters(Long requesterId, Long raidId, Long targetUserId) {
        RaidSchedule schedule = raidScheduleRepository.findById(raidId)
                .orElseThrow(() -> new IllegalArgumentException("레이드 일정을 찾을 수 없습니다."));

        boolean requesterIsCreator = schedule.getCreator() != null && Objects.equals(schedule.getCreator().getId(), requesterId);
        boolean requesterIsTarget = Objects.equals(requesterId, targetUserId);
        if (!requesterIsCreator && !requesterIsTarget) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        boolean targetIsCreator = schedule.getCreator() != null && Objects.equals(schedule.getCreator().getId(), targetUserId);
        boolean targetIsParticipant = raidParticipantRepository.existsByRaidScheduleIdAndUserId(raidId, targetUserId);
        if (!targetIsCreator && !targetIsParticipant) {
            throw new IllegalArgumentException("레이드 멤버가 아닙니다.");
        }

        AppUser targetUser = appUserRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (targetUser.getDiscordId() == null || targetUser.getDiscordId().isBlank()) {
            throw new IllegalArgumentException("디스코드 연동이 필요합니다.");
        }
        if (targetUser.getMainCharacterName() == null || targetUser.getMainCharacterName().isBlank()) {
            throw new IllegalArgumentException("내 캐릭터 등록이 필요합니다.");
        }

        List<SiblingCharacterDto> siblings = lostArkProfileDomainService.fetchSiblingCharacters(targetUser.getMainCharacterName());
        return siblings.stream()
                .filter(item -> item.getCharacterName() != null && !item.getCharacterName().isBlank())
                .map(item -> ExpeditionCharacterResponse.builder()
                        .characterName(item.getCharacterName())
                        .serverName(item.getServerName())
                        .characterClassName(item.getCharacterClassName())
                        .itemAvgLevel(item.getItemAvgLevel())
                        .itemMaxLevel(item.getItemMaxLevel())
                        .build())
                .toList();
    }

    @Transactional
    public RaidScheduleResponse updateMemberCharacter(Long requesterId, Long raidId, Long participantId, String characterName) {
        if (characterName == null || characterName.isBlank()) {
            throw new IllegalArgumentException("캐릭터명이 필요합니다.");
        }

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

        AppUser targetUser = participant.getUser();
        if (targetUser == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        if (targetUser.getMainCharacterName() == null || targetUser.getMainCharacterName().isBlank()) {
            throw new IllegalArgumentException("내 캐릭터 등록이 필요합니다.");
        }

        List<SiblingCharacterDto> siblings = lostArkProfileDomainService.fetchSiblingCharacters(targetUser.getMainCharacterName());
        boolean exists = siblings.stream()
                .anyMatch(item -> item.getCharacterName() != null && item.getCharacterName().equals(characterName));
        if (!exists) {
            throw new IllegalArgumentException("원정대 캐릭터 목록에 없는 캐릭터입니다.");
        }

        if (participant.getStatus() == ParticipantStatus.ACCEPTED) {
            String current = participant.getCharacterName();
            if (current == null || !current.equals(characterName)) {
                raidWeeklyLockoutService.validateCanAccept(schedule, characterName);
            }
        }

        participant.setCharacterName(characterName);
        raidParticipantRepository.save(participant);
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
