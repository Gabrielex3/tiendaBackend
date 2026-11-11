package com.exe.contactservice.WebSocket;

import com.exe.contactservice.Model.ChatMessage;
import com.exe.contactservice.Service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final TicketService ticketService;
    private final ObjectMapper mapper;
    private final Map<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(TicketService ticketService, ObjectMapper mapper) {
        this.ticketService = ticketService;
        this.mapper = mapper;
    }

    private Long ticketId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;
        String q = uri.getQuery();
        if (q == null) return null;
        for (String part : q.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals("ticketId")) {
                try { return Long.parseLong(kv[1]); } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long tid = ticketId(session);
        System.out.println("WS CONNECT ticketId=" + tid + " uri=" + session.getUri());
        if (tid == null) { try { session.close(); } catch (IOException ignored) {} return; }
        sessions.computeIfAbsent(tid, k -> ConcurrentHashMap.newKeySet()).add(session);
        System.out.println("WS sessions for ticket " + tid + ": " + sessions.get(tid).size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long tid = ticketId(session);
        if (tid == null) return;
        com.exe.contactservice.Model.Ticket t = ticketService.get(tid);
        if (t == null || "CERRADO".equalsIgnoreCase(t.getStatus())) return;
        Map<String, Object> data = mapper.readValue(message.getPayload(), Map.class);
        String sender = String.valueOf(data.getOrDefault("sender", "USER"));
        String senderName = String.valueOf(data.getOrDefault("senderName", ""));
        String content = String.valueOf(data.getOrDefault("content", ""));
        ChatMessage saved = ticketService.guardarMensaje(tid, sender, senderName, content);
        broadcast(tid, saved);
    }

    public void broadcast(Long ticketId, ChatMessage msg) {
        try {
            Set<WebSocketSession> set = sessions.get(ticketId);
            System.out.println("WS BROADCAST ticket=" + ticketId + " sessions=" + (set == null ? 0 : set.size()));
            if (set == null) return;
            String json = mapper.writeValueAsString(msg);
            System.out.println("WS JSON=" + json);
            TextMessage tm = new TextMessage(json);
            for (WebSocketSession s : set) {
                System.out.println("WS session isOpen=" + s.isOpen());
                if (s.isOpen()) {
                    try { s.sendMessage(tm); System.out.println("WS SENT to session"); }
                    catch (IOException e) { System.out.println("WS SEND ERROR: " + e); }
                }
            }
        } catch (Exception e) {
            System.out.println("WS BROADCAST EXCEPTION: " + e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long tid = ticketId(session);
        if (tid == null) return;
        Set<WebSocketSession> set = sessions.get(tid);
        if (set != null) set.remove(session);
    }
}