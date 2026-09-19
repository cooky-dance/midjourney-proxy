package com.github.novicezk.midjourney.service;

import cn.hutool.core.text.CharSequenceUtil;

public final class DiscordSessionIds {
	private DiscordSessionIds() {
	}

	public static String forInteraction(String liveSessionId) {
		if (CharSequenceUtil.isBlank(liveSessionId)) {
			throw new IllegalStateException("discord session is not ready");
		}
		return liveSessionId;
	}
}
