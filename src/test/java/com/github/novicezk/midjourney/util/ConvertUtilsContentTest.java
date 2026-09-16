package com.github.novicezk.midjourney.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConvertUtilsContentTest {
	@Test
	void parsesCurrentMidjourneyJobLinkFormat() {
		ContentParseData parsed = ConvertUtils.parseContent(
				"**a blue ceramic cup** - <@866170769272078346> "
						+ "[(Open on website for full quality)](<https://midjourney.com/jobs/123>) (fast)");

		assertNotNull(parsed);
		assertEquals("a blue ceramic cup", parsed.getPrompt());
		assertEquals("fast", parsed.getStatus());
	}
}
