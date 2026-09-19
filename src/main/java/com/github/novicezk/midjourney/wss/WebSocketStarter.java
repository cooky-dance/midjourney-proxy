package com.github.novicezk.midjourney.wss;


public interface WebSocketStarter {

	void start() throws Exception;

	default void reconnect() throws Exception {
		start();
	}

}
