package com.ssharma.messaging;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class ChatController {
    public record NewUser(String username) {}
    public record Outgoing(String sender, String text) {}

    private final UserRepository users;
    private final ChatMessageRepository messages;

    private final Presence presence;

    public ChatController(UserRepository users, ChatMessageRepository messages, Presence presence) {
        this.users = users;
        this.messages = messages;
        this.presence = presence;
    }

    /** For clients that load the page after others joined. */
    @GetMapping("/api/online")
    public List<Presence.Online> online() { return presence.online(); }

    /** Register, or "log in" if the name already exists (no passwords). */
    @PostMapping("/api/users")
    public String register(@RequestBody NewUser req) {
        String name = req.username() == null ? "" : req.username().trim();
        if (!name.matches("[A-Za-z0-9_]{1,30}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username: 1-30 letters, digits or _");
        return users.findByUsername(name).orElseGet(() -> users.save(new User(name))).getUsername();
    }

    @GetMapping("/api/messages")
    public List<ChatMessage> history() {
        return messages.findTop100ByOrderByIdDesc().reversed();
    }

    /** Client calls this right after connecting to announce who it is. */
    @MessageMapping("/join")
    public void join(NewUser in, SimpMessageHeaderAccessor h) {
        if (in.username() == null || users.findByUsername(in.username()).isEmpty())
            throw new IllegalArgumentException("unknown user");
        presence.join(h.getSessionId(), new Presence.Online(in.username(), (String) h.getSessionAttributes().get("ip")));
    }

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public ChatMessage send(Outgoing in) {
        String text = in.text() == null ? "" : in.text().trim();
        if (text.isEmpty() || text.length() > 500 || in.sender() == null
                || users.findByUsername(in.sender()).isEmpty())
            throw new IllegalArgumentException("invalid message");
        return messages.save(new ChatMessage(in.sender(), text));
    }
}
