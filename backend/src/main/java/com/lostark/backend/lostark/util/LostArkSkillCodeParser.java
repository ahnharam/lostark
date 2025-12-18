package com.lostark.backend.lostark.util;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LostArkSkillCodeParser {

    private static final String SCRIPT_VAR_PATTERN_TEMPLATE =
            "(?is)(?:var|let|const)\\s+%s\\s*=\\s*(['\\\"])([^'\\\"\\r\\n]*)\\1\\s*;?";
    private static final Pattern SKILL_CODE_PATTERN = Pattern.compile(
            "(?is)<div\\s+class\\s*=\\s*['\\\"]code['\\\"]\\s*>\\s*([0-9a-fA-F\\s]+)\\s*</div>"
    );

    private LostArkSkillCodeParser() {
    }

    public record ProfileIdentifiers(String memberNo, String worldNo, String pcId) {
        public ProfileIdentifiers {
            Objects.requireNonNull(memberNo, "memberNo");
            Objects.requireNonNull(worldNo, "worldNo");
            Objects.requireNonNull(pcId, "pcId");
        }
    }

    public static Optional<ProfileIdentifiers> parseProfileIdentifiers(String profileHtml) {
        if (profileHtml == null || profileHtml.isBlank()) {
            return Optional.empty();
        }

        Optional<String> memberNo = extractScriptVar(profileHtml, "_memberNo");
        Optional<String> worldNo = extractScriptVar(profileHtml, "_worldNo");
        Optional<String> pcId = extractScriptVar(profileHtml, "_pcId");

        if (memberNo.isEmpty() || worldNo.isEmpty() || pcId.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ProfileIdentifiers(memberNo.get(), worldNo.get(), pcId.get()));
    }

    public static Optional<String> extractSkillCodeFromContent(String contentHtml) {
        if (contentHtml == null || contentHtml.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = SKILL_CODE_PATTERN.matcher(contentHtml);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String raw = matcher.group(1);
        if (raw == null) {
            return Optional.empty();
        }

        String normalized = raw.replaceAll("\\s+", "").trim();
        if (normalized.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(normalized);
    }

    private static Optional<String> extractScriptVar(String html, String variableName) {
        Pattern pattern = Pattern.compile(String.format(SCRIPT_VAR_PATTERN_TEMPLATE, Pattern.quote(variableName)));
        Matcher matcher = pattern.matcher(html);
        if (!matcher.find()) {
            return Optional.empty();
        }
        String value = matcher.group(2);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(value.trim());
    }
}
