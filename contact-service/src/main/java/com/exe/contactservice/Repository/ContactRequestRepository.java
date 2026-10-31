package com.exe.contactservice.Repository;

import com.exe.contactservice.Model.ContactRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {
    List<ContactRequest> findAllByOrderByCreatedAtDesc();
}