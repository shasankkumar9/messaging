package com.ssharma.messaging;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Who is connected right now. In memory: resets on restart, single server only. */
@Component
public class Presence {
    public record Online(String username, String ip) {}

    private final Map<String, Online> sessions = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate out;

    public Presence(SimpMessagingTemplate out) { this.out = out; }

    public void join(String sessionId, Online who) {
        sessions.put(sessionId, who);
        publish();
    }

    public void leave(String sessionId) {
        if (sessions.remove(sessionId) != null) publish();
    }

    @EventListener
    void onDisconnect(SessionDisconnectEvent e) { leave(e.getSessionId()); }

    /** One entry per user, even with several tabs open. */
    public List<Online> online() {
        var byName = new TreeMap<String, Online>();
        sessions.values().forEach(o -> byName.putIfAbsent(o.username(), o));
        return new ArrayList<>(byName.values());
    }

    private void publish() { out.convertAndSend("/topic/online", online()); }
}
