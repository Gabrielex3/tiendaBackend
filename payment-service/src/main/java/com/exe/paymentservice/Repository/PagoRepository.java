package com.exe.paymentservice.Repository;

import com.exe.paymentservice.Model.Pago;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findAllByOrderByCreatedAtDesc();
    List<Pago> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);
}
