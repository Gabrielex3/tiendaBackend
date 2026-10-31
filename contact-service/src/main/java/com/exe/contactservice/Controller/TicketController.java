package com.exe.contactservice.Controller;

import com.exe.contactservice.Model.ChatMessage;
import com.exe.contactservice.Model.Ticket;
import com.exe.contactservice.Service.TicketService;
import com.exe.contactservice.WebSocket.ChatWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService service;
    private final ChatWebSocketHandler ws;

    public TicketController(TicketService service, ChatWebSocketHandler ws) {
        this.service = service;
        this.ws = ws;
    }

    @PostMapping
    public ResponseEntity<Ticket> crear(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.crear(
                body.get("userEmail"), body.get("userName"), body.get("subject")));
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<ChatMessage>> mensajes(@PathVariable Long id) {
        return ResponseEntity.ok(service.mensajes(id));
    }

    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<Ticket> cerrar(@PathVariable Long id) {
        Ticket t = service.get(id);
        if (t == null) return ResponseEntity.notFound().build();
        if (!"CERRADO".equalsIgnoreCase(t.getStatus())) {
            t = service.cerrar(id);
            ChatMessage sys = service.guardarMensaje(id, "SYSTEM", "Sistema", "Ticket cerrado.");
            ws.broadcast(id, sys);
        }
        return ResponseEntity.ok(t);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ChatMessage> enviar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Ticket t = service.get(id);
        if (t == null || "CERRADO".equalsIgnoreCase(t.getStatus())) {
            return ResponseEntity.status(409).build();
        }
        ChatMessage m = service.guardarMensaje(id, body.get("sender"), body.get("senderName"), body.get("content"));
        ws.broadcast(id, m);
        return ResponseEntity.ok(m);
    }
}