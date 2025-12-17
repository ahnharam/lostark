package com.lostark.backend.raid.service;

import com.lostark.backend.discord.DiscordBotClient;
import com.lostark.backend.raid.entity.ParticipantStatus;
import com.lostark.backend.raid.entity.RaidParticipant;
import com.lostark.backend.raid.entity.RaidSchedule;
import com.lostark.backend.raid.repository.RaidParticipantRepository;
import com.lostark.backend.user.entity.AppUser;
import com.lostark.backend.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaidParticipantService {

    private final RaidParticipantRepository raidParticipantRepository;
    private final AppUserRepository appUserRepository;
    private final DiscordBotClient discordBotClient;
    private final RaidWeeklyLockoutService raidWeeklyLockoutService;

    /**
     * Discord 버튼 클릭으로 참가 상태 업데이트
     */
    @Transactional
    public void updateParticipantStatusByDiscord(Long raidScheduleId, String discordUserId, 
                                                  ParticipantStatus status, String reason) {
        AppUser user = appUserRepository.findByDiscordId(discordUserId)
                .orElseThrow(() -> new IllegalArgumentException("연동된 사용자를 찾을 수 없습니다."));

        RaidParticipant participant = raidParticipantRepository
                .findByRaidScheduleIdAndUserId(raidScheduleId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("참가자 정보를 찾을 수 없습니다."));

        if (status == ParticipantStatus.ACCEPTED && participant.getStatus() != ParticipantStatus.ACCEPTED) {
            raidWeeklyLockoutService.validateCanAccept(participant.getRaidSchedule(), participant.getCharacterName());
        }

        participant.setStatus(status);
        participant.setRespondedAt(LocalDateTime.now());
        
        if (status == ParticipantStatus.CHANGE_REQUESTED && reason != null) {
            participant.setChangeRequestReason(reason);
        }

        raidParticipantRepository.save(participant);

        // 생성자에게 알림 발송
        notifyCreator(participant, status, reason);
    }

    /**
     * 웹/앱에서 참가 상태 업데이트
     */
    @Transactional
    public void updateParticipantStatus(Long participantId, Long userId, 
                                        ParticipantStatus status, String reason) {
        RaidParticipant participant = raidParticipantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("참가자 정보를 찾을 수 없습니다."));

        if (!participant.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 참가 상태만 변경할 수 있습니다.");
        }

        if (status == ParticipantStatus.ACCEPTED && participant.getStatus() != ParticipantStatus.ACCEPTED) {
            raidWeeklyLockoutService.validateCanAccept(participant.getRaidSchedule(), participant.getCharacterName());
        }

        participant.setStatus(status);
        participant.setRespondedAt(LocalDateTime.now());

        if (status == ParticipantStatus.CHANGE_REQUESTED && reason != null) {
            participant.setChangeRequestReason(reason);
        }

        raidParticipantRepository.save(participant);

        notifyCreator(participant, status, reason);
    }

    private void notifyCreator(RaidParticipant participant, ParticipantStatus status, String reason) {
        RaidSchedule schedule = participant.getRaidSchedule();
        AppUser creator = schedule.getCreator();

        if (creator.getDiscordId() != null && discordBotClient.isAvailable()) {
            String participantName = participant.getUser().getDiscordUsername() != null ?
                    participant.getUser().getDiscordUsername() : participant.getUser().getKakaoNickname();

            discordBotClient.sendResponseNotification(
                    creator.getDiscordId(),
                    participantName,
                    schedule.getRaidName() + " " + schedule.getDifficulty(),
                    status.name(),
                    reason
            );
        }

        log.info("참가 상태 변경: raidId={}, userId={}, status={}", 
                schedule.getId(), participant.getUser().getId(), status);
    }
}
