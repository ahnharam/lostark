package com.lostark.backend.discord;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class DiscordBotClient {

    @Value("${discord.bot.token:}")
    private String botToken;

    @Getter
    private JDA jda;

    private final DiscordButtonListener buttonListener;

    public DiscordBotClient(DiscordButtonListener buttonListener) {
        this.buttonListener = buttonListener;
    }

    @PostConstruct
    public void init() {
        if (botToken == null || botToken.isEmpty()) {
            log.warn("Discord bot token이 설정되지 않았습니다. Discord 기능이 비활성화됩니다.");
            return;
        }

        try {
            jda = JDABuilder.createDefault(botToken)
                    .enableIntents(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(buttonListener)
                    .build()
                    .awaitReady();
            log.info("Discord 봇이 성공적으로 시작되었습니다: {}", jda.getSelfUser().getName());
        } catch (Exception e) {
            log.error("Discord 봇 시작 실패", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            log.info("Discord 봇이 종료되었습니다.");
        }
    }

    public boolean isAvailable() {
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    /**
     * 레이드 초대 DM 발송
     */
    public CompletableFuture<String> sendRaidInviteDm(
            String discordUserId,
            Long raidScheduleId,
            String raidName,
            String difficulty,
            LocalDateTime scheduledAt,
            String creatorName,
            String description
    ) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Discord 봇이 연결되지 않았습니다."));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                User user = jda.retrieveUserById(discordUserId).complete();
                PrivateChannel channel = user.openPrivateChannel().complete();

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("🗡️ 레이드 초대")
                        .setColor(Color.ORANGE)
                        .addField("레이드", raidName + " " + difficulty, true)
                        .addField("시간", scheduledAt.format(DateTimeFormatter.ofPattern("MM/dd (E) HH:mm")), true)
                        .addField("생성자", creatorName, true)
                        .setDescription(description != null ? description : "")
                        .setFooter("아래 버튼을 눌러 참여 여부를 알려주세요!");

                MessageCreateData message = new MessageCreateBuilder()
                        .addEmbeds(embed.build())
                        .addActionRow(
                                Button.success("raid_accept_" + raidScheduleId, "✅ 참여"),
                                Button.danger("raid_decline_" + raidScheduleId, "❌ 불참"),
                                Button.secondary("raid_change_" + raidScheduleId, "🔄 시간 변경 요청")
                        )
                        .build();

                return channel.sendMessage(message).complete().getId();
            } catch (Exception e) {
                log.error("DM 발송 실패: userId={}", discordUserId, e);
                throw new RuntimeException("DM 발송 실패", e);
            }
        });
    }

    /**
     * 생성자에게 참가자 응답 알림
     */
    public CompletableFuture<Void> sendResponseNotification(
            String creatorDiscordId,
            String participantName,
            String raidName,
            String status,
            String reason
    ) {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Discord 봇이 연결되지 않았습니다."));
        }

        return CompletableFuture.runAsync(() -> {
            try {
                User user = jda.retrieveUserById(creatorDiscordId).complete();
                PrivateChannel channel = user.openPrivateChannel().complete();

                String emoji = switch (status) {
                    case "ACCEPTED" -> "✅";
                    case "DECLINED" -> "❌";
                    case "CHANGE_REQUESTED" -> "🔄";
                    default -> "📋";
                };

                String statusText = switch (status) {
                    case "ACCEPTED" -> "참여 확정";
                    case "DECLINED" -> "불참";
                    case "CHANGE_REQUESTED" -> "시간 변경 요청";
                    default -> status;
                };

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(emoji + " 레이드 응답 알림")
                        .setColor(status.equals("ACCEPTED") ? Color.GREEN : 
                                 status.equals("DECLINED") ? Color.RED : Color.YELLOW)
                        .addField("레이드", raidName, true)
                        .addField("참가자", participantName, true)
                        .addField("응답", statusText, true);

                if (reason != null && !reason.isEmpty()) {
                    embed.addField("사유", reason, false);
                }

                channel.sendMessageEmbeds(embed.build()).queue();
            } catch (Exception e) {
                log.error("알림 발송 실패: creatorId={}", creatorDiscordId, e);
            }
        });
    }
}
