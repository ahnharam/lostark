package com.lostark.backend.discord;

import com.lostark.backend.dto.CharacterProfileDto;
import com.lostark.backend.lostark.domain.LostArkProfileDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordMarketService {

    private final LostArkProfileDomainService profileDomainService;

    @Value("${lostark.api.key}")
    private String apiKey;

    @Value("${lostark.api.base-url}")
    private String baseUrl;

    private WebClient getWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("authorization", "bearer " + apiKey)
                .defaultHeader("accept", "application/json")
                .build();
    }

    /**
     * 거래소 시세 조회
     */
    public void searchMarketPrice(String itemName, SlashCommandInteractionEvent event) {
        try {
            Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("Sort", "CURRENT_MIN_PRICE");
            requestBody.put("CategoryCode", 0);
            requestBody.put("ItemName", itemName);
            requestBody.put("PageNo", 1);
            requestBody.put("SortCondition", "ASC");

            var response = getWebClient().post()
                    .uri("/markets/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.get("Items") == null) {
                event.getHook().editOriginal("❌ 검색 결과가 없습니다.").queue();
                return;
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("Items");

            if (items.isEmpty()) {
                event.getHook().editOriginal("❌ '" + itemName + "' 검색 결과가 없습니다.").queue();
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("💰 거래소 시세 - " + itemName)
                    .setColor(Color.CYAN);

            NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
            int count = 0;

            for (Map<String, Object> item : items) {
                if (count >= 5) break;

                String name = (String) item.get("Name");
                String grade = (String) item.get("Grade");
                Object priceObj = item.get("CurrentMinPrice");
                long price = priceObj instanceof Number ? ((Number) priceObj).longValue() : 0;
                Object recentObj = item.get("RecentPrice");
                long recentPrice = recentObj instanceof Number ? ((Number) recentObj).longValue() : 0;

                String gradeEmoji = getGradeEmoji(grade);

                embed.addField(
                        gradeEmoji + " " + name,
                        "현재 최저가: **" + nf.format(price) + "**G\n" +
                                "최근 거래가: " + nf.format(recentPrice) + "G",
                        false
                );
                count++;
            }

            event.getHook().editOriginalEmbeds(embed.build()).queue();

        } catch (Exception e) {
            log.error("거래소 시세 조회 실패", e);
            event.getHook().editOriginal("❌ 시세 조회 중 오류 발생: " + e.getMessage()).queue();
        }
    }

    /**
     * 경매장 검색
     */
    public void searchAuction(String itemName, SlashCommandInteractionEvent event) {
        try {
            Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("ItemName", itemName);
            requestBody.put("PageNo", 1);
            requestBody.put("Sort", "BUY_PRICE");
            requestBody.put("SortCondition", "ASC");

            var response = getWebClient().post()
                    .uri("/auctions/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.get("Items") == null) {
                event.getHook().editOriginal("❌ 검색 결과가 없습니다.").queue();
                return;
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("Items");

            if (items.isEmpty()) {
                event.getHook().editOriginal("❌ '" + itemName + "' 경매 검색 결과가 없습니다.").queue();
                return;
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🔨 경매장 검색 - " + itemName)
                    .setColor(Color.ORANGE);

            NumberFormat nf = NumberFormat.getInstance(Locale.KOREA);
            int count = 0;

            for (Map<String, Object> item : items) {
                if (count >= 5) break;

                String name = (String) item.get("Name");
                String grade = (String) item.get("Grade");
                
                Map<String, Object> auctionInfo = (Map<String, Object>) item.get("AuctionInfo");
                Object buyPriceObj = auctionInfo != null ? auctionInfo.get("BuyPrice") : null;
                Object bidPriceObj = auctionInfo != null ? auctionInfo.get("BidStartPrice") : null;
                
                long buyPrice = buyPriceObj instanceof Number ? ((Number) buyPriceObj).longValue() : 0;
                long bidPrice = bidPriceObj instanceof Number ? ((Number) bidPriceObj).longValue() : 0;

                String gradeEmoji = getGradeEmoji(grade);

                StringBuilder priceInfo = new StringBuilder();
                if (buyPrice > 0) {
                    priceInfo.append("즉시 구매: **").append(nf.format(buyPrice)).append("**G\n");
                }
                priceInfo.append("입찰 시작: ").append(nf.format(bidPrice)).append("G");

                embed.addField(
                        gradeEmoji + " " + name,
                        priceInfo.toString(),
                        false
                );
                count++;
            }

            event.getHook().editOriginalEmbeds(embed.build()).queue();

        } catch (Exception e) {
            log.error("경매장 검색 실패", e);
            event.getHook().editOriginal("❌ 경매 검색 중 오류 발생: " + e.getMessage()).queue();
        }
    }

    /**
     * 캐릭터 검색
     */
    public void searchCharacter(String characterName, SlashCommandInteractionEvent event) {
        try {
            CharacterProfileDto profile = profileDomainService.fetchCharacterProfile(characterName);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🎮 " + profile.getCharacterName())
                    .setColor(Color.BLUE)
                    .setThumbnail(profile.getCharacterImage())
                    .addField("서버", profile.getServerName(), true)
                    .addField("직업", profile.getCharacterClassName(), true)
                    .addField("아이템 레벨", profile.getItemAvgLevel(), true)
                    .addField("전투 레벨", String.valueOf(profile.getCharacterLevel()), true)
                    .addField("원정대 레벨", String.valueOf(profile.getExpeditionLevel()), true);

            if (profile.getGuildName() != null) {
                embed.addField("길드", profile.getGuildName(), true);
            }

            if (profile.getTitle() != null) {
                embed.addField("칭호", profile.getTitle(), false);
            }

            event.getHook().editOriginalEmbeds(embed.build()).queue();

        } catch (Exception e) {
            log.error("캐릭터 검색 실패", e);
            event.getHook().editOriginal("❌ 캐릭터를 찾을 수 없습니다: " + characterName).queue();
        }
    }

    private String getGradeEmoji(String grade) {
        if (grade == null) return "⬜";
        return switch (grade) {
            case "고대" -> "🟡";
            case "유물" -> "🟠";
            case "전설" -> "🟤";
            case "영웅" -> "🟣";
            case "희귀" -> "🔵";
            case "고급" -> "🟢";
            default -> "⬜";
        };
    }
}
