package com.lostark.backend.raid.service;

import com.lostark.backend.discord.DiscordBotClient;
import com.lostark.backend.raid.dto.*;
import com.lostark.backend.raid.entity.*;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaidScheduleService {

    private final RaidScheduleRepository raidScheduleRepository;
    private final RaidParticipantRepository raidParticipantRepository;
    private final AppUserRepository appUserRepository;
    private final DiscordBotClient discordBotClient;

    @Transactional
    public RaidScheduleResponse createRaidSchedule(Long creatorId, RaidScheduleCreateRequest request) {
        AppUser creator = appUserRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RaidSchedule schedule = new RaidSchedule();
        schedule.setRaidName(request.getRaidName());
        schedule.setDifficulty(request.getDifficulty());
        schedule.setScheduledAt(request.getScheduledAt());
        schedule.setDescription(request.getDescription());
        schedule.setMaxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : 8);
        schedule.setCreator(creator);
        schedule.setStatus(RaidStatus.RECRUITING);

        RaidSchedule saved = raidScheduleRepository.save(schedule);

        // 초대된 유저들에게 참가자 등록 + Discord DM 발송
        if (request.getInvitedUserIds() != null && !request.getInvitedUserIds().isEmpty()) {
            for (Long userId : request.getInvitedUserIds()) {
                inviteParticipant(saved, userId, creator);
            }
        }

        return toResponse(saved);
    }

    private void inviteParticipant(RaidSchedule schedule, Long userId, AppUser creator) {
        AppUser user = appUserRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("초대할 사용자를 찾을 수 없습니다: {}", userId);
            return;
        }

        // 참가자 등록
        RaidParticipant participant = new RaidParticipant();
        participant.setRaidSchedule(schedule);
        participant.setUser(user);
        participant.setStatus(ParticipantStatus.PENDING);
        participant.setCharacterName(user.getMainCharacterName());
        raidParticipantRepository.save(participant);

        // Discord DM 발송
        if (user.getDiscordId() != null && discordBotClient.isAvailable()) {
            discordBotClient.sendRaidInviteDm(
                    user.getDiscordId(),
                    schedule.getId(),
                    schedule.getRaidName(),
                    schedule.getDifficulty(),
                    schedule.getScheduledAt(),
                    creator.getDiscordUsername() != null ? creator.getDiscordUsername() : creator.getKakaoNickname(),
                    schedule.getDescription()
            ).thenAccept(messageId -> {
                participant.setDmMessageId(messageId);
                raidParticipantRepository.save(participant);
                log.info("Discord DM 발송 완료: userId={}, messageId={}", userId, messageId);
            }).exceptionally(e -> {
                log.error("Discord DM 발송 실패: userId={}", userId, e);
                return null;
            });
        }
    }

    @Transactional(readOnly = true)
    public RaidScheduleResponse getRaidSchedule(Long scheduleId) {
        RaidSchedule schedule = raidScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("레이드 일정을 찾을 수 없습니다."));
        return toResponse(schedule);
    }

    @Transactional(readOnly = true)
    public List<RaidScheduleResponse> getUpcomingRaids() {
        return raidScheduleRepository
                .findUpcomingByStatus(RaidStatus.RECRUITING, LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RaidScheduleResponse> getMyRaids(Long userId) {
        return raidScheduleRepository.findByCreatorId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelRaid(Long scheduleId, Long userId) {
        RaidSchedule schedule = raidScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("레이드 일정을 찾을 수 없습니다."));

        if (!schedule.getCreator().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 생성한 레이드만 취소할 수 있습니다.");
        }

        schedule.setStatus(RaidStatus.CANCELLED);
        raidScheduleRepository.save(schedule);

        // TODO: 참가자들에게 취소 알림 발송
    }

    @Transactional
    public void inviteUsers(Long scheduleId, Long creatorId, List<Long> userIds) {
        RaidSchedule schedule = raidScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("레이드 일정을 찾을 수 없습니다."));

        if (!schedule.getCreator().getId().equals(creatorId)) {
            throw new IllegalArgumentException("본인이 생성한 레이드만 초대할 수 있습니다.");
        }

        AppUser creator = schedule.getCreator();
        for (Long userId : userIds) {
            if (!raidParticipantRepository.existsByRaidScheduleIdAndUserId(scheduleId, userId)) {
                inviteParticipant(schedule, userId, creator);
            }
        }
    }

    private RaidScheduleResponse toResponse(RaidSchedule schedule) {
        List<ParticipantResponse> participants = schedule.getParticipants().stream()
                .map(this::toParticipantResponse)
                .collect(Collectors.toList());

        return RaidScheduleResponse.builder()
                .id(schedule.getId())
                .raidName(schedule.getRaidName())
                .difficulty(schedule.getDifficulty())
                .scheduledAt(schedule.getScheduledAt())
                .description(schedule.getDescription())
                .status(schedule.getStatus())
                .maxParticipants(schedule.getMaxParticipants())
                .confirmedCount(schedule.getConfirmedCount())
                .creatorName(schedule.getCreator().getDiscordUsername() != null ? 
                        schedule.getCreator().getDiscordUsername() : schedule.getCreator().getKakaoNickname())
                .participants(participants)
                .createdAt(schedule.getCreatedAt())
                .build();
    }

    private ParticipantResponse toParticipantResponse(RaidParticipant participant) {
        return ParticipantResponse.builder()
                .id(participant.getId())
                .userId(participant.getUser().getId())
                .discordUsername(participant.getUser().getDiscordUsername())
                .characterName(participant.getCharacterName())
                .characterClass(participant.getCharacterClass())
                .itemLevel(participant.getItemLevel())
                .status(participant.getStatus())
                .changeRequestReason(participant.getChangeRequestReason())
                .respondedAt(participant.getRespondedAt())
                .build();
    }
}
