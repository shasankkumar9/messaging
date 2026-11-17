package com.ssharma.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MessagingApplicationTests {

	@Autowired ChatController chat;

	@Autowired Presence presence;

	@Test
	void presenceListsEachUserOnceUntilLastTabCloses() {
		presence.join("s1", new Presence.Online("carol", "10.0.0.2"));
		presence.join("s2", new Presence.Online("carol", "10.0.0.2")); // second tab
		presence.join("s3", new Presence.Online("dave", "10.0.0.3"));
		assertEquals(2, presence.online().size());

		presence.leave("s1");
		assertEquals(2, presence.online().size()); // carol still has a tab open
		presence.leave("s2");
		presence.leave("s3");
		assertTrue(presence.online().isEmpty());
	}

	@Test
	void registerSendAndReadHistory() {
		assertEquals("alice", chat.register(new ChatController.NewUser(" alice ")));
		assertEquals("alice", chat.register(new ChatController.NewUser("alice"))); // idempotent

		chat.send(new ChatController.Outgoing("alice", "hello"));
		assertEquals("hello", chat.history().getLast().getText());

		assertThrows(IllegalArgumentException.class, () -> chat.send(new ChatController.Outgoing("ghost", "hi")));
		assertThrows(IllegalArgumentException.class, () -> chat.send(new ChatController.Outgoing("alice", "  ")));
		assertThrows(ResponseStatusException.class, () -> chat.register(new ChatController.NewUser("bad name!")));
	}
}
