package com.lostark.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lostark.backend.dto.CombatSkillDto;
import com.lostark.backend.dto.SkillGemDto;
import com.lostark.backend.dto.SkillGemSkillDto;
import com.lostark.backend.dto.SkillMenuResponseDto;
import com.lostark.backend.lostark.domain.LostArkProfileDomainService;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillService {

    private final LostArkProfileDomainService lostArkProfileDomainService;
    private final ObjectMapper objectMapper;

    public SkillMenuResponseDto getSkillMenu(String characterName) {
        List<CombatSkillDto> combatSkills = lostArkProfileDomainService.fetchCombatSkills(characterName);
        List<SkillGemDto> skillGems = lostArkProfileDomainService.fetchSkillGems(characterName);

        // Parse gem tooltips to extract skill information
        skillGems.forEach(this::parseGemTooltip);

        SkillMenuResponseDto response = new SkillMenuResponseDto();
        response.setCharacterName(characterName);
        response.setCombatSkills(combatSkills);
        response.setSkillGems(skillGems);
        return response;
    }

    private void parseGemTooltip(SkillGemDto gem) {
        if (gem.getTooltip() == null || gem.getTooltip().isEmpty()) {
            return;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(gem.getTooltip());
            JsonNode element007 = rootNode.path("Element_007");

            if (!element007.isMissingNode()) {
                JsonNode value = element007.path("value");
                JsonNode element001 = value.path("Element_001");

                if (!element001.isMissingNode()) {
                    String effectText = element001.asText();
                    String skillName = extractSkillName(effectText);
                    String description = cleanHtmlTags(effectText);

                    if (skillName != null && !skillName.isEmpty()) {
                        SkillGemSkillDto skillDto = new SkillGemSkillDto();
                        skillDto.setName(skillName);
                        skillDto.setDescription(description);
                        gem.setSkill(skillDto);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse gem tooltip for gem: {}", gem.getName(), e);
        }
    }

    private String extractSkillName(String effectText) {
        // Extract skill name from HTML: <FONT COLOR='#FFD200'>스킬명</FONT>
        Pattern pattern = Pattern.compile("<FONT[^>]*COLOR='#FFD200'[^>]*>([^<]+)</FONT>");
        Matcher matcher = pattern.matcher(effectText);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String cleanHtmlTags(String html) {
        if (html == null) {
            return "";
        }
        // Remove HTML tags and clean up
        return html.replaceAll("<[^>]+>", "")
                   .replaceAll("<BR>", " ")
                   .replaceAll("\\s+", " ")
                   .trim();
    }
}
