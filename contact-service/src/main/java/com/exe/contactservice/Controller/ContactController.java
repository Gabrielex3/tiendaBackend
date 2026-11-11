package com.exe.contactservice.Controller;

import com.exe.contactservice.Model.ContactRequest;
import com.exe.contactservice.Service.ContactService;
import com.exe.contactservice.Service.IaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacto")
public class ContactController {
    private final ContactService service;
    private final IaService iaService;

    public ContactController(ContactService service, IaService iaService) {
        this.service = service;
        this.iaService = iaService;
    }

    @PostMapping
    public ResponseEntity<ContactRequest> crear(@RequestBody ContactRequest request) {
        return ResponseEntity.ok(service.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<ContactRequest>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PostMapping("/ia")
    public ResponseEntity<Map<String, String>> ia(@RequestBody Map<String, String> body) {
        String mensaje = body.getOrDefault("message", "");
        String historial = body.getOrDefault("history", "");
        String reply = iaService.responder(mensaje, historial);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}