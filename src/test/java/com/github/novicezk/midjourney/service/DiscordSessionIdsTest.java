package com.github.novicezk.midjourney.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiscordSessionIdsTest {
	@Test
	void liveSessionIsUsedForInteractions() {
		assertEquals("live-session", DiscordSessionIds.forInteraction("live-session"));
	}

	@Test
	void blankSessionIsRejected() {
		assertThrows(IllegalStateException.class, () -> DiscordSessionIds.forInteraction(" "));
	}
}
