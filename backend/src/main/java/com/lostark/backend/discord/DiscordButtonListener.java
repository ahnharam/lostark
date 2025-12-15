package com.lostark.backend.discord;

import com.lostark.backend.raid.entity.ParticipantStatus;
import com.lostark.backend.raid.service.RaidParticipantService;
import com.lostark.backend.friend.service.FriendshipService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DiscordButtonListener extends ListenerAdapter {

    private final RaidParticipantService raidParticipantService;
    private final FriendshipService friendshipService;

    public DiscordButtonListener(
            @Lazy RaidParticipantService raidParticipantService,
            @Lazy FriendshipService friendshipService
    ) {
        this.raidParticipantService = raidParticipantService;
        this.friendshipService = friendshipService;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        String discordUserId = event.getUser().getId();

        log.info("버튼 클릭: componentId={}, userId={}", componentId, discordUserId);

        try {
            if (componentId.startsWith("raid_accept_")) {
                Long raidId = extractRaidId(componentId, "raid_accept_");
                handleAccept(event, raidId, discordUserId);
            } else if (componentId.startsWith("raid_decline_")) {
                Long raidId = extractRaidId(componentId, "raid_decline_");
                handleDecline(event, raidId, discordUserId);
            } else if (componentId.startsWith("raid_change_")) {
                Long raidId = extractRaidId(componentId, "raid_change_");
                showChangeReasonModal(event, raidId);
            } else if (componentId.startsWith("friend_accept_")) {
                Long requestId = extractRaidId(componentId, "friend_accept_");
                friendshipService.respondToRequestFromDiscord(requestId, discordUserId, true);
                event.reply("✅ 친구 요청을 수락했어요.").setEphemeral(true).queue();
                disableButtons(event);
            } else if (componentId.startsWith("friend_decline_")) {
                Long requestId = extractRaidId(componentId, "friend_decline_");
                friendshipService.respondToRequestFromDiscord(requestId, discordUserId, false);
                event.reply("❌ 친구 요청을 거절했어요.").setEphemeral(true).queue();
                disableButtons(event);
            }
        } catch (Exception e) {
            log.error("버튼 처리 실패", e);
            event.reply("오류가 발생했습니다. 다시 시도해주세요.").setEphemeral(true).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String modalId = event.getModalId();

        if (modalId.startsWith("change_reason_modal_")) {
            Long raidId = extractRaidId(modalId, "change_reason_modal_");
            String reason = event.getValue("reason_input").getAsString();
            String discordUserId = event.getUser().getId();

            try {
                raidParticipantService.updateParticipantStatusByDiscord(
                        raidId,
                        discordUserId,
                        ParticipantStatus.CHANGE_REQUESTED,
                        reason
                );
                event.reply("🔄 시간 변경 요청이 전달되었습니다!").setEphemeral(true).queue();
                disableButtons(event);
            } catch (Exception e) {
                log.error("변경 요청 처리 실패", e);
                event.reply("오류가 발생했습니다.").setEphemeral(true).queue();
            }
        }
    }

    private void handleAccept(ButtonInteractionEvent event, Long raidId, String discordUserId) {
        raidParticipantService.updateParticipantStatusByDiscord(
                raidId,
                discordUserId,
                ParticipantStatus.ACCEPTED,
                null
        );
        event.reply("✅ 참여 확정되었습니다!").setEphemeral(true).queue();
        disableButtons(event);
    }

    private void handleDecline(ButtonInteractionEvent event, Long raidId, String discordUserId) {
        raidParticipantService.updateParticipantStatusByDiscord(
                raidId,
                discordUserId,
                ParticipantStatus.DECLINED,
                null
        );
        event.reply("❌ 불참 처리되었습니다.").setEphemeral(true).queue();
        disableButtons(event);
    }

    private void showChangeReasonModal(ButtonInteractionEvent event, Long raidId) {
        TextInput reasonInput = TextInput.create("reason_input", "변경 요청 사유", TextInputStyle.PARAGRAPH)
                .setPlaceholder("ex) 30분 늦을 것 같습니다 / 다음날로 변경 가능할까요?")
                .setRequired(true)
                .setMaxLength(500)
                .build();

        Modal modal = Modal.create("change_reason_modal_" + raidId, "시간 변경 요청")
                .addComponents(ActionRow.of(reasonInput))
                .build();

        event.replyModal(modal).queue();
    }

    private void disableButtons(ButtonInteractionEvent event) {
        event.getMessage().editMessageComponents().queue();
    }

    private void disableButtons(ModalInteractionEvent event) {
        // Modal에서는 원본 메시지 접근이 어려우므로 스킵
    }

    private Long extractRaidId(String componentId, String prefix) {
        return Long.parseLong(componentId.replace(prefix, ""));
    }
}
