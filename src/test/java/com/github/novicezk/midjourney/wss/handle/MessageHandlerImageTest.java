package com.github.novicezk.midjourney.wss.handle;

import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.support.DiscordHelper;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageHandlerImageTest {
	private ExposedMessageHandler handler;

	@BeforeEach
	void setUp() {
		this.handler = new ExposedMessageHandler();
		this.handler.discordHelper = new DiscordHelper(new ProxyProperties());
	}

	@Test
	void readsDiscordAttachmentImage() {
		DataObject message = DataObject.fromJson("""
				{"attachments":[{"url":"https://cdn.discordapp.com/attachments/1/2/grid.png?width=1024"}]}
				""");

		assertTrue(this.handler.hasImage(message));
		assertEquals("https://cdn.discordapp.com/attachments/1/2/grid.png?width=1024", this.handler.imageUrl(message));
	}

	@Test
	void readsDiscordEmbedImageWhenThereIsNoAttachment() {
		DataObject message = DataObject.fromJson("""
				{"attachments":[],"embeds":[{"type":"image","image":{"url":"https://cdn.discordapp.com/attachments/1/2/grid.webp?width=1024"}}]}
				""");

		assertTrue(this.handler.hasImage(message));
		assertEquals("https://cdn.discordapp.com/attachments/1/2/grid.webp?width=1024", this.handler.imageUrl(message));
	}

	@Test
	void doesNotTreatNonImageEmbedLinkAsAnImage() {
		DataObject message = DataObject.fromJson("""
				{"attachments":[],"embeds":[{"type":"link","url":"https://midjourney.com/jobs/123"}]}
				""");

		assertFalse(this.handler.hasImage(message));
		assertNull(this.handler.imageUrl(message));
	}

	private static class ExposedMessageHandler extends MessageHandler {
		@Override
		public void handle(com.github.novicezk.midjourney.loadbalancer.DiscordInstance instance,
				com.github.novicezk.midjourney.enums.MessageType messageType, DataObject message) {
		}

		public boolean hasImage(DataObject message) {
			return super.hasImage(message);
		}

		public String imageUrl(DataObject message) {
			return super.getImageUrl(message);
		}
	}
}
