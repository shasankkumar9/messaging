package com.ssharma.messaging;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sender;
    private String text;
    @Column(name = "sent_at")
    private Instant sentAt;

    protected ChatMessage() {}
    public ChatMessage(String sender, String text) {
        this.sender = sender;
        this.text = text;
        this.sentAt = Instant.now();
    }
    public Long getId() { return id; }
    public String getSender() { return sender; }
    public String getText() { return text; }
    public Instant getSentAt() { return sentAt; }
}
