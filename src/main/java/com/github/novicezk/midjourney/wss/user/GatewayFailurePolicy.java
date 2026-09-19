package com.github.novicezk.midjourney.wss.user;

public final class GatewayFailurePolicy {
	private GatewayFailurePolicy() {
	}

	public enum Action {
		DISABLE,
		RESUME,
		NEW_CONNECT
	}

	public static Action decide(int closeCode) {
		if (closeCode == SpringWebSocketHandler.CLOSE_CODE_INVALIDATE || closeCode >= 4000) {
			return Action.DISABLE;
		}
		if (closeCode == SpringWebSocketHandler.CLOSE_CODE_RECONNECT) {
			return Action.RESUME;
		}
		return Action.NEW_CONNECT;
	}
}
