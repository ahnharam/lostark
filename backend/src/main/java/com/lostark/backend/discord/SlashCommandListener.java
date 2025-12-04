package com.lostark.backend.discord;

import com.lostark.backend.raid.dto.RaidScheduleCreateRequest;
import com.lostark.backend.raid.service.RaidScheduleService;

import com.lostark.backend.user.entity.AppUser;
import com.lostark.backend.user.repository.AppUserRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SlashCommandListener extends ListenerAdapter {

    private final RaidScheduleService raidScheduleService;
    private final AppUserRepository appUserRepository;
    private final DiscordMarketService discordMarketService;

    public SlashCommandListener(
            @Lazy RaidScheduleService raidScheduleService,
            AppUserRepository appUserRepository,
            @Lazy DiscordMarketService discordMarketService) {
        this.raidScheduleService = raidScheduleService;
        this.appUserRepository = appUserRepository;
        this.discordMarketService = discordMarketService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        log.info("슬래시 커맨드 실행: {} by {}", commandName, event.getUser().getName());

        switch (commandName) {
            case "레이드" -> handleRaidCommand(event);
            case "시세" -> handleMarketCommand(event);
            case "경매" -> handleAuctionCommand(event);
            case "캐릭터" -> handleCharacterCommand(event);
            case "등록" -> handleRegisterCommand(event);
            default -> event.reply("알 수 없는 명령어입니다.").setEphemeral(true).queue();
        }
    }

    private void handleRaidCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        try {
            String raidName = event.getOption("레이드명").getAsString();
            String dayTime = event.getOption("일시").getAsString();
            String difficulty = event.getOption("난이도") != null ? 
                    event.getOption("난이도").getAsString() : "노말";

            // 일시 파싱 (예: "수요일 21시", "토 20:30")
            LocalDateTime scheduledAt = parseDayTime(dayTime);

            // 유저 확인
            AppUser creator = appUserRepository.findByDiscordId(event.getUser().getId())
                    .orElse(null);

            if (creator == null) {
                event.getHook().editOriginal("❌ 먼저 `/등록` 명령어로 유저 등록을 해주세요.").queue();
                return;
            }

            // 레이드 생성
            RaidScheduleCreateRequest request = new RaidScheduleCreateRequest();
            request.setRaidName(raidName);
            request.setDifficulty(difficulty);
            request.setScheduledAt(scheduledAt);
            request.setMaxParticipants(8);

            var response = raidScheduleService.createRaidSchedule(creator.getId(), request);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🗡️ 레이드 생성 완료!")
                    .setColor(Color.GREEN)
                    .addField("레이드", raidName + " " + difficulty, true)
                    .addField("일시", scheduledAt.format(DateTimeFormatter.ofPattern("MM/dd (E) HH:mm")), true)
                    .addField("ID", String.valueOf(response.getId()), true)
                    .setFooter("참가자 초대: /초대 명령어 사용");

            event.getHook().editOriginalEmbeds(embed.build()).queue();

        } catch (Exception e) {
            log.error("레이드 생성 실패", e);
            event.getHook().editOriginal("❌ 레이드 생성 실패: " + e.getMessage()).queue();
        }
    }

    private void handleMarketCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        try {
            String itemName = event.getOption("아이템").getAsString();
            discordMarketService.searchMarketPrice(itemName, event);
        } catch (Exception e) {
            log.error("시세 조회 실패", e);
            event.getHook().editOriginal("❌ 시세 조회 실패: " + e.getMessage()).queue();
        }
    }

    private void handleAuctionCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        try {
            String itemName = event.getOption("아이템").getAsString();
            discordMarketService.searchAuction(itemName, event);
        } catch (Exception e) {
            log.error("경매 검색 실패", e);
            event.getHook().editOriginal("❌ 경매 검색 실패: " + e.getMessage()).queue();
        }
    }

    private void handleCharacterCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        try {
            String characterName = event.getOption("캐릭터명").getAsString();
            discordMarketService.searchCharacter(characterName, event);
        } catch (Exception e) {
            log.error("캐릭터 조회 실패", e);
            event.getHook().editOriginal("❌ 캐릭터 조회 실패: " + e.getMessage()).queue();
        }
    }

    private void handleRegisterCommand(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue(); // ephemeral reply

        try {
            String characterName = event.getOption("캐릭터명").getAsString();
            String discordId = event.getUser().getId();
            String discordUsername = event.getUser().getName();

            // 이미 등록된 유저인지 확인
            var existingUser = appUserRepository.findByDiscordId(discordId);
            if (existingUser.isPresent()) {
                AppUser user = existingUser.get();
                user.setMainCharacterName(characterName);
                user.setDiscordUsername(discordUsername);
                appUserRepository.save(user);
                event.getHook().editOriginal("✅ 대표 캐릭터가 **" + characterName + "**(으)로 변경되었습니다.").queue();
                return;
            }

            // 신규 등록
            AppUser newUser = new AppUser();
            newUser.setDiscordId(discordId);
            newUser.setDiscordUsername(discordUsername);
            newUser.setMainCharacterName(characterName);
            appUserRepository.save(newUser);

            event.getHook().editOriginal("✅ 등록 완료!\n대표 캐릭터: **" + characterName + "**").queue();

        } catch (Exception e) {
            log.error("유저 등록 실패", e);
            event.getHook().editOriginal("❌ 등록 실패: " + e.getMessage()).queue();
        }
    }

    private LocalDateTime parseDayTime(String dayTime) {
        // "수요일 21시", "수 21시", "수요일 21:30" 등 파싱
        String cleaned = dayTime.trim().toLowerCase();
        
        Map<String, DayOfWeek> dayMap = new HashMap<>();
        dayMap.put("월", DayOfWeek.MONDAY); dayMap.put("월요일", DayOfWeek.MONDAY);
        dayMap.put("화", DayOfWeek.TUESDAY); dayMap.put("화요일", DayOfWeek.TUESDAY);
        dayMap.put("수", DayOfWeek.WEDNESDAY); dayMap.put("수요일", DayOfWeek.WEDNESDAY);
        dayMap.put("목", DayOfWeek.THURSDAY); dayMap.put("목요일", DayOfWeek.THURSDAY);
        dayMap.put("금", DayOfWeek.FRIDAY); dayMap.put("금요일", DayOfWeek.FRIDAY);
        dayMap.put("토", DayOfWeek.SATURDAY); dayMap.put("토요일", DayOfWeek.SATURDAY);
        dayMap.put("일", DayOfWeek.SUNDAY); dayMap.put("일요일", DayOfWeek.SUNDAY);

        DayOfWeek targetDay = null;
        String timePart = cleaned;

        for (Map.Entry<String, DayOfWeek> entry : dayMap.entrySet()) {
            if (cleaned.contains(entry.getKey())) {
                targetDay = entry.getValue();
                timePart = cleaned.replace(entry.getKey(), "").trim();
                break;
            }
        }

        // 시간 파싱
        int hour = 21;
        int minute = 0;

        timePart = timePart.replace("시", "").replace(" ", "");
        if (timePart.contains(":")) {
            String[] parts = timePart.split(":");
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        } else if (!timePart.isEmpty()) {
            hour = Integer.parseInt(timePart);
        }

        LocalDate date = LocalDate.now();
        if (targetDay != null) {
            date = date.with(TemporalAdjusters.nextOrSame(targetDay));
        }

        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }
}
