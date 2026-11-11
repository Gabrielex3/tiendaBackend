package com.exe.contactservice.Service;

import com.exe.contactservice.Model.ChatMessage;
import com.exe.contactservice.Model.Ticket;
import com.exe.contactservice.Repository.ChatMessageRepository;
import com.exe.contactservice.Repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TicketService {
    private final TicketRepository ticketRepo;
    private final ChatMessageRepository msgRepo;

    public TicketService(TicketRepository ticketRepo, ChatMessageRepository msgRepo) {
        this.ticketRepo = ticketRepo;
        this.msgRepo = msgRepo;
    }

    @Transactional
    public Ticket crear(String userEmail, String userName, String subject) {
        Ticket t = new Ticket();
        t.setUserEmail(userEmail);
        t.setUserName(userName);
        t.setSubject(subject);
        return ticketRepo.save(t);
    }

    @Transactional(readOnly = true)
    public List<Ticket> listar() { return ticketRepo.findAllByOrderByCreatedAtDesc(); }

    @Transactional(readOnly = true)
    public Ticket get(Long id) { return ticketRepo.findById(id).orElse(null); }

    @Transactional
    public ChatMessage guardarMensaje(Long ticketId, String sender, String senderName, String content) {
        ChatMessage m = new ChatMessage();
        m.setTicketId(ticketId);
        m.setSender(sender);
        m.setSenderName(senderName);
        m.setContent(content);
        return msgRepo.save(m);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> mensajes(Long ticketId) { return msgRepo.findByTicketIdOrderByCreatedAtAsc(ticketId); }

    @Transactional
    public Ticket cerrar(Long id) {
        Ticket t = ticketRepo.findById(id).orElse(null);
        if (t != null) { t.setStatus("CERRADO"); ticketRepo.save(t); }
        return t;
    }
}