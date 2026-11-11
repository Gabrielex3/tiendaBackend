package com.exe.contactservice.Service;

import com.exe.contactservice.Model.ContactRequest;
import com.exe.contactservice.Repository.ContactRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ContactService {
    private final ContactRequestRepository repo;

    public ContactService(ContactRequestRepository repo) { this.repo = repo; }

    @Transactional
    public ContactRequest crear(ContactRequest r) { return repo.save(r); }

    @Transactional(readOnly = true)
    public List<ContactRequest> listar() { return repo.findAllByOrderByCreatedAtDesc(); }
}