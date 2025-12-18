package com.lostark.backend.lostark.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.lostark.backend.lostark.util.LostArkSkillCodeParser.ProfileIdentifiers;
import org.junit.jupiter.api.Test;

class LostArkSkillCodeParserTest {

    @Test
    void parseProfileIdentifiers_extractsIdentifiersFromProfileHtml() {
        String html = """
                <html>
                  <head></head>
                  <body>
                    <script type="text/javascript">
                      var _memberNo = '/eB2k50bDAxi8ejm0s7L+2XP/hosYPI+QnRZE=/T6Z0=';
                      var _pcId = 'XnmgTFbmMq6UCfCicdfe8Boa9BLKF/YVbchUWND03ro=';
                      var _worldNo = 'aE8brDgu64VvvhVlkemnNg==';
                    </script>
                  </body>
                </html>
                """;

        assertThat(LostArkSkillCodeParser.parseProfileIdentifiers(html))
                .contains(new ProfileIdentifiers(
                        "/eB2k50bDAxi8ejm0s7L+2XP/hosYPI+QnRZE=/T6Z0=",
                        "aE8brDgu64VvvhVlkemnNg==",
                        "XnmgTFbmMq6UCfCicdfe8Boa9BLKF/YVbchUWND03ro="
                ));
    }

    @Test
    void parseProfileIdentifiers_returnsEmptyWhenMissingAnyIdentifier() {
        String html = """
                <script>
                  var _memberNo = 'abc';
                  var _pcId = 'def';
                </script>
                """;

        assertThat(LostArkSkillCodeParser.parseProfileIdentifiers(html)).isEmpty();
    }

    @Test
    void extractSkillCodeFromContent_extractsHexCodeFromContentHtml() {
        String content = """
                <div class="code">
                  51F2DB97941CE5D1446DE32B1F96D22F19391D1B2CC285CBC98F8D3C7275A5F402
                  6CC0C62B7C6D400A91CBC130341280D0EEEEC7C5BFAD86DDD57F9D3584FAD1
                </div>
                """;

        assertThat(LostArkSkillCodeParser.extractSkillCodeFromContent(content))
                .contains("51F2DB97941CE5D1446DE32B1F96D22F19391D1B2CC285CBC98F8D3C7275A5F4026CC0C62B7C6D400A91CBC130341280D0EEEEC7C5BFAD86DDD57F9D3584FAD1");
    }
}

