package com.github.novicezk.midjourney.wss.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GatewayFailurePolicyTest {
	@Test
	void invalidSessionDisablesAccount() {
		assertEquals(GatewayFailurePolicy.Action.DISABLE, GatewayFailurePolicy.decide(SpringWebSocketHandler.CLOSE_CODE_INVALIDATE));
	}

	@Test
	void discordAuthFailureDisablesAccount() {
		assertEquals(GatewayFailurePolicy.Action.DISABLE, GatewayFailurePolicy.decide(4004));
	}

	@Test
	void serverReconnectTriesResume() {
		assertEquals(GatewayFailurePolicy.Action.RESUME, GatewayFailurePolicy.decide(SpringWebSocketHandler.CLOSE_CODE_RECONNECT));
	}

	@Test
	void ordinaryCloseTriesNewConnect() {
		assertEquals(GatewayFailurePolicy.Action.NEW_CONNECT, GatewayFailurePolicy.decide(1006));
	}
}
